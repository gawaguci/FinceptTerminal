package kr.co.mango.stock.market;

import java.time.Instant;
import java.util.Objects;

/**
 * 특정 시점 한 종목의 시장/지표 종합 스냅샷(불변 객체).
 *
 * <p>분봉 빌더와 지표 계산기가 산출한 값을 한데 모아 스캐너/매수타이밍 엔진/리스크 매니저에 전달한다.
 * 가격은 원 단위 정수, 비율은 소수(예: 0.03 = 3%)로 표현한다.
 */
public final class MarketSnapshot {

    private final String stockCode;
    private final String stockName;
    private final Instant snapshotTime;

    private final long currentPrice;
    private final long dailyTradeAmount;    // 당일 누적 거래대금(원)
    private final long oneMinuteVolume;     // 직전 확정 1분봉 거래량
    private final double avg20MinuteVolume;  // 최근 20분 평균 거래량
    private final double vwap;
    private final long fiveMinuteClose;     // 직전 확정 5분봉 종가
    private final double ma20OnFiveMinute;   // 5분봉 20봉 이동평균
    private final long previousHigh;        // 직전 고점/당일 고가
    private final double macd;
    private final double macdSignal;
    private final double macdHistogram;
    private final boolean histogramRising;   // Histogram 2봉 이상 증가
    private final double rsi;
    private final double changeRate;         // 등락률(소수, 예: 0.05 = +5%)
    private final int spreadTicks;          // 호가 스프레드 틱
    private final boolean apiStable;         // 키움 연결/실시간 수신 정상 여부

    private MarketSnapshot(Builder b) {
        this.stockCode = Objects.requireNonNull(b.stockCode, "stockCode는 필수이다");
        this.stockName = b.stockName;
        this.snapshotTime = b.snapshotTime != null ? b.snapshotTime : Instant.now();
        this.currentPrice = b.currentPrice;
        this.dailyTradeAmount = b.dailyTradeAmount;
        this.oneMinuteVolume = b.oneMinuteVolume;
        this.avg20MinuteVolume = b.avg20MinuteVolume;
        this.vwap = b.vwap;
        this.fiveMinuteClose = b.fiveMinuteClose;
        this.ma20OnFiveMinute = b.ma20OnFiveMinute;
        this.previousHigh = b.previousHigh;
        this.macd = b.macd;
        this.macdSignal = b.macdSignal;
        this.macdHistogram = b.macdHistogram;
        this.histogramRising = b.histogramRising;
        this.rsi = b.rsi;
        this.changeRate = b.changeRate;
        this.spreadTicks = b.spreadTicks;
        this.apiStable = b.apiStable;
    }

    public static Builder builder(String stockCode) {
        return new Builder(stockCode);
    }

    public String getStockCode() { return stockCode; }
    public String getStockName() { return stockName; }
    public Instant getSnapshotTime() { return snapshotTime; }
    public long getCurrentPrice() { return currentPrice; }
    public long getDailyTradeAmount() { return dailyTradeAmount; }
    public long getOneMinuteVolume() { return oneMinuteVolume; }
    public double getAvg20MinuteVolume() { return avg20MinuteVolume; }
    public double getVwap() { return vwap; }
    public long getFiveMinuteClose() { return fiveMinuteClose; }
    public double getMa20OnFiveMinute() { return ma20OnFiveMinute; }
    public long getPreviousHigh() { return previousHigh; }
    public double getMacd() { return macd; }
    public double getMacdSignal() { return macdSignal; }
    public double getMacdHistogram() { return macdHistogram; }
    public boolean isHistogramRising() { return histogramRising; }
    public double getRsi() { return rsi; }
    public double getChangeRate() { return changeRate; }
    public int getSpreadTicks() { return spreadTicks; }
    public boolean isApiStable() { return apiStable; }

    /** 현재가가 VWAP 대비 이격된 비율(소수). VWAP이 0이면 0을 반환한다. */
    public double vwapGapRate() {
        if (vwap <= 0.0) {
            return 0.0;
        }
        return (currentPrice - vwap) / vwap;
    }

    /** 현재가가 직전 고점을 돌파했는지 여부. */
    public boolean isBreakout() {
        return previousHigh > 0 && currentPrice > previousHigh;
    }

    public static final class Builder {
        private final String stockCode;
        private String stockName;
        private Instant snapshotTime;
        private long currentPrice;
        private long dailyTradeAmount;
        private long oneMinuteVolume;
        private double avg20MinuteVolume;
        private double vwap;
        private long fiveMinuteClose;
        private double ma20OnFiveMinute;
        private long previousHigh;
        private double macd;
        private double macdSignal;
        private double macdHistogram;
        private boolean histogramRising;
        private double rsi;
        private double changeRate;
        private int spreadTicks = 1;
        private boolean apiStable = true;

        private Builder(String stockCode) {
            this.stockCode = stockCode;
        }

        public Builder stockName(String v) { this.stockName = v; return this; }
        public Builder snapshotTime(Instant v) { this.snapshotTime = v; return this; }
        public Builder currentPrice(long v) { this.currentPrice = v; return this; }
        public Builder dailyTradeAmount(long v) { this.dailyTradeAmount = v; return this; }
        public Builder oneMinuteVolume(long v) { this.oneMinuteVolume = v; return this; }
        public Builder avg20MinuteVolume(double v) { this.avg20MinuteVolume = v; return this; }
        public Builder vwap(double v) { this.vwap = v; return this; }
        public Builder fiveMinuteClose(long v) { this.fiveMinuteClose = v; return this; }
        public Builder ma20OnFiveMinute(double v) { this.ma20OnFiveMinute = v; return this; }
        public Builder previousHigh(long v) { this.previousHigh = v; return this; }
        public Builder macd(double v) { this.macd = v; return this; }
        public Builder macdSignal(double v) { this.macdSignal = v; return this; }
        public Builder macdHistogram(double v) { this.macdHistogram = v; return this; }
        public Builder histogramRising(boolean v) { this.histogramRising = v; return this; }
        public Builder rsi(double v) { this.rsi = v; return this; }
        public Builder changeRate(double v) { this.changeRate = v; return this; }
        public Builder spreadTicks(int v) { this.spreadTicks = v; return this; }
        public Builder apiStable(boolean v) { this.apiStable = v; return this; }

        public MarketSnapshot build() {
            return new MarketSnapshot(this);
        }
    }
}
