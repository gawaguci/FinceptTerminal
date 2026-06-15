package kr.co.mango.stock.scanner;

/**
 * 후보 점수에 따른 등급(계획서 10.3).
 */
public enum CandidateGrade {
    /** 70점 미만: 후보 아님 */
    NONE,
    /** 70점 이상: 관심종목 */
    INTEREST,
    /** 80점 이상: 매수타이밍 감시 */
    WATCH,
    /** 90점 이상: 강한 매수 후보(단, RiskManager 통과 필요) */
    STRONG
}
