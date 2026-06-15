package kr.co.mango.stock.signal;

import kr.co.mango.stock.config.StrategyConfig;
import kr.co.mango.stock.market.MarketSnapshot;
import kr.co.mango.stock.scanner.CandidateScore;

import java.util.StringJoiner;

/**
 * MACD + RSI 기반 매수타이밍 엔진(계획서 11장).
 *
 * <p>판단 순서:
 * <ol>
 *   <li>매수 제외 조건(11.4) 우선 검사 → 해당 시 REJECTED</li>
 *   <li>추세형 매수 조건(11.2) 전체 충족 + 후보 점수 임계값 이상 → BUY_CANDIDATE</li>
 *   <li>그 외 → REJECTED</li>
 * </ol>
 *
 * <p>본 엔진은 지표/가격 기반 판단만 수행한다. 쿨다운, 1일 손실, 계좌 잔고,
 * 시간 컷오프 등 상태 기반 차단은 {@code RiskManager}가 담당한다.
 * 실주문 여부(orderEnabled)는 항상 false로 둔다.
 */
public final class BuyTimingEngine {

    private final StrategyConfig config;

    public BuyTimingEngine(StrategyConfig config) {
        this.config = config;
    }

    public BuySignal evaluate(MarketSnapshot s, CandidateScore score) {
        BuySignal.Builder base = BuySignal.builder(s.getStockCode())
                .signalTime(s.getSnapshotTime())
                .currentPrice(s.getCurrentPrice())
                .candidateScore(score.getTotal())
                .macd(s.getMacd())
                .macdSignal(s.getMacdSignal())
                .macdHistogram(s.getMacdHistogram())
                .rsi(s.getRsi())
                .vwap(s.getVwap())
                .orderEnabled(false);

        // 1) 매수 제외 조건
        RejectReason excluded = checkExclusion(s);
        if (excluded != RejectReason.NONE) {
            return base.signalType(SignalType.REJECTED)
                    .rejectReason(excluded)
                    .reason("제외 조건: " + excluded.getLabel())
                    .build();
        }

        // 2) 추세형 매수 조건 전체 충족 여부
        StringJoiner met = new StringJoiner(", ");
        boolean trendOk = checkTrendConditions(s, met);
        if (!trendOk) {
            return base.signalType(SignalType.REJECTED)
                    .rejectReason(RejectReason.TREND_CONDITION_NOT_MET)
                    .reason("추세 조건 미충족")
                    .build();
        }

        // 3) 후보 점수 임계값(매수타이밍 감시 기준 이상)
        if (score.getTotal() < config.getWatchThreshold()) {
            return base.signalType(SignalType.REJECTED)
                    .rejectReason(RejectReason.SCORE_TOO_LOW)
                    .reason("후보 점수 미달(" + score.getTotal() + " < " + config.getWatchThreshold() + ")")
                    .build();
        }

        return base.signalType(SignalType.BUY_CANDIDATE)
                .rejectReason(RejectReason.NONE)
                .reason(met.toString())
                .build();
    }

    /** 매수 제외 조건(11.4) 중 지표/가격 기반 항목 검사. */
    private RejectReason checkExclusion(MarketSnapshot s) {
        if (!s.isApiStable()) {
            return RejectReason.API_UNSTABLE;
        }
        if (s.getRsi() >= config.getMaxRsiForEntry()) {
            return RejectReason.RSI_OVERHEATED;
        }
        if (s.vwapGapRate() >= config.getMaxVwapGapRate()) {
            return RejectReason.VWAP_GAP_TOO_LARGE;
        }
        if (s.getSpreadTicks() > config.getMaxSpreadTicks()) {
            return RejectReason.SPREAD_TOO_WIDE;
        }
        return RejectReason.NONE;
    }

    /** 추세형 매수 조건(11.2) 1~6번을 모두 만족해야 한다. */
    private boolean checkTrendConditions(MarketSnapshot s, StringJoiner met) {
        boolean ok = true;

        // 조건 1: 현재가 > VWAP
        if (s.getVwap() > 0 && s.getCurrentPrice() > s.getVwap()) {
            met.add("VWAP 상단");
        } else {
            ok = false;
        }
        // 조건 2: 5분봉 종가 > 20봉 이동평균
        if (s.getMa20OnFiveMinute() > 0 && s.getFiveMinuteClose() > s.getMa20OnFiveMinute()) {
            met.add("5분봉 추세 상단");
        } else {
            ok = false;
        }
        // 조건 3: MACD선 > Signal선
        if (s.getMacd() > s.getMacdSignal()) {
            met.add("MACD 상향");
        } else {
            ok = false;
        }
        // 조건 4: Histogram 2봉 이상 증가
        if (s.isHistogramRising()) {
            met.add("Histogram 증가");
        } else {
            ok = false;
        }
        // 조건 5: RSI 50 이상
        if (s.getRsi() >= 50.0) {
            met.add("RSI 50 이상");
        } else {
            ok = false;
        }
        // 조건 6: 1분 거래량 > 20분 평균 × 배수
        if (s.getAvg20MinuteVolume() > 0
                && s.getOneMinuteVolume() > s.getAvg20MinuteVolume() * config.getVolumeSpikeMultiplier()) {
            met.add("거래량 급증");
        } else {
            ok = false;
        }
        return ok;
    }
}
