package kr.co.mango.stock.repository;

import kr.co.mango.stock.signal.BuySignal;

import java.util.List;

/**
 * 매수 신호/거절 사유 로그 저장소(계획서 13장 buy_signal_log).
 *
 * <p>모든 신호는 판단 결과와 거절 사유까지 저장하여 검증 가능성을 확보한다.
 */
public interface SignalLogRepository {

    /** 신호를 저장한다. */
    void save(BuySignal signal);

    /** 특정 종목의 신호 이력을 시간 순으로 반환한다. */
    List<BuySignal> findByStockCode(String stockCode);

    /** 저장된 전체 신호 수. */
    int count();
}
