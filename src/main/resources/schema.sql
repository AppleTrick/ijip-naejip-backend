-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE =
    'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema ssafy_home
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `ssafy_home` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `ssafy_home`;

-- -----------------------------------------------------
-- Table `ssafy_home`.`dongcodes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_home`.`dongcodes`;

CREATE TABLE IF NOT EXISTS `ssafy_home`.`dongcodes`
(
    `dong_code`  VARCHAR(10)    NOT NULL COMMENT '법정동코드',
    `sido_name`  VARCHAR(30)    NULL DEFAULT NULL COMMENT '시도이름',
    `gugun_name` VARCHAR(30)    NULL DEFAULT NULL COMMENT '구군이름',
    `dong_name`  VARCHAR(30)    NULL DEFAULT NULL COMMENT '동이름',
    `latitude`   DECIMAL(10, 8) NULL DEFAULT NULL COMMENT '대표 위도',
    `longitude`  DECIMAL(11, 8) NULL DEFAULT NULL COMMENT '대표 경도',
    `avg_price`  INT            NULL DEFAULT NULL COMMENT '대표가격(지난달+이번달 평균)',
    `update_at`  DATETIME       NULL DEFAULT NULL COMMENT '대표가격 업데이트 시간',
    PRIMARY KEY (`dong_code`),
    INDEX `idx_coordinates` (`latitude` ASC, `longitude` ASC) VISIBLE
)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '법정동코드테이블';


-- -----------------------------------------------------
-- Table `ssafy_home`.`houseinfos`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_home`.`houseinfos`;

CREATE TABLE IF NOT EXISTS `ssafy_home`.`houseinfos`
(
    `apt_seq`       VARCHAR(20) NOT NULL COMMENT '아파트코드(식별자)',
    `sgg_cd`        VARCHAR(5)  NULL DEFAULT NULL COMMENT '시군구코드',
    `umd_cd`        VARCHAR(5)  NULL DEFAULT NULL COMMENT '읍면동코드',
    `jibun`         VARCHAR(10) NULL DEFAULT NULL COMMENT '지번',
    `road_nm`       VARCHAR(20) NULL DEFAULT NULL COMMENT '도로명',
    `road_nm_bonbun` VARCHAR(10) NULL DEFAULT NULL COMMENT '도로명기초번호',
    `road_nm_bubun` VARCHAR(10) NULL DEFAULT NULL COMMENT '도로명추가번호',
    `apt_nm`        VARCHAR(40) NULL DEFAULT NULL COMMENT '아파트이름',
    `build_year`    INT         NULL DEFAULT NULL COMMENT '준공년도',
    `latitude`      VARCHAR(40) NULL DEFAULT NULL COMMENT '위도',
    `longitude`     VARCHAR(40) NULL DEFAULT NULL COMMENT '경도',
    `geo_status`    VARCHAR(10)      DEFAULT 'READY' COMMENT '지오코딩상태(READY, DONE, FAIL)',
    `avg_price`     INT         NULL DEFAULT NULL COMMENT '대표가격(지난달+이번달 평균)',
    `update_at`     DATETIME    NULL DEFAULT NULL COMMENT '대표가격 업데이트 시간',
    PRIMARY KEY (`apt_seq`)
)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '주택정보테이블';


-- -----------------------------------------------------
-- Table `ssafy_home`.`housedeals`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_home`.`housedeals`;

CREATE TABLE IF NOT EXISTS `ssafy_home`.`housedeals`
(
    `no`           INT           NOT NULL AUTO_INCREMENT COMMENT '거래번호',
    `apt_seq`      VARCHAR(20)   NULL DEFAULT NULL COMMENT '아파트코드',
    `apt_dong`     VARCHAR(40)   NULL DEFAULT NULL COMMENT '아파트동',
    `floor`        VARCHAR(3)    NULL DEFAULT NULL COMMENT '아파트층',
    `deal_date`    INT           NULL DEFAULT NULL COMMENT '거래일자(YYYYMMDD)',
    `exclu_use_ar` DECIMAL(7, 2) NULL DEFAULT NULL COMMENT '전용면적',
    `deal_amount`  INT           NULL DEFAULT NULL COMMENT '거래가격(만원단위)',
    PRIMARY KEY (`no`),
    INDEX `apt_seq_to_house_info_idx` (`apt_seq` ASC) VISIBLE,
    CONSTRAINT `apt_seq_to_house_info`
        FOREIGN KEY (`apt_seq`)
            REFERENCES `ssafy_home`.`houseinfos` (`apt_seq`)
            ON DELETE CASCADE
            ON UPDATE CASCADE
)
    ENGINE = InnoDB
    AUTO_INCREMENT = 1
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '주택거래정보테이블';

