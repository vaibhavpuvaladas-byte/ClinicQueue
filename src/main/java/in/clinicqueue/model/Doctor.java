package in.clinicqueue.model;

/**
 * Represents a consulting doctor in the clinic.
 */
public class Doctor {
    private int id;
    private int departmentId;
    private String displayName;
    private boolean active;
    
    // Convenience field for UI display (joined from department)
    private String departmentName;

    public Doctor() {
    }

    public Doctor(int id, int departmentId, String displayName, boolean active) {
        this.id = id;
        this.departmentId = departmentId;
        this.displayName = displayName;
        this.active = active;
    }

    public Doctor(int id, int departmentId, String displayName, boolean active, String departmentName) {
        this.id = id;
        this.departmentId = departmentId;
        this.displayName = displayName;
        this.active = active;
        this.departmentName = departmentName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    @Override
    public String toString() {
        if (departmentName != null && !departmentName.isEmpty()) {
            return displayName + " (" + departmentName + ")";
        }
        return displayName;
    }
}
