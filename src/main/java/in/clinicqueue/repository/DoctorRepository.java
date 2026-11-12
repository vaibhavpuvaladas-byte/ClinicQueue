package in.clinicqueue.repository;

import in.clinicqueue.database.DatabaseManager;
import in.clinicqueue.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorRepository {
    private static final String SELECT_BASE = """
        SELECT d.id, d.department_id, d.display_name, d.active, dept.name AS department_name
        FROM doctors d JOIN departments dept ON d.department_id = dept.id
    """;

    public List<Doctor> getAllActive() throws SQLException {
        return queryList(SELECT_BASE + " WHERE d.active = 1 ORDER BY d.display_name ASC", null);
    }

    public List<Doctor> getByDepartmentId(int departmentId) throws SQLException {
        return queryList(SELECT_BASE + " WHERE d.department_id = ? AND d.active = 1 ORDER BY d.display_name ASC", departmentId);
    }

    public Doctor getById(int id) throws SQLException {
        List<Doctor> list = queryList(SELECT_BASE + " WHERE d.id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<Doctor> queryList(String sql, Integer param) throws SQLException {
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Doctor(rs.getInt("id"), rs.getInt("department_id"), rs.getString("display_name"), rs.getInt("active") == 1, rs.getString("department_name")));
                }
            }
        }
        return list;
    }
}
