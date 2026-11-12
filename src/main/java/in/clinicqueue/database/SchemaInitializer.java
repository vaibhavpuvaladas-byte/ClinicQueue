package in.clinicqueue.database;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SchemaInitializer {
    private SchemaInitializer() {}

    public static void initDatabase() {
        try (Connection conn = DatabaseManager.getConnection()) {
            createTables(conn);
            seedBaseSetup(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement s = conn.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS departments (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, active INTEGER NOT NULL DEFAULT 1)");
            s.execute("CREATE TABLE IF NOT EXISTS doctors (id INTEGER PRIMARY KEY AUTOINCREMENT, department_id INTEGER NOT NULL, display_name TEXT NOT NULL, active INTEGER NOT NULL DEFAULT 1, FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT)");
            s.execute("CREATE TABLE IF NOT EXISTS patients (id INTEGER PRIMARY KEY AUTOINCREMENT, full_name TEXT NOT NULL, phone TEXT, created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')))");
            s.execute("CREATE TABLE IF NOT EXISTS visits (id INTEGER PRIMARY KEY AUTOINCREMENT, patient_id INTEGER NOT NULL, doctor_id INTEGER NOT NULL, visit_date TEXT NOT NULL, visit_type TEXT NOT NULL CHECK(visit_type IN ('APPOINTMENT', 'WALK_IN')), token_sequence INTEGER NOT NULL, token_number TEXT NOT NULL, status TEXT NOT NULL CHECK(status IN ('WAITING', 'IN_CONSULTATION', 'COMPLETED', 'CANCELLED')), created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE, FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE RESTRICT, UNIQUE (doctor_id, visit_date, token_sequence))");
            s.execute("CREATE INDEX IF NOT EXISTS idx_visits_lookup ON visits(doctor_id, visit_date, status, created_at)");
        }
    }

    private static void seedBaseSetup(Connection conn) throws SQLException {
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM departments")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        try (Statement s = conn.createStatement()) {
            s.execute("INSERT INTO departments (id, name, active) VALUES (1, 'General Medicine', 1), (2, 'Pediatrics', 1)");
            s.execute("INSERT INTO doctors (id, department_id, display_name, active) VALUES (1, 1, 'Dr. Sarah Sharma', 1), (2, 1, 'Dr. Rajesh Patel', 1), (3, 2, 'Dr. Ananya Iyer', 1)");
        }
    }

    public static void seedDemoVisitsIfEmpty() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement s = conn.prepareStatement("SELECT COUNT(*) FROM visits WHERE visit_date = ?")) {
            s.setString(1, today);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return;
            }
            seedSampleData(conn, today);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void resetAndReseedDemoData() throws SQLException {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement s = conn.createStatement()) {
                s.execute("DELETE FROM visits");
                s.execute("DELETE FROM patients");
                seedSampleData(conn, today);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static void seedSampleData(Connection conn, String today) throws SQLException {
        String[] names = {"Rohan Verma (Sample)", "Priya Nair (Sample)", "Amit Kumar (Sample)", "Kavita Reddy (Sample)", "Neha Gupta (Sample)", "Vikram Singh (Sample)"};
        String[] phones = {"9876543210", "9876543211", "9876543212", "9876543213", "9876543214", "9876543215"};
        int[] pIds = new int[names.length];

        String pSql = "INSERT INTO patients (full_name, phone) VALUES (?, ?)";
        for (int i = 0; i < names.length; i++) {
            try (PreparedStatement ps = conn.prepareStatement(pSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, names[i]); ps.setString(2, phones[i]);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) pIds[i] = rs.getInt(1); }
            }
        }

        Object[][] vData = {
            {0, 1, "WALK_IN", 1, "COMPLETED"}, {1, 1, "APPOINTMENT", 2, "IN_CONSULTATION"},
            {2, 1, "WALK_IN", 3, "WAITING"}, {3, 1, "APPOINTMENT", 4, "WAITING"},
            {4, 2, "WALK_IN", 1, "IN_CONSULTATION"}, {5, 3, "APPOINTMENT", 1, "WAITING"}
        };

        String vSql = "INSERT INTO visits (patient_id, doctor_id, visit_date, visit_type, token_sequence, token_number, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(vSql)) {
            for (Object[] v : vData) {
                int docId = (int) v[1], seq = (int) v[3];
                ps.setInt(1, pIds[(int) v[0]]); ps.setInt(2, docId); ps.setString(3, today);
                ps.setString(4, (String) v[2]); ps.setInt(5, seq);
                ps.setString(6, String.format("D%02d-%s-%03d", docId, today, seq));
                ps.setString(7, (String) v[4]);
                ps.executeUpdate();
            }
        }
    }
}
