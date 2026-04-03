# Health Check API (헬스 체크)

> **Module Summary**
>
> 서버의 정상 가동 상태를 확인하는 시스템 API입니다.
> - **주요 기능**:
>     - **서버 상태 점검**: 서버가 정상적으로 요청을 받을 수 있는 상태인지 확인합니다. (Status: UP)


## 헬스 체크

**Description**: 서버가 정상적으로 작동하고 있는지 확인합니다

### Request
- **Method**: `GET`
- **URL**: `/`

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

