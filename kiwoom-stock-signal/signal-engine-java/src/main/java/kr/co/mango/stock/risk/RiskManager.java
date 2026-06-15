package kr.co.mango.stock.risk;

import kr.co.mango.stock.order.OrderDecision;
import kr.co.mango.stock.order.OrderRequest;
import kr.co.mango.stock.order.OrderSide;
import kr.co.mango.stock.signal.BuySignal;
import kr.co.mango.stock.signal.RejectReason;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * 리스크 매니저(계획서 12장). 주문 전 검증과 주문 수량 산정을 담당한다.
 *
 * <p>검증 순서는 12.2를 따른다. 어느 단계든 실패하면 거절(OrderDecision.rejected)을 반환한다.
 * 주문 수량은 12.3의 금액/리스크 기준 중 작은 값으로 산정한다.
 *
 * <p>RiskManager 외부에서 주문 가부를 임의로 판단하지 않는다(코드 표준 20.2).
 */
public final class RiskManager {

    /** 한국 시장 기준 시간대. */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RiskPolicy policy;
    private final ZoneId zone;

    public RiskManager(RiskPolicy policy) {
        this(policy, KST);
    }

    public RiskManager(RiskPolicy policy, ZoneId zone) {
        this.policy = policy;
        this.zone = zone;
    }

    /**
     * 매수 후보 신호에 대해 주문 가부를 판단한다.
     *
     * @param signal        매수 후보 신호(BUY_CANDIDATE 이어야 함)
     * @param accountValue  총자산(원)
     * @param currentPrice  현재가(원)
     * @param stopLossPrice 손절가(원). currentPrice보다 작아야 함
     * @param position      리스크 런타임 상태
     * @param now           현재 시각
     * @param apiConnected  키움 연결 정상 여부
     * @param realtimeOk    실시간 시세 정상 수신 여부
     */
    public OrderDecision evaluate(BuySignal signal,
                                  long accountValue,
                                  long currentPrice,
                                  long stopLossPrice,
                                  PositionLimit position,
                                  Instant now,
                                  boolean apiConnected,
                                  boolean realtimeOk) {

        // 후보가 아닌 신호는 주문 대상이 아니다.
        if (signal == null || !signal.isBuyCandidate()) {
            return OrderDecision.rejected(RejectReason.TREND_CONDITION_NOT_MET);
        }
        // 1) 주문 기능 활성화 여부
        if (!policy.isOrderEnabled()) {
            return OrderDecision.rejected(RejectReason.ORDER_DISABLED);
        }
        // 2~3) API 연결 및 실시간 수신 상태
        if (!apiConnected || !realtimeOk) {
            return OrderDecision.rejected(RejectReason.API_UNSTABLE);
        }
        // 4) 계좌 잔고 확인
        if (accountValue <= 0) {
            return OrderDecision.rejected(RejectReason.INSUFFICIENT_ACCOUNT);
        }
        // 신규매수 제한 시간(15:10 이후 금지)
        LocalTime nowTime = LocalTime.ofInstant(now, zone);
        if (!nowTime.isBefore(policy.getNewBuyCutoffTime())) {
            return OrderDecision.rejected(RejectReason.AFTER_CUTOFF_TIME);
        }
        // 6) 1일 누적 손익 확인
        long maxDailyLoss = (long) (accountValue * policy.getMaxDailyLossRate());
        if (position.isDailyLossLimitReached(maxDailyLoss)) {
            return OrderDecision.rejected(RejectReason.DAILY_LOSS_LIMIT);
        }
        // 7) 동일 종목 보유 여부(중복매수 제한)
        if (position.isHolding(signal.getStockCode())) {
            return OrderDecision.rejected(RejectReason.ALREADY_HOLDING);
        }
        // 최대 보유 종목 수
        if (!position.canAddPosition(policy.getMaxHoldingCount())) {
            return OrderDecision.rejected(RejectReason.MAX_HOLDING_REACHED);
        }
        // 8) 동일 종목 쿨다운
        if (position.isWithinCooldown(signal.getStockCode(), now, policy.getCooldownMinutes())) {
            return OrderDecision.rejected(RejectReason.COOLDOWN_NOT_ELAPSED);
        }
        // 10) 손절가 산정 가능 여부
        if (stopLossPrice <= 0 || stopLossPrice >= currentPrice) {
            return OrderDecision.rejected(RejectReason.INVALID_STOP_LOSS);
        }
        // 11) 주문 수량 산정
        int quantity = calculateQuantity(accountValue, currentPrice, stopLossPrice);
        if (quantity <= 0) {
            return OrderDecision.rejected(RejectReason.INSUFFICIENT_QUANTITY);
        }
        // 12) 주문 요청 생성(지정가, 시장가 금지)
        OrderRequest request = new OrderRequest(
                signal.getStockCode(), OrderSide.BUY, quantity, currentPrice, false, now);
        return OrderDecision.approved(quantity, request,
                "리스크 검증 통과: 수량 " + quantity + "주");
    }

    /**
     * 주문 수량 산정(계획서 12.3).
     * 금액 기준 수량과 리스크 기준 수량 중 작은 값을 사용한다.
     */
    public int calculateQuantity(long accountValue, long currentPrice, long stopLossPrice) {
        if (currentPrice <= 0) {
            return 0;
        }
        long riskPerShare = currentPrice - stopLossPrice;
        if (riskPerShare <= 0) {
            return 0;
        }
        double amountLimit = accountValue * policy.getMaxPositionRate();
        double lossLimit = accountValue * policy.getMaxLossPerTradeRate();

        long quantityByAmount = (long) Math.floor(amountLimit / currentPrice);
        long quantityByRisk = (long) Math.floor(lossLimit / riskPerShare);
        long quantity = Math.min(quantityByAmount, quantityByRisk);

        if (quantity <= 0) {
            return 0;
        }
        // int 범위 방어
        return (int) Math.min(quantity, Integer.MAX_VALUE);
    }
}
