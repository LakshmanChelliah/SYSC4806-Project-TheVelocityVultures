package vv.pms.auth;

/**
 * Auth module's public DTO describing a logged-in principal. Kept in auth to avoid UI <> auth coupling.
 */
public record LoginRecord(Long id, String name, String email, String role) {
}
