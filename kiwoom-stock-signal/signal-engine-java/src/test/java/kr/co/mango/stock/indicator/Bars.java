package kr.co.mango.stock.indicator;

import kr.co.mango.stock.market.BarInterval;
import kr.co.mango.stock.market.MinuteBar;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 테스트용 분봉 생성 헬퍼.
 */
final class Bars {

    private Bars() {
    }

    /** 종가 배열로부터 1분봉 리스트를 만든다(OHLC=종가, 거래량 기본 1000). */
    static List<MinuteBar> ofCloses(long... closes) {
        return ofClosesWithVolume(1000L, closes);
    }

    static List<MinuteBar> ofClosesWithVolume(long volume, long... closes) {
        List<MinuteBar> bars = new ArrayList<>();
        Instant start = Instant.parse("2026-06-15T00:00:00Z");
        for (int i = 0; i < closes.length; i++) {
            long c = closes[i];
            bars.add(new MinuteBar("005930", BarInterval.M1, start.plusSeconds(i * 60L),
                    c, c, c, c, volume, c * volume, true));
        }
        return bars;
    }
}
