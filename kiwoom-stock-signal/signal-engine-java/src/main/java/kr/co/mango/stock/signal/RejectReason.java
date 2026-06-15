package kr.co.mango.stock.signal;

/**
 * 매수 신호 거절 사유(계획서 11.4 제외 조건 및 리스크 차단).
 */
public enum RejectReason {

    NONE("거절 없음"),
    SCORE_TOO_LOW("후보 점수 미달"),
    TREND_CONDITION_NOT_MET("추세형 매수 조건 미충족"),
    RSI_OVERHEATED("RSI 과열(상한 초과)"),
    VWAP_GAP_TOO_LARGE("VWAP 대비 이격 과다"),
    SPREAD_TOO_WIDE("호가 스프레드 과다"),
    API_UNSTABLE("API 연결 불안정 또는 주문 상태 미확인"),
    COOLDOWN_NOT_ELAPSED("동일 종목 쿨다운 미경과"),
    DAILY_LOSS_LIMIT("1일 최대 손실 도달"),
    AFTER_CUTOFF_TIME("장마감 근접 신규매수 제한"),
    ALREADY_HOLDING("동일 종목 보유 중(중복매수 제한)"),
    MAX_HOLDING_REACHED("최대 보유 종목 수 초과"),
    ORDER_DISABLED("주문 기능 비활성"),
    INVALID_STOP_LOSS("손절가 산정 불가"),
    INSUFFICIENT_QUANTITY("주문 수량 0 이하"),
    INSUFFICIENT_ACCOUNT("계좌 잔고/예수금 부족");

    private final String label;

    RejectReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
