# Kiwoom Stock Signal

키움 OpenAPI+ 기반 **실시간 종목발굴 + 매수타이밍 알림** 프로그램.

초기 버전(MVP)은 자동매수가 아니라 **실시간 후보 발굴 + 매수신호 알림 + 수동승인**을 목표로 한다.
주문 기능 기본값은 항상 비활성(`orderEnabled=false`)이다.

> 본 소프트웨어는 투자 자문이 아니다. 실매매 적용 전 모의투자, 리플레이 테스트, 소액 검증, 법적 검토가 필요하다.

## 아키텍처
```
키움 OpenAPI+ OCX → Delphi Gateway → (JSON) → Java Signal Engine → DB
                                                  ├ Risk Manager → Order Gateway
                                                  └ Alert / Monitor UI
```

## 모듈
| 디렉터리 | 설명 | 빌드 환경 |
|---|---|---|
| `signal-engine-java/` | 지표/스캐너/매수타이밍/리스크 엔진 | Java 17+, Maven (이 저장소에서 빌드·테스트 가능) |
| `gateway-delphi/` | 키움 OCX 연동 Gateway | Windows + Delphi XE7 + 키움 OpenAPI+ |
| `sql/` | DB 스키마/인덱스/시드 | PostgreSQL(우선) / SQLite(MVP) |
| `docs/` | 개발계획/전략/리스크/운영/제약 문서 | - |
| `replay-engine/` | 과거 데이터 리플레이 검증 | Java |
| `tools/` | Codex 프롬프트, 로그 파서 등 | - |

## 빠른 시작 (Java 엔진)
```bash
cd signal-engine-java
mvn test                                   # 전체 단위 테스트 (44개)
mvn -q package                             # 빌드
java -cp target/classes kr.co.mango.stock.app.SignalEngineApplication   # 데모 파이프라인
```

## 구현 현황
- [x] Java Signal Engine: 분봉 생성, MACD/RSI/VWAP/MA, 후보 점수화, 매수타이밍, 리스크, 상태머신 (+JUnit)
- [x] DB 스키마 / 시드 (PostgreSQL)
- [x] 문서: 개발계획, API 제약, 전략/리스크 명세, 운영 매뉴얼
- [x] Delphi Gateway 순수 로직: TrRateLimiter, ScreenNoManager
- [ ] Delphi Gateway OCX 연동(KiwoomApiUnit 등) — Windows 환경 필요
- [ ] Replay Engine 구현
- [ ] 신호 로그 JDBC 저장소

## 문서
- 개발계획서: `docs/development-plan.md`
- API 제약: `docs/api-constraints.md`
- 전략 명세: `docs/strategy-spec.md`
- 리스크 정책: `docs/risk-policy.md`
- 운영 매뉴얼: `docs/operation-manual.md`
- 에이전트 가이드: `AGENTS.md`

## 보안
계좌번호, 인증서, 비밀번호, API 계정 정보는 저장소에 **절대 커밋하지 않는다**.
`.gitignore`로 `.env*`, 인증서, 로그, 실거래 데이터, DB 파일을 차단한다.
