package com.example.sato_clinic2;

import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
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

    private final String adminId;
    private final String adminPassword;

    public AdminAuthController(
            @Value("${ADMIN_ID:admin}") String adminId,
            @Value("${ADMIN_PASSWORD:edix}") String adminPassword) {
        this.adminId = adminId;
        this.adminPassword = adminPassword;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        if (request == null || !Objects.equals(adminId, request.loginId())
                || !Objects.equals(adminPassword, request.password())) {
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
