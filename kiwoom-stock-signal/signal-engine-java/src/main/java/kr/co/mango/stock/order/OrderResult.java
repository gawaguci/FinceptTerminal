package kr.co.mango.stock.order;

/**
 * 주문 처리 결과(불변 객체). Gateway의 체결/접수 이벤트를 정규화한다.
 */
public final class OrderResult {

    /** 주문 상태. */
    public enum Status {
        ACCEPTED,   // 접수
        FILLED,     // 전량 체결
        PARTIAL,    // 일부 체결
        REJECTED,   // 거부
        CANCELLED   // 취소
    }

    private final String orderNo;
    private final String stockCode;
    private final Status status;
    private final int filledQuantity;
    private final long filledPrice;
    private final String message;

    public OrderResult(String orderNo, String stockCode, Status status,
                       int filledQuantity, long filledPrice, String message) {
        this.orderNo = orderNo;
        this.stockCode = stockCode;
        this.status = status;
        this.filledQuantity = filledQuantity;
        this.filledPrice = filledPrice;
        this.message = message;
    }

    public String getOrderNo() { return orderNo; }
    public String getStockCode() { return stockCode; }
    public Status getStatus() { return status; }
    public int getFilledQuantity() { return filledQuantity; }
    public long getFilledPrice() { return filledPrice; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return "OrderResult{orderNo='" + orderNo + "', stockCode='" + stockCode
                + "', status=" + status + ", filledQty=" + filledQuantity + '}';
    }
}
