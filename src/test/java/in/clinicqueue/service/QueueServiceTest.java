package in.clinicqueue.service;

import in.clinicqueue.database.DatabaseManager;
import in.clinicqueue.database.SchemaInitializer;
import in.clinicqueue.model.Visit;
import in.clinicqueue.model.VisitStatus;
import in.clinicqueue.model.VisitType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QueueServiceTest {

    private static final String TEST_DB = "jdbc:sqlite:clinicqueue_test.db";
    private VisitService visitService;
    private QueueService queueService;
    private String today;

    @BeforeAll
    public static void setupDatabase() {
        DatabaseManager.setDbUrl(TEST_DB);
        SchemaInitializer.initDatabase();
    }

    @AfterAll
    public static void teardownDatabase() {
        DatabaseManager.resetToDefaultUrl();
        new File("clinicqueue_test.db").delete();
    }

    @BeforeEach
    public void setup() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM visits");
            stmt.execute("DELETE FROM patients");
        }
        visitService = new VisitService();
        queueService = new QueueService();
        today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Test
    public void testCallNext_PicksOldestWaitingAndBlocksSecondCall() throws SQLException {
        // Register two patients for Doctor 1
        Visit v1 = visitService.registerVisit("Patient One", null, 1, VisitType.WALK_IN);
        Visit v2 = visitService.registerVisit("Patient Two", null, 1, VisitType.APPOINTMENT);

        // First Call Next should pick Patient One
        Visit called = queueService.callNext(1, today);
        assertEquals(v1.getId(), called.getId());
        assertEquals(VisitStatus.IN_CONSULTATION, called.getStatus());

        // Second Call Next while v1 is still IN_CONSULTATION should be blocked (FR-08)
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            queueService.callNext(1, today);
        });
        assertTrue(ex.getMessage().contains("already has an active patient"));

        // Complete consultation for Patient One
        queueService.completeConsultation(v1.getId());

        // Now Call Next should pick Patient Two
        Visit calledSecond = queueService.callNext(1, today);
        assertEquals(v2.getId(), calledSecond.getId());
        assertEquals(VisitStatus.IN_CONSULTATION, calledSecond.getStatus());
    }

    @Test
    public void testCompleteFromWaiting_ThrowsException() throws SQLException {
        Visit v = visitService.registerVisit("Waiting Patient", null, 1, VisitType.WALK_IN);

        // Cannot complete while in WAITING status (FR-07)
        assertThrows(IllegalStateException.class, () -> {
            queueService.completeConsultation(v.getId());
        });
    }

    @Test
    public void testCancelRules() throws SQLException {
        Visit v = visitService.registerVisit("Cancel Me", null, 1, VisitType.WALK_IN);

        // Cancel succeeds from WAITING
        queueService.cancelVisit(v.getId());

        // Calling next should now find no waiting patients
        assertThrows(IllegalStateException.class, () -> {
            queueService.callNext(1, today);
        });

        // Cannot cancel already cancelled visit
        assertThrows(IllegalStateException.class, () -> {
            queueService.cancelVisit(v.getId());
        });
    }

    @Test
    public void testWaitTimeCalculation() throws SQLException {
        visitService.registerVisit("Patient A", null, 1, VisitType.WALK_IN);
        visitService.registerVisit("Patient B", null, 1, VisitType.WALK_IN);
        visitService.registerVisit("Patient C", null, 1, VisitType.WALK_IN);

        List<Visit> queue = queueService.getDoctorQueueWithWaitTimes(1, today);
        assertEquals(3, queue.size());

        // Patient A is 1st in line (0 ahead): 0 mins
        assertEquals(1, queue.get(0).getWaitingPosition());
        assertEquals(0, queue.get(0).getEstimatedWaitMinutes());

        // Patient B is 2nd in line (1 ahead): 10 mins
        assertEquals(2, queue.get(1).getWaitingPosition());
        assertEquals(10, queue.get(1).getEstimatedWaitMinutes());

        // Patient C is 3rd in line (2 ahead): 20 mins
        assertEquals(3, queue.get(2).getWaitingPosition());
        assertEquals(20, queue.get(2).getEstimatedWaitMinutes());
    }
}
