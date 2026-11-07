package in.clinicqueue.model;

/**
 * Summary DTO representing a doctor's queue overview for the dashboard.
 */
public class DoctorQueueSummary {
    private int doctorId;
    private String doctorName;
    private String departmentName;
    private int waitingCount;
    private String activePatientToken;
    private String activePatientName;

    public DoctorQueueSummary(int doctorId, String doctorName, String departmentName,
                              int waitingCount, String activePatientToken, String activePatientName) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.departmentName = departmentName;
        this.waitingCount = waitingCount;
        this.activePatientToken = activePatientToken;
        this.activePatientName = activePatientName;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public int getWaitingCount() {
        return waitingCount;
    }

    public String getActivePatientToken() {
        return activePatientToken != null ? activePatientToken : "None";
    }

    public String getActivePatientName() {
        return activePatientName != null ? activePatientName : "No active consultation";
    }
}
