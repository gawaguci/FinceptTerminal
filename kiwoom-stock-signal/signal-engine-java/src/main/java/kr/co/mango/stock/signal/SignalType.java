package kr.co.mango.stock.signal;

/**
 * 매수타이밍 판단 결과 유형.
 */
public enum SignalType {
    /** 매수 후보(조건 충족). 단, 실주문은 RiskManager/수동승인 별도. */
    BUY_CANDIDATE,
    /** 거절(조건 미충족 또는 제외 조건 해당) */
    REJECTED
}
