# Area API (지역 검색)

> **Module Summary**
>
> 지도 화면 기반으로 영역 내의 아파트 및 지역 정보를 검색하는 API 모듈입니다.
> - **주요 기능**:
>     - **지도 범위 검색**: 현재 보고 있는 지도 화면(좌표 범위) 내의 아파트 리스트를 조회합니다.
>     - **필터링**: 가격, 평형 등 조건에 맞는 매물을 필터링하여 검색합니다.


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

