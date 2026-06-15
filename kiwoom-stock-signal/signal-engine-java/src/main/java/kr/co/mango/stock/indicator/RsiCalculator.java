package kr.co.mango.stock.indicator;

import kr.co.mango.stock.market.MinuteBar;

import java.util.List;
import java.util.OptionalDouble;

/**
 * RSI(상대강도지수) 계산기. Wilder 평활(smoothing) 방식을 사용한다.
 *
 * <p>period 기본값은 14이며 생성자에서 변경 가능하다.
 * 종가 변화량 기준으로 평균 상승/하락폭을 구한 뒤 최신 봉의 RSI를 반환한다.
 * 봉 개수가 period+1 미만이면 {@link OptionalDouble#empty()}를 반환한다.
 */
public final class RsiCalculator {

    public static final int DEFAULT_PERIOD = 14;

    private final int period;

    public RsiCalculator() {
        this(DEFAULT_PERIOD);
    }

    public RsiCalculator(int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("period는 1 이상이어야 한다. 입력값=" + period);
        }
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }

    /**
     * 최신 봉 기준 RSI를 계산한다.
     *
     * @param bars 시간 오름차순 봉 리스트
     * @return RSI 값(0~100), 데이터 부족 시 empty
     */
    public OptionalDouble calculate(List<MinuteBar> bars) {
        if (bars == null || bars.size() < period + 1) {
            return OptionalDouble.empty();
        }

        // 1) 초기 period개 변화량으로 평균 상승/하락폭 산정
        double avgGain = 0.0;
        double avgLoss = 0.0;
        for (int i = 1; i <= period; i++) {
            double change = bars.get(i).getClose() - bars.get(i - 1).getClose();
            if (change >= 0) {
                avgGain += change;
            } else {
                avgLoss += -change;
            }
        }
        avgGain /= period;
        avgLoss /= period;

        // 2) 이후 봉에 Wilder 평활 적용
        for (int i = period + 1; i < bars.size(); i++) {
            double change = bars.get(i).getClose() - bars.get(i - 1).getClose();
            double gain = change > 0 ? change : 0.0;
            double loss = change < 0 ? -change : 0.0;
            avgGain = (avgGain * (period - 1) + gain) / period;
            avgLoss = (avgLoss * (period - 1) + loss) / period;
        }

        if (avgLoss == 0.0) {
            // 하락이 전혀 없으면 RSI=100으로 본다.
            return OptionalDouble.of(100.0);
        }
        double rs = avgGain / avgLoss;
        double rsi = 100.0 - (100.0 / (1.0 + rs));
        return OptionalDouble.of(rsi);
    }
}
