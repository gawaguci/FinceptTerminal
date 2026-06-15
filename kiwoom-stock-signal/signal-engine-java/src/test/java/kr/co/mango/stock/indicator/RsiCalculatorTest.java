package kr.co.mango.stock.indicator;

import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class RsiCalculatorTest {

    @Test
    void 데이터가_부족하면_empty를_반환한다() {
        RsiCalculator calc = new RsiCalculator(14);
        assertTrue(calc.calculate(Bars.ofCloses(100, 101, 102)).isEmpty());
    }

    @Test
    void 지속_상승이면_RSI는_100이다() {
        RsiCalculator calc = new RsiCalculator(3);
        OptionalDouble rsi = calc.calculate(Bars.ofCloses(10, 11, 12, 13, 14));
        assertTrue(rsi.isPresent());
        assertEquals(100.0, rsi.getAsDouble(), 1e-9);
    }

    @Test
    void 지속_하락이면_RSI는_0이다() {
        RsiCalculator calc = new RsiCalculator(3);
        OptionalDouble rsi = calc.calculate(Bars.ofCloses(14, 13, 12, 11, 10));
        assertTrue(rsi.isPresent());
        assertEquals(0.0, rsi.getAsDouble(), 1e-9);
    }

    @Test
    void 손으로_계산한_값과_일치한다() {
        // period=3, closes=[10,11,10,12,11,13] → Wilder 평활 결과 RSI=75.0
        RsiCalculator calc = new RsiCalculator(3);
        OptionalDouble rsi = calc.calculate(Bars.ofCloses(10, 11, 10, 12, 11, 13));
        assertTrue(rsi.isPresent());
        assertEquals(75.0, rsi.getAsDouble(), 1e-6);
    }
}
