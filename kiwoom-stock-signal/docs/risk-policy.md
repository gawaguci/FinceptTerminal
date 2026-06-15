# 리스크 정책서

본 문서는 `signal-engine-java`의 risk 패키지(RiskManager/RiskPolicy/PositionLimit) 구현과 대응한다.

## 1. 기본 리스크 정책 (RiskPolicy.defaults)
| 항목 | 기본값 |
|---|---:|
| 주문 기능(orderEnabled) | OFF |
| 종목당 최대 투입(maxPositionRate) | 총자산 5% |
| 1일 최대 손실(maxDailyLossRate) | 총자산 1% |
| 1회 최대 손실(maxLossPerTradeRate) | 총자산 0.3% |
| 최대 보유 종목 수 | 5 |
| 동일 종목 쿨다운 | 20분 |
| 신규매수 제한 시각 | 15:10 (Asia/Seoul) |

## 2. 주문 전 검증 순서 (RiskManager.evaluate)
1. 후보 신호 여부(BUY_CANDIDATE)
2. 주문 기능 활성화 여부 → ORDER_DISABLED
3. API 연결/실시간 수신 정상 → API_UNSTABLE
4. 계좌 잔고 > 0 → INSUFFICIENT_ACCOUNT
5. 신규매수 제한 시각 이전 → AFTER_CUTOFF_TIME
6. 1일 손실 한도 미도달 → DAILY_LOSS_LIMIT
7. 동일 종목 미보유 → ALREADY_HOLDING
8. 최대 보유 수 미초과 → MAX_HOLDING_REACHED
9. 쿨다운 경과 → COOLDOWN_NOT_ELAPSED
10. 손절가 유효(0 < stopLoss < currentPrice) → INVALID_STOP_LOSS
11. 주문 수량 산정(> 0) → INSUFFICIENT_QUANTITY
12. 주문 요청 생성(지정가)

## 3. 주문 수량 계산 (12.3)
```
amountLimit      = accountValue × maxPositionRate
lossLimit        = accountValue × maxLossPerTradeRate
riskPerShare     = currentPrice - stopLossPrice
quantityByAmount = floor(amountLimit / currentPrice)
quantityByRisk   = floor(lossLimit / riskPerShare)
orderQuantity    = min(quantityByAmount, quantityByRisk)   // 0 이하이면 거절
```

## 4. 장애 대응 연계
- API 연결 끊김/실시간 지연/체결 미확인 시 신규주문 차단.
- DB 장애 시 파일 로그 백업 후 신규주문 중단.
- 모든 차단은 risk_event_log에 사유와 함께 기록한다.

## 5. 자동주문 활성화 기준 (계획서 21장)
지표 검증, 20거래일 이상 리플레이, 2주 이상 Dry-run 무오류, 모든 차단 케이스 테스트 통과,
장애 대응 확인, 운영자의 명시적 `orderEnabled=true` 변경을 모두 만족해야 한다.
