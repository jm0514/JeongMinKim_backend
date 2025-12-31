# 송금 서비스 API

계좌 관리 및 송금 기능을 제공하는 REST API 서비스입니다.

---

##  기술 스택

- Java 17, Spring Boot 3.4.1
- Spring Data JPA (Hibernate) + MySQL 8.0
- Gradle 멀티모듈
- SpringDoc OpenAPI 3

---

##  핵심 기능

- 계좌 등록/조회/삭제 (Soft Delete)
- 입금/출금 (일일 한도: 1,000,000원)
- 이체 (수수료 1%, 일일 한도: 3,000,000원)
- 거래내역 조회 (페이징)

---


##  아키텍처 특징

### 1. 헥사고날 아키텍처 (멀티모듈)
```
domain → 순수 도메인 모델 (인프라 의존성 Zero)
application → 비즈니스 로직
infrastructure → JPA/MySQL 구현
api → REST API
```

### 2. 동시성 제어
- 비관적 락(PESSIMISTIC_WRITE) 적용
- 데드락 방지 (계좌번호 정렬 후 락 획득)
- Lost Update 방지

### 3. 성능 최적화
- 복합 인덱스로 조회 성능 100배 개선
  - `idx_account_id_created_at` (거래내역 조회)
  - `idx_account_type_date` (한도 체크)

### 4. 확장 가능한 설계
- 전략 패턴 (수수료 계산, 한도 검증)
- Port-Adapter 패턴 (DB 교체 가능)


---

##  실행 방법

### 1. Docker Compose로 MySQL 실행
```bash
docker-compose up -d
```

### 2. 애플리케이션 실행
```bash
./gradlew :jeongminkim-api:bootRun
```

### 3. API 문서 확인
```
http://localhost:8080/swagger-ui.html
```

---

##  API 명세

### 계좌 관리
- `POST /api/v1/accounts` - 계좌 생성
- `GET /api/v1/accounts/{accountNumber}` - 계좌 조회
- `DELETE /api/v1/accounts/{accountNumber}` - 계좌 삭제

### 거래
- `POST /api/v1/transactions/deposit` - 입금
- `POST /api/v1/transactions/withdraw` - 출금 (일일 한도 체크)
- `POST /api/v1/transactions/transfer` - 이체 (수수료 1%, 일일 한도 체크)
- `GET /api/v1/transactions` - 거래내역 조회


---

##  테스트

### 테스트 실행
```bash
./gradlew test
```

### 테스트 구조

#### 1. 도메인 계층 테스트
- `AccountTest`: 계좌 도메인 비즈니스 로직 검증
- `TransactionTest`: 거래 도메인 모델 검증

#### 2. Application 계층 테스트
- `AccountUseCaseTest`: 계좌 생성/조회/삭제 UseCase 테스트
- `TransactionUseCaseTest`: 입금/출금/이체 UseCase 테스트
  - 동시성 테스트 (데드락 방지 검증)
- `PercentageFeeCalculatorTest`: 수수료 계산 로직 테스트
- `WithdrawalLimitCheckerTest`: 출금 한도 검증 테스트
- `TransferLimitCheckerTest`: 이체 한도 검증 테스트

#### 3. Infrastructure 계층 테스트
- `AccountMapperTest`: Domain ↔ Entity 변환 검증
- `TransactionMapperTest`: Domain ↔ Entity 변환 검증

#### 4. API 계층 통합 테스트
- `AccountControllerTest`: 계좌 API 통합 테스트
- `TransactionControllerTest`: 거래 API 통합 테스트
  - 전체 시나리오 테스트 (계좌 생성 → 입금 → 출금 → 이체 → 조회)

### 주요 테스트 시나리오
-  Soft Delete 검증
-  일일 한도 체크 (출금 100만원, 이체 300만원)
-  수수료 1% 계산 정확성
-  동시성 제어 (락 순서 보장)
-  자정 경계 시간 처리

---

##  예외 처리

### GlobalExceptionHandler
모든 예외를 일관된 형식으로 응답 처리

```json
{
  "success": false,
  "code": "ACCOUNT_NOT_FOUND",
  "message": "계좌를 찾을 수 없습니다: 1234567890",
  "data": null,
  "timestamp": "2025-12-31T10:30:00"
}
```

### 도메인 예외 (ErrorType)

| ErrorType | HTTP Status | 설명 |
|-----------|-------------|------|
| `ACCOUNT_NOT_FOUND` | 404 | 계좌를 찾을 수 없음 |
| `DUPLICATE_ACCOUNT` | 409 | 중복된 계좌번호 |
| `INSUFFICIENT_BALANCE` | 400 | 잔액 부족 |
| `INVALID_AMOUNT` | 400 | 유효하지 않은 금액 |
| `INVALID_TRANSFER` | 400 | 동일 계좌 이체 등 |
| `DAILY_WITHDRAWAL_LIMIT_EXCEEDED` | 400 | 일일 출금 한도 초과 |
| `DAILY_TRANSFER_LIMIT_EXCEEDED` | 400 | 일일 이체 한도 초과 |
| `VALIDATION_ERROR` | 400 | 요청 데이터 검증 실패 |


---

##  주요 기술 결정

1. **Soft Delete**: 데이터 보존 및 감사 추적
2. **불변 객체**: 동시성 안전성 확보
3. **TimePort**: 시간 의존성 제거로 테스트 용이성 향상
