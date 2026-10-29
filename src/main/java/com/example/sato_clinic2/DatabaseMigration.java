package com.example.sato_clinic2;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigration {
    public DatabaseMigration(JdbcTemplate jdbc) {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS news_items (
                  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                  publish_date DATE NOT NULL,
                  title VARCHAR(255) NOT NULL,
                  tag VARCHAR(100) NOT NULL,
                  href VARCHAR(500) NOT NULL DEFAULT '/information/index.html',
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  PRIMARY KEY (id),
                  KEY idx_news_publish_date (publish_date)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

        Integer newsCount = jdbc.queryForObject("SELECT COUNT(*) FROM news_items", Integer.class);
        if (newsCount != null && newsCount == 0) {
            jdbc.update("""
                    INSERT INTO news_items (publish_date, title, tag, href) VALUES
                    ('2025-11-20', 'インフルエンザ予防接種について', 'お知らせ', '/information/index.html#flu'),
                    ('2025-11-10', '年末年始の休診について', '休診', '/information/index.html#holiday'),
                    ('2025-10-28', '健康診断のご案内', 'ご案内', '/information/index.html#checkup'),
                    ('2025-10-15', '新型コロナウイルス対策について', 'お知らせ', '/information/index.html#covid'),
                    ('2025-10-01', 'オンライン診療の導入について', 'ご案内', '/information/index.html#online')
                    """);
        }

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
