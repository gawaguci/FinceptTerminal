package kr.co.mango.stock.scanner;

/**
 * 후보 종목 점수화 항목과 가중치(계획서 10.3).
 *
 * <p>각 항목 충족 시 해당 점수를 합산하여 후보 점수를 산정한다.
 */
public enum CandidateReason {

    TRADE_AMOUNT(20, "거래대금 기준 충족"),
    VOLUME_SPIKE(20, "1분 거래량 > 20분 평균 × 배수"),
    PRICE_ABOVE_VWAP(15, "현재가 > VWAP"),
    TREND(15, "5분봉 종가 > 20봉 이동평균"),
    BREAKOUT(15, "직전 고점/당일 고가 돌파"),
    MACD(10, "MACD선 > Signal선, Histogram 증가"),
    RSI(5, "RSI 50 이상");

    private final int weight;
    private final String label;

    CandidateReason(int weight, String label) {
        this.weight = weight;
        this.label = label;
    }

    public int getWeight() {
        return weight;
    }

    public String getLabel() {
        return label;
    }
}
