package kr.co.mango.stock.market;

import java.time.Instant;
import java.util.Objects;

/**
 * 키움 OpenAPI+ 실시간 체결 데이터를 정규화한 틱 이벤트(불변 객체).
 *
 * <p>키움 Gateway(Delphi)가 OnReceiveRealData에서 FID(9001 종목코드, 10 현재가,
 * 13 누적거래량)를 파싱하여 JSON으로 발행하면, Java 엔진이 본 객체로 역직렬화한다.
 *
 * <p>가격은 원 단위 정수(long)로 처리한다. 누적거래량은 당일 누적값이다.
 */
public final class TickEvent {

    private final String stockCode;
    private final String stockName;
    private final Instant eventTime;
    private final long currentPrice;
    private final long accumulatedVolume;
    private final Instant receivedAt;

    public TickEvent(String stockCode,
                     String stockName,
                     Instant eventTime,
                     long currentPrice,
                     long accumulatedVolume,
                     Instant receivedAt) {
        this.stockCode = Objects.requireNonNull(stockCode, "stockCode는 필수이다");
        this.stockName = stockName;
        this.eventTime = Objects.requireNonNull(eventTime, "eventTime은 필수이다");
        this.currentPrice = currentPrice;
        this.accumulatedVolume = accumulatedVolume;
        this.receivedAt = receivedAt != null ? receivedAt : eventTime;
    }

    public String getStockCode() {
        return stockCode;
    }

    public String getStockName() {
        return stockName;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    /** 현재가(원 단위 정수) */
    public long getCurrentPrice() {
        return currentPrice;
    }

    /** 당일 누적거래량 */
    public long getAccumulatedVolume() {
        return accumulatedVolume;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    @Override
    public String toString() {
        return "TickEvent{" +
                "stockCode='" + stockCode + '\'' +
                ", eventTime=" + eventTime +
                ", currentPrice=" + currentPrice +
                ", accumulatedVolume=" + accumulatedVolume +
                '}';
    }
}
