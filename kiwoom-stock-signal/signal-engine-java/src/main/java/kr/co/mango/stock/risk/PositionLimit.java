package kr.co.mango.stock.risk;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 보유/누적손익/쿨다운 등 리스크 런타임 상태 추적기.
 *
 * <p>RiskManager가 주문 전 검증에 사용한다. 단일 계좌 단위로 동작한다.
 * 스레드 안전하지 않으므로 단일 처리 스레드에서 사용한다.
 */
public final class PositionLimit {

    /** 현재 보유 종목 코드 */
    private final Set<String> holdings = new HashSet<>();
    /** 종목별 마지막 신호/진입 시각(쿨다운 판단용) */
    private final Map<String, Instant> lastSignalTime = new HashMap<>();
    /** 당일 실현 손익(원). 손실은 음수. */
    private long dailyRealizedPnl = 0L;

    public int getHoldingCount() {
        return holdings.size();
    }

    public boolean isHolding(String stockCode) {
        return holdings.contains(stockCode);
    }

    public void openPosition(String stockCode) {
        holdings.add(stockCode);
    }

    public void closePosition(String stockCode, long realizedPnl) {
        holdings.remove(stockCode);
        dailyRealizedPnl += realizedPnl;
    }

    public long getDailyRealizedPnl() {
        return dailyRealizedPnl;
    }

    /** 신호/진입 시각을 기록한다(쿨다운 시작점). */
    public void recordSignalTime(String stockCode, Instant time) {
        lastSignalTime.put(stockCode, time);
    }

    /**
     * 쿨다운 미경과 여부.
     *
     * @return 마지막 신호 후 cooldownMinutes가 지나지 않았으면 true
     */
    public boolean isWithinCooldown(String stockCode, Instant now, int cooldownMinutes) {
        Instant last = lastSignalTime.get(stockCode);
        if (last == null) {
            return false;
        }
        Duration elapsed = Duration.between(last, now);
        return elapsed.toMinutes() < cooldownMinutes;
    }

    /** 신규 종목을 추가로 보유할 수 있는지 여부. */
    public boolean canAddPosition(int maxHoldingCount) {
        return holdings.size() < maxHoldingCount;
    }

    /** 당일 손실이 한도에 도달했는지 여부. maxDailyLoss는 양수(원). */
    public boolean isDailyLossLimitReached(long maxDailyLoss) {
        return dailyRealizedPnl <= -maxDailyLoss;
    }
}
