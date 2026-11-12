package in.clinicqueue.model;

public class Visit {
    private int id, patientId, doctorId, tokenSequence, waitingPosition, estimatedWaitMinutes;
    private String visitDate, tokenNumber, createdAt, updatedAt, patientName, patientPhone, doctorName, departmentName;
    private VisitType visitType;
    private VisitStatus status;

    public Visit() {}

    public Visit(int id, int patientId, int doctorId, String visitDate, VisitType visitType,
                 int tokenSequence, String tokenNumber, VisitStatus status, String createdAt, String updatedAt) {
        this.id = id; this.patientId = patientId; this.doctorId = doctorId;
        this.visitDate = visitDate; this.visitType = visitType;
        this.tokenSequence = tokenSequence; this.tokenNumber = tokenNumber;
        this.status = status; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }
    public VisitType getVisitType() { return visitType; }
    public void setVisitType(VisitType visitType) { this.visitType = visitType; }
    public int getTokenSequence() { return tokenSequence; }
    public void setTokenSequence(int tokenSequence) { this.tokenSequence = tokenSequence; }
    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }
    public VisitStatus getStatus() { return status; }
    public void setStatus(VisitStatus status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public int getWaitingPosition() { return waitingPosition; }
    public void setWaitingPosition(int waitingPosition) { this.waitingPosition = waitingPosition; }
    public int getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }
}
