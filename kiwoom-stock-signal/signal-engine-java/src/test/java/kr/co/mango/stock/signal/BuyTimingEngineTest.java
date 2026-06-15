package kr.co.mango.stock.signal;

import kr.co.mango.stock.config.StrategyConfig;
import kr.co.mango.stock.market.MarketSnapshot;
import kr.co.mango.stock.scanner.CandidateScore;
import kr.co.mango.stock.scanner.RealtimeScanner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuyTimingEngineTest {

    private final StrategyConfig config = StrategyConfig.defaults();
    private final RealtimeScanner scanner = new RealtimeScanner(config);
    private final BuyTimingEngine engine = new BuyTimingEngine(config);

    /** 모든 매수 조건을 충족하는 기본 스냅샷 빌더. */
    private MarketSnapshot.Builder buyReady() {
        return MarketSnapshot.builder("005930")
                .currentPrice(1000)
                .dailyTradeAmount(5_000_000_000L)
                .oneMinuteVolume(200)
                .avg20MinuteVolume(100)
                .vwap(990)              // 이격률 약 1% < 3%
                .fiveMinuteClose(1000)
                .ma20OnFiveMinute(950)
                .previousHigh(995)
                .macd(2.0).macdSignal(1.0).macdHistogram(1.0).histogramRising(true)
                .rsi(55)
                .spreadTicks(1)
                .apiStable(true);
    }

    @Test
    void 모든_조건_충족시_매수후보를_발생시킨다() {
        MarketSnapshot s = buyReady().build();
        BuySignal signal = engine.evaluate(s, scanner.score(s));
        assertEquals(SignalType.BUY_CANDIDATE, signal.getSignalType());
        assertEquals(RejectReason.NONE, signal.getRejectReason());
        assertFalse(signal.isOrderEnabled(), "신호 단계에서 주문은 항상 비활성이다");
    }

    @Test
    void RSI_과열이면_거절한다() {
        MarketSnapshot s = buyReady().rsi(80).build();
        BuySignal signal = engine.evaluate(s, scanner.score(s));
        assertEquals(SignalType.REJECTED, signal.getSignalType());
        assertEquals(RejectReason.RSI_OVERHEATED, signal.getRejectReason());
    }

    @Test
    void VWAP_이격_과다면_거절한다() {
        MarketSnapshot s = buyReady().vwap(900).build(); // 이격률 약 11%
        BuySignal signal = engine.evaluate(s, scanner.score(s));
        assertEquals(SignalType.REJECTED, signal.getSignalType());
        assertEquals(RejectReason.VWAP_GAP_TOO_LARGE, signal.getRejectReason());
    }

    @Test
    void 호가_스프레드_과다면_거절한다() {
        MarketSnapshot s = buyReady().spreadTicks(5).build();
        BuySignal signal = engine.evaluate(s, scanner.score(s));
        assertEquals(RejectReason.SPREAD_TOO_WIDE, signal.getRejectReason());
    }

    @Test
    void API_불안정이면_거절한다() {
        MarketSnapshot s = buyReady().apiStable(false).build();
        BuySignal signal = engine.evaluate(s, scanner.score(s));
        assertEquals(RejectReason.API_UNSTABLE, signal.getRejectReason());
    }

    @Test
    void 추세_조건_미충족이면_거절한다() {
        // MACD가 Signal 아래 → 추세 조건 실패(제외 조건은 통과하도록 구성)
        MarketSnapshot s = buyReady().macd(1.0).macdSignal(2.0).build();
        BuySignal signal = engine.evaluate(s, scanner.score(s));
        assertEquals(SignalType.REJECTED, signal.getSignalType());
        assertEquals(RejectReason.TREND_CONDITION_NOT_MET, signal.getRejectReason());
    }
}
