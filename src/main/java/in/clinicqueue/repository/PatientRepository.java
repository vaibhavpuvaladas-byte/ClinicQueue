package in.clinicqueue.repository;

import in.clinicqueue.database.DatabaseManager;
import in.clinicqueue.model.Patient;

import java.sql.*;

public class PatientRepository {

    public int insert(Connection conn, Patient p) throws SQLException {
        String sql = "INSERT INTO patients (full_name, phone) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getFullName());
            ps.setString(2, p.getPhone());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) { int id = rs.getInt(1); p.setId(id); return id; }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for patient: " + p.getFullName());
    }

    public int insert(Patient p) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) { return insert(conn, p); }
    }

    public Patient getById(int id) throws SQLException {
        String sql = "SELECT id, full_name, phone, created_at FROM patients WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Patient(rs.getInt("id"), rs.getString("full_name"), rs.getString("phone"), rs.getString("created_at")) : null;
            }
        }
    }
}
