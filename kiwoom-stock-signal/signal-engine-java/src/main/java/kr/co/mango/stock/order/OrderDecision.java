package kr.co.mango.stock.order;

import kr.co.mango.stock.signal.RejectReason;

import java.util.Optional;

/**
 * RiskManager의 주문 가부 판단 결과(불변 객체).
 *
 * <p>승인 시 주문 수량과 (선택적으로) 생성된 주문 요청을 포함한다.
 * 거절 시 거절 사유를 포함하며, orderRequest는 비어 있다.
 */
public final class OrderDecision {

    private final boolean approved;
    private final int quantity;
    private final RejectReason rejectReason;
    private final String message;
    private final OrderRequest orderRequest;

    private OrderDecision(boolean approved, int quantity, RejectReason rejectReason,
                          String message, OrderRequest orderRequest) {
        this.approved = approved;
        this.quantity = quantity;
        this.rejectReason = rejectReason;
        this.message = message;
        this.orderRequest = orderRequest;
    }

    public static OrderDecision approved(int quantity, OrderRequest request, String message) {
        return new OrderDecision(true, quantity, RejectReason.NONE, message, request);
    }

    public static OrderDecision rejected(RejectReason reason) {
        return new OrderDecision(false, 0, reason, reason.getLabel(), null);
    }

    public boolean isApproved() {
        return approved;
    }

    public int getQuantity() {
        return quantity;
    }

    public RejectReason getRejectReason() {
        return rejectReason;
    }

    public String getMessage() {
        return message;
    }

    public Optional<OrderRequest> getOrderRequest() {
        return Optional.ofNullable(orderRequest);
    }

    @Override
    public String toString() {
        return "OrderDecision{approved=" + approved + ", quantity=" + quantity
                + ", rejectReason=" + rejectReason + ", message='" + message + "'}";
    }
}
