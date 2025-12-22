-- 관심 아파트 테이블 생성
-- MySQL/MariaDB

CREATE TABLE IF NOT EXISTS favorite_apartments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    apt_seq VARCHAR(50) NOT NULL,
    apt_name VARCHAR(100),
    address VARCHAR(255),
    pyung INT,
    deal_amount VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_user_apt_pyung (user_id, apt_seq, pyung),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
