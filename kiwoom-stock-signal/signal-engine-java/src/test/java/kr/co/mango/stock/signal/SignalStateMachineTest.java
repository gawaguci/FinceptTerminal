package kr.co.mango.stock.signal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignalStateMachineTest {

    @Test
    void 초기상태는_WAIT다() {
        assertEquals(SignalState.WAIT, new SignalStateMachine().getState());
    }

    @Test
    void 정상_매수흐름_전이가_가능하다() {
        SignalStateMachine sm = new SignalStateMachine();
        sm.transit(SignalState.WATCH);
        sm.transit(SignalState.READY);
        sm.transit(SignalState.BUY_SIGNAL);
        sm.transit(SignalState.ORDER_WAIT);
        sm.transit(SignalState.ORDER_SENT);
        sm.transit(SignalState.POSITION_OPEN);
        sm.transit(SignalState.EXIT_READY);
        sm.transit(SignalState.POSITION_CLOSED);
        sm.transit(SignalState.WAIT);
        assertEquals(SignalState.WAIT, sm.getState());
    }

    @Test
    void 주문차단_흐름도_가능하다() {
        SignalStateMachine sm = new SignalStateMachine();
        sm.transit(SignalState.WATCH);
        sm.transit(SignalState.READY);
        sm.transit(SignalState.BUY_SIGNAL);
        sm.transit(SignalState.ORDER_WAIT);
        sm.transit(SignalState.ORDER_BLOCKED);
        sm.transit(SignalState.WAIT);
        assertEquals(SignalState.WAIT, sm.getState());
    }

    @Test
    void 허용되지_않은_전이는_예외다() {
        SignalStateMachine sm = new SignalStateMachine();
        assertThrows(IllegalStateException.class, () -> sm.transit(SignalState.READY));
    }
}
