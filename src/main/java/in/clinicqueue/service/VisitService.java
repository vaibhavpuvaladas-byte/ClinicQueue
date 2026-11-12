package in.clinicqueue.service;

import in.clinicqueue.database.DatabaseManager;
import in.clinicqueue.model.*;
import in.clinicqueue.repository.PatientRepository;
import in.clinicqueue.repository.VisitRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VisitService {
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final TokenService tokenService;

    public VisitService() { this(new PatientRepository(), new VisitRepository(), new TokenService()); }

    public VisitService(PatientRepository pRepo, VisitRepository vRepo, TokenService tSvc) {
        this.patientRepository = pRepo; this.visitRepository = vRepo; this.tokenService = tSvc;
    }

    public Visit registerVisit(String fullName, String phone, int doctorId, VisitType visitType) throws SQLException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient name is required and cannot be blank.");
        }
        String trimmedPhone = (phone != null && !phone.trim().isEmpty()) ? phone.trim() : null;
        if (trimmedPhone != null && trimmedPhone.length() < 7) {
            throw new IllegalArgumentException("Phone number must have at least 7 digits if provided.");
        }
        if (doctorId <= 0) throw new IllegalArgumentException("A consulting doctor must be selected.");
        if (visitType == null) throw new IllegalArgumentException("Visit type (Appointment or Walk-In) must be selected.");

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int patientId = patientRepository.insert(conn, new Patient(fullName.trim(), trimmedPhone));
                int seq = visitRepository.getNextTokenSequence(conn, doctorId, today);
                String token = tokenService.formatToken(doctorId, today, seq);

                Visit v = new Visit(0, patientId, doctorId, today, visitType, seq, token, VisitStatus.WAITING, null, null);
                int visitId = visitRepository.insert(conn, v);
                v.setId(visitId);
                conn.commit();
                return v;
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Failed to register visit transactionally: " + e.getMessage(), e);
            }
        }
    }
}
