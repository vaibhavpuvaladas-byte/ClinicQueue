package in.clinicqueue.repository;

import in.clinicqueue.database.DatabaseManager;
import in.clinicqueue.model.Visit;
import in.clinicqueue.model.VisitStatus;
import in.clinicqueue.model.VisitType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for Visits.
 * Implements pure JDBC prepared statements and transactional insertions.
 */
public class VisitRepository {

    /**
     * Calculates the next sequential token number for a given doctor on a specific calendar date.
     * Starts at 1 each day for each doctor.
     */
    public int getNextTokenSequence(Connection conn, int doctorId, String visitDate) throws SQLException {
        String sql = "SELECT COALESCE(MAX(token_sequence), 0) + 1 FROM visits WHERE doctor_id = ? AND visit_date = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, visitDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 1;
    }

    /**
     * Inserts a visit record within an active database transaction.
     */
    public int insert(Connection conn, Visit visit) throws SQLException {
        String sql = """
            INSERT INTO visits (
                patient_id, doctor_id, visit_date, visit_type,
                token_sequence, token_number, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, visit.getPatientId());
            pstmt.setInt(2, visit.getDoctorId());
            pstmt.setString(3, visit.getVisitDate());
            pstmt.setString(4, visit.getVisitType().name());
            pstmt.setInt(5, visit.getTokenSequence());
            pstmt.setString(6, visit.getTokenNumber());
            pstmt.setString(7, visit.getStatus().name());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    visit.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for visit token: " + visit.getTokenNumber());
    }

    /**
     * Checks if a doctor already has an active visit in IN_CONSULTATION status today.
     */
    public boolean hasActiveConsultation(int doctorId, String visitDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM visits WHERE doctor_id = ? AND visit_date = ? AND status = 'IN_CONSULTATION'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, visitDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Finds the oldest WAITING visit for a doctor on a specific date (FIFO).
     */
    public Visit findOldestWaitingVisit(int doctorId, String visitDate) throws SQLException {
        String sql = """
            SELECT v.id, v.patient_id, v.doctor_id, v.visit_date, v.visit_type,
                   v.token_sequence, v.token_number, v.status, v.created_at, v.updated_at,
                   p.full_name AS patient_name, p.phone AS patient_phone,
                   d.display_name AS doctor_name, dept.name AS department_name
            FROM visits v
            JOIN patients p ON v.patient_id = p.id
            JOIN doctors d ON v.doctor_id = d.id
            JOIN departments dept ON d.department_id = dept.id
            WHERE v.doctor_id = ? AND v.visit_date = ? AND v.status = 'WAITING'
            ORDER BY v.created_at ASC, v.id ASC
            LIMIT 1
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, visitDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToVisit(rs);
                }
            }
        }
        return null;
    }

    /**
     * Updates the status of an existing visit.
     */
    public boolean updateStatus(int visitId, VisitStatus newStatus) throws SQLException {
        String sql = "UPDATE visits SET status = ?, updated_at = datetime('now', 'localtime') WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.name());
            pstmt.setInt(2, visitId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves a single visit by ID.
     */
    public Visit getVisitById(int visitId) throws SQLException {
        String sql = """
            SELECT v.id, v.patient_id, v.doctor_id, v.visit_date, v.visit_type,
                   v.token_sequence, v.token_number, v.status, v.created_at, v.updated_at,
                   p.full_name AS patient_name, p.phone AS patient_phone,
                   d.display_name AS doctor_name, dept.name AS department_name
            FROM visits v
            JOIN patients p ON v.patient_id = p.id
            JOIN doctors d ON v.doctor_id = d.id
            JOIN departments dept ON d.department_id = dept.id
            WHERE v.id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, visitId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToVisit(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all visits for a given calendar date, ordered by FIFO.
     */
    public List<Visit> getVisitsForDate(String visitDate) throws SQLException {
        List<Visit> list = new ArrayList<>();
        String sql = """
            SELECT v.id, v.patient_id, v.doctor_id, v.visit_date, v.visit_type,
                   v.token_sequence, v.token_number, v.status, v.created_at, v.updated_at,
                   p.full_name AS patient_name, p.phone AS patient_phone,
                   d.display_name AS doctor_name, dept.name AS department_name
            FROM visits v
            JOIN patients p ON v.patient_id = p.id
            JOIN doctors d ON v.doctor_id = d.id
            JOIN departments dept ON d.department_id = dept.id
            WHERE v.visit_date = ?
            ORDER BY v.created_at ASC, v.id ASC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, visitDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToVisit(rs));
                }
            }
        }
        return list;
    }

    /**
     * Searches and filters visits dynamically based on criteria.
     */
    public List<Visit> searchVisits(String visitDate, Integer doctorId, VisitStatus status, String queryText) throws SQLException {
        List<Visit> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT v.id, v.patient_id, v.doctor_id, v.visit_date, v.visit_type,
                   v.token_sequence, v.token_number, v.status, v.created_at, v.updated_at,
                   p.full_name AS patient_name, p.phone AS patient_phone,
                   d.display_name AS doctor_name, dept.name AS department_name
            FROM visits v
            JOIN patients p ON v.patient_id = p.id
            JOIN doctors d ON v.doctor_id = d.id
            JOIN departments dept ON d.department_id = dept.id
            WHERE v.visit_date = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(visitDate);

        if (doctorId != null && doctorId > 0) {
            sql.append(" AND v.doctor_id = ?");
            params.add(doctorId);
        }

        if (status != null) {
            sql.append(" AND v.status = ?");
            params.add(status.name());
        }

        if (queryText != null && !queryText.trim().isEmpty()) {
            sql.append(" AND (LOWER(p.full_name) LIKE ? OR LOWER(v.token_number) LIKE ?)");
            String wildcard = "%" + queryText.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
        }

        sql.append(" ORDER BY v.created_at ASC, v.id ASC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToVisit(rs));
                }
            }
        }
        return list;
    }

    private Visit mapRowToVisit(ResultSet rs) throws SQLException {
        Visit v = new Visit(
            rs.getInt("id"),
            rs.getInt("patient_id"),
            rs.getInt("doctor_id"),
            rs.getString("visit_date"),
            VisitType.valueOf(rs.getString("visit_type")),
            rs.getInt("token_sequence"),
            rs.getString("token_number"),
            VisitStatus.valueOf(rs.getString("status")),
            rs.getString("created_at"),
            rs.getString("updated_at")
        );
        v.setPatientName(rs.getString("patient_name"));
        v.setPatientPhone(rs.getString("patient_phone"));
        v.setDoctorName(rs.getString("doctor_name"));
        v.setDepartmentName(rs.getString("department_name"));
        return v;
    }
}
