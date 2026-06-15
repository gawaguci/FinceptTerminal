# Replay Engine

과거 틱/분봉 데이터를 장중처럼 시간 순서로 재생하여 지표/신호 엔진을 검증한다(계획서 16.3).

## 흐름
```
과거 틱 또는 분봉 데이터 준비
  → 시간 순서대로 재생
  → MinuteBarBuilder / Indicator / SignalEngine 통과
  → 발생 신호와 당시 가격 저장
  → 슬리피지/수수료/세금 반영 성과 계산
```

## 디렉터리
- `sample-data/` : 재생용 샘플 CSV(민감/실거래 데이터 금지)
- `scripts/`     : 데이터 변환/실행 스크립트

## 샘플 CSV 형식(제안)
```
stockCode,eventTime,currentPrice,accumulatedVolume
005930,2026-06-15T09:00:01+09:00,72000,1000
005930,2026-06-15T09:00:02+09:00,72100,2500
```

## 구현 메모
`ReplayRunner`(Java)는 signal-engine-java의 `SignalEngineApplication` 파이프라인을 재사용하여
CSV 틱을 TickEvent로 변환 후 동일 경로로 처리한다.
