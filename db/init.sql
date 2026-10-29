CREATE TABLE IF NOT EXISTS reservations (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  patient_type ENUM('new', 'existing') NOT NULL,
  card_number VARCHAR(32) NULL,
  department VARCHAR(100) NOT NULL,
  patient_name VARCHAR(100) NOT NULL,
  patient_kana VARCHAR(100) NOT NULL,
  patient_phone VARCHAR(30) NOT NULL,
  reserve_date DATE NOT NULL,
  reserve_time VARCHAR(20) NOT NULL,
  status ENUM('booked', 'cancelled') NOT NULL DEFAULT 'booked',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_active_slot (reserve_date, reserve_time, status),
  KEY idx_reserve_date (reserve_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS admin_users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  login_id VARCHAR(100) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_admin_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
