# Kiwoom Gateway (Delphi XE7)

키움 OpenAPI+ OCX와 직접 연결되는 유일한 모듈이다(계획서 8장).

> 주의: 이 모듈은 **Windows + Delphi XE7 + 키움 OpenAPI+ 설치 환경**에서만 빌드/실행된다.
> Linux/CI 환경에서는 컴파일되지 않으므로, 순수 로직 유닛(TrRateLimiter, ScreenNoManager)은
> 가능한 경우 DUnitX로 별도 검증한다.

## 책임
| 책임 | 메소드/이벤트 |
|---|---|
| 로그인 | CommConnect / OnEventConnect |
| TR 요청 | CommRqData / OnReceiveTrData |
| 실시간 시세 | SetRealReg / OnReceiveRealData / SetRealRemove |
| 조건검색 | GetConditionLoad / GetConditionNameList / SendCondition / OnReceiveRealCondition |
| 주문 | SendOrder |
| 체결/잔고 | OnReceiveChejanData |
| 이벤트 발행 | Java 엔진으로 JSON 송신 |

## 유닛 구성 (src/)
| 유닛 | 상태 | 설명 |
|---|---|---|
| TrRateLimiter.pas | 구현 완료 | TR 호출 제한 관리(순수 로직) |
| ScreenNoManager.pas | 구현 완료 | 화면번호 풀 관리(순수 로직) |
| KiwoomApiUnit.pas | 스텁 | OCX 래퍼(연결/TR/실시간/주문) |
| ConditionSearchManager.pas | 스텁 | 조건검색 로딩/실시간 편입·이탈 |
| RealTimeRegManager.pas | 스텁 | 실시간 등록(100종목 단위 분할) |
| OrderGatewayUnit.pas | 스텁 | 주문 상태 머신 |
| ChejanEventParser.pas | 스텁 | 체결/잔고 이벤트 파싱 |
| RealDataParser.pas | 스텁 | 실시간 FID 파싱 |
| JsonEventPublisher.pas | 스텁 | Java 엔진으로 JSON 발행 |
| AppConfigUnit.pas | 스텁 | 설정/환경 로딩(민감정보 제외) |
| MainForm.pas | 스텁 | 운영 대시보드 UI |

## 개발 원칙 (계획서 20.3)
- 키움 이벤트 핸들러에서는 무거운 계산을 하지 않는다(즉시 DTO 변환 후 큐 적재).
- UI 스레드와 데이터 처리 스레드를 분리한다.
- COM/OCX 호출 실패 시 오류코드와 호출 파라미터를 로그로 남긴다.
- 화면번호/조건명/FID 목록은 상수 또는 설정 파일로 관리한다.
- 주문 기본값은 항상 비활성(orderEnabled=false).
