package in.clinicqueue.repository;

import in.clinicqueue.database.DatabaseManager;
import in.clinicqueue.model.Visit;
import in.clinicqueue.model.VisitStatus;
import in.clinicqueue.model.VisitType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitRepository {
    private static final String SELECT_BASE = """
        SELECT v.id, v.patient_id, v.doctor_id, v.visit_date, v.visit_type,
               v.token_sequence, v.token_number, v.status, v.created_at, v.updated_at,
               p.full_name AS patient_name, p.phone AS patient_phone,
               d.display_name AS doctor_name, dept.name AS department_name
        FROM visits v
        JOIN patients p ON v.patient_id = p.id
        JOIN doctors d ON v.doctor_id = d.id
        JOIN departments dept ON d.department_id = dept.id
    """;

    public int getNextTokenSequence(Connection conn, int doctorId, String visitDate) throws SQLException {
        String sql = "SELECT COALESCE(MAX(token_sequence), 0) + 1 FROM visits WHERE doctor_id = ? AND visit_date = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId); ps.setString(2, visitDate);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 1; }
        }
    }

    public int insert(Connection conn, Visit v) throws SQLException {
        String sql = "INSERT INTO visits (patient_id, doctor_id, visit_date, visit_type, token_sequence, token_number, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getPatientId()); ps.setInt(2, v.getDoctorId()); ps.setString(3, v.getVisitDate());
            ps.setString(4, v.getVisitType().name()); ps.setInt(5, v.getTokenSequence());
            ps.setString(6, v.getTokenNumber()); ps.setString(7, v.getStatus().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) { int id = rs.getInt(1); v.setId(id); return id; }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for visit");
    }

    public boolean hasActiveConsultation(int doctorId, String visitDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM visits WHERE doctor_id = ? AND visit_date = ? AND status = 'IN_CONSULTATION'";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId); ps.setString(2, visitDate);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        }
    }

    public Visit findOldestWaitingVisit(int doctorId, String visitDate) throws SQLException {
        String sql = SELECT_BASE + " WHERE v.doctor_id = ? AND v.visit_date = ? AND v.status = 'WAITING' ORDER BY v.created_at ASC, v.id ASC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId); ps.setString(2, visitDate);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapRow(rs) : null; }
        }
    }

    public boolean updateStatus(int visitId, VisitStatus newStatus) throws SQLException {
        String sql = "UPDATE visits SET status = ?, updated_at = datetime('now', 'localtime') WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name()); ps.setInt(2, visitId);
            return ps.executeUpdate() > 0;
        }
    }

    public Visit getVisitById(int visitId) throws SQLException {
        String sql = SELECT_BASE + " WHERE v.id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, visitId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapRow(rs) : null; }
        }
    }

    public List<Visit> getVisitsForDate(String visitDate) throws SQLException {
        return searchVisits(visitDate, null, null, null);
    }

    public List<Visit> searchVisits(String visitDate, Integer doctorId, VisitStatus status, String queryText) throws SQLException {
        List<Visit> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE v.visit_date = ?");
        List<Object> params = new ArrayList<>();
        params.add(visitDate);

        if (doctorId != null && doctorId > 0) {
            sql.append(" AND v.doctor_id = ?"); params.add(doctorId);
        }
        if (status != null) {
            sql.append(" AND v.status = ?"); params.add(status.name());
        }
        if (queryText != null && !queryText.trim().isEmpty()) {
            sql.append(" AND (LOWER(p.full_name) LIKE ? OR LOWER(v.token_number) LIKE ?)");
            String q = "%" + queryText.trim().toLowerCase() + "%";
            params.add(q); params.add(q);
        }
        sql.append(" ORDER BY v.created_at ASC, v.id ASC");

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Visit mapRow(ResultSet rs) throws SQLException {
        Visit v = new Visit(
            rs.getInt("id"), rs.getInt("patient_id"), rs.getInt("doctor_id"),
            rs.getString("visit_date"), VisitType.valueOf(rs.getString("visit_type")),
            rs.getInt("token_sequence"), rs.getString("token_number"),
            VisitStatus.valueOf(rs.getString("status")),
            rs.getString("created_at"), rs.getString("updated_at")
        );
        v.setPatientName(rs.getString("patient_name"));
        v.setPatientPhone(rs.getString("patient_phone"));
        v.setDoctorName(rs.getString("doctor_name"));
        v.setDepartmentName(rs.getString("department_name"));
        return v;
    }
}
