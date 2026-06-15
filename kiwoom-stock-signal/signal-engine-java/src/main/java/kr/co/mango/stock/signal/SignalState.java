package kr.co.mango.stock.signal;

/**
 * 매수 신호 상태(계획서 11.5 상태 머신).
 */
public enum SignalState {
    WAIT,
    WATCH,
    READY,
    BUY_SIGNAL,
    ORDER_WAIT,
    ORDER_BLOCKED,
    ORDER_SENT,
    POSITION_OPEN,
    EXIT_READY,
    POSITION_CLOSED
}
