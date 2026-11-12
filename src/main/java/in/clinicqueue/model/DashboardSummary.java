package in.clinicqueue.model;

import java.util.List;

public class DashboardSummary {
    private final int totalVisits, waitingCount, inConsultationCount, completedCount, cancelledCount;
    private final List<DoctorQueueSummary> doctorSummaries;

    public DashboardSummary(int totalVisits, int waitingCount, int inConsultationCount,
                            int completedCount, int cancelledCount, List<DoctorQueueSummary> doctorSummaries) {
        this.totalVisits = totalVisits; this.waitingCount = waitingCount;
        this.inConsultationCount = inConsultationCount; this.completedCount = completedCount;
        this.cancelledCount = cancelledCount; this.doctorSummaries = doctorSummaries;
    }

    public int getTotalVisits() { return totalVisits; }
    public int getWaitingCount() { return waitingCount; }
    public int getInConsultationCount() { return inConsultationCount; }
    public int getCompletedCount() { return completedCount; }
    public int getCancelledCount() { return cancelledCount; }
    public List<DoctorQueueSummary> getDoctorSummaries() { return doctorSummaries; }
}
