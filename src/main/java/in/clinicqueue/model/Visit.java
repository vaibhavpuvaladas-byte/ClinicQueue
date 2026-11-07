package in.clinicqueue.model;

/**
 * Represents a patient visit entry in a doctor's daily queue.
 */
public class Visit {
    private int id;
    private int patientId;
    private int doctorId;
    private String visitDate;
    private VisitType visitType;
    private int tokenSequence;
    private String tokenNumber;
    private VisitStatus status;
    private String createdAt;
    private String updatedAt;

    // Joined fields for UI convenience
    private String patientName;
    private String patientPhone;
    private String doctorName;
    private String departmentName;

    // Dynamic queue calculation fields
    private int waitingPosition;
    private int estimatedWaitMinutes;

    public Visit() {
    }

    public Visit(int id, int patientId, int doctorId, String visitDate, VisitType visitType,
                 int tokenSequence, String tokenNumber, VisitStatus status,
                 String createdAt, String updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.visitDate = visitDate;
        this.visitType = visitType;
        this.tokenSequence = tokenSequence;
        this.tokenNumber = tokenNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public VisitType getVisitType() {
        return visitType;
    }

    public void setVisitType(VisitType visitType) {
        this.visitType = visitType;
    }

    public int getTokenSequence() {
        return tokenSequence;
    }

    public void setTokenSequence(int tokenSequence) {
        this.tokenSequence = tokenSequence;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public int getWaitingPosition() {
        return waitingPosition;
    }

    public void setWaitingPosition(int waitingPosition) {
        this.waitingPosition = waitingPosition;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }
}
