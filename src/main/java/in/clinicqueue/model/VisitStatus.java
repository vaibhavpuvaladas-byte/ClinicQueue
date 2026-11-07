package in.clinicqueue.model;

/**
 * Represents the four official states a clinic visit can be in.
 * 
 * WAITING: Patient registered and waiting in the queue.
 * IN_CONSULTATION: Doctor is currently attending to this patient.
 * COMPLETED: Doctor finished the consultation.
 * CANCELLED: Visit was cancelled prior to consultation.
 */
public enum VisitStatus {
    WAITING("Waiting"),
    IN_CONSULTATION("In Consultation"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    VisitStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
