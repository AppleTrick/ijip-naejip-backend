# AI Chatbot Tool Calling 개선

## 📋 개요
기존의 단계적 SQL 생성/실행 방식에서 **OpenAI Tool Calling** 방식으로 전환하여 AI가 자율적으로 SQL을 생성하고 실행할 수 있도록 개선했습니다.

## 🎯 주요 개선 사항

### 1. Tool Calling 방식 도입
- **기존**: AI가 JSON 응답 → 파싱 → SQL 실행 → 재해석 (4단계)
- **개선**: AI가 Tool을 직접 호출하여 SQL 실행 (1단계)

### 2. 병렬 쿼리 실행 지원
- AI가 필요에 따라 **여러 SQL을 동시에 실행** 가능
- 예: "강남구와 서초구의 평균 가격 비교" → 2개의 쿼리를 병렬로 실행

### 3. 구조 개선
- 복잡한 파싱 로직 제거
- AI가 자율적으로 데이터를 수집하고 분석
- 더 자연스러운 대화형 응답

## 🏗️ 아키텍처

```
User Question
     ↓
AIChatbotService
     ↓
ChatClient (with Tool)
     ↓
AI Model (GPT-4, etc.)
     ├─→ [Tool Call] executeDatabaseQuery(sql="SELECT...")
     ├─→ [Tool Call] executeDatabaseQuery(sql="SELECT...")  (병렬)
     └─→ [Tool Call] executeDatabaseQuery(sql="SELECT...")  (병렬)
          ↓
     DatabaseQueryTool
          ↓
     SqlQueryValidator → JdbcTemplate
          ↓
     Query Results → AI → Natural Language Response
```

## 📁 변경된 파일

### 1. `DatabaseQueryTool.java` (신규)
```java
@Component("executeDatabaseQuery")
public class DatabaseQueryTool implements Function<QueryRequest, QueryResponse>
```
- AI가 호출할 수 있는 SQL 실행 도구
- 보안 검증 후 쿼리 실행
- 성공/실패 결과를 구조화된 형태로 반환

### 2. `AIChatbotServiceImpl.java` (대폭 간소화)
```java
String response = chatClient.prompt()
    .system(systemPrompt)
    .user(userMessage)
    .functions("executeDatabaseQuery")  // Tool 등록
    .call()
    .content();
```
- 기존 4단계 프로세스 → 1단계로 간소화
- SQL 파싱, 재시도, 결과 해석 로직 제거
- AI가 자율적으로 처리

### 3. `PromptTemplateManager.java` (업데이트)
- Tool 사용 방식에 맞게 프롬프트 재작성
- 병렬 실행 가이드라인 추가
- JSON 응답 형식 제거

### 4. `AIConfig.java` (정리)
- Function Callback 설정 제거 (자동 감지)

## 🚀 사용 예시

### 예시 1: 단일 쿼리
**질문**: "강남구의 최근 거래된 아파트를 보여주세요"

**AI 동작**:
1. Tool Call: `executeDatabaseQuery(sql="SELECT ...")`
2. 결과 수신
3. 자연어 응답 생성

### 예시 2: 병렬 쿼리
**질문**: "강남구와 서초구의 평균 가격을 비교해주세요"

**AI 동작**:
1. **병렬** Tool Call:
   - `executeDatabaseQuery(sql="SELECT AVG(deal_amount) FROM ... WHERE 강남구")`
   - `executeDatabaseQuery(sql="SELECT AVG(deal_amount) FROM ... WHERE 서초구")`
2. 두 결과 수신
3. 비교 분석 응답 생성

### 예시 3: 복잡한 분석
**질문**: "3억 이하 아파트 중 거래량이 많은 지역 Top 5를 알려주세요"

**AI 동작**:
1. Tool Call: 복잡한 JOIN 및 GROUP BY 쿼리
2. 필요시 추가 쿼리 병렬 실행
3. 상세 분석 제공

## 🔒 보안

- `SqlQueryValidator`를 통한 보안 검증 유지
- SELECT 문만 허용
- 허용된 테이블만 접근 가능
- SQL Injection 방어

## 📊 성능

### 개선점
✅ **병렬 처리**: 여러 쿼리 동시 실행  
✅ **중간 단계 제거**: 파싱/재해석 오버헤드 제거  
✅ **재시도 자동화**: AI가 오류 시 자동으로 수정  

### 기존 방식
```
사용자 질문 → AI(SQL 생성) → 파싱 → 실행 → AI(결과 해석) → 응답
예상 시간: 5-10초
```

### 개선 방식
```
사용자 질문 → AI + Tool(병렬 실행) → 응답
예상 시간: 3-6초
```

## 🧪 테스트

### 테스트 API
```bash
POST http://localhost:8080/api/v1/ai/chat
Content-Type: application/json

{
  "message": "강남구의 최근 거래 아파트를 보여주세요"
}
```

### 로그 확인
```
Tool Called - Description: Recent apartment deals in Gangnam
Tool Called - SQL: SELECT h.apt_nm, d.deal_amount, d.deal_date ...
Query executed successfully. Rows: 10
```

## 🔄 마이그레이션 가이드

### 제거된 컴포넌트
- ❌ `SqlResponseParser` - 더 이상 JSON 파싱 불필요
- ❌ `SqlQueryExecutor` - Tool이 직접 실행
- ❌ `ResponseInterpreter` - AI가 직접 응답 생성

### 유지된 컴포넌트
- ✅ `SqlQueryValidator` - 보안 검증
- ✅ `PromptTemplateManager` - 프롬프트 관리 (업데이트됨)
- ✅ `AIChatbotService` - 인터페이스 유지

## 💡 장점

1. **코드 간소화**: 300+ 줄 → 80 줄
2. **병렬 처리**: 복잡한 질문에 대한 빠른 응답
3. **유연성**: AI가 상황에 맞게 쿼리 개수 결정
4. **자율성**: AI가 오류 시 자동으로 쿼리 수정
5. **확장성**: 새로운 Tool 추가 용이

## 📝 향후 개선 가능 사항

1. **캐싱**: 자주 실행되는 쿼리 결과 캐싱
2. **추가 Tool**: 
   - `get_apartment_details(apt_seq)` - 아파트 상세 정보
   - `calculate_statistics(data)` - 통계 계산
   - `generate_chart(data)` - 차트 생성
3. **스트리밍 응답**: 실시간 Tool 실행 상태 전달
4. **쿼리 최적화**: AI가 실행 계획을 고려하도록 유도

## 🎓 참고 자료

- [Spring AI Function Calling](https://docs.spring.io/spring-ai/reference/api/functions.html)
- [OpenAI Function Calling Guide](https://platform.openai.com/docs/guides/function-calling)

