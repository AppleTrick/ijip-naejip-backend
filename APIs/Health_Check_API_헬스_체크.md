# Health Check API (헬스 체크)

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

