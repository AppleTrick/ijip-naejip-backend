# User API

## 회원정보 수정

**Description**: 로그인한 사용자의 정보를 수정합니다.

### Request
- **Method**: `PUT`
- **URL**: `/user/update`

#### Request Body
Schema Overview:
```json
{
    "id": <integer>
    "email": <string>
    "password": <string>
    "name": <string>
    "phone": <string>
    "profileImage": <string>
    "role": <string>
    "socialType": <string>
    "socialId": <string>
    "gender": <string>
    "ageGroup": <string>
    "job": <string>
    "maritalStatus": <string>
    "appPush": <boolean>
    "emailNotification": <boolean>
    "marketingNotification": <boolean>
    "myHouseName": <string>
    "myHouseAddress": <string>
    "myHouseArea": <string>
    "myHouseFloor": <string>
    "myHousePrice": <string>
    "createdAt": <string>
    "updatedAt": <string>
    "deletedAt": <string>
    "emailVerified": <boolean>
}
```
Example:
```json
{
  "id": 0,
  "email": "string",
  "password": "string",
  "name": "string",
  "phone": "string",
  "profileImage": "string",
  "role": "string",
  "socialType": "string",
  "socialId": "string",
  "gender": "string",
  "ageGroup": "string",
  "job": "string",
  "maritalStatus": "string",
  "appPush": true,
  "emailNotification": true,
  "marketingNotification": true,
  "myHouseName": "string",
  "myHouseAddress": "string",
  "myHouseArea": "string",
  "myHouseFloor": "string",
  "myHousePrice": "string",
  "createdAt": "string",
  "updatedAt": "string",
  "deletedAt": "string",
  "emailVerified": true
}
```
### Response
#### Status: 200 OK
Schema:
```json
N/A
```
Example:
```json
{}
```

---

## 회원가입

**Description**: 새로운 사용자를 등록합니다.

### Request
- **Method**: `POST`
- **URL**: `/user/signup`

#### Request Body
Schema Overview:
```json
{
    "id": <integer>
    "email": <string>
    "password": <string>
    "name": <string>
    "phone": <string>
    "profileImage": <string>
    "role": <string>
    "socialType": <string>
    "socialId": <string>
    "gender": <string>
    "ageGroup": <string>
    "job": <string>
    "maritalStatus": <string>
    "appPush": <boolean>
    "emailNotification": <boolean>
    "marketingNotification": <boolean>
    "myHouseName": <string>
    "myHouseAddress": <string>
    "myHouseArea": <string>
    "myHouseFloor": <string>
    "myHousePrice": <string>
    "createdAt": <string>
    "updatedAt": <string>
    "deletedAt": <string>
    "emailVerified": <boolean>
}
```
Example:
```json
{
  "id": 0,
  "email": "string",
  "password": "string",
  "name": "string",
  "phone": "string",
  "profileImage": "string",
  "role": "string",
  "socialType": "string",
  "socialId": "string",
  "gender": "string",
  "ageGroup": "string",
  "job": "string",
  "maritalStatus": "string",
  "appPush": true,
  "emailNotification": true,
  "marketingNotification": true,
  "myHouseName": "string",
  "myHouseAddress": "string",
  "myHouseArea": "string",
  "myHouseFloor": "string",
  "myHousePrice": "string",
  "createdAt": "string",
  "updatedAt": "string",
  "deletedAt": "string",
  "emailVerified": true
}
```
### Response
#### Status: 200 OK
Schema:
```json
N/A
```
Example:
```json
{}
```

---

## 비밀번호 초기화 요청

**Description**: 이메일로 임시 비밀번호를 전송합니다.

### Request
- **Method**: `POST`
- **URL**: `/user/reset-password`

#### Request Body
Schema Overview:
```json
{
}
```
Example:
```json
{}
```
### Response
#### Status: 200 OK
Schema:
```json
N/A
```
Example:
```json
{}
```

---

## 토큰 재발급

**Description**: Refresh Token을 이용하여 새로운 Access Token을 발급받습니다.

### Request
- **Method**: `POST`
- **URL**: `/user/reissue`

#### Parameters
| Name | In | Type | Required | Description |
| --- | --- | --- | --- | --- |
| refreshToken | cookie | string | False | 리프레시 토큰 (쿠키) |

### Response
#### Status: 200 OK
Schema:
```json
N/A
```
Example:
```json
{}
```

---

## 로그아웃

**Description**: 사용자를 로그아웃 처리합니다. 클라이언트 측에서 Access Token을 삭제해야 합니다.

### Request
- **Method**: `POST`
- **URL**: `/user/logout`

### Response
#### Status: 200 OK
Schema:
```json
N/A
```
Example:
```json
{}
```

---

## 로그인

**Description**: 이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.

### Request
- **Method**: `POST`
- **URL**: `/user/login`

#### Request Body
Schema Overview:
```json
{
}
```
Example:
```json
{}
```
### Response
#### Status: 200 OK
Schema:
```json
N/A
```
Example:
```json
{}
```

---

## 이메일 인증 코드 요청

**Description**: 회원가입을 위한 이메일 인증 코드를 전송합니다.

### Request
- **Method**: `POST`
- **URL**: `/user/email-verification/request`

#### Request Body
Schema Overview:
```json
{
}
```
Example:
```json
{}
```
### Response
#### Status: 200 OK
Schema:
```json
N/A
```
Example:
```json
{}
```

---

## 이메일 인증 코드 확인

**Description**: 전송된 이메일 인증 코드를 검증합니다.

### Request
- **Method**: `POST`
- **URL**: `/user/email-verification/confirm`

#### Request Body
Schema Overview:
```json
{
}
```
Example:
```json
{}
```
### Response
#### Status: 200 OK
Schema:
```json
N/A
```
Example:
```json
{}
```

---

## 비밀번호 변경

**Description**: 로그인한 사용자의 비밀번호를 변경합니다.

### Request
- **Method**: `POST`
- **URL**: `/user/change-password`

#### Request Body
Schema Overview:
```json
{
}
```
Example:
```json
{}
```
### Response
#### Status: 200 OK
Schema:
```json
N/A
```
Example:
```json
{}
```

---

## 회원정보 조회

**Description**: 로그인한 사용자의 상세 정보를 조회합니다.

### Request
- **Method**: `GET`
- **URL**: `/user/info`

### Response
#### Status: 200 OK
Schema:
```json
N/A
```
Example:
```json
{}
```

---

