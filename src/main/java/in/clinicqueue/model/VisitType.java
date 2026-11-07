package in.clinicqueue.model;

/**
 * Represents the type of clinic visit.
 * Both APPOINTMENT and WALK_IN enter the same queue in this MVP,
 * but are visually distinguished.
 */
public enum VisitType {
    APPOINTMENT("Appointment"),
    WALK_IN("Walk-In");

    private final String label;

    VisitType(String label) {
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
