package in.clinicqueue.service;

import in.clinicqueue.model.Visit;
import in.clinicqueue.model.VisitStatus;
import in.clinicqueue.repository.VisitRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles queue management, status transitions, and "Call Next" rules.
 */
public class QueueService {

    public static final int DEFAULT_AVG_CONSULTATION_MINUTES = 10;

    private final VisitRepository visitRepository;

    public QueueService() {
        this(new VisitRepository());
    }

    public QueueService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    /**
     * Calls the next waiting patient for a doctor.
     * Enforces that a doctor can only have ONE active consultation at a time (FR-06, FR-08).
     * 
     * @param doctorId Consulting doctor ID
     * @param date Today's date (YYYY-MM-DD)
     * @return The updated Visit now in IN_CONSULTATION status
     */
    public Visit callNext(int doctorId, String date) throws SQLException {
        // FR-08: Prevent a second IN_CONSULTATION visit for the same doctor
        if (visitRepository.hasActiveConsultation(doctorId, date)) {
            throw new IllegalStateException("Doctor already has an active patient in consultation. Complete the ongoing visit first.");
        }

        // FR-06: Select the oldest WAITING visit for this doctor/date
        Visit oldestWaiting = visitRepository.findOldestWaitingVisit(doctorId, date);
        if (oldestWaiting == null) {
            throw new IllegalStateException("No waiting patients in queue for this doctor.");
        }

        // Transition from WAITING -> IN_CONSULTATION
        visitRepository.updateStatus(oldestWaiting.getId(), VisitStatus.IN_CONSULTATION);
        oldestWaiting.setStatus(VisitStatus.IN_CONSULTATION);
        return oldestWaiting;
    }

    /**
     * Completes a consultation (FR-07).
     * Only allowed when the visit is currently IN_CONSULTATION.
     */
    public void completeConsultation(int visitId) throws SQLException {
        Visit visit = visitRepository.getVisitById(visitId);
        if (visit == null) {
            throw new IllegalArgumentException("Visit record not found with ID: " + visitId);
        }

        if (visit.getStatus() != VisitStatus.IN_CONSULTATION) {
            throw new IllegalStateException("Cannot complete visit. Only visits currently 'In Consultation' can be completed.");
        }

        visitRepository.updateStatus(visitId, VisitStatus.COMPLETED);
    }

    /**
     * Cancels a visit (FR-07).
     * Only allowed when the visit is currently WAITING.
     */
    public void cancelVisit(int visitId) throws SQLException {
        Visit visit = visitRepository.getVisitById(visitId);
        if (visit == null) {
            throw new IllegalArgumentException("Visit record not found with ID: " + visitId);
        }

        if (visit.getStatus() != VisitStatus.WAITING) {
            throw new IllegalStateException("Cannot cancel visit. Only visits with 'Waiting' status can be cancelled.");
        }

        visitRepository.updateStatus(visitId, VisitStatus.CANCELLED);
    }

    /**
     * Computes the queue with positions ahead and approximate wait times (FR-11).
     * Formula: Wait Time = Eligible WAITING visits ahead * 10 mins.
     */
    public List<Visit> getDoctorQueueWithWaitTimes(int doctorId, String date) throws SQLException {
        List<Visit> allVisits = visitRepository.searchVisits(date, doctorId, null, null);
        return calculateWaitTimes(allVisits);
    }

    /**
     * Calculates wait time approximations for a list of visits.
     */
    public List<Visit> calculateWaitTimes(List<Visit> visits) {
        int waitingAheadCount = 0;
        for (Visit v : visits) {
            if (v.getStatus() == VisitStatus.WAITING) {
                v.setWaitingPosition(waitingAheadCount + 1); // 1-based position in line
                v.setEstimatedWaitMinutes(waitingAheadCount * DEFAULT_AVG_CONSULTATION_MINUTES);
                waitingAheadCount++;
            } else {
                v.setWaitingPosition(0);
                v.setEstimatedWaitMinutes(0);
            }
        }
        return visits;
    }
}
