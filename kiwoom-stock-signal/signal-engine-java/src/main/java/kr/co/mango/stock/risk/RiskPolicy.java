package kr.co.mango.stock.risk;

import java.time.LocalTime;

/**
 * 리스크 정책(불변 객체). 계획서 12.1 기본 리스크 정책을 반영한다.
 *
 * <p>주문 기능(orderEnabled) 기본값은 false이다(자동주문 금지 원칙).
 */
public final class RiskPolicy {

    private final boolean orderEnabled;
    private final double maxPositionRate;     // 종목당 최대 투입 비율(총자산 대비)
    private final double maxDailyLossRate;    // 1일 최대 손실 비율(총자산 대비)
    private final double maxLossPerTradeRate; // 1회 최대 손실 비율(총자산 대비)
    private final int maxHoldingCount;       // 최대 보유 종목 수
    private final int cooldownMinutes;       // 동일 종목 쿨다운(분)
    private final LocalTime newBuyCutoffTime; // 신규매수 제한 시각

    private RiskPolicy(Builder b) {
        this.orderEnabled = b.orderEnabled;
        this.maxPositionRate = b.maxPositionRate;
        this.maxDailyLossRate = b.maxDailyLossRate;
        this.maxLossPerTradeRate = b.maxLossPerTradeRate;
        this.maxHoldingCount = b.maxHoldingCount;
        this.cooldownMinutes = b.cooldownMinutes;
        this.newBuyCutoffTime = b.newBuyCutoffTime;
    }

    /** 계획서 기본값으로 구성된 정책(주문 비활성). */
    public static RiskPolicy defaults() {
        return new Builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isOrderEnabled() { return orderEnabled; }
    public double getMaxPositionRate() { return maxPositionRate; }
    public double getMaxDailyLossRate() { return maxDailyLossRate; }
    public double getMaxLossPerTradeRate() { return maxLossPerTradeRate; }
    public int getMaxHoldingCount() { return maxHoldingCount; }
    public int getCooldownMinutes() { return cooldownMinutes; }
    public LocalTime getNewBuyCutoffTime() { return newBuyCutoffTime; }

    public static final class Builder {
        private boolean orderEnabled = false; // 기본값: 주문 비활성
        private double maxPositionRate = 0.05;
        private double maxDailyLossRate = 0.01;
        private double maxLossPerTradeRate = 0.003;
        private int maxHoldingCount = 5;
        private int cooldownMinutes = 20;
        private LocalTime newBuyCutoffTime = LocalTime.of(15, 10);

        public Builder orderEnabled(boolean v) { this.orderEnabled = v; return this; }
        public Builder maxPositionRate(double v) { this.maxPositionRate = v; return this; }
        public Builder maxDailyLossRate(double v) { this.maxDailyLossRate = v; return this; }
        public Builder maxLossPerTradeRate(double v) { this.maxLossPerTradeRate = v; return this; }
        public Builder maxHoldingCount(int v) { this.maxHoldingCount = v; return this; }
        public Builder cooldownMinutes(int v) { this.cooldownMinutes = v; return this; }
        public Builder newBuyCutoffTime(LocalTime v) { this.newBuyCutoffTime = v; return this; }

        public RiskPolicy build() {
            return new RiskPolicy(this);
        }
    }
}
