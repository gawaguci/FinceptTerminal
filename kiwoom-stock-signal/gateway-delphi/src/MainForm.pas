unit MainForm;

{
  [스텁] 운영 대시보드 메인 폼 (계획서 17장)

  표시 영역:
    - 연결 상태: 키움 로그인 / 실시간 수신 / Java 엔진 / DB
    - 후보 종목: 종목코드, 종목명, 현재가, 등락률, 거래대금, 점수
    - 지표: MACD, Signal, Histogram, RSI, VWAP, 이동평균
    - 신호: 신호 시간, 매수 후보 여부, 거절 사유
    - 리스크: 보유 종목 수, 당일 손익, 주문 가능 여부
    - 로그: API 오류, TR 제한, 주문 차단, 연결 이벤트

  상태 구분(필수): 정상 / 주의 / 차단 / 오류

  주의: UI 스레드와 데이터 처리 스레드를 분리한다.
  TODO: Windows + Delphi XE7 환경에서 폼/컴포넌트 구현.
}

interface

implementation

end.
