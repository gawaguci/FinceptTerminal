# API 제약 명세 (키움 OpenAPI+)

> 출처: 키움 OpenAPI+ 개발가이드 v1.5. 미확인 항목은 KOA Studio에서 확인 후 갱신한다.
> 확인 전에는 임의로 FID/필드명을 생성하지 않는다.

## 1. TR 호출 제한
| 구분 | 제한 | 설계 반영 |
|---|---|---|
| 초당 | 5건 | `TrRateLimiter`로 통과 |
| 분당 | 100건 | `TrRateLimiter`로 통과 |
| 시간당 | 1,000건 | `TrRateLimiter`로 통과 |

- 모든 TR 요청은 RateLimiter를 통과시킨다.
- 제한 초과 시 요청을 보류(WAIT_UNTIL)하거나 거절(REJECT)하며, 프로그램이 멈추지 않게 한다.

## 2. 실시간 시세 제한
| 구분 | 제한 | 설계 반영 |
|---|---|---|
| 화면번호 | 최대 200개 | `ScreenNoManager`로 관리 |
| 화면당 종목 | 최대 100종목 | 화면 분할 |
| SetRealReg 종목/FID | 각 100개 제한 | 100종목 단위 분할 등록 |

## 3. 실시간 조건검색 제한
- 실시간 조건검색은 최대 10개 조건까지로 명시됨.
- 핵심 조건 1~3개만 운영한다.

## 4. 실시간 FID 1차 범위
| FID | 의미 | 사용 |
|---:|---|---|
| 9001 | 종목코드 | 필수 |
| 10 | 현재가 | 필수 |
| 13 | 누적거래량 | 필수 |

- 추가 FID(고가/저가/등락률/호가 등)는 KOA Studio 실시간 목록에서 확인 후 본 문서에 추가한다.
- 가격(FID 10)은 부호 포함 문자열로 수신될 수 있으므로 절대값/부호 파싱에 유의한다.

## 5. 주요 메소드/이벤트
| 분류 | 메소드/이벤트 |
|---|---|
| 로그인 | CommConnect / OnEventConnect |
| 계좌정보 | GetLoginInfo |
| TR | CommRqData / OnReceiveTrData |
| 실시간 | SetRealReg / OnReceiveRealData / SetRealRemove |
| 조건검색 | GetConditionLoad / OnReceiveConditionVer / GetConditionNameList / SendCondition / OnReceiveTrCondition / OnReceiveRealCondition |
| 주문 | SendOrder |
| 체결/잔고 | OnReceiveChejanData |

## 6. 미확정/확인 필요
- 추가 실시간 FID 목록
- TR별 입력/출력 필드명 (KOA Studio 확인)
- 호가 데이터 FID (현재가 중심 MVP 이후 검토)
