package com.example.sato_clinic2;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigration {
    public DatabaseMigration(JdbcTemplate jdbc) {
        Integer columnCount = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'reservations'
                  AND column_name = 'patient_kana'
                """, Integer.class);
        if (columnCount != null && columnCount == 0) {
            jdbc.execute("ALTER TABLE reservations ADD COLUMN patient_kana VARCHAR(100) NOT NULL DEFAULT '' AFTER patient_name");
        }
    }
}
