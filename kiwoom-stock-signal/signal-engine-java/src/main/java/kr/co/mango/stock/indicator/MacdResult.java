package kr.co.mango.stock.indicator;

/**
 * MACD 계산 결과(불변 객체).
 *
 * <ul>
 *   <li>macd: MACD선 = FastEMA - SlowEMA</li>
 *   <li>signal: MACD Signal선 = MACD선의 EMA</li>
 *   <li>histogram: MACD - Signal</li>
 * </ul>
 */
public final class MacdResult {

    private final double macd;
    private final double signal;
    private final double histogram;

    public MacdResult(double macd, double signal, double histogram) {
        this.macd = macd;
        this.signal = signal;
        this.histogram = histogram;
    }

    public double getMacd() {
        return macd;
    }

    public double getSignal() {
        return signal;
    }

    public double getHistogram() {
        return histogram;
    }

    @Override
    public String toString() {
        return "MacdResult{macd=" + macd + ", signal=" + signal + ", histogram=" + histogram + '}';
    }
}
