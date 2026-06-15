package kr.co.mango.stock.indicator;

import kr.co.mango.stock.market.MinuteBar;

import java.util.List;
import java.util.OptionalDouble;

/**
 * VWAP(거래량가중평균가격) 계산기.
 *
 * <p>당일 누적 기준으로 VWAP = Σ(거래대금) / Σ(거래량) 으로 계산한다.
 * 봉의 거래대금(tradeAmount)이 0보다 큰 경우 이를 사용하고,
 * 거래대금 정보가 없으면 종가×거래량으로 근사한다.
 */
public final class VwapCalculator {

    /**
     * 봉 리스트의 당일 누적 VWAP을 계산한다.
     *
     * @param bars 시간 오름차순 봉 리스트(당일 봉)
     * @return VWAP, 누적 거래량이 0이면 empty
     */
    public OptionalDouble calculate(List<MinuteBar> bars) {
        if (bars == null || bars.isEmpty()) {
            return OptionalDouble.empty();
        }
        double sumAmount = 0.0;
        double sumVolume = 0.0;
        for (MinuteBar bar : bars) {
            long volume = bar.getVolume();
            if (volume <= 0) {
                continue;
            }
            long amount = bar.getTradeAmount();
            if (amount <= 0) {
                // 거래대금 미제공 시 종가×거래량으로 근사
                amount = bar.getClose() * volume;
            }
            sumAmount += amount;
            sumVolume += volume;
        }
        if (sumVolume <= 0.0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(sumAmount / sumVolume);
    }
}
