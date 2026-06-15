package kr.co.mango.stock.signal;

import java.time.Instant;

/**
 * 매수타이밍 판단 결과(불변 객체). 계획서 9.2 BuySignal JSON 스키마에 대응한다.
 *
 * <p>{@code orderEnabled}는 기본 false이며, 본 신호 단계에서 실주문을 의미하지 않는다.
 */
public final class BuySignal {

    private final String stockCode;
    private final Instant signalTime;
    private final SignalType signalType;
    private final long currentPrice;
    private final int candidateScore;
    private final double macd;
    private final double macdSignal;
    private final double macdHistogram;
    private final double rsi;
    private final double vwap;
    private final String reason;
    private final RejectReason rejectReason;
    private final boolean orderEnabled;

    private BuySignal(Builder b) {
        this.stockCode = b.stockCode;
        this.signalTime = b.signalTime;
        this.signalType = b.signalType;
        this.currentPrice = b.currentPrice;
        this.candidateScore = b.candidateScore;
        this.macd = b.macd;
        this.macdSignal = b.macdSignal;
        this.macdHistogram = b.macdHistogram;
        this.rsi = b.rsi;
        this.vwap = b.vwap;
        this.reason = b.reason;
        this.rejectReason = b.rejectReason;
        this.orderEnabled = b.orderEnabled;
    }

    public static Builder builder(String stockCode) {
        return new Builder(stockCode);
    }

    public String getStockCode() { return stockCode; }
    public Instant getSignalTime() { return signalTime; }
    public SignalType getSignalType() { return signalType; }
    public long getCurrentPrice() { return currentPrice; }
    public int getCandidateScore() { return candidateScore; }
    public double getMacd() { return macd; }
    public double getMacdSignal() { return macdSignal; }
    public double getMacdHistogram() { return macdHistogram; }
    public double getRsi() { return rsi; }
    public double getVwap() { return vwap; }
    public String getReason() { return reason; }
    public RejectReason getRejectReason() { return rejectReason; }
    public boolean isOrderEnabled() { return orderEnabled; }

    public boolean isBuyCandidate() {
        return signalType == SignalType.BUY_CANDIDATE;
    }

    public static final class Builder {
        private final String stockCode;
        private Instant signalTime = Instant.now();
        private SignalType signalType = SignalType.REJECTED;
        private long currentPrice;
        private int candidateScore;
        private double macd;
        private double macdSignal;
        private double macdHistogram;
        private double rsi;
        private double vwap;
        private String reason = "";
        private RejectReason rejectReason = RejectReason.NONE;
        private boolean orderEnabled = false;

        private Builder(String stockCode) {
            this.stockCode = stockCode;
        }

        public Builder signalTime(Instant v) { this.signalTime = v; return this; }
        public Builder signalType(SignalType v) { this.signalType = v; return this; }
        public Builder currentPrice(long v) { this.currentPrice = v; return this; }
        public Builder candidateScore(int v) { this.candidateScore = v; return this; }
        public Builder macd(double v) { this.macd = v; return this; }
        public Builder macdSignal(double v) { this.macdSignal = v; return this; }
        public Builder macdHistogram(double v) { this.macdHistogram = v; return this; }
        public Builder rsi(double v) { this.rsi = v; return this; }
        public Builder vwap(double v) { this.vwap = v; return this; }
        public Builder reason(String v) { this.reason = v; return this; }
        public Builder rejectReason(RejectReason v) { this.rejectReason = v; return this; }
        public Builder orderEnabled(boolean v) { this.orderEnabled = v; return this; }

        public BuySignal build() {
            return new BuySignal(this);
        }
    }

    @Override
    public String toString() {
        return "BuySignal{stockCode='" + stockCode + "', type=" + signalType
                + ", score=" + candidateScore + ", reason='" + reason
                + "', reject=" + rejectReason + ", orderEnabled=" + orderEnabled + '}';
    }
}
