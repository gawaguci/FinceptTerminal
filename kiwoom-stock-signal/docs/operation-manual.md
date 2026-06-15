# 운영 매뉴얼

## 1. 구성 요소
- Delphi Gateway (Windows): 키움 OCX 연동, 실시간 이벤트 수신, 운영 UI
- Java Signal Engine: 지표/스캐너/매수타이밍/리스크/로그
- DB: PostgreSQL(우선) 또는 SQLite(단일 PC MVP)

## 2. 사전 준비
1. 키움 OpenAPI+ 설치 및 OpenAPI 사용 등록
2. KOA Studio에서 사용할 TR/FID/실시간 항목 확인
3. HTS 조건검색식 저장(MANGO_VOL_SPIKE 등)
4. 민감정보(.env.local 또는 Windows 환경변수) 설정 — 저장소에 커밋 금지
5. DB 스키마 적용: `sql/001_schema.sql` → `002_indexes.sql` → `003_seed_strategy_config.sql`

## 3. 실행 순서
1. DB 기동 확인
2. Java Signal Engine 기동
3. Delphi Gateway 기동 → CommConnect 로그인
4. 조건검색식 로딩 및 실시간 등록
5. 대시보드에서 연결 상태(키움/실시간/Java/DB) 정상 확인

## 4. 종료 순서
1. 실시간 해제(SetRealRemove) 및 조건검색 중지
2. Gateway 종료
3. Java Signal Engine 종료
4. DB 정리

## 5. 장애 대응 (계획서 18장)
| 장애 | 감지 | 대응 |
|---|---|---|
| 키움 연결 끊김 | OnEventConnect/수신 정지 | 신규주문 중단, 재로그인 |
| 실시간 지연 | 마지막 Tick 수신 시각 | 신호 중단, UI 경고 |
| TR 제한 접근 | RateLimiter 카운터 | 요청 보류, 알림 |
| 화면번호 고갈 | ScreenNoManager 실패 | 신규 등록 중단 |
| DB 장애 | insert/update 실패 | 파일 로그 백업, 신규주문 중단 |
| Java 엔진 중단 | Health check 실패 | Gateway 단독 주문 금지 |
| 체결 미확인 | 주문 후 Chejan 미수신 | 중복주문 차단, 수동 확인 |

## 6. 일상 점검 체크리스트 (실매매 전, 계획서 16.4)
1. HTS 차트와 MACD/RSI/VWAP 값 비교
2. 진행 중 봉 종가를 확정봉으로 쓰지 않는지 확인
3. TR 제한 초과 시 보류 동작 확인
4. 실시간 등록 종목 수 제한 확인
5. 주문 비활성 기본값 유지 확인
6. 연결 끊김 시 신규주문 차단 확인
7. 체결 미확인 상태 중복주문 방지 확인
