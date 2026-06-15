package kr.co.mango.stock.scanner;

import kr.co.mango.stock.config.StrategyConfig;
import kr.co.mango.stock.market.MarketSnapshot;

import java.util.EnumSet;

/**
 * 실시간 후보 종목 점수화기(계획서 10.3).
 *
 * <p>시장 스냅샷을 입력받아 항목별 조건 충족 여부를 평가하고 합산 점수와 등급을 산정한다.
 * 순수 함수로 동작하여 단위 테스트가 용이하다.
 */
public final class RealtimeScanner {

    private final StrategyConfig config;

    public RealtimeScanner(StrategyConfig config) {
        this.config = config;
    }

    /**
     * 스냅샷 기준 후보 점수를 계산한다.
     */
    public CandidateScore score(MarketSnapshot s) {
        EnumSet<CandidateReason> matched = EnumSet.noneOf(CandidateReason.class);
        int total = 0;

        // 거래대금
        if (s.getDailyTradeAmount() >= config.getMinTradeAmount()) {
            matched.add(CandidateReason.TRADE_AMOUNT);
            total += CandidateReason.TRADE_AMOUNT.getWeight();
        }
        // 거래량 급증
        if (isVolumeSpike(s)) {
            matched.add(CandidateReason.VOLUME_SPIKE);
            total += CandidateReason.VOLUME_SPIKE.getWeight();
        }
        // 가격 위치(VWAP 상단)
        if (s.getVwap() > 0 && s.getCurrentPrice() > s.getVwap()) {
            matched.add(CandidateReason.PRICE_ABOVE_VWAP);
            total += CandidateReason.PRICE_ABOVE_VWAP.getWeight();
        }
        // 추세(5분봉 종가 > 20봉 이동평균)
        if (s.getMa20OnFiveMinute() > 0 && s.getFiveMinuteClose() > s.getMa20OnFiveMinute()) {
            matched.add(CandidateReason.TREND);
            total += CandidateReason.TREND.getWeight();
        }
        // 돌파
        if (s.isBreakout()) {
            matched.add(CandidateReason.BREAKOUT);
            total += CandidateReason.BREAKOUT.getWeight();
        }
        // MACD: MACD선 > Signal선 이고 Histogram 증가
        if (s.getMacd() > s.getMacdSignal() && s.isHistogramRising()) {
            matched.add(CandidateReason.MACD);
            total += CandidateReason.MACD.getWeight();
        }
        // RSI 50 이상
        if (s.getRsi() >= 50.0) {
            matched.add(CandidateReason.RSI);
            total += CandidateReason.RSI.getWeight();
        }

        return new CandidateScore(s.getStockCode(), total, grade(total), matched);
    }

    private boolean isVolumeSpike(MarketSnapshot s) {
        if (s.getAvg20MinuteVolume() <= 0.0) {
            return false;
        }
        return s.getOneMinuteVolume() > s.getAvg20MinuteVolume() * config.getVolumeSpikeMultiplier();
    }

    private CandidateGrade grade(int total) {
        if (total >= config.getStrongThreshold()) {
            return CandidateGrade.STRONG;
        }
        if (total >= config.getWatchThreshold()) {
            return CandidateGrade.WATCH;
        }
        if (total >= config.getInterestThreshold()) {
            return CandidateGrade.INTEREST;
        }
        return CandidateGrade.NONE;
    }
}
