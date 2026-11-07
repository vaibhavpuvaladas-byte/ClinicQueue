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

import static org.junit.jupiter.api.Assertions.*;

public class VisitServiceTest {

    private static final String TEST_DB = "jdbc:sqlite:clinicqueue_test.db";
    private VisitService visitService;

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
    public void cleanTables() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM visits");
            stmt.execute("DELETE FROM patients");
        }
        visitService = new VisitService();
    }

    @Test
    public void testValidRegistration_CreatesSequentialTokens() throws SQLException {
        Visit v1 = visitService.registerVisit("Aarav Sharma", "9876543210", 1, VisitType.WALK_IN);
        assertNotNull(v1);
        assertEquals(1, v1.getTokenSequence());
        assertTrue(v1.getTokenNumber().endsWith("-001"));
        assertEquals(VisitStatus.WAITING, v1.getStatus());

        Visit v2 = visitService.registerVisit("Riya Sen", "9876543211", 1, VisitType.APPOINTMENT);
        assertNotNull(v2);
        assertEquals(2, v2.getTokenSequence());
        assertTrue(v2.getTokenNumber().endsWith("-002"));

        // Different doctor starts their token numbering from 001
        Visit vDoc2 = visitService.registerVisit("Kabir Mehta", "9876543212", 2, VisitType.WALK_IN);
        assertNotNull(vDoc2);
        assertEquals(1, vDoc2.getTokenSequence());
        assertTrue(vDoc2.getTokenNumber().startsWith("D02-"));
        assertTrue(vDoc2.getTokenNumber().endsWith("-001"));
    }

    @Test
    public void testBlankPatientName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            visitService.registerVisit("   ", "9876543210", 1, VisitType.WALK_IN);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            visitService.registerVisit("", "9876543210", 1, VisitType.WALK_IN);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            visitService.registerVisit(null, "9876543210", 1, VisitType.WALK_IN);
        });
    }

    @Test
    public void testInvalidDoctor_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            visitService.registerVisit("John Doe", "9876543210", 0, VisitType.WALK_IN);
        });
    }
}
