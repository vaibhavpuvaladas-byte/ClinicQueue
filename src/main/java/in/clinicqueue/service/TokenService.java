package in.clinicqueue.service;

/**
 * Generates formatted, sequential daily tokens for doctor visits.
 * Standard format: D{doctorId:02d}-{YYYY-MM-DD}-{sequence:03d}
 * Example: D01-2026-10-20-001
 */
public class TokenService {

    /**
     * Formats a token string according to the specification.
     * 
     * @param doctorId Doctor ID
     * @param date Calendar date (YYYY-MM-DD)
     * @param sequence Sequential integer for that doctor on that date (1, 2, 3...)
     * @return Formatted token string (e.g. D01-2026-10-20-001)
     */
    public String formatToken(int doctorId, String date, int sequence) {
        return String.format("D%02d-%s-%03d", doctorId, date, sequence);
    }
}
