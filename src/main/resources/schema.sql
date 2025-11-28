-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema ssafy_home
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `ssafy_home` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `ssafy_home` ;

-- -----------------------------------------------------
-- Table `ssafy_home`.`dongcodes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_home`.`dongcodes` ;

CREATE TABLE IF NOT EXISTS `ssafy_home`.`dongcodes` (
    `dong_code` VARCHAR(10) NOT NULL COMMENT '법정동코드',
    `sido_name` VARCHAR(30) NULL DEFAULT NULL COMMENT '시도이름',
    `gugun_name` VARCHAR(30) NULL DEFAULT NULL COMMENT '구군이름',
    `dong_name` VARCHAR(30) NULL DEFAULT NULL COMMENT '동이름',
    `latitude` DECIMAL(10, 8) NULL DEFAULT NULL COMMENT '대표 위도',
    `longitude` DECIMAL(11, 8) NULL DEFAULT NULL COMMENT '대표 경도',
    PRIMARY KEY (`dong_code`),
    INDEX `idx_coordinates` (`latitude` ASC, `longitude` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '법정동코드테이블';


-- -----------------------------------------------------
-- Table `ssafy_home`.`houseinfos`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_home`.`houseinfos` ;

CREATE TABLE IF NOT EXISTS `ssafy_home`.`houseinfos` (
    `apt_seq` VARCHAR(20) NOT NULL COMMENT '아파트코드(식별자)',
    `sgg_cd` VARCHAR(5) NULL DEFAULT NULL COMMENT '시군구코드',
    `umd_cd` VARCHAR(5) NULL DEFAULT NULL COMMENT '읍면동코드',
    `jibun` VARCHAR(10) NULL DEFAULT NULL COMMENT '지번',
    `road_nm` VARCHAR(20) NULL DEFAULT NULL COMMENT '도로명',
    `road_nm_bonbun` VARCHAR(10) NULL DEFAULT NULL COMMENT '도로명기초번호',
    `road_nm_bubun` VARCHAR(10) NULL DEFAULT NULL COMMENT '도로명추가번호',
    `apt_nm` VARCHAR(40) NULL DEFAULT NULL COMMENT '아파트이름',
    `build_year` INT NULL DEFAULT NULL COMMENT '준공년도',
    `latitude` VARCHAR(40) NULL DEFAULT NULL COMMENT '위도',
    `longitude` VARCHAR(40) NULL DEFAULT NULL COMMENT '경도',
    `geo_status` VARCHAR(10) DEFAULT 'READY' COMMENT '지오코딩상태(READY, DONE, FAIL)',
    PRIMARY KEY (`apt_seq`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '주택정보테이블';


-- -----------------------------------------------------
-- Table `ssafy_home`.`housedeals`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_home`.`housedeals` ;

CREATE TABLE IF NOT EXISTS `ssafy_home`.`housedeals` (
    `no` INT NOT NULL AUTO_INCREMENT COMMENT '거래번호',
    `apt_seq` VARCHAR(20) NULL DEFAULT NULL COMMENT '아파트코드',
    `apt_dong` VARCHAR(40) NULL DEFAULT NULL COMMENT '아파트동',
    `floor` VARCHAR(3) NULL DEFAULT NULL COMMENT '아파트층',
    `deal_date` INT NULL DEFAULT NULL COMMENT '거래일자(YYYYMMDD)',
    `exclu_use_ar` DECIMAL(7,2) NULL DEFAULT NULL COMMENT '전용면적',
    `deal_amount` INT NULL DEFAULT NULL COMMENT '거래가격(만원단위)',
    PRIMARY KEY (`no`),
    INDEX `apt_seq_to_house_info_idx` (`apt_seq` ASC) VISIBLE,
    CONSTRAINT `apt_seq_to_house_info`
    FOREIGN KEY (`apt_seq`)
    REFERENCES `ssafy_home`.`houseinfos` (`apt_seq`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    AUTO_INCREMENT = 1
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '주택거래정보테이블';

-- -----------------------------------------------------
-- Table `ssafy_home`.`apt_job_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_home`.`apt_job_history` ;

CREATE TABLE IF NOT EXISTS `ssafy_home`.`apt_job_history` (
    `job_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '작업ID',
    `target_date` VARCHAR(7) NOT NULL COMMENT '적재대상월(YYYYMM)',
    `status` VARCHAR(10) NOT NULL DEFAULT 'RUNNING' COMMENT '상태(SUCCESS/FAIL/RUNNING)',
    `row_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '적재건수',
    `start_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시작시간',
    `end_at` DATETIME NULL DEFAULT NULL COMMENT '종료시간',
    `error_msg` TEXT NULL DEFAULT NULL COMMENT '에러메시지',
    PRIMARY KEY (`job_id`),
    INDEX `idx_status` (`status` ASC) VISIBLE,
    INDEX `idx_target_date` (`target_date` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '아파트실거래가_수집이력';

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;