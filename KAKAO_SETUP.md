# KakaoMap 관리자 페이지 설정 가이드

## 📌 개요
이 프로젝트는 KakaoMap API를 사용하여 지역 정보를 시각화하는 관리자 페이지입니다.

## 🔑 Kakao API 키 설정

### 1. Kakao Developers에서 앱 생성
1. [Kakao Developers](https://developers.kakao.com/)에 접속
2. 로그인 후 "내 애플리케이션" 메뉴로 이동
3. "애플리케이션 추가하기" 클릭
4. 앱 이름 입력 후 저장
5. 생성된 앱을 선택하여 상세 페이지로 이동

### 2. JavaScript 키 발급
1. 앱 상세 페이지에서 "JavaScript 키" 확인
2. "플랫폼 설정하기" 메뉴에서 "Web 플랫폼 등록"
3. 사이트 도메인 등록 (예: `http://localhost:8080`)

### 3. admin.html 파일 수정
`src/main/resources/static/admin.html` 파일을 열고 다음 부분을 수정:

```html
<!-- 기존 -->
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_KAKAO_APPKEY"></script>

<!-- 수정 후 -->
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=발급받은_JavaScript_키"></script>
```

## 🚀 사용 방법

### 1. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 2. 관리자 페이지 접속
브라우저에서 다음 URL로 접속:
```
http://localhost:8080/admin.html
```

### 3. 기능 사용
1. **AreaScope 선택**: 검색 범위를 동(DONG), 구군(GUGUN), 시도(SIDO) 중 선택
2. **지도 이동**: 마우스로 원하는 지역으로 지도 이동
3. **검색 실행**: "현재 지도 영역 검색" 버튼 클릭
4. **결과 확인**: 지도에 마커가 표시되고 지역명 확인 가능
5. **마커 클릭**: 마커를 클릭하면 상세 정보 표시

### 4. 주요 기능
- **지도 초기화**: 서울 시청 기준으로 지도를 리셋
- **마커 모두 제거**: 현재 표시된 모든 마커 제거
- **실시간 지도 정보**: 현재 지도의 중심 좌표와 확대 레벨 표시

## 📊 API 엔드포인트

### 지역 검색 API
```
GET /api/v1/area
```

**파라미터:**
- `minLat`: 최소 위도 (필수)
- `maxLat`: 최대 위도 (필수)
- `minLng`: 최소 경도 (필수)
- `maxLng`: 최대 경도 (필수)
- `scope`: 검색 범위 (dong/gugun/sido, 필수)

**응답 예시:**
```json
{
  "code": "200",
  "message": "Success",
  "data": [
    {
      "dongCode": "1168010100",
      "sidoName": "서울특별시",
      "gugunName": "강남구",
      "dongName": "역삼동",
      "latitude": 37.5012767,
      "longitude": 127.0396225
    }
  ]
}
```

## 🔧 트러블슈팅

### Kakao Map이 표시되지 않는 경우
1. JavaScript 키가 올바르게 설정되었는지 확인
2. 브라우저 콘솔에서 에러 메시지 확인
3. Kakao Developers에서 도메인이 제대로 등록되었는지 확인

### 데이터가 표시되지 않는 경우
1. 데이터베이스에 `dongcodes` 테이블 데이터가 있는지 확인
2. `latitude`와 `longitude` 값이 NULL이 아닌지 확인
3. 브라우저 콘솔에서 API 응답 확인

### 빌드 오류
```bash
# 클린 빌드
./gradlew clean build -x test

# 의존성 다시 다운로드
./gradlew clean build --refresh-dependencies -x test
```

## 📝 참고 사항

- Kakao JavaScript API는 무료로 사용 가능하지만, 일일 호출 제한이 있습니다
- 프로덕션 환경에서는 환경 변수로 API 키를 관리하는 것을 권장합니다
- 지도 검색 결과는 최대 100개로 제한됩니다 (AreaMapper 설정)

## 🔗 관련 링크

- [Kakao Developers](https://developers.kakao.com/)
- [Kakao Map JavaScript API 문서](https://apis.map.kakao.com/web/)
- [Swagger UI](http://localhost:8080/swagger-ui.html)

