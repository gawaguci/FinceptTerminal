package kr.co.mango.stock.scanner;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * 후보 종목 점수화 결과(불변 객체).
 */
public final class CandidateScore {

    private final String stockCode;
    private final int total;
    private final CandidateGrade grade;
    private final Set<CandidateReason> matched;

    public CandidateScore(String stockCode, int total, CandidateGrade grade, Set<CandidateReason> matched) {
        this.stockCode = stockCode;
        this.total = total;
        this.grade = grade;
        // 불변 복사
        this.matched = matched.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(matched));
    }

    public String getStockCode() {
        return stockCode;
    }

    public int getTotal() {
        return total;
    }

    public CandidateGrade getGrade() {
        return grade;
    }

    public Set<CandidateReason> getMatched() {
        return matched;
    }

    /** 충족 항목 라벨을 사람이 읽을 수 있는 사유 문자열로 결합한다. */
    public String reasonText() {
        if (matched.isEmpty()) {
            return "충족 항목 없음";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (CandidateReason reason : matched) {
            joiner.add(reason.getLabel());
        }
        return joiner.toString();
    }

    @Override
    public String toString() {
        return "CandidateScore{stockCode='" + stockCode + "', total=" + total
                + ", grade=" + grade + ", matched=" + matched + '}';
    }
}
