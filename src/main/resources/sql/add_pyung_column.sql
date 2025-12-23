-- user_houses 테이블에 pyung 컬럼 추가
-- 기존 테이블에 pyung 컬럼이 없으면 실행

ALTER TABLE `ssafy_home`.`user_houses`
ADD COLUMN `pyung` INT NULL COMMENT '선택한 평수' AFTER `apt_seq`;

-- 동일 아파트 중복 방지 (user_id + apt_seq + ownership_type)
-- 기존 인덱스가 없으면 실행
-- ALTER TABLE `ssafy_home`.`user_houses`
-- ADD UNIQUE KEY `unique_user_apt` (`user_id`, `apt_seq`, `ownership_type`);
