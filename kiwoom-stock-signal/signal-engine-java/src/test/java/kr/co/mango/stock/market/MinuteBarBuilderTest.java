package kr.co.mango.stock.market;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MinuteBarBuilder 단위 테스트.
 * 봉 경계 확정, OHLC 집계, 누적거래량 차분 검증.
 */
class MinuteBarBuilderTest {

    private TickEvent tick(String time, long price, long accVol) {
        Instant t = Instant.parse(time);
        return new TickEvent("005930", "삼성전자", t, price, accVol, t);
    }

    @Test
    void 봉_경계를_넘으면_직전봉이_확정된다() {
        MinuteBarBuilder builder = new MinuteBarBuilder("005930", BarInterval.M1);

        assertTrue(builder.onTick(tick("2026-06-15T00:00:05Z", 100, 100)).isEmpty());
        assertTrue(builder.onTick(tick("2026-06-15T00:00:30Z", 110, 150)).isEmpty());

        // 새로운 분 진입 → 직전 봉 확정
        Optional<MinuteBar> completed = builder.onTick(tick("2026-06-15T00:01:10Z", 105, 200));
        assertTrue(completed.isPresent());

        MinuteBar bar0 = completed.get();
        assertEquals(Instant.parse("2026-06-15T00:00:00Z"), bar0.getBarTime());
        assertEquals(100, bar0.getOpen());
        assertEquals(110, bar0.getHigh());
        assertEquals(100, bar0.getLow());
        assertEquals(110, bar0.getClose());
        assertEquals(50, bar0.getVolume());          // 누적 100→150 차분
        assertEquals(110 * 50, bar0.getTradeAmount()); // 110원 * 50주
        assertTrue(bar0.isCompleted());
    }

    @Test
    void 진행중_봉은_completed가_false다() {
        MinuteBarBuilder builder = new MinuteBarBuilder("005930", BarInterval.M1);
        builder.onTick(tick("2026-06-15T00:00:05Z", 100, 100));
        builder.onTick(tick("2026-06-15T00:00:30Z", 90, 130));

        Optional<MinuteBar> current = builder.getCurrent();
        assertTrue(current.isPresent());
        MinuteBar bar = current.get();
        assertFalse(bar.isCompleted());
        assertEquals(100, bar.getOpen());
        assertEquals(90, bar.getLow());
        assertEquals(30, bar.getVolume());
    }

    @Test
    void flush는_진행중_봉을_확정한다() {
        MinuteBarBuilder builder = new MinuteBarBuilder("005930", BarInterval.M1);
        builder.onTick(tick("2026-06-15T00:00:05Z", 100, 100));
        Optional<MinuteBar> flushed = builder.flush();
        assertTrue(flushed.isPresent());
        assertTrue(flushed.get().isCompleted());
        assertTrue(builder.getCurrent().isEmpty());
    }

    @Test
    void 누적거래량_감소는_0으로_방어한다() {
        MinuteBarBuilder builder = new MinuteBarBuilder("005930", BarInterval.M1);
        builder.onTick(tick("2026-06-15T00:00:05Z", 100, 200));
        // 누적값이 줄어드는 비정상 입력
        builder.onTick(tick("2026-06-15T00:00:30Z", 100, 150));
        assertEquals(0, builder.getCurrent().orElseThrow().getVolume());
    }

    @Test
    void 오분봉_버킷_경계가_정확하다() {
        Instant t = Instant.parse("2026-06-15T00:07:30Z");
        Instant bucket = MinuteBarBuilder.floorToBucket(t, BarInterval.M5);
        assertEquals(Instant.parse("2026-06-15T00:05:00Z"), bucket);
    }
}
