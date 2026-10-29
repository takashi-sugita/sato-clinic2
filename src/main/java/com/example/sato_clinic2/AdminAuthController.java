package com.example.sato_clinic2;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
public class AdminAuthController {
    static final String SESSION_KEY = "satoAdminLoginId";

    private final JdbcTemplate jdbc;

    public AdminAuthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        if (request == null || request.loginId() == null || request.password() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "IDまたはパスワードが正しくありません。"));
        }
        var hashes = jdbc.query("SELECT password_hash FROM admin_users WHERE login_id = ?",
                (rs, rowNum) -> rs.getString("password_hash"), request.loginId());
        if (hashes.isEmpty() || !PasswordHasher.matches(request.password(), hashes.get(0))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "IDまたはパスワードが正しくありません。"));
        }
        session.setAttribute(SESSION_KEY, request.loginId());
        return ResponseEntity.ok(Map.of("message", "ログインしました。"));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<?> me(WebRequest request) {
        Object loginId = request.getAttribute(SESSION_KEY, WebRequest.SCOPE_SESSION);
        if (loginId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(Map.of("loginId", loginId));
    }

    static boolean isLoggedIn(WebRequest request) {
        return request.getAttribute(SESSION_KEY, WebRequest.SCOPE_SESSION) != null;
    }

    public record LoginRequest(String loginId, String password) { }
}
