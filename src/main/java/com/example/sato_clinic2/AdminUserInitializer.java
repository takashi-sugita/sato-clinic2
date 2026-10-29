package com.example.sato_clinic2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer {
    public AdminUserInitializer(JdbcTemplate jdbc,
            @Value("${ADMIN_ID:admin}") String loginId,
            @Value("${ADMIN_PASSWORD:edix}") String password) {
        Integer existing = jdbc.queryForObject("SELECT COUNT(*) FROM admin_users WHERE login_id = ?",
                Integer.class, loginId);
        if (existing == null || existing == 0) {
            jdbc.update("INSERT INTO admin_users (login_id, password_hash) VALUES (?, ?)",
                    loginId, PasswordHasher.hash(password));
        }
    }
}
