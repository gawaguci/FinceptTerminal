package kr.co.mango.stock.indicator;

import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class MovingAverageCalculatorTest {

    @Test
    void 최근_N개_종가의_평균을_계산한다() {
        MovingAverageCalculator ma = new MovingAverageCalculator(3);
        OptionalDouble result = ma.calculate(Bars.ofCloses(10, 20, 30));
        assertTrue(result.isPresent());
        assertEquals(20.0, result.getAsDouble(), 1e-9);
    }

    @Test
    void period보다_봉이_많으면_최근_N개만_사용한다() {
        MovingAverageCalculator ma = new MovingAverageCalculator(2);
        OptionalDouble result = ma.calculate(Bars.ofCloses(10, 20, 30));
        assertTrue(result.isPresent());
        assertEquals(25.0, result.getAsDouble(), 1e-9); // (20+30)/2
    }

    @Test
    void 데이터가_부족하면_empty를_반환한다() {
        MovingAverageCalculator ma = new MovingAverageCalculator(5);
        assertTrue(ma.calculate(Bars.ofCloses(10, 20)).isEmpty());
    }

    @Test
    void period가_0이하면_예외를_던진다() {
        assertThrows(IllegalArgumentException.class, () -> new MovingAverageCalculator(0));
    }
}
