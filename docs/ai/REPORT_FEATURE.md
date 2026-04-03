# AI 보고서 자동 저장 기능

## 개요
AIChatbotController에서 생성되는 마크다운 형태의 분석 결과를 자동으로 영구 저장하는 기능입니다.
**로그인한 사용자의 모든 채팅이 자동으로 보고서로 저장됩니다.**

## 특징
- ✅ **완전 자동**: 채팅할 때마다 자동으로 보고서 생성
- ✅ **자동 제목**: 질문 내용에서 제목 자동 생성 (최대 50자)
- ✅ **비침투적**: 저장 실패 시에도 채팅 응답에 영향 없음
- ✅ **로그인 사용자만**: JWT 토큰이 있는 경우에만 저장

## 데이터베이스 스키마

### ai_reports 테이블
```sql
CREATE TABLE IF NOT EXISTS `ssafy_home`.`ai_reports` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '보고서 ID',
    `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
    `title` VARCHAR(255) NOT NULL COMMENT '보고서 제목 (자동 생성)',
    `query` TEXT NOT NULL COMMENT '사용자 질문',
    `markdown_content` LONGTEXT NOT NULL COMMENT '마크다운 형태의 분석 결과',
    `result_count` INT DEFAULT 0 COMMENT '검색 결과 개수',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id` ASC),
    INDEX `idx_created_at` (`created_at` DESC),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);
```

## API 엔드포인트

### 채팅 질의 (자동 저장)
**POST** `/api/v1/ai/chat`

AI 챗봇 질의 시 자동으로 보고서가 저장됩니다.

**Request Body:**
```json
{
  "message": "강남구 아파트 가격 분석해줘"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "results": [...],
    "analysis": "# 분석 결과\n..."
  }
}
```

**동작 방식:**
1. 사용자가 채팅 요청
2. AI가 분석 결과 생성
3. **자동으로 보고서 저장** (로그인 사용자만)
   - 제목: 질문 내용에서 자동 생성 (최대 50자)
   - 내용: 마크다운 분석 결과
   - 결과 개수: 검색된 아파트 수
4. 응답 반환

## 보고서 조회 방법

보고서는 데이터베이스에 저장되므로 다음과 같이 조회할 수 있습니다:

### 사용자별 보고서 조회
```sql
-- 특정 사용자의 모든 보고서 (최신순)
SELECT * FROM ai_reports 
WHERE user_id = ? 
ORDER BY created_at DESC;

-- 최근 10개 보고서
SELECT * FROM ai_reports 
WHERE user_id = ? 
ORDER BY created_at DESC 
LIMIT 10;
```

### 보고서 상세 조회
```sql
-- 특정 보고서 전체 내용
SELECT * FROM ai_reports 
WHERE id = ? AND user_id = ?;
```

## 사용 예시

### 1. 채팅 요청 (자동 저장됨)
```javascript
// 프론트엔드 예시
const response = await fetch('/api/v1/ai/chat', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_JWT_TOKEN'  // 로그인 필요
  },
  body: JSON.stringify({
    message: '강남구 10억 이하 아파트 찾아줘'
  })
});

// 자동으로 다음 정보가 저장됨:
// - title: "강남구 10억 이하 아파트 찾아줘"
// - query: "강남구 10억 이하 아파트 찾아줘"
// - markdown_content: AI 분석 결과
// - result_count: 검색된 아파트 개수
```

### 2. 비로그인 사용자
```javascript
// JWT 토큰 없이 요청하면 보고서는 저장되지 않음 (응답은 정상 반환)
const response = await fetch('/api/v1/ai/chat', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    message: '강남구 아파트 가격 분석'
  })
});
```

## 주요 기능

1. **완전 자동 저장**: 옵션 선택 불필요, 모든 채팅이 자동 저장
2. **자동 제목 생성**: 질문 내용에서 자동으로 제목 생성 (최대 50자)
3. **마크다운 지원**: 분석 결과를 마크다운 형식으로 저장
4. **실패 안전**: 저장 실패해도 채팅 응답은 정상 반환
5. **권한 관리**: 본인의 보고서만 데이터베이스에서 조회 가능
6. **자동 정리**: 사용자 삭제 시 보고서도 자동 삭제 (CASCADE)

## 보안

- **JWT 토큰 기반 인증** (필수)
- **로그인 필수**: AI 채팅 사용 시 반드시 로그인 필요
- **비로그인 차단**: 로그인하지 않은 사용자는 401 에러 반환
- **사용자별 보고서 격리**: 본인의 보고서만 접근 가능
- **Foreign Key로 CASCADE 삭제**: 사용자 삭제 시 보고서도 자동 삭제

## 성능 최적화

- `user_id`에 인덱스 생성 (빠른 조회)
- `created_at`에 인덱스 생성 (최신순 정렬 최적화)
- 비동기 저장 방식 (저장 실패해도 응답 지연 없음)
- 트랜잭션 관리

## 파일 구조

```
src/main/java/com/ssafy/home/ai/
├── controller/
│   └── AIChatbotController.java      # 수정: 자동 저장 기능 추가
├── service/
│   ├── AIReportService.java          # 신규: 자동 저장 서비스 인터페이스
│   └── impl/
│       └── AIReportServiceImpl.java  # 신규: 자동 저장 서비스 구현
├── mapper/
│   └── AIReportMapper.java           # 신규: 보고서 매퍼
└── dto/
    ├── AIReport.java                 # 신규: 보고서 엔티티
    └── AIReportResponse.java         # 신규: 보고서 응답 DTO

src/main/resources/
├── schema.sql                        # 수정: ai_reports 테이블 추가
└── mapper/
    └── AIReportMapper.xml            # 신규: 보고서 매퍼 XML
```

## 향후 확장 가능 기능

1. **보고서 검색**: 제목, 내용 기반 검색 API
2. **보고서 공유**: 링크로 공유 기능
3. **보고서 내보내기**: PDF, Word 변환
4. **보고서 태그**: 카테고리 분류
5. **페이징**: 목록 조회 시 페이징 처리
6. **보고서 통계**: 사용자별 분석 통계

