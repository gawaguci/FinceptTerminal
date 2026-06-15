package kr.co.mango.stock.scanner;

import kr.co.mango.stock.config.StrategyConfig;
import kr.co.mango.stock.market.MarketSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RealtimeScannerTest {

    private final RealtimeScanner scanner = new RealtimeScanner(StrategyConfig.defaults());

    @Test
    void 모든_조건_충족시_100점_STRONG_등급이다() {
        MarketSnapshot s = MarketSnapshot.builder("005930")
                .currentPrice(1000)
                .dailyTradeAmount(5_000_000_000L)
                .oneMinuteVolume(200)
                .avg20MinuteVolume(100)
                .vwap(900)
                .fiveMinuteClose(1000)
                .ma20OnFiveMinute(950)
                .previousHigh(990)
                .macd(2.0).macdSignal(1.0).histogramRising(true)
                .rsi(55)
                .build();

        CandidateScore score = scanner.score(s);
        assertEquals(100, score.getTotal());
        assertEquals(CandidateGrade.STRONG, score.getGrade());
        assertEquals(7, score.getMatched().size());
    }

    @Test
    void 조건_미충족시_0점_NONE_등급이다() {
        MarketSnapshot s = MarketSnapshot.builder("005930")
                .currentPrice(100)
                .dailyTradeAmount(0)
                .oneMinuteVolume(0)
                .avg20MinuteVolume(0)
                .vwap(200)
                .fiveMinuteClose(100)
                .ma20OnFiveMinute(200)
                .previousHigh(0)
                .macd(0).macdSignal(0).histogramRising(false)
                .rsi(40)
                .build();

        CandidateScore score = scanner.score(s);
        assertEquals(0, score.getTotal());
        assertEquals(CandidateGrade.NONE, score.getGrade());
    }

    @Test
    void 거래량_급증_단독_점수는_20점이다() {
        MarketSnapshot s = MarketSnapshot.builder("005930")
                .currentPrice(100)
                .dailyTradeAmount(0)
                .oneMinuteVolume(200)        // 100 * 1.5 = 150 초과
                .avg20MinuteVolume(100)
                .vwap(200)                   // 가격 위치 미충족
                .fiveMinuteClose(100)
                .ma20OnFiveMinute(200)
                .previousHigh(0)
                .macd(0).macdSignal(0).histogramRising(false)
                .rsi(40)
                .build();

        CandidateScore score = scanner.score(s);
        assertEquals(20, score.getTotal());
        assertTrue(score.getMatched().contains(CandidateReason.VOLUME_SPIKE));
    }
}
