# 🎨 AI Chatbot 테스트 페이지 가이드

## 📋 개요

AI Chatbot의 마크다운 리포트 응답을 테스트할 수 있는 정적 HTML 페이지입니다.

---

## 🚀 접속 방법

### 1. 애플리케이션 실행
```bash
cd C:\dev\ssafy_git\backend
.\gradlew bootRun
```

### 2. 브라우저에서 접속
```
http://localhost:8080/chatbot-test.html
```

---

## ✨ 주요 기능

### 1. **마크다운 렌더링** ✅
- AI가 생성한 마크다운 리포트를 실시간으로 렌더링
- 테이블, 목록, 강조, 이모지 모두 지원
- `marked.js` 라이브러리 사용

### 2. **로딩 표시** ✅
- AI가 응답을 생성하는 동안 로딩 스피너 표시
- "AI가 데이터를 분석하고 있습니다..." 메시지

### 3. **아파트 상세 정보** ✅
- 리포트의 `results` 배열을 아파트 카드로 표시
- 카드 클릭 시 `/api/v1/apartments/{aptSeq}` API 호출
- 커스텀 모달로 상세 정보 표시:
  - 기본 정보 (이름, 위치, 준공년도, 주소)
  - 평형별 통계 (평균/최고/최저 가격, 거래 건수)
  - 최근 거래 내역 (최대 5건)
  - 추가 정보 (총 거래 건수, 좌표)

### 4. **빠른 질문** ✅
- 자주 사용하는 질문을 버튼으로 제공
- 클릭 한 번으로 질문 전송

---

## 🎯 사용 예시

### 예시 1: 지역 비교
1. "🔍 지역 비교" 버튼 클릭 또는 직접 입력
2. AI가 Tool Calling으로 데이터 조회
3. 마크다운 형식의 비교 리포트 표시
4. 참고 아파트 목록이 하단에 카드 형태로 표시
5. 아파트 카드 클릭 → 상세 정보 모달 팝업

### 예시 2: 아파트 추천
1. "🏢 아파트 추천" 버튼 클릭
2. 로딩 스피너 표시
3. AI가 추천 아파트 리스트와 함께 리포트 생성
4. 각 아파트 카드에서 평균가, 평형 확인
5. 클릭하여 상세 거래 내역 확인

---

## 🎨 UI 특징

### 디자인
- **그라데이션 배경**: Purple/Blue 그라데이션
- **깔끔한 카드**: 둥근 모서리, 그림자 효과
- **반응형 디자인**: 다양한 화면 크기 지원
- **애니메이션**: 페이드인, 슬라이드업 효과

### 컬러 스킴
- **Primary**: #667eea (Purple Blue)
- **Secondary**: #764ba2 (Purple)
- **Background**: #f8f9fa (Light Gray)
- **Text**: #333 (Dark Gray)

### 마크다운 스타일링
```css
# 제목 → 보라색, 하단 테두리
## 소제목 → 진한 보라색
테이블 → 헤더 보라색, 호버 효과
강조 → 볼드 + 보라색
```

---

## 💻 코드 구조

### HTML 구조
```html
<div class="container">
  <div class="header">헤더</div>
  <div class="chat-container">채팅 메시지들</div>
  <div class="loading-container">로딩 스피너</div>
  <div class="input-container">
    <div class="quick-questions">빠른 질문 버튼들</div>
    <div class="input-wrapper">입력창 + 전송 버튼</div>
  </div>
</div>

<div class="custom-alert">커스텀 모달</div>
```

### JavaScript 주요 함수

#### `sendMessage()`
- 사용자 입력을 받아 API 호출
- `/api/v1/ai/chat` POST 요청
- 응답을 마크다운으로 렌더링

#### `addMessage(role, content, results)`
- 채팅 메시지 추가
- `marked.parse()`로 마크다운 렌더링
- results가 있으면 아파트 카드 생성

#### `showApartmentDetail(aptSeq, aptName)`
- 아파트 상세 정보 조회
- `/api/v1/apartments/{aptSeq}?pyung=all` GET 요청
- 커스텀 모달에 정보 표시

#### `formatApartmentDetail(data)`
- API 응답 데이터를 HTML로 포맷팅
- 기본 정보, 통계, 거래 내역 섹션 구성

