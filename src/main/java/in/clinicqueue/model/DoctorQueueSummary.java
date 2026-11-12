package in.clinicqueue.model;

public class DoctorQueueSummary {
    private final int doctorId, waitingCount;
    private final String doctorName, departmentName, activePatientToken, activePatientName;

    public DoctorQueueSummary(int doctorId, String doctorName, String departmentName,
                              int waitingCount, String activePatientToken, String activePatientName) {
        this.doctorId = doctorId; this.doctorName = doctorName;
        this.departmentName = departmentName; this.waitingCount = waitingCount;
        this.activePatientToken = activePatientToken; this.activePatientName = activePatientName;
    }

    public int getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getDepartmentName() { return departmentName; }
    public int getWaitingCount() { return waitingCount; }
    public String getActivePatientToken() { return activePatientToken != null ? activePatientToken : "None"; }
    public String getActivePatientName() { return activePatientName != null ? activePatientName : "No active consultation"; }
}
