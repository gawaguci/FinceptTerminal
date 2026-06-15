package kr.co.mango.stock.order;

import java.time.Instant;
import java.util.Objects;

/**
 * 주문 요청(불변 객체). RiskManager 통과 및 승인 후 Gateway로 전달된다.
 *
 * <p>가격은 원 단위 정수이며, 0이면 시장가를 의미한다(초기 운영은 지정가 권장).
 */
public final class OrderRequest {

    private final String stockCode;
    private final OrderSide side;
    private final int quantity;
    private final long price;
    private final boolean marketOrder;
    private final Instant createdAt;

    public OrderRequest(String stockCode, OrderSide side, int quantity, long price,
                        boolean marketOrder, Instant createdAt) {
        this.stockCode = Objects.requireNonNull(stockCode, "stockCode는 필수이다");
        this.side = Objects.requireNonNull(side, "side는 필수이다");
        if (quantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 1 이상이어야 한다. 입력값=" + quantity);
        }
        this.quantity = quantity;
        this.price = price;
        this.marketOrder = marketOrder;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getStockCode() { return stockCode; }
    public OrderSide getSide() { return side; }
    public int getQuantity() { return quantity; }
    public long getPrice() { return price; }
    public boolean isMarketOrder() { return marketOrder; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "OrderRequest{stockCode='" + stockCode + "', side=" + side
                + ", qty=" + quantity + ", price=" + price + ", market=" + marketOrder + '}';
    }
}
