package in.clinicqueue.repository;

import in.clinicqueue.database.DatabaseManager;
import in.clinicqueue.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentRepository {
    public List<Department> getAllActive() throws SQLException {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT id, name, active FROM departments WHERE active = 1 ORDER BY name ASC";
        try (Connection conn = DatabaseManager.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) list.add(new Department(rs.getInt("id"), rs.getString("name"), rs.getInt("active") == 1));
        }
        return list;
    }

    public Department getById(int id) throws SQLException {
        String sql = "SELECT id, name, active FROM departments WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Department(rs.getInt("id"), rs.getString("name"), rs.getInt("active") == 1) : null;
            }
        }
    }
}
