package in.clinicqueue.repository;

import in.clinicqueue.database.DatabaseManager;
import in.clinicqueue.model.Doctor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for Doctors.
 */
public class DoctorRepository {

    public List<Doctor> getAllActive() throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = """
            SELECT d.id, d.department_id, d.display_name, d.active, dept.name AS department_name
            FROM doctors d
            JOIN departments dept ON d.department_id = dept.id
            WHERE d.active = 1
            ORDER BY d.display_name ASC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Doctor(
                    rs.getInt("id"),
                    rs.getInt("department_id"),
                    rs.getString("display_name"),
                    rs.getInt("active") == 1,
                    rs.getString("department_name")
                ));
            }
        }
        return list;
    }

    public List<Doctor> getByDepartmentId(int departmentId) throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = """
            SELECT d.id, d.department_id, d.display_name, d.active, dept.name AS department_name
            FROM doctors d
            JOIN departments dept ON d.department_id = dept.id
            WHERE d.department_id = ? AND d.active = 1
            ORDER BY d.display_name ASC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, departmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Doctor(
                        rs.getInt("id"),
                        rs.getInt("department_id"),
                        rs.getString("display_name"),
                        rs.getInt("active") == 1,
                        rs.getString("department_name")
                    ));
                }
            }
        }
        return list;
    }

    public Doctor getById(int id) throws SQLException {
        String sql = """
            SELECT d.id, d.department_id, d.display_name, d.active, dept.name AS department_name
            FROM doctors d
            JOIN departments dept ON d.department_id = dept.id
            WHERE d.id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Doctor(
                        rs.getInt("id"),
                        rs.getInt("department_id"),
                        rs.getString("display_name"),
                        rs.getInt("active") == 1,
                        rs.getString("department_name")
                    );
                }
            }
        }
        return null;
    }
}
