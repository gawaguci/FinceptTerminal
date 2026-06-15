# AGENTS.md (signal-engine-java)

## 모듈 목적
지표 계산, 후보 종목 점수화, 매수타이밍 판단, 리스크 관리, 신호 로그 저장을 담당하는
순수 Java 비즈니스 로직 모듈이다. 키움 OCX에 직접 의존하지 않는다.

## 패키지 구조
- `market`    : TickEvent, MinuteBar, BarInterval, MinuteBarBuilder, MarketSnapshot
- `indicator` : MacdCalculator, RsiCalculator, VwapCalculator, MovingAverageCalculator
- `scanner`   : RealtimeScanner, CandidateScore, CandidateReason, CandidateGrade
- `signal`    : BuyTimingEngine, BuySignal, RejectReason, SignalStateMachine, SignalState
- `risk`      : RiskManager, RiskPolicy, PositionLimit
- `order`     : OrderRequest, OrderResult, OrderDecision, OrderSide
- `repository`: SignalLogRepository(+ InMemory 구현)
- `config`    : StrategyConfig
- `app`       : SignalEngineApplication (데모 파이프라인)

## 테스트 명령
- `mvn test`           : 전체 단위 테스트
- `mvn -q package`     : 빌드 + 테스트 + jar

## 설계 원칙
- 모든 지표 계산기는 순수 함수로 작성하고, 데이터 부족 시 `OptionalDouble.empty()` 또는 `Optional.empty()`를 반환한다.
- 도메인 객체는 불변(immutable)으로 작성한다.
- 가격은 long(원 단위), 지표/비율은 double로 처리한다.
- 주문 가부는 `RiskManager`만 판단한다. 다른 곳에서 임의 판단하지 않는다.
- BuySignal의 `orderEnabled`는 신호 단계에서 항상 false이다.

## 주의
- 진행 중 봉(completed=false)을 확정봉처럼 지표 계산에 사용하지 않는다.
- 실계좌 주문 코드/테스트를 작성하지 않는다.
