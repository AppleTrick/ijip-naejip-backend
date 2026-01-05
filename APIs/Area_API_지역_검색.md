# Area API (지역 검색)

## 사각형 범위 기반 지역 목록 조회

**Description**: 사각형 좌표 범위에 따른 지역 목록을 조회합니다.
- DONG/GUGUN/SIDO 범위: minPrice/maxPrice는 평당가(만원) 기준
- APT/APT_DONG 범위: minPrice/maxPrice는 총 거래가(만원) 기준

### Request
- **Method**: `GET`
- **URL**: `/api/v1/area`

#### Parameters
| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| minLat | query | number | True |  |
| maxLat | query | number | True |  |
| minLng | query | number | True |  |
| maxLng | query | number | True |  |
| scope | query | string | True |  |
| minPrice | query | integer | False |  |
| maxPrice | query | integer | False |  |
| minPyung | query | integer | False |  |
| maxPyung | query | integer | False |  |

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

