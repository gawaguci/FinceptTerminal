package kr.co.mango.stock.app;

import kr.co.mango.stock.config.StrategyConfig;
import kr.co.mango.stock.indicator.MacdCalculator;
import kr.co.mango.stock.indicator.MacdResult;
import kr.co.mango.stock.indicator.MovingAverageCalculator;
import kr.co.mango.stock.indicator.RsiCalculator;
import kr.co.mango.stock.indicator.VwapCalculator;
import kr.co.mango.stock.market.BarInterval;
import kr.co.mango.stock.market.MarketSnapshot;
import kr.co.mango.stock.market.MinuteBar;
import kr.co.mango.stock.market.MinuteBarBuilder;
import kr.co.mango.stock.market.TickEvent;
import kr.co.mango.stock.order.OrderDecision;
import kr.co.mango.stock.repository.InMemorySignalLogRepository;
import kr.co.mango.stock.risk.PositionLimit;
import kr.co.mango.stock.risk.RiskManager;
import kr.co.mango.stock.risk.RiskPolicy;
import kr.co.mango.stock.scanner.CandidateScore;
import kr.co.mango.stock.scanner.RealtimeScanner;
import kr.co.mango.stock.signal.BuySignal;
import kr.co.mango.stock.signal.BuyTimingEngine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Signal Engine 데모 파이프라인.
 *
 * <p>실시간 키움 연동 없이, 합성 틱 데이터로 전체 흐름을 시연한다.
 * <pre>
 *   틱 수신 → 1분봉 생성 → 지표 계산 → 후보 점수화 → 매수타이밍 판단 → 리스크 검증 → 로그 저장
 * </pre>
 * 실제 운영에서는 Delphi Gateway의 JSON TickEvent를 입력으로 사용한다.
 */
public final class SignalEngineApplication {

    public static void main(String[] args) {
        StrategyConfig strategy = StrategyConfig.defaults();
        RealtimeScanner scanner = new RealtimeScanner(strategy);
        BuyTimingEngine engine = new BuyTimingEngine(strategy);
        InMemorySignalLogRepository signalLog = new InMemorySignalLogRepository();

        String stockCode = "005930";
        List<MinuteBar> oneMinBars = buildBarsFromSyntheticTicks(stockCode);

        // 지표 계산
        MacdCalculator macdCalc = new MacdCalculator(
                strategy.getMacdFast(), strategy.getMacdSlow(), strategy.getMacdSignal());
        RsiCalculator rsiCalc = new RsiCalculator(strategy.getRsiPeriod());
        VwapCalculator vwapCalc = new VwapCalculator();
        MovingAverageCalculator ma20 = new MovingAverageCalculator(20);

        MacdResult macd = macdCalc.calculate(oneMinBars).orElse(new MacdResult(0, 0, 0));
        List<Double> hist = macdCalc.histogramSeries(oneMinBars);
        boolean histRising = isHistogramRising(hist);
        double rsi = rsiCalc.calculate(oneMinBars).orElse(0);
        double vwap = vwapCalc.calculate(oneMinBars).orElse(0);
        double maValue = ma20.calculate(oneMinBars).orElse(0);
        MinuteBar last = oneMinBars.get(oneMinBars.size() - 1);

        // 스냅샷 구성(데모이므로 5분봉 추세는 1분봉 값으로 근사)
        MarketSnapshot snapshot = MarketSnapshot.builder(stockCode)
                .stockName("삼성전자")
                .snapshotTime(last.getBarTime())
                .currentPrice(last.getClose())
                .dailyTradeAmount(50_000_000_000L)
                .oneMinuteVolume(last.getVolume())
                .avg20MinuteVolume(averageVolume(oneMinBars))
                .vwap(vwap)
                .fiveMinuteClose(last.getClose())
                .ma20OnFiveMinute(maValue)
                .previousHigh(last.getClose() - 1)
                .macd(macd.getMacd())
                .macdSignal(macd.getSignal())
                .macdHistogram(macd.getHistogram())
                .histogramRising(histRising)
                .rsi(rsi)
                .changeRate(0.04)
                .spreadTicks(1)
                .apiStable(true)
                .build();

        // 후보 점수화 + 매수타이밍 판단
        CandidateScore score = scanner.score(snapshot);
        BuySignal signal = engine.evaluate(snapshot, score);
        signalLog.save(signal);

        // 리스크 검증(주문 비활성 기본 정책 → ORDER_DISABLED 예상)
        RiskManager riskDefault = new RiskManager(RiskPolicy.defaults());
        OrderDecision decisionDefault = riskDefault.evaluate(
                signal, 100_000_000L, snapshot.getCurrentPrice(),
                (long) (snapshot.getCurrentPrice() * 0.98), new PositionLimit(),
                Instant.parse("2026-06-15T04:35:00Z"), true, true);

        // 출력
        System.out.println("=== Kiwoom Signal Engine 데모 ===");
        System.out.println("분봉 개수: " + oneMinBars.size());
        System.out.printf("MACD=%.3f Signal=%.3f Hist=%.3f(증가=%s) RSI=%.2f VWAP=%.1f MA20=%.1f%n",
                macd.getMacd(), macd.getSignal(), macd.getHistogram(), histRising, rsi, vwap, maValue);
        System.out.println("후보 점수: " + score.getTotal() + " 등급=" + score.getGrade()
                + " 사유=" + score.reasonText());
        System.out.println("매수타이밍: " + signal);
        System.out.println("리스크(기본정책): " + decisionDefault);
        System.out.println("저장된 신호 수: " + signalLog.count());
    }

    /** 상승 추세 합성 틱으로 1분봉 리스트를 생성한다. */
    private static List<MinuteBar> buildBarsFromSyntheticTicks(String stockCode) {
        MinuteBarBuilder builder = new MinuteBarBuilder(stockCode, BarInterval.M1);
        List<MinuteBar> bars = new ArrayList<>();
        Instant start = Instant.parse("2026-06-15T00:00:00Z");
        long price = 70000;
        long accVolume = 0;
        // 40분간 분당 2틱, 완만한 상승
        for (int minute = 0; minute < 40; minute++) {
            for (int t = 0; t < 2; t++) {
                Instant time = start.plusSeconds(minute * 60L + t * 20L);
                price += 30; // 점진 상승
                accVolume += 1000;
                TickEvent tick = new TickEvent(stockCode, "삼성전자", time, price, accVolume, time);
                builder.onTick(tick).ifPresent(bars::add);
            }
        }
        builder.flush().ifPresent(bars::add);
        return bars;
    }

    /** Histogram이 최근 2봉 이상 연속 증가했는지 판단한다. */
    private static boolean isHistogramRising(List<Double> hist) {
        if (hist.size() < 3) {
            return false;
        }
        int n = hist.size();
        return hist.get(n - 1) > hist.get(n - 2) && hist.get(n - 2) > hist.get(n - 3);
    }

    private static double averageVolume(List<MinuteBar> bars) {
        if (bars.isEmpty()) {
            return 0;
        }
        long sum = 0;
        for (MinuteBar b : bars) {
            sum += b.getVolume();
        }
        return (double) sum / bars.size();
    }

    private SignalEngineApplication() {
    }
}
