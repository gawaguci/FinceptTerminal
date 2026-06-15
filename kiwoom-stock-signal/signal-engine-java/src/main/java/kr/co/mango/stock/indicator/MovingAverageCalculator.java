package kr.co.mango.stock.indicator;

import kr.co.mango.stock.market.MinuteBar;

import java.util.List;
import java.util.OptionalDouble;

/**
 * 단순 이동평균(SMA) 계산기.
 *
 * <p>입력 봉 리스트의 종가 기준 최근 period개 평균을 계산한다.
 * 봉 개수가 period보다 적으면 {@link OptionalDouble#empty()}를 반환한다.
 */
public final class MovingAverageCalculator {

    private final int period;

    public MovingAverageCalculator(int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("period는 1 이상이어야 한다. 입력값=" + period);
        }
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }

    /**
     * 최근 period개 봉의 종가 단순 이동평균을 계산한다.
     *
     * @param bars 시간 오름차순 봉 리스트
     * @return 이동평균값, 데이터 부족 시 empty
     */
    public OptionalDouble calculate(List<MinuteBar> bars) {
        if (bars == null || bars.size() < period) {
            return OptionalDouble.empty();
        }
        long sum = 0L;
        int from = bars.size() - period;
        for (int i = from; i < bars.size(); i++) {
            sum += bars.get(i).getClose();
        }
        return OptionalDouble.of((double) sum / period);
    }
}
