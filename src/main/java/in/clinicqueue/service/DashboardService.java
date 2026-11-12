package in.clinicqueue.service;

import in.clinicqueue.model.*;
import in.clinicqueue.repository.DoctorRepository;
import in.clinicqueue.repository.VisitRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardService {
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;

    public DashboardService() { this(new VisitRepository(), new DoctorRepository()); }
    public DashboardService(VisitRepository vRepo, DoctorRepository dRepo) {
        this.visitRepository = vRepo; this.doctorRepository = dRepo;
    }

    public DashboardSummary getDashboardSummary(String visitDate) throws SQLException {
        List<Doctor> doctors = doctorRepository.getAllActive();
        List<Visit> visits = visitRepository.getVisitsForDate(visitDate);

        int total = visits.size(), waiting = 0, inCons = 0, completed = 0, cancelled = 0;
        for (Visit v : visits) {
            switch (v.getStatus()) {
                case WAITING -> waiting++;
                case IN_CONSULTATION -> inCons++;
                case COMPLETED -> completed++;
                case CANCELLED -> cancelled++;
            }
        }

        List<DoctorQueueSummary> docSummaries = new ArrayList<>();
        for (Doctor doc : doctors) {
            int docWaiting = 0;
            String token = null, patient = null;
            for (Visit v : visits) {
                if (v.getDoctorId() == doc.getId()) {
                    if (v.getStatus() == VisitStatus.WAITING) docWaiting++;
                    else if (v.getStatus() == VisitStatus.IN_CONSULTATION) {
                        token = v.getTokenNumber(); patient = v.getPatientName();
                    }
                }
            }
            docSummaries.add(new DoctorQueueSummary(doc.getId(), doc.getDisplayName(), doc.getDepartmentName(), docWaiting, token, patient));
        }

        return new DashboardSummary(total, waiting, inCons, completed, cancelled, docSummaries);
    }
}
