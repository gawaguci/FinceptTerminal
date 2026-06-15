package kr.co.mango.stock.indicator;

import kr.co.mango.stock.market.MinuteBar;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MacdCalculatorTest {

    @Test
    void 데이터가_부족하면_empty를_반환한다() {
        MacdCalculator calc = new MacdCalculator(); // 12/26/9
        assertTrue(calc.calculate(Bars.ofCloses(1, 2, 3, 4, 5)).isEmpty());
    }

    @Test
    void 상승추세에서_MACD가_양수이고_Signal보다_크다() {
        // 가속 상승(2차 곡선): MACD선이 계속 상승하므로 Signal선보다 크게 유지된다.
        long[] closes = new long[60];
        for (int i = 0; i < closes.length; i++) {
            closes[i] = 1000 + (long) i * i; // 가속 상승
        }
        MacdCalculator calc = new MacdCalculator();
        Optional<MacdResult> result = calc.calculate(Bars.ofCloses(closes));
        assertTrue(result.isPresent());
        MacdResult r = result.get();
        assertTrue(r.getMacd() > 0, "상승추세에서 MACD는 양수여야 한다");
        assertTrue(r.getMacd() > r.getSignal(), "상승추세에서 MACD가 Signal보다 커야 한다");
        assertEquals(r.getMacd() - r.getSignal(), r.getHistogram(), 1e-9);
    }

    @Test
    void 가격이_일정하면_MACD는_0이다() {
        long[] closes = new long[40];
        java.util.Arrays.fill(closes, 1000L);
        MacdCalculator calc = new MacdCalculator();
        Optional<MacdResult> result = calc.calculate(Bars.ofCloses(closes));
        assertTrue(result.isPresent());
        assertEquals(0.0, result.get().getMacd(), 1e-6);
        assertEquals(0.0, result.get().getSignal(), 1e-6);
        assertEquals(0.0, result.get().getHistogram(), 1e-6);
    }

    @Test
    void Histogram_시계열을_반환한다() {
        long[] closes = new long[60];
        for (int i = 0; i < closes.length; i++) {
            closes[i] = 1000 + i * 10L;
        }
        MacdCalculator calc = new MacdCalculator();
        List<MinuteBar> bars = Bars.ofCloses(closes);
        List<Double> hist = calc.histogramSeries(bars);
        assertFalse(hist.isEmpty());
        // 마지막 Histogram은 calculate()의 Histogram과 일치
        assertEquals(calc.calculate(bars).orElseThrow().getHistogram(),
                hist.get(hist.size() - 1), 1e-9);
    }
}
