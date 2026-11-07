package in.clinicqueue.service;

import in.clinicqueue.database.DatabaseManager;
import in.clinicqueue.model.Patient;
import in.clinicqueue.model.Visit;
import in.clinicqueue.model.VisitStatus;
import in.clinicqueue.model.VisitType;
import in.clinicqueue.repository.PatientRepository;
import in.clinicqueue.repository.VisitRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Handles patient visit registration with strict validation and transactional integrity.
 */
public class VisitService {

    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final TokenService tokenService;

    public VisitService() {
        this(new PatientRepository(), new VisitRepository(), new TokenService());
    }

    public VisitService(PatientRepository patientRepository, VisitRepository visitRepository, TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.visitRepository = visitRepository;
        this.tokenService = tokenService;
    }

    /**
     * Registers a new patient visit transactionally.
     * 
     * @param fullName Patient's name (required, non-blank)
     * @param phone Optional phone number
     * @param doctorId Selected doctor ID
     * @param visitType APPOINTMENT or WALK_IN
     * @return The created Visit containing the allocated token
     * @throws IllegalArgumentException if validation fails
     * @throws SQLException if a database error occurs
     */
    public Visit registerVisit(String fullName, String phone, int doctorId, VisitType visitType) throws SQLException {
        // 1. Validation (FR-01)
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient name is required and cannot be blank.");
        }
        String trimmedName = fullName.trim();

        String trimmedPhone = (phone != null && !phone.trim().isEmpty()) ? phone.trim() : null;
        if (trimmedPhone != null && trimmedPhone.length() < 7) {
            throw new IllegalArgumentException("Phone number must have at least 7 digits if provided.");
        }

        if (doctorId <= 0) {
            throw new IllegalArgumentException("A consulting doctor must be selected.");
        }

        if (visitType == null) {
            throw new IllegalArgumentException("Visit type (Appointment or Walk-In) must be selected.");
        }

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // 2. Transactional Insertion (FR-02, FR-03, FR-15)
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert Patient
                Patient patient = new Patient(trimmedName, trimmedPhone);
                int patientId = patientRepository.insert(conn, patient);

                // Allocate next sequential token number for this doctor on this date
                int sequence = visitRepository.getNextTokenSequence(conn, doctorId, today);
                String tokenNumber = tokenService.formatToken(doctorId, today, sequence);

                // Create Visit object
                Visit visit = new Visit();
                visit.setPatientId(patientId);
                visit.setDoctorId(doctorId);
                visit.setVisitDate(today);
                visit.setVisitType(visitType);
                visit.setTokenSequence(sequence);
                visit.setTokenNumber(tokenNumber);
                visit.setStatus(VisitStatus.WAITING);

                // Insert Visit
                int visitId = visitRepository.insert(conn, visit);
                visit.setId(visitId);

                // Commit Transaction
                conn.commit();
                return visit;
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Failed to register visit transactionally: " + e.getMessage(), e);
            }
        }
    }
}
