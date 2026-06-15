package kr.co.mango.stock.indicator;

import kr.co.mango.stock.market.MinuteBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * MACD 계산기. 기본 파라미터는 Fast 12, Slow 26, Signal 9이다.
 *
 * <p>EMA는 초기 period개 값의 단순평균(SMA)으로 시드한 뒤 지수이동평균으로 갱신한다.
 * 데이터가 부족하면 결과를 반환하지 않는다.
 */
public final class MacdCalculator {

    public static final int DEFAULT_FAST = 12;
    public static final int DEFAULT_SLOW = 26;
    public static final int DEFAULT_SIGNAL = 9;

    private final int fastPeriod;
    private final int slowPeriod;
    private final int signalPeriod;

    public MacdCalculator() {
        this(DEFAULT_FAST, DEFAULT_SLOW, DEFAULT_SIGNAL);
    }

    public MacdCalculator(int fastPeriod, int slowPeriod, int signalPeriod) {
        if (fastPeriod <= 0 || slowPeriod <= 0 || signalPeriod <= 0) {
            throw new IllegalArgumentException("period 값은 모두 1 이상이어야 한다.");
        }
        if (fastPeriod >= slowPeriod) {
            throw new IllegalArgumentException("fastPeriod는 slowPeriod보다 작아야 한다.");
        }
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
        this.signalPeriod = signalPeriod;
    }

    /**
     * 최신 봉 기준 MACD 결과를 계산한다.
     *
     * @param bars 시간 오름차순 봉 리스트
     * @return MACD/Signal/Histogram, 데이터 부족 시 empty
     */
    public Optional<MacdResult> calculate(List<MinuteBar> bars) {
        if (bars == null) {
            return Optional.empty();
        }
        double[] closes = toCloses(bars);
        double[] macdLine = ema(closes, fastPeriod, slowPeriod);
        if (macdLine == null) {
            return Optional.empty();
        }
        double[] signalLine = ema(macdLine, signalPeriod);
        if (signalLine == null) {
            return Optional.empty();
        }
        // macdLine과 signalLine은 길이가 다르므로(끝 정렬) 각자의 마지막 값을 사용한다.
        double macd = macdLine[macdLine.length - 1];
        double signal = signalLine[signalLine.length - 1];
        return Optional.of(new MacdResult(macd, signal, macd - signal));
    }

    /**
     * Histogram 시계열(정의된 구간만, 시간 오름차순)을 반환한다.
     * 매수타이밍 엔진의 "Histogram 2봉 이상 증가" 판단에 사용한다.
     */
    public List<Double> histogramSeries(List<MinuteBar> bars) {
        List<Double> result = new ArrayList<>();
        if (bars == null) {
            return result;
        }
        double[] closes = toCloses(bars);
        double[] macdLine = ema(closes, fastPeriod, slowPeriod);
        if (macdLine == null) {
            return result;
        }
        double[] signalLine = ema(macdLine, signalPeriod);
        if (signalLine == null) {
            return result;
        }
        // signalLine은 macdLine 기준으로 산출되므로 끝 정렬되어 있다.
        int offset = macdLine.length - signalLine.length;
        for (int i = 0; i < signalLine.length; i++) {
            result.add(macdLine[offset + i] - signalLine[i]);
        }
        return result;
    }

    private static double[] toCloses(List<MinuteBar> bars) {
        double[] closes = new double[bars.size()];
        for (int i = 0; i < bars.size(); i++) {
            closes[i] = bars.get(i).getClose();
        }
        return closes;
    }

    /**
     * MACD선 = FastEMA - SlowEMA. SlowEMA가 정의되는 시점부터의 값만 반환한다.
     *
     * @return MACD선 배열(끝 정렬), 데이터 부족 시 null
     */
    private static double[] ema(double[] values, int fast, int slow) {
        if (values.length < slow) {
            return null;
        }
        double[] fastEma = emaFull(values, fast);
        double[] slowEma = emaFull(values, slow);
        int start = slow - 1; // SlowEMA가 처음 정의되는 인덱스
        int n = values.length - start;
        double[] macdLine = new double[n];
        for (int i = 0; i < n; i++) {
            macdLine[i] = fastEma[start + i] - slowEma[start + i];
        }
        return macdLine;
    }

    /**
     * 단일 기간 EMA. 정의되는 구간만(끝 정렬) 반환한다.
     *
     * @return EMA 배열, 데이터 부족 시 null
     */
    private static double[] ema(double[] values, int period) {
        if (values.length < period) {
            return null;
        }
        double k = 2.0 / (period + 1);
        double seed = 0.0;
        for (int i = 0; i < period; i++) {
            seed += values[i];
        }
        seed /= period;
        int n = values.length - period + 1;
        double[] out = new double[n];
        out[0] = seed;
        double prev = seed;
        for (int i = period; i < values.length; i++) {
            prev = values[i] * k + prev * (1 - k);
            out[i - period + 1] = prev;
        }
        return out;
    }

    /**
     * 단일 기간 EMA를 입력과 동일 길이로 반환한다(시드 이전 구간은 NaN).
     */
    private static double[] emaFull(double[] values, int period) {
        double[] out = new double[values.length];
        Arrays.fill(out, Double.NaN);
        if (values.length < period) {
            return out;
        }
        double k = 2.0 / (period + 1);
        double seed = 0.0;
        for (int i = 0; i < period; i++) {
            seed += values[i];
        }
        seed /= period;
        out[period - 1] = seed;
        double prev = seed;
        for (int i = period; i < values.length; i++) {
            prev = values[i] * k + prev * (1 - k);
            out[i] = prev;
        }
        return out;
    }
}
