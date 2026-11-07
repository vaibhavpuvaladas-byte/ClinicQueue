package in.clinicqueue.repository;

import in.clinicqueue.database.DatabaseManager;
import in.clinicqueue.model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data access operations for Patients.
 */
public class PatientRepository {

    /**
     * Inserts a patient within an existing database transaction.
     */
    public int insert(Connection conn, Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (full_name, phone) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, patient.getFullName());
            pstmt.setString(2, patient.getPhone());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    patient.setId(generatedId);
                    return generatedId;
                }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for patient: " + patient.getFullName());
    }

    /**
     * Standalone insert (creates its own connection).
     */
    public int insert(Patient patient) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            return insert(conn, patient);
        }
    }

    public Patient getById(int id) throws SQLException {
        String sql = "SELECT id, full_name, phone, created_at FROM patients WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("created_at")
                    );
                }
            }
        }
        return null;
    }
}
