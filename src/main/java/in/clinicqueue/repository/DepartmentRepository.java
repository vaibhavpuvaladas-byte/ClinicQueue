package in.clinicqueue.repository;

import in.clinicqueue.database.DatabaseManager;
import in.clinicqueue.model.Department;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for Departments.
 */
public class DepartmentRepository {

    public List<Department> getAllActive() throws SQLException {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT id, name, active FROM departments WHERE active = 1 ORDER BY name ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Department(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("active") == 1
                ));
            }
        }
        return list;
    }
}
