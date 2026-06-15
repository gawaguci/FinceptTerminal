package kr.co.mango.stock.market;

import java.time.Instant;
import java.util.Optional;

/**
 * 틱 이벤트를 받아 단일 종목/단일 주기의 분봉을 생성한다.
 *
 * <p>핵심 규칙:
 * <ul>
 *   <li>봉 경계를 넘어가는 틱이 들어오면 이전 봉을 확정(completed=true)하여 반환한다.</li>
 *   <li>진행 중 봉은 {@link #getCurrent()}로 조회하되 completed=false로 표시한다.</li>
 *   <li>거래량은 누적거래량의 차분(delta)으로 계산한다. 누적값 감소(리셋)는 0으로 방어한다.</li>
 * </ul>
 *
 * <p>본 빌더는 스레드 안전하지 않다. 종목/주기별로 별도 인스턴스를 사용한다.
 */
public final class MinuteBarBuilder {

    private final String stockCode;
    private final BarInterval interval;

    private boolean hasCurrent = false;
    private Instant currentBarStart;
    private long open;
    private long high;
    private long low;
    private long close;
    private long barVolume;
    private long barTradeAmount;

    /** 직전 틱의 누적거래량. -1이면 아직 기준값이 없음을 의미한다. */
    private long prevAccumulatedVolume = -1L;

    public MinuteBarBuilder(String stockCode, BarInterval interval) {
        this.stockCode = stockCode;
        this.interval = interval;
    }

    /**
     * 틱을 반영한다.
     *
     * @return 봉 경계를 넘으며 확정된 직전 봉이 있으면 해당 봉, 없으면 empty
     */
    public Optional<MinuteBar> onTick(TickEvent tick) {
        final Instant bucket = floorToBucket(tick.getEventTime(), interval);

        // 누적거래량 차분으로 이번 틱의 체결량을 산정한다.
        long delta = 0L;
        if (prevAccumulatedVolume >= 0L) {
            delta = tick.getAccumulatedVolume() - prevAccumulatedVolume;
            if (delta < 0L) {
                // 누적값이 줄어드는 비정상 상황(리셋 등)은 0으로 방어한다.
                delta = 0L;
            }
        }
        prevAccumulatedVolume = tick.getAccumulatedVolume();

        Optional<MinuteBar> completedBar = Optional.empty();

        if (!hasCurrent) {
            startNewBar(bucket, tick.getCurrentPrice());
        } else if (bucket.isAfter(currentBarStart)) {
            // 새로운 버킷 진입 → 직전 봉을 확정 후 새 봉 시작
            completedBar = Optional.of(buildBar(true));
            startNewBar(bucket, tick.getCurrentPrice());
        }

        // 현재 봉 갱신
        high = Math.max(high, tick.getCurrentPrice());
        low = Math.min(low, tick.getCurrentPrice());
        close = tick.getCurrentPrice();
        barVolume += delta;
        barTradeAmount += tick.getCurrentPrice() * delta;

        return completedBar;
    }

    /** 진행 중인 봉(completed=false). 봉이 없으면 empty. */
    public Optional<MinuteBar> getCurrent() {
        if (!hasCurrent) {
            return Optional.empty();
        }
        return Optional.of(buildBar(false));
    }

    /**
     * 장 종료 등으로 진행 중 봉을 강제 확정한다(리플레이/테스트 종료 처리용).
     *
     * @return 확정된 봉, 진행 중 봉이 없으면 empty
     */
    public Optional<MinuteBar> flush() {
        if (!hasCurrent) {
            return Optional.empty();
        }
        MinuteBar bar = buildBar(true);
        hasCurrent = false;
        return Optional.of(bar);
    }

    private void startNewBar(Instant bucket, long price) {
        hasCurrent = true;
        currentBarStart = bucket;
        open = price;
        high = price;
        low = price;
        close = price;
        barVolume = 0L;
        barTradeAmount = 0L;
    }

    private MinuteBar buildBar(boolean completed) {
        return new MinuteBar(stockCode, interval, currentBarStart,
                open, high, low, close, barVolume, barTradeAmount, completed);
    }

    /**
     * 이벤트 시각을 봉 주기 버킷의 시작 시각으로 내림(floor)한다.
     * 한국 시장 오프셋(+09:00)은 정수 분 단위이므로 epoch 분 기준 내림으로 벽시계 분 경계와 일치한다.
     */
    static Instant floorToBucket(Instant time, BarInterval interval) {
        long epochMinutes = Math.floorDiv(time.getEpochSecond(), 60L);
        long bucketMinutes = Math.floorDiv(epochMinutes, interval.getMinutes()) * interval.getMinutes();
        return Instant.ofEpochSecond(bucketMinutes * 60L);
    }
}
