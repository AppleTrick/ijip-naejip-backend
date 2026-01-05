# Apartment API (아파트 정보)

## 아파트별 상세 정보 조회

**Description**: 특정 아파트의 상세 정보를 조회합니다

### Request
- **Method**: `GET`
- **URL**: `/api/v1/apartments/{aptSeq}`

#### Parameters
| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| aptSeq | path | string | True | 아파트 시퀀스 (예: 11680-1) |
| pyung | query | string | False | 평형 (예: 22) |

### Response
#### Status: 200 OK
Schema:
```json
{
    "code": <string>
    "message": <string>
    "data": <object> // 아파트 상세 정보 데이터
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

