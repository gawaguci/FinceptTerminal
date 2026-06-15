package kr.co.mango.stock.indicator;

import kr.co.mango.stock.market.BarInterval;
import kr.co.mango.stock.market.MinuteBar;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class VwapCalculatorTest {

    private MinuteBar bar(long close, long volume, long tradeAmount) {
        return new MinuteBar("005930", BarInterval.M1, Instant.parse("2026-06-15T00:00:00Z"),
                close, close, close, close, volume, tradeAmount, true);
    }

    @Test
    void 누적_거래대금과_거래량으로_VWAP를_계산한다() {
        List<MinuteBar> bars = new ArrayList<>();
        bars.add(bar(100, 10, 1000)); // 100원 * 10주
        bars.add(bar(200, 10, 2000)); // 200원 * 10주
        OptionalDouble vwap = new VwapCalculator().calculate(bars);
        assertTrue(vwap.isPresent());
        assertEquals(150.0, vwap.getAsDouble(), 1e-9); // 3000 / 20
    }

    @Test
    void 거래대금이_없으면_종가곱거래량으로_근사한다() {
        List<MinuteBar> bars = new ArrayList<>();
        bars.add(bar(100, 10, 0)); // 거래대금 미제공 → 100*10=1000 근사
        bars.add(bar(300, 10, 0)); // 300*10=3000 근사
        OptionalDouble vwap = new VwapCalculator().calculate(bars);
        assertTrue(vwap.isPresent());
        assertEquals(200.0, vwap.getAsDouble(), 1e-9); // 4000 / 20
    }

    @Test
    void 거래량이_0이면_empty를_반환한다() {
        List<MinuteBar> bars = new ArrayList<>();
        bars.add(bar(100, 0, 0));
        assertTrue(new VwapCalculator().calculate(bars).isEmpty());
    }

    @Test
    void 빈_리스트면_empty를_반환한다() {
        assertTrue(new VwapCalculator().calculate(new ArrayList<>()).isEmpty());
    }
}
