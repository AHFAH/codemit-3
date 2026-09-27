# Todo API

Spring Boot 3.5.16, Java 17, Spring Data JPA, H2로 만든 단일 애플리케이션 REST API입니다. 회원 가입과 로그인은 제공하지 않습니다.

## 실행 방법

- JDK 17 이상
- 별도 DB 설치 불필요 (H2 인메모리 데이터베이스 사용)

프로젝트 디렉터리에서 다음 명령 하나로 실행합니다.

```bash
./gradlew bootRun
```

Windows PowerShell에서는 다음을 실행합니다.

```powershell
.\gradlew.bat bootRun
```

서버는 `http://localhost:8080`에서 실행됩니다. H2 콘솔은 `http://localhost:8080/h2-console`이며 JDBC URL은 `jdbc:h2:mem:tododb`, 사용자명은 `sa`, 비밀번호는 비어 있습니다. 인메모리 DB라 서버를 재시작하면 데이터가 초기화됩니다.

## API 명세

모든 요청과 응답 본문은 JSON입니다. 시간은 UTC 기준 ISO-8601 형식입니다. 제목은 앞뒤 공백을 제거해 저장하며, 공백 제거 후 1~100자만 허용합니다.

| 메서드 | 주소 | 요청 본문 | 성공 응답 |
|---|---|---|---|
| `POST` | `/api/todos` | `{"title":"장보기"}` | `201 Created`, 생성된 할 일 객체와 `Location: /api/todos/{id}` |
| `GET` | `/api/todos` | 없음 | `200 OK`, 할 일 객체 배열 (없으면 `[]`) |
| `GET` | `/api/todos/{id}` | 없음 | `200 OK`, 할 일 객체 |
| `PUT` | `/api/todos/{id}` | `{"title":"마트에서 장보기"}` | `200 OK`, 제목이 바뀐 할 일 객체. 완료 상태와 생성 시각은 유지 |
| `PATCH` | `/api/todos/{id}/completion` | `{"completed":true}` | `200 OK`, 완료 상태가 바뀐 할 일 객체 |
| `DELETE` | `/api/todos/{id}` | 없음 | `204 No Content` |

할 일 객체의 모양:

```json
{
  "id": 1,
  "title": "장보기",
  "completed": false,
  "createdAt": "2026-09-27T01:00:00Z"
}
```

### 오류 응답

오류 응답은 상태 코드와 관계없이 다음 필드를 사용합니다. `details`는 입력 필드 검증 오류를 담고, 그 외 오류에서는 빈 배열입니다.

```json
{
  "timestamp": "2026-09-27T01:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "요청 값이 올바르지 않습니다.",
  "path": "/api/todos",
  "details": [
    { "field": "title", "message": "제목은 비어 있거나 공백일 수 없습니다." }
  ]
}
```

주요 오류 상태:

- `400 Bad Request`: 제목이 비어 있거나 공백뿐인 경우, 100자를 초과한 경우, 본문 JSON이나 경로 ID 형식이 잘못된 경우
- `404 Not Found`: 존재하지 않는 할 일 ID 또는 경로
- `405 Method Not Allowed`: 지원하지 않는 HTTP 메서드
- `500 Internal Server Error`: 그 밖의 서버 내부 오류

## 설계 설명

- `/api/todos`를 컬렉션으로, `/api/todos/{id}`를 개별 자원으로 표현했습니다. 생성은 `POST`, 조회는 `GET`, 전체 제목 교체는 `PUT`, 삭제는 `DELETE`를 사용합니다.
- 완료 여부는 제목 수정과 독립적으로 바꿀 수 있도록 `/api/todos/{id}/completion`에 `PATCH`를 사용합니다. 완료 값을 요청 본문에서 명시하므로 동일 요청을 재시도해도 결과가 뒤집히지 않습니다.
- 생성 성공은 새 자원의 주소를 `Location` 헤더에 담아 `201`로 반환하고, 삭제 성공은 반환할 표현이 없으므로 `204`로 응답합니다. 입력 오류는 `400`, 없는 자원은 `404`로 구분합니다.
- H2 인메모리 DB는 별도 설치나 계정 설정 없이 바로 실행하고 API를 확인할 수 있어 이 과제의 로컬 개발용으로 선택했습니다. 데이터 영속성이 필요한 운영 환경에서는 PostgreSQL 등으로 바꾸는 것을 권장합니다.
- 패키지는 `api`(Controller/DTO/오류 응답), `service`, `repository`, `domain`으로 나눴습니다. API 입력과 출력은 DTO로 처리해 JPA 엔티티를 외부에 노출하지 않습니다.

## 실행 예시

아래 요청은 서버가 실행 중인 상태에서 순서대로 호출합니다. 생성 응답에서 받은 ID를 이후 요청에 사용합니다.

### 1. 만들기

```bash
curl -i -X POST http://localhost:8080/api/todos \
  -H 'Content-Type: application/json' \
  -d '{"title":"장보기"}'
```

```http
HTTP/1.1 201
Location: /api/todos/1
Content-Type: application/json

{"id":1,"title":"장보기","completed":false,"createdAt":"2026-09-27T01:00:00Z"}
```

### 2. 목록 보기

```bash
curl -i http://localhost:8080/api/todos
```

```http
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"title":"장보기","completed":false,"createdAt":"2026-09-27T01:00:00Z"}]
```

### 3. 완료 처리

```bash
curl -i -X PATCH http://localhost:8080/api/todos/1/completion \
  -H 'Content-Type: application/json' \
  -d '{"completed":true}'
```

```http
HTTP/1.1 200
Content-Type: application/json

{"id":1,"title":"장보기","completed":true,"createdAt":"2026-09-27T01:00:00Z"}
```

### 4. 삭제

```bash
curl -i -X DELETE http://localhost:8080/api/todos/1
```

```http
HTTP/1.1 204
```

### 400 입력 오류

```bash
curl -i -X POST http://localhost:8080/api/todos \
  -H 'Content-Type: application/json' \
  -d '{"title":"   "}'
```

```http
HTTP/1.1 400
Content-Type: application/json

{"timestamp":"2026-09-27T01:00:00Z","status":400,"error":"Bad Request","message":"요청 값이 올바르지 않습니다.","path":"/api/todos","details":[{"field":"title","message":"제목은 비어 있거나 공백일 수 없습니다."}]}
```

### 404 없는 할 일

```bash
curl -i http://localhost:8080/api/todos/9999
```

```http
HTTP/1.1 404
Content-Type: application/json

{"timestamp":"2026-09-27T01:00:00Z","status":404,"error":"Not Found","message":"할 일을 찾을 수 없습니다. id=9999","path":"/api/todos/9999","details":[]}
```
