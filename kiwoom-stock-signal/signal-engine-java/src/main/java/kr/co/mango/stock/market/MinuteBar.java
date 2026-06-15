package kr.co.mango.stock.market;

import java.time.Instant;
import java.util.Objects;

/**
 * 분봉(OHLCV) 데이터(불변 객체).
 *
 * <p>가격은 원 단위 정수(long), 거래대금(tradeAmount)은 원 단위 정수로 처리한다.
 *
 * <p>주의: {@code completed=false}인 봉은 아직 진행 중인 봉이므로,
 * 종가를 확정봉처럼 사용하면 안 된다(계획서 금지사항).
 */
public final class MinuteBar {

    private final String stockCode;
    private final BarInterval interval;
    private final Instant barTime;
    private final long open;
    private final long high;
    private final long low;
    private final long close;
    private final long volume;
    private final long tradeAmount;
    private final boolean completed;

    public MinuteBar(String stockCode,
                     BarInterval interval,
                     Instant barTime,
                     long open,
                     long high,
                     long low,
                     long close,
                     long volume,
                     long tradeAmount,
                     boolean completed) {
        this.stockCode = Objects.requireNonNull(stockCode, "stockCode는 필수이다");
        this.interval = Objects.requireNonNull(interval, "interval은 필수이다");
        this.barTime = Objects.requireNonNull(barTime, "barTime은 필수이다");
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.tradeAmount = tradeAmount;
        this.completed = completed;
    }

    public String getStockCode() {
        return stockCode;
    }

    public BarInterval getInterval() {
        return interval;
    }

    /** 봉 시작 시각(버킷 경계) */
    public Instant getBarTime() {
        return barTime;
    }

    public long getOpen() {
        return open;
    }

    public long getHigh() {
        return high;
    }

    public long getLow() {
        return low;
    }

    public long getClose() {
        return close;
    }

    public long getVolume() {
        return volume;
    }

    /** 봉 누적 거래대금(원). VWAP 계산에 사용한다. */
    public long getTradeAmount() {
        return tradeAmount;
    }

    /** 확정봉 여부. false이면 진행 중인 봉이다. */
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public String toString() {
        return "MinuteBar{" +
                "stockCode='" + stockCode + '\'' +
                ", interval=" + interval +
                ", barTime=" + barTime +
                ", o=" + open + ", h=" + high + ", l=" + low + ", c=" + close +
                ", vol=" + volume +
                ", completed=" + completed +
                '}';
    }
}
