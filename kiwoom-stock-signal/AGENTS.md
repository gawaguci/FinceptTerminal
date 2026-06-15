# AGENTS.md

## 프로젝트 목적
키움 OpenAPI+ 기반 실시간 종목발굴 및 매수타이밍 알림 프로그램을 개발한다.
초기 버전은 자동매수가 아니라 알림과 수동승인을 목표로 한다.

## 기술 스택
- Delphi XE7: 키움 OpenAPI+ OCX 연동, 실시간 이벤트 수신, 운영 UI (Windows 전용)
- Java 17 이상: 지표 계산, 후보 종목 스캐너, 매수타이밍 엔진, 리스크 관리, 로그 저장
- DB: PostgreSQL 우선, 단일 PC MVP는 SQLite 허용

## 저장소 구조
- `signal-engine-java/` : 지표/스캐너/매수타이밍/리스크 엔진 (이 환경에서 빌드·테스트 가능)
- `gateway-delphi/`     : 키움 OCX 연동 Gateway (Windows + Delphi 필요)
- `sql/`                : DB 스키마 및 시드
- `docs/`              : 개발계획서, API 제약, 전략/리스크 명세, 운영 매뉴얼
- `replay-engine/`     : 과거 데이터 리플레이 검증
- `tools/`             : Codex 프롬프트, 로그 파서, 데이터 익스포트

## 코딩 규칙
- 주석은 한국어로 작성한다.
- 기존 로직을 삭제하지 말고 개선 시 주석으로 변경 사유를 남긴다.
- 주문 관련 기본값은 항상 비활성화(`orderEnabled=false`)한다.
- 계좌, 인증, 비밀번호, 인증서 경로는 코드와 테스트 데이터에 포함하지 않는다.
- 모든 Java 비즈니스 로직은 JUnit 테스트를 작성한다.
- 가격은 정수 원 단위로 처리하고, 수익률과 지표는 double 기준으로 처리하되 허용 오차를 테스트에 명시한다.
- 키움 API 직접 호출은 `gateway-delphi` 모듈 밖에서 금지한다.

## 테스트 명령
- Java 테스트: `cd signal-engine-java && mvn test`
- Java 패키징: `cd signal-engine-java && mvn package`
- 데모 실행: `cd signal-engine-java && mvn -q exec:java` 또는
  `java -cp target/classes kr.co.mango.stock.app.SignalEngineApplication`
- SQL 검증: 로컬 DB에 `sql/001_schema.sql` 적용 후 기본 insert/select 확인

## 금지 사항
- 실계좌 주문을 실행하는 테스트 작성 금지
- 인증정보 하드코딩 금지
- API 제한을 무시한 반복 TR 호출 금지
- 아직 완성되지 않은 봉(completed=false)의 종가를 확정봉처럼 사용하는 로직 금지
