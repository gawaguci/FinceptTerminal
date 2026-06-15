unit OrderGatewayUnit;

{
  [스텁] OrderGatewayUnit

  본 유닛은 Windows + Delphi XE7 + 키움 OpenAPI+ 환경에서 구현한다.
  계획서 8장(Kiwoom Gateway 설계)을 참고하여 인터페이스를 채운다.

  공통 원칙:
    - 키움 OCX 직접 호출은 본 Gateway 모듈 내부로 한정한다.
    - 이벤트 핸들러는 무거운 계산을 하지 않고 DTO 변환 후 큐에 적재한다.
    - 주문 기본값은 항상 비활성(orderEnabled=false)이다.
    - 민감정보(계좌/인증/비밀번호)는 코드에 포함하지 않는다.

  TODO: 구현 필요.
}

interface

implementation

end.
