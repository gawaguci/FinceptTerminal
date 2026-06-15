package kr.co.mango.stock.config;

/**
 * 전략 파라미터(불변 객체). 계획서 19.2 설정 예시를 코드 기본값으로 반영한다.
 *
 * <p>실제 운영 시에는 외부 설정 파일(application.yml) 또는 strategy_config 테이블에서 로딩한다.
 */
public final class StrategyConfig {

    // 지표 파라미터
    private final int macdFast;
    private final int macdSlow;
    private final int macdSignal;
    private final int rsiPeriod;

    // 후보/필터 파라미터
    private final long minTradeAmount;        // 당일 거래대금 하한(원)
    private final double volumeSpikeMultiplier; // 1분 거래량 / 20분 평균 배수 기준
    private final double maxRsiForEntry;       // 진입 허용 RSI 상한(과열 제외)
    private final double maxVwapGapRate;       // VWAP 대비 허용 이격률
    private final int maxSpreadTicks;          // 허용 호가 스프레드 틱

    // 후보 점수 등급 임계값
    private final int interestThreshold;       // 관심종목
    private final int watchThreshold;          // 매수타이밍 감시
    private final int strongThreshold;         // 강한 매수 후보

    private StrategyConfig(Builder b) {
        this.macdFast = b.macdFast;
        this.macdSlow = b.macdSlow;
        this.macdSignal = b.macdSignal;
        this.rsiPeriod = b.rsiPeriod;
        this.minTradeAmount = b.minTradeAmount;
        this.volumeSpikeMultiplier = b.volumeSpikeMultiplier;
        this.maxRsiForEntry = b.maxRsiForEntry;
        this.maxVwapGapRate = b.maxVwapGapRate;
        this.maxSpreadTicks = b.maxSpreadTicks;
        this.interestThreshold = b.interestThreshold;
        this.watchThreshold = b.watchThreshold;
        this.strongThreshold = b.strongThreshold;
    }

    /** 계획서 기본값으로 구성된 설정. */
    public static StrategyConfig defaults() {
        return new Builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getMacdFast() { return macdFast; }
    public int getMacdSlow() { return macdSlow; }
    public int getMacdSignal() { return macdSignal; }
    public int getRsiPeriod() { return rsiPeriod; }
    public long getMinTradeAmount() { return minTradeAmount; }
    public double getVolumeSpikeMultiplier() { return volumeSpikeMultiplier; }
    public double getMaxRsiForEntry() { return maxRsiForEntry; }
    public double getMaxVwapGapRate() { return maxVwapGapRate; }
    public int getMaxSpreadTicks() { return maxSpreadTicks; }
    public int getInterestThreshold() { return interestThreshold; }
    public int getWatchThreshold() { return watchThreshold; }
    public int getStrongThreshold() { return strongThreshold; }

    public static final class Builder {
        private int macdFast = 12;
        private int macdSlow = 26;
        private int macdSignal = 9;
        private int rsiPeriod = 14;
        private long minTradeAmount = 3_000_000_000L; // 30억 원
        private double volumeSpikeMultiplier = 1.5;
        private double maxRsiForEntry = 75.0;
        private double maxVwapGapRate = 0.03;
        private int maxSpreadTicks = 3;
        private int interestThreshold = 70;
        private int watchThreshold = 80;
        private int strongThreshold = 90;

        public Builder macdFast(int v) { this.macdFast = v; return this; }
        public Builder macdSlow(int v) { this.macdSlow = v; return this; }
        public Builder macdSignal(int v) { this.macdSignal = v; return this; }
        public Builder rsiPeriod(int v) { this.rsiPeriod = v; return this; }
        public Builder minTradeAmount(long v) { this.minTradeAmount = v; return this; }
        public Builder volumeSpikeMultiplier(double v) { this.volumeSpikeMultiplier = v; return this; }
        public Builder maxRsiForEntry(double v) { this.maxRsiForEntry = v; return this; }
        public Builder maxVwapGapRate(double v) { this.maxVwapGapRate = v; return this; }
        public Builder maxSpreadTicks(int v) { this.maxSpreadTicks = v; return this; }
        public Builder interestThreshold(int v) { this.interestThreshold = v; return this; }
        public Builder watchThreshold(int v) { this.watchThreshold = v; return this; }
        public Builder strongThreshold(int v) { this.strongThreshold = v; return this; }

        public StrategyConfig build() {
            return new StrategyConfig(this);
        }
    }
}
