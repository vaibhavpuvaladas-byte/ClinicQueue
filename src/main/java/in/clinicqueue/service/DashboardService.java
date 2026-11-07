package in.clinicqueue.service;

import in.clinicqueue.model.DashboardSummary;
import in.clinicqueue.model.Doctor;
import in.clinicqueue.model.DoctorQueueSummary;
import in.clinicqueue.model.Visit;
import in.clinicqueue.model.VisitStatus;
import in.clinicqueue.repository.DoctorRepository;
import in.clinicqueue.repository.VisitRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates live queue statistics and per-doctor summaries for the Dashboard.
 */
public class DashboardService {

    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;

    public DashboardService() {
        this(new VisitRepository(), new DoctorRepository());
    }

    public DashboardService(VisitRepository visitRepository, DoctorRepository doctorRepository) {
        this.visitRepository = visitRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * Calculates the dashboard metrics for a given date.
     */
    public DashboardSummary getDashboardSummary(String visitDate) throws SQLException {
        List<Doctor> doctors = doctorRepository.getAllActive();
        List<Visit> visits = visitRepository.getVisitsForDate(visitDate);

        int totalVisits = visits.size();
        int waitingCount = 0;
        int inConsultationCount = 0;
        int completedCount = 0;
        int cancelledCount = 0;

        for (Visit v : visits) {
            if (v.getStatus() == VisitStatus.WAITING) waitingCount++;
            else if (v.getStatus() == VisitStatus.IN_CONSULTATION) inConsultationCount++;
            else if (v.getStatus() == VisitStatus.COMPLETED) completedCount++;
            else if (v.getStatus() == VisitStatus.CANCELLED) cancelledCount++;
        }

        List<DoctorQueueSummary> doctorSummaries = new ArrayList<>();
        for (Doctor doc : doctors) {
            int docWaiting = 0;
            String activeToken = null;
            String activePatient = null;

            for (Visit v : visits) {
                if (v.getDoctorId() == doc.getId()) {
                    if (v.getStatus() == VisitStatus.WAITING) {
                        docWaiting++;
                    } else if (v.getStatus() == VisitStatus.IN_CONSULTATION) {
                        activeToken = v.getTokenNumber();
                        activePatient = v.getPatientName();
                    }
                }
            }

            doctorSummaries.add(new DoctorQueueSummary(
                doc.getId(),
                doc.getDisplayName(),
                doc.getDepartmentName(),
                docWaiting,
                activeToken,
                activePatient
            ));
        }

        return new DashboardSummary(
            totalVisits,
            waitingCount,
            inConsultationCount,
            completedCount,
            cancelledCount,
            doctorSummaries
        );
    }
}
