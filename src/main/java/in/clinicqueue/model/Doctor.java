package in.clinicqueue.model;

public class Doctor {
    private int id, departmentId;
    private String displayName, departmentName;
    private boolean active;

    public Doctor(int id, int departmentId, String displayName, boolean active, String departmentName) {
        this.id = id; this.departmentId = departmentId;
        this.displayName = displayName; this.active = active;
        this.departmentName = departmentName;
    }

    public int getId() { return id; }
    public int getDepartmentId() { return departmentId; }
    public String getDisplayName() { return displayName; }
    public boolean isActive() { return active; }
    public String getDepartmentName() { return departmentName; }

    @Override
    public String toString() {
        return displayName + (departmentName != null ? " (" + departmentName + ")" : "");
    }
}