---

## 📊 API 통합

### 1. AI Chat API
```javascript
POST /api/v1/ai/chat
Content-Type: application/json

{
  "message": "강남구의 평균 거래가는?"
}

Response:
{
  "data": {
    "analysis": "# 📊 강남구 분석...",  // Markdown
    "results": [                          // 아파트 목록
      {
        "aptSeq": "11680-1",
        "aptName": "래미안강남",
        "aptDong": "역삼동",
        "avgPrice": 125000,
        "primaryPyung": 32
      }
    ]
  }
}
```

### 2. Apartment Detail API
```javascript
GET /api/v1/apartments/11680-1?pyung=all

Response:
{
  "data": {
    "aptName": "래미안강남",
    "dongName": "역삼동",
    "buildYear": 2018,
    "roadName": "역삼로 123",
    "pyungStats": [...],      // 평형별 통계
    "recentDeals": [...],     // 최근 거래
    "totalDealCount": 45,
    "latitude": 37.123,
    "longitude": 127.123
  }
}
```

---

## 🎯 사용자 흐름

```
1. 페이지 접속
   ↓
2. 환영 메시지 표시
   ↓
3. 빠른 질문 선택 or 직접 입력
   ↓
4. [전송] 클릭 또는 Enter
   ↓
5. 사용자 메시지 표시 (오른쪽, 보라색)
   ↓
6. 로딩 스피너 표시
   ↓
7. AI가 Tool Calling으로 데이터 조회
   ↓
8. 마크다운 리포트 렌더링 (왼쪽, 흰색)
   ↓
9. 참고 아파트 카드 표시 (있는 경우)
   ↓
10. 아파트 카드 클릭
    ↓
11. 상세 정보 API 호출
    ↓
12. 커스텀 모달 팝업
    ↓
13. 평형별 통계 + 최근 거래 표시
```

---

## 🔧 커스터마이징

### 컬러 변경
```css
/* 메인 컬러 변경 */
background: linear-gradient(135deg, #새컬러1 0%, #새컬러2 100%);

/* 테이블 헤더 컬러 */
.message-content table th {
    background: #새컬러;
}
```

### API 엔드포인트 변경
```javascript
const API_BASE_URL = '/api/v1';  // 여기서 변경
```

### 빠른 질문 추가
```html
<button class="quick-btn" onclick="sendQuickQuestion('새로운 질문')">
    🆕 새 질문
</button>
```

---

## 🐛 문제 해결

### 1. 마크다운이 렌더링되지 않음
- **원인**: marked.js 로드 실패
- **해결**: CDN 연결 확인 또는 로컬 파일 사용

### 2. 아파트 상세 정보 조회 실패
- **원인**: API 엔드포인트 불일치
- **해결**: `API_BASE_URL` 확인, CORS 설정 확인

### 3. 로딩이 계속 표시됨
- **원인**: API 응답 대기 중 오류
- **해결**: 브라우저 콘솔에서 오류 확인

### 4. 스타일이 깨짐
- **원인**: CSS 충돌
- **해결**: 다른 CSS 파일과 격리 확인

---

## 📱 반응형 디자인

### 데스크톱 (1200px+)
- 채팅 컨테이너: 최대 1200px
- 메시지 최대 너비: 85%
- 빠른 질문 버튼: 한 줄에 4개

### 태블릿 (768px - 1199px)
- 채팅 컨테이너: 100%
- 메시지 최대 너비: 90%
- 빠른 질문 버튼: 한 줄에 2-3개

### 모바일 (767px 이하)
- 채팅 컨테이너: 전체 화면
- 메시지 최대 너비: 95%
- 빠른 질문 버튼: 세로 배치

---

## 🎉 완료!

**파일 위치**: `src/main/resources/static/chatbot-test.html`

**접속 URL**: `http://localhost:8080/chatbot-test.html`

**주요 기능**:
- ✅ 마크다운 렌더링
- ✅ 로딩 표시
- ✅ 아파트 상세 정보 모달
- ✅ 반응형 디자인
- ✅ 빠른 질문 버튼
- ✅ 실시간 채팅 UI

이제 AI Chatbot의 마크다운 리포트를 시각적으로 테스트할 수 있습니다! 🚀

