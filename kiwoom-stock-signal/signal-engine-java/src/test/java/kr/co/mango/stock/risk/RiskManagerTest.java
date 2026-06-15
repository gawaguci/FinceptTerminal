package kr.co.mango.stock.risk;

import kr.co.mango.stock.order.OrderDecision;
import kr.co.mango.stock.signal.BuySignal;
import kr.co.mango.stock.signal.RejectReason;
import kr.co.mango.stock.signal.SignalType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RiskManagerTest {

    private static final long ACCOUNT = 100_000_000L;
    private static final long PRICE = 1000L;
    private static final long STOP = 980L;
    // 2026-06-15T04:00:00Z = 13:00 KST (컷오프 15:10 이전)
    private static final Instant NOW = Instant.parse("2026-06-15T04:00:00Z");

    private BuySignal candidate() {
        return BuySignal.builder("005930")
                .signalType(SignalType.BUY_CANDIDATE)
                .currentPrice(PRICE)
                .build();
    }

    private RiskPolicy enabledPolicy() {
        return RiskPolicy.builder().orderEnabled(true).build();
    }

    @Test
    void 기본정책은_주문비활성으로_거절한다() {
        RiskManager rm = new RiskManager(RiskPolicy.defaults());
        OrderDecision d = rm.evaluate(candidate(), ACCOUNT, PRICE, STOP,
                new PositionLimit(), NOW, true, true);
        assertFalse(d.isApproved());
        assertEquals(RejectReason.ORDER_DISABLED, d.getRejectReason());
    }

    @Test
    void 주문활성_정상조건이면_승인하고_수량을_산정한다() {
        RiskManager rm = new RiskManager(enabledPolicy());
        OrderDecision d = rm.evaluate(candidate(), ACCOUNT, PRICE, STOP,
                new PositionLimit(), NOW, true, true);
        assertTrue(d.isApproved());
        // 금액기준 5,000,000/1,000=5000, 리스크기준 300,000/20=15000 → min=5000
        assertEquals(5000, d.getQuantity());
        assertTrue(d.getOrderRequest().isPresent());
    }

    @Test
    void API_불안정이면_거절한다() {
        RiskManager rm = new RiskManager(enabledPolicy());
        OrderDecision d = rm.evaluate(candidate(), ACCOUNT, PRICE, STOP,
                new PositionLimit(), NOW, false, true);
        assertEquals(RejectReason.API_UNSTABLE, d.getRejectReason());
    }

    @Test
    void 신규매수_제한시간_이후면_거절한다() {
        RiskManager rm = new RiskManager(enabledPolicy());
        // 2026-06-15T06:30:00Z = 15:30 KST (컷오프 이후)
        Instant after = Instant.parse("2026-06-15T06:30:00Z");
        OrderDecision d = rm.evaluate(candidate(), ACCOUNT, PRICE, STOP,
                new PositionLimit(), after, true, true);
        assertEquals(RejectReason.AFTER_CUTOFF_TIME, d.getRejectReason());
    }

    @Test
    void 이미_보유중이면_거절한다() {
        RiskManager rm = new RiskManager(enabledPolicy());
        PositionLimit pos = new PositionLimit();
        pos.openPosition("005930");
        OrderDecision d = rm.evaluate(candidate(), ACCOUNT, PRICE, STOP, pos, NOW, true, true);
        assertEquals(RejectReason.ALREADY_HOLDING, d.getRejectReason());
    }

    @Test
    void 쿨다운_미경과면_거절한다() {
        RiskManager rm = new RiskManager(enabledPolicy());
        PositionLimit pos = new PositionLimit();
        pos.recordSignalTime("005930", NOW.minusSeconds(300)); // 5분 전, 쿨다운 20분
        OrderDecision d = rm.evaluate(candidate(), ACCOUNT, PRICE, STOP, pos, NOW, true, true);
        assertEquals(RejectReason.COOLDOWN_NOT_ELAPSED, d.getRejectReason());
    }

    @Test
    void 손절가가_현재가_이상이면_거절한다() {
        RiskManager rm = new RiskManager(enabledPolicy());
        OrderDecision d = rm.evaluate(candidate(), ACCOUNT, PRICE, PRICE,
                new PositionLimit(), NOW, true, true);
        assertEquals(RejectReason.INVALID_STOP_LOSS, d.getRejectReason());
    }

    @Test
    void 일일손실_한도_도달시_거절한다() {
        RiskManager rm = new RiskManager(enabledPolicy());
        PositionLimit pos = new PositionLimit();
        // maxDailyLossRate 0.01 → 1,000,000원. 손실 1,500,000 반영
        pos.closePosition("000000", -1_500_000L);
        OrderDecision d = rm.evaluate(candidate(), ACCOUNT, PRICE, STOP, pos, NOW, true, true);
        assertEquals(RejectReason.DAILY_LOSS_LIMIT, d.getRejectReason());
    }

    @Test
    void 후보가_아닌_신호는_거절한다() {
        RiskManager rm = new RiskManager(enabledPolicy());
        BuySignal rejected = BuySignal.builder("005930")
                .signalType(SignalType.REJECTED).currentPrice(PRICE).build();
        OrderDecision d = rm.evaluate(rejected, ACCOUNT, PRICE, STOP,
                new PositionLimit(), NOW, true, true);
        assertFalse(d.isApproved());
    }

    @Test
    void 주문수량_계산이_정확하다() {
        RiskManager rm = new RiskManager(enabledPolicy());
        // amountLimit=5,000,000/1000=5000, riskLimit=300,000/20=15000 → 5000
        assertEquals(5000, rm.calculateQuantity(ACCOUNT, PRICE, STOP));
        // 손절폭이 크면 리스크 기준 수량이 작아진다: riskPerShare=100 → 300,000/100=3000
        assertEquals(3000, rm.calculateQuantity(ACCOUNT, PRICE, 900));
    }
}
