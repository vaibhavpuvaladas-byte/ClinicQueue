package in.clinicqueue.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Initializes the SQLite database schema and seeds initial departments, doctors, and demo visits.
 * Designed to be 100% idempotent and safe to run on every startup.
 */
public class SchemaInitializer {

    // Disallow instantiation
    private SchemaInitializer() {}

    /**
     * Initializes the database schema and seeds base doctors/departments if empty.
     */
    public static void initDatabase() {
        try (Connection conn = DatabaseManager.getConnection()) {
            createTables(conn);
            seedBaseSetup(conn);
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 1. Departments table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS departments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    active INTEGER NOT NULL DEFAULT 1
                );
            """);

            // 2. Doctors table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS doctors (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    department_id INTEGER NOT NULL,
                    display_name TEXT NOT NULL,
                    active INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT
                );
            """);

            // 3. Patients table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS patients (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    phone TEXT,
                    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
                );
            """);

            // 4. Visits table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS visits (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id INTEGER NOT NULL,
                    doctor_id INTEGER NOT NULL,
                    visit_date TEXT NOT NULL,
                    visit_type TEXT NOT NULL CHECK(visit_type IN ('APPOINTMENT', 'WALK_IN')),
                    token_sequence INTEGER NOT NULL,
                    token_number TEXT NOT NULL,
                    status TEXT NOT NULL CHECK(status IN ('WAITING', 'IN_CONSULTATION', 'COMPLETED', 'CANCELLED')),
                    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
                    updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
                    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
                    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE RESTRICT,
                    UNIQUE (doctor_id, visit_date, token_sequence)
                );
            """);

            // Helpful index for fast queue ordering & status lookups
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_visits_lookup 
                ON visits(doctor_id, visit_date, status, created_at);
            """);
        }
    }

    /**
     * Seeds default departments and doctors if they do not exist yet.
     */
    private static void seedBaseSetup(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM departments")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Already seeded
            }
        }

        // Insert Departments
        String insertDeptSql = "INSERT INTO departments (id, name, active) VALUES (?, ?, 1)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertDeptSql)) {
            pstmt.setInt(1, 1);
            pstmt.setString(2, "General Medicine");
            pstmt.executeUpdate();

            pstmt.setInt(1, 2);
            pstmt.setString(2, "Pediatrics");
            pstmt.executeUpdate();
        }

        // Insert Doctors
        String insertDocSql = "INSERT INTO doctors (id, department_id, display_name, active) VALUES (?, ?, ?, 1)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertDocSql)) {
            // Doctor 1: Dr. Sarah Sharma
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 1); // General Medicine
            pstmt.setString(3, "Dr. Sarah Sharma");
            pstmt.executeUpdate();

            // Doctor 2: Dr. Rajesh Patel
            pstmt.setInt(1, 2);
            pstmt.setInt(2, 1); // General Medicine
            pstmt.setString(3, "Dr. Rajesh Patel");
            pstmt.executeUpdate();

            // Doctor 3: Dr. Ananya Iyer
            pstmt.setInt(1, 3);
            pstmt.setInt(2, 2); // Pediatrics
            pstmt.setString(3, "Dr. Ananya Iyer");
            pstmt.executeUpdate();
        }
    }

    /**
     * Seeds a small fictional dataset for today's demo if today's visits are empty.
     */
    public static void seedDemoVisitsIfEmpty() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM visits WHERE visit_date = ?")) {
                stmt.setString(1, today);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return; // Visits already exist for today
                    }
                }
            }
            seedSampleData(conn, today);
        } catch (SQLException e) {
            System.err.println("Demo seed failed: " + e.getMessage());
        }
    }

    /**
     * Clears all visits and re-seeds clean sample demo data.
     */
    public static void resetAndReseedDemoData() throws SQLException {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM visits");
                stmt.execute("DELETE FROM patients");
                seedSampleData(conn, today);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static void seedSampleData(Connection conn, String today) throws SQLException {
        // Sample Patients
        String[] patientNames = {
            "Rohan Verma (Sample)",
            "Priya Nair (Sample)",
            "Amit Kumar (Sample)",
            "Kavita Reddy (Sample)",
            "Neha Gupta (Sample)",
            "Vikram Singh (Sample)"
        };
        String[] phones = {
            "9876543210",
            "9876543211",
            "9876543212",
            "9876543213",
            "9876543214",
            "9876543215"
        };

        int[] patientIds = new int[patientNames.length];
        String insertPatientSql = "INSERT INTO patients (full_name, phone) VALUES (?, ?)";
        for (int i = 0; i < patientNames.length; i++) {
            try (PreparedStatement pstmt = conn.prepareStatement(insertPatientSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, patientNames[i]);
                pstmt.setString(2, phones[i]);
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        patientIds[i] = rs.getInt(1);
                    }
                }
            }
        }

        // Sample Visits across doctors and statuses
        // Format: patientIdx, doctorId, visitType, seq, status
        Object[][] sampleVisits = {
            // Dr. Sarah Sharma (Doctor 1)
            {0, 1, "WALK_IN", 1, "COMPLETED"},
            {1, 1, "APPOINTMENT", 2, "IN_CONSULTATION"},
            {2, 1, "WALK_IN", 3, "WAITING"},
            {3, 1, "APPOINTMENT", 4, "WAITING"},
            // Dr. Rajesh Patel (Doctor 2)
            {4, 2, "WALK_IN", 1, "IN_CONSULTATION"},
            // Dr. Ananya Iyer (Doctor 3)
            {5, 3, "APPOINTMENT", 1, "WAITING"}
        };

        String insertVisitSql = """
            INSERT INTO visits (patient_id, doctor_id, visit_date, visit_type, token_sequence, token_number, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(insertVisitSql)) {
            for (Object[] v : sampleVisits) {
                int pIdx = (int) v[0];
                int docId = (int) v[1];
                String vType = (String) v[2];
                int seq = (int) v[3];
                String status = (String) v[4];
                String token = String.format("D%02d-%s-%03d", docId, today, seq);

                pstmt.setInt(1, patientIds[pIdx]);
                pstmt.setInt(2, docId);
                pstmt.setString(3, today);
                pstmt.setString(4, vType);
                pstmt.setInt(5, seq);
                pstmt.setString(6, token);
                pstmt.setString(7, status);
                pstmt.executeUpdate();
            }
        }
    }
}
