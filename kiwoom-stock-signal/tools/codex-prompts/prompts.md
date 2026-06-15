# Codex 프롬프트 모음 (계획서 7.3)

작업 단위를 작게 분리하고, 테스트 없는 코드 생성을 금지한다.

## RSI 계산기 (구현 완료 예시)
```
signal-engine-java 모듈에 RSI 계산기를 구현해줘.
1. 파일: src/main/java/kr/co/mango/stock/indicator/RsiCalculator.java
2. period 기본값 14, 생성자에서 변경 가능
3. 입력 List<MinuteBar>, 출력 OptionalDouble
4. 봉 부족 시 OptionalDouble.empty()
5. 주석 한국어
6. JUnit 테스트 필수, mvn test 통과
```

## TR 제한 관리 (Delphi, 구현 완료 예시)
```
gateway-delphi 모듈에 TR 호출 제한 관리 클래스를 작성해줘.
1. 파일: src/TrRateLimiter.pas
2. 초당 5, 분당 100, 시간당 1000 제한을 설정값으로
3. CanRequest, MarkRequest, NextAvailableTime 제공
4. 요청 유형 enum 추가
5. 주석 한국어
```

## 다음 작업 후보
```
- VwapCalculator에 시간가중 옵션 추가 + 테스트
- MinuteBarBuilder를 3분/5분봉 동시 생성하도록 멀티 인터벌 래퍼 작성 + 테스트
- SignalLogRepository의 JDBC(PostgreSQL) 구현 + 통합 테스트(testcontainers)
- ReplayRunner: 과거 분봉 CSV를 읽어 파이프라인 재생 + 성과 집계
- ConditionSearchManager.pas: 조건명 파싱 및 실시간 편입/이탈 처리(Delphi)
```
