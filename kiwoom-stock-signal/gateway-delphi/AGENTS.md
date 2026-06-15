# AGENTS.md (gateway-delphi)

## 모듈 목적
키움 OpenAPI+ OCX와 연결되는 유일한 Gateway. 로그인, TR, 실시간 시세, 조건검색,
주문/체결을 처리하고 Java 엔진으로 JSON 이벤트를 발행한다.

## 환경
- Windows + Delphi XE7 + 키움 OpenAPI+ 설치 필요
- Linux/CI에서는 컴파일 불가. 순수 로직 유닛만 DUnitX로 테스트 권장.

## 규칙 (계획서 20.3)
- 키움 OCX 직접 호출은 이 모듈 내부로 한정한다.
- 이벤트 핸들러는 무거운 계산 금지. DTO 변환 후 큐 적재.
- COM/OCX 실패 시 오류코드 + 호출 파라미터를 로그로 남긴다.
- 화면번호/조건명/FID는 상수 또는 설정으로 관리.
- 주문 기본값은 항상 비활성(orderEnabled=false).
- 계좌/인증/비밀번호/인증서 경로는 코드/테스트에 포함 금지.

## 우선 구현(테스트 가능 순수 로직)
1. TrRateLimiter.pas (완료)
2. ScreenNoManager.pas (완료)
이후 KiwoomApiUnit → ConditionSearchManager → RealTimeRegManager → OrderGatewayUnit 순.
