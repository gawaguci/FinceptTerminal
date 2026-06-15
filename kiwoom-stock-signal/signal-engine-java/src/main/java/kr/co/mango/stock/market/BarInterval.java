package kr.co.mango.stock.market;

/**
 * 분봉 집계 단위.
 * 계획서 기준 1분/3분/5분봉을 사용한다(1분봉 진입, 5분봉 추세 확인).
 */
public enum BarInterval {
    M1(1),
    M3(3),
    M5(5);

    /** 봉 길이(분) */
    private final int minutes;

    BarInterval(int minutes) {
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }
}
