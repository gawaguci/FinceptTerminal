# 전략 명세서 (MACD + RSI 매수타이밍)

본 문서는 `signal-engine-java`의 scanner/signal 패키지 구현과 1:1로 대응한다.

## 1. 기본 파라미터
| 지표 | 기본값 |
|---|---:|
| MACD Fast EMA | 12 |
| MACD Slow EMA | 26 |
| MACD Signal EMA | 9 |
| RSI Period | 14 |
| 기준봉 | 1분봉(진입) + 5분봉(추세) |
| VWAP | 당일 누적 |

## 2. 후보 종목 점수화 (RealtimeScanner)
| 항목(CandidateReason) | 조건 | 점수 |
|---|---|---:|
| TRADE_AMOUNT | 당일 거래대금 >= 30억 | 20 |
| VOLUME_SPIKE | 1분 거래량 > 20분 평균 × 1.5 | 20 |
| PRICE_ABOVE_VWAP | 현재가 > VWAP | 15 |
| TREND | 5분봉 종가 > 20봉 이동평균 | 15 |
| BREAKOUT | 직전 고점/당일 고가 돌파 | 15 |
| MACD | MACD선 > Signal선 + Histogram 증가 | 10 |
| RSI | RSI 50 이상 | 5 |

등급(CandidateGrade): 70+ INTEREST, 80+ WATCH, 90+ STRONG

## 3. 추세형 매수 조건 (BuyTimingEngine, 1~6 모두 충족 필요)
1. 현재가 > VWAP
2. 5분봉 종가 > 20봉 이동평균
3. MACD선 > Signal선
4. MACD Histogram 2봉 이상 증가
5. RSI 50 이상
6. 1분 거래량 > 20분 평균 × 1.5
- 추가로 후보 점수 >= WATCH(80) 이상이어야 BUY_CANDIDATE 발생.

## 4. 매수 제외 조건 (RejectReason)
지표/가격 기반(BuyTimingEngine에서 처리):
- RSI >= 75 → RSI_OVERHEATED
- VWAP 대비 이격 >= 3% → VWAP_GAP_TOO_LARGE
- 호가 스프레드 > 3틱 → SPREAD_TOO_WIDE
- API 불안정 → API_UNSTABLE

상태 기반(RiskManager에서 처리):
- 동일 종목 쿨다운 미경과 → COOLDOWN_NOT_ELAPSED
- 1일 최대 손실 도달 → DAILY_LOSS_LIMIT
- 신규매수 제한 시각(15:10) 이후 → AFTER_CUTOFF_TIME
- 동일 종목 보유/최대 보유 초과 → ALREADY_HOLDING / MAX_HOLDING_REACHED

## 5. 상태 머신 (SignalStateMachine)
WAIT → WATCH → READY → BUY_SIGNAL → ORDER_WAIT → (ORDER_BLOCKED | ORDER_SENT)
→ POSITION_OPEN → EXIT_READY → POSITION_CLOSED → WAIT

## 6. 지표 계산 주의
- 진행 중 봉(completed=false)은 지표 계산에 사용하지 않는다.
- RSI는 Wilder 평활, MACD EMA는 초기 SMA 시드 방식.
- 실매매 전 HTS 차트값과 오차 허용범위 내 일치 검증 필수(계획서 21장).
