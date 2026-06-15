package kr.co.mango.stock.repository;

import kr.co.mango.stock.signal.BuySignal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 인메모리 신호 로그 저장소(MVP/테스트용).
 *
 * <p>운영 환경에서는 JDBC 기반 PostgreSQL/SQLite 구현으로 교체한다.
 * 인터페이스로 분리하여 DB 의존 없이도 엔진 테스트가 가능하다.
 */
public final class InMemorySignalLogRepository implements SignalLogRepository {

    private final List<BuySignal> store = new ArrayList<>();

    @Override
    public void save(BuySignal signal) {
        store.add(signal);
    }

    @Override
    public List<BuySignal> findByStockCode(String stockCode) {
        return store.stream()
                .filter(s -> s.getStockCode().equals(stockCode))
                .collect(Collectors.toList());
    }

    @Override
    public int count() {
        return store.size();
    }

    /** 저장된 전체 신호(읽기 전용 복사본). */
    public List<BuySignal> findAll() {
        return new ArrayList<>(store);
    }
}