-- -----------------------------------------------------
-- Table `ssafy_home`.`apt_job_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_home`.`apt_job_history`;

CREATE TABLE IF NOT EXISTS `ssafy_home`.`apt_job_history`
(
    `job_id`      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '작업ID',
    `target_date` VARCHAR(7)      NOT NULL COMMENT '적재대상월(YYYYMM)',
    `status`      VARCHAR(10)     NOT NULL DEFAULT 'RUNNING' COMMENT '상태(SUCCESS/FAIL/RUNNING)',
    `row_count`   INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '적재건수',
    `start_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시작시간',
    `end_at`      DATETIME        NULL     DEFAULT NULL COMMENT '종료시간',
    `error_msg`   TEXT            NULL     DEFAULT NULL COMMENT '에러메시지',
    PRIMARY KEY (`job_id`),
    INDEX `idx_status` (`status` ASC) VISIBLE,
    INDEX `idx_target_date` (`target_date` ASC) VISIBLE
)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '아파트실거래가_수집이력';

-- -----------------------------------------------------
-- Table `ssafy_home`.`users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_home`.`users`;

CREATE TABLE IF NOT EXISTS `ssafy_home`.`users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(100) NOT NULL COMMENT '이메일 (로그인 ID)',
    `password` VARCHAR(255) NULL COMMENT '비밀번호 (OAuth2 가입 시 NULL 가능)',
    `name` VARCHAR(50) NOT NULL COMMENT '사용자 이름/닉네임',
    `phone` VARCHAR(20) NULL COMMENT '전화번호',
    `profile_image` VARCHAR(255) NULL COMMENT '프로필 이미지 URL',
    `role` ENUM('ROLE_USER', 'ROLE_ADMIN') NOT NULL DEFAULT 'ROLE_USER',
    `social_type` ENUM('NONE', 'KAKAO', 'NAVER', 'GOOGLE') NOT NULL DEFAULT 'NONE',
    `social_id` VARCHAR(255) NULL COMMENT '소셜 식별값 (sub/id)',
    `is_email_verified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '이메일 인증 여부 (0/1)',
    `gender` ENUM('male', 'female', 'other') NULL,
    `age_group` ENUM('20s', '30s', '40s', '50+') NULL,
    `job` ENUM('student', 'employee', 'business', 'freelancer', 'other') NULL,
    `marital_status` ENUM('single', 'married') NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '사용자 정보';

-- -----------------------------------------------------
-- Table `ssafy_home`.`user_notification_settings`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_home`.`user_notification_settings`;

CREATE TABLE IF NOT EXISTS `ssafy_home`.`user_notification_settings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `app_push` TINYINT(1) NOT NULL DEFAULT 1,
    `email_noti` TINYINT(1) NOT NULL DEFAULT 1,
    `marketing_noti` TINYINT(1) NOT NULL DEFAULT 0,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `fk_user_noti_user_idx` (`user_id` ASC) VISIBLE,
    CONSTRAINT `fk_user_noti_user`
        FOREIGN KEY (`user_id`)
        REFERENCES `ssafy_home`.`users` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '사용자 알림 설정';

-- -----------------------------------------------------
-- Table `ssafy_home`.`user_houses`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_home`.`user_houses`;

CREATE TABLE IF NOT EXISTS `ssafy_home`.`user_houses` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `apt_seq` VARCHAR(20) NOT NULL,
    `ownership_type` ENUM('OWNED', 'INTEREST') NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `fk_user_houses_user_idx` (`user_id` ASC) VISIBLE,
    INDEX `fk_user_houses_apt_idx` (`apt_seq` ASC) VISIBLE,
    CONSTRAINT `fk_user_houses_user`
        FOREIGN KEY (`user_id`)
        REFERENCES `ssafy_home`.`users` (`id`)
    --     ON DELETE CASCADE
    --     ON UPDATE CASCADE,
    -- CONSTRAINT `fk_user_houses_apt`
    --     FOREIGN KEY (`apt_seq`)
    --     REFERENCES `ssafy_home`.`houseinfos` (`apt_seq`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '사용자 관심/소유 매물';

SET SQL_MODE = @OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS;
