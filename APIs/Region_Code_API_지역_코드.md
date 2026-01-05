# Region Code API (지역 코드)

## 법정동 상세 정보 조회

**Description**: 동코드를 이용하여 해당 법정동의 상세 정보(좌표, 주소 등)를 조회합니다

### Request
- **Method**: `GET`
- **URL**: `/api/v1/dongcode/{code}`

#### Parameters
| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| code | path | string | True | 동코드 (예: 1168010100) |

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

## 시도 목록 조회

**Description**: 전국 모든 시도 목록을 조회합니다

### Request
- **Method**: `GET`
- **URL**: `/api/v1/dongcode/sido`

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

## 구군 목록 조회

**Description**: 선택한 시도에 해당하는 구군 목록을 조회합니다

### Request
- **Method**: `GET`
- **URL**: `/api/v1/dongcode/gugun`

#### Parameters
| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| sido | query | string | True | 시도명 (예: 서울특별시) |

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

## 동 목록 조회

**Description**: 선택한 시도와 구군에 해당하는 동 목록을 조회합니다

### Request
- **Method**: `GET`
- **URL**: `/api/v1/dongcode/dong`

#### Parameters
| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| sido | query | string | True | 시도명 (예: 서울특별시) |
| gugun | query | string | True | 구군명 (예: 강남구) |

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

