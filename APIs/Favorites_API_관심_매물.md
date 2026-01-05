# Favorites API (관심 매물)

> **Module Summary**
>
> 사용자가 관심 있게 지켜보는 아파트 매물을 관리하는 API 모듈입니다.
> - **주요 기능**:
>     - **관심 아파트 등록** 및 삭제
>     - 내 **관심 목록 조회** (마이페이지 등 활용)
>     - 특정 아파트의 **관심 등록 여부 확인** (상세 페이지 활용)


## 관심 아파트 목록 조회

**Description**: 로그인한 사용자가 등록한 관심 아파트 목록을 조회합니다.

### Request
- **Method**: `GET`
- **URL**: `/api/v1/favorites`

### Response
#### Status: 200 OK
Schema:
```json
{
    "code": <string>
    "message": <string>
    "data": <array>
}
```
Example:
```json
{
  "code": "string",
  "message": "string",
  "data": null
}
```

---

## 관심 아파트 추가

**Description**: 새로운 관심 아파트를 등록합니다.

### Request
- **Method**: `POST`
- **URL**: `/api/v1/favorites`

#### Request Body
Schema Overview:
```json
{
    "aptSeq": <string> // 아파트 고유번호 (ex: 11680-102)
    "aptName": <string> // 아파트명 (ex: 래미안 강남)
    "address": <string> // 주소 (ex: 서울특별시 강남구 역삼동)
    "pyung": <integer> // 선택 평수 (ex: 34)
    "dealAmount": <string> // 거래가격 (ex: 15억 5000만원)
    "latitude": <number> // 위도 (ex: 37.5665)
    "longitude": <number> // 경도 (ex: 126.978)
}
```
Example:
```json
{
  "aptSeq": "11680-102",
  "aptName": "래미안 강남",
  "address": "서울특별시 강남구 역삼동",
  "pyung": 34,
  "dealAmount": "15억 5000만원",
  "latitude": 37.5665,
  "longitude": 126.978
}
```
### Response
#### Status: 200 OK
Schema:
```json
{
    "code": <string>
    "message": <string>
    "data": <object> // 관심 아파트 정보
}
```
Example:
```json
{
  "code": "string",
  "message": "string",
  "data": null
}
```

---

## 관심 등록 여부 확인

**Description**: 특정 아파트(평수 포함)가 관심 목록에 등록되어 있는지 확인합니다.

### Request
- **Method**: `GET`
- **URL**: `/api/v1/favorites/check`

#### Parameters
| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| aptSeq | query | string | True |  |
| pyung | query | integer | True |  |

### Response
#### Status: 200 OK
Schema:
```json
{
    "code": <string>
    "message": <string>
    "data": <boolean>
}
```
Example:
```json
{
  "code": "string",
  "message": "string",
  "data": true
}
```

---

## 관심 아파트 삭제 (ID)

**Description**: 관심 아파트 ID(PK)를 이용하여 관심 목록에서 삭제합니다.

### Request
- **Method**: `DELETE`
- **URL**: `/api/v1/favorites/{id}`

#### Parameters
| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| id | path | integer | True |  |

### Response
#### Status: 200 OK
Schema:
```json
{
    "code": <string>
    "message": <string>
    "data": <object>
}
```
Example:
```json
{
  "code": "string",
  "message": "string",
  "data": null
}
```

---

## 관심 아파트 삭제 (aptSeq)

**Description**: 아파트 고유번호(aptSeq)를 이용하여 관심 목록에서 삭제합니다.

### Request
- **Method**: `DELETE`
- **URL**: `/api/v1/favorites/apt/{aptSeq}`

#### Parameters
| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| aptSeq | path | string | True |  |

### Response
#### Status: 200 OK
Schema:
```json
{
    "code": <string>
    "message": <string>
    "data": <object>
}
```
Example:
```json
{
  "code": "string",
  "message": "string",
  "data": null
}
```

---

