package in.clinicqueue.service;

import in.clinicqueue.model.Visit;
import in.clinicqueue.model.VisitStatus;
import in.clinicqueue.repository.VisitRepository;

import java.sql.SQLException;
import java.util.List;

public class QueueService {
    public static final int DEFAULT_AVG_CONSULTATION_MINUTES = 10;
    private final VisitRepository visitRepository;

    public QueueService() { this(new VisitRepository()); }
    public QueueService(VisitRepository vRepo) { this.visitRepository = vRepo; }

    public Visit callNext(int doctorId, String date) throws SQLException {
        if (visitRepository.hasActiveConsultation(doctorId, date)) {
            throw new IllegalStateException("Doctor already has an active patient in consultation. Complete the ongoing visit first.");
        }
        Visit oldest = visitRepository.findOldestWaitingVisit(doctorId, date);
        if (oldest == null) throw new IllegalStateException("No waiting patients in queue for this doctor.");

        visitRepository.updateStatus(oldest.getId(), VisitStatus.IN_CONSULTATION);
        oldest.setStatus(VisitStatus.IN_CONSULTATION);
        return oldest;
    }

    public void completeConsultation(int visitId) throws SQLException {
        Visit v = visitRepository.getVisitById(visitId);
        if (v == null) throw new IllegalArgumentException("Visit record not found with ID: " + visitId);
        if (v.getStatus() != VisitStatus.IN_CONSULTATION) {
            throw new IllegalStateException("Cannot complete visit. Only visits currently 'In Consultation' can be completed.");
        }
        visitRepository.updateStatus(visitId, VisitStatus.COMPLETED);
    }

    public void cancelVisit(int visitId) throws SQLException {
        Visit v = visitRepository.getVisitById(visitId);
        if (v == null) throw new IllegalArgumentException("Visit record not found with ID: " + visitId);
        if (v.getStatus() != VisitStatus.WAITING) {
            throw new IllegalStateException("Cannot cancel visit. Only visits with 'Waiting' status can be cancelled.");
        }
        visitRepository.updateStatus(visitId, VisitStatus.CANCELLED);
    }

    public List<Visit> getDoctorQueueWithWaitTimes(int doctorId, String date) throws SQLException {
        return calculateWaitTimes(visitRepository.searchVisits(date, doctorId, null, null));
    }

    public List<Visit> calculateWaitTimes(List<Visit> visits) {
        int count = 0;
        for (Visit v : visits) {
            if (v.getStatus() == VisitStatus.WAITING) {
                v.setWaitingPosition(++count);
                v.setEstimatedWaitMinutes((count - 1) * DEFAULT_AVG_CONSULTATION_MINUTES);
            } else {
                v.setWaitingPosition(0);
                v.setEstimatedWaitMinutes(0);
            }
        }
        return visits;
    }
}
