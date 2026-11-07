package in.clinicqueue.model;

/**
 * Represents a registered patient.
 */
public class Patient {
    private int id;
    private String fullName;
    private String phone;
    private String createdAt;

    public Patient() {
    }

    public Patient(String fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
    }

    public Patient(int id, String fullName, String phone, String createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
