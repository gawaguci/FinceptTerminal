unit ScreenNoManager;

{
  화면번호 관리 유닛 (계획서 8.4)

  키움 실시간 제한:
    - 화면번호 최대 200개, 화면당 최대 100종목

  설계:
    - 화면번호는 4자리 문자열
    - 용도별 구간 분리(조건검색/실시간시세/TR조회/주문잔고)
    - 실시간 해제 후 재사용
    - 상한 도달 시 신규 할당 중단 및 호출측에 실패 통지

  주의: 순수 로직으로 작성하여 단위 테스트 가능. OCX를 직접 호출하지 않는다.
}

interface

uses
  System.SysUtils, System.Generics.Collections;

type
  // 화면번호 용도 구분
  TScreenPurpose = (spCondition, spRealtime, spTrQuery, spOrder);

  TScreenNoManager = class
  private
    FRangeStart: Integer;     // 용도 구간 시작(포함)
    FRangeEnd: Integer;       // 용도 구간 끝(포함)
    FInUse: TDictionary<string, Boolean>;
    FNext: Integer;
    function Format4(AValue: Integer): string;
  public
    // 예: Create(spRealtime, 1000, 1199) → 실시간용 화면번호 풀
    constructor Create(APurpose: TScreenPurpose; ARangeStart, ARangeEnd: Integer);
    destructor Destroy; override;

    // 화면번호 할당. 성공 시 True와 OutScreenNo, 고갈 시 False.
    function Acquire(out AScreenNo: string): Boolean;
    // 사용 종료된 화면번호를 반납(재사용 가능).
    procedure Release(const AScreenNo: string);
    // 현재 사용 중 개수
    function UsedCount: Integer;
    // 풀 용량
    function Capacity: Integer;
  end;

implementation

constructor TScreenNoManager.Create(APurpose: TScreenPurpose; ARangeStart, ARangeEnd: Integer);
begin
  inherited Create;
  FRangeStart := ARangeStart;
  FRangeEnd := ARangeEnd;
  FNext := ARangeStart;
  FInUse := TDictionary<string, Boolean>.Create;
end;

destructor TScreenNoManager.Destroy;
begin
  FInUse.Free;
  inherited;
end;

function TScreenNoManager.Format4(AValue: Integer): string;
begin
  // 4자리 문자열(예: 0123)
  Result := FormatFloat('0000', AValue);
end;

function TScreenNoManager.Acquire(out AScreenNo: string): Boolean;
var
  i, candidate: Integer;
  sNo: string;
begin
  // 구간 내에서 미사용 화면번호를 순환 탐색
  for i := FRangeStart to FRangeEnd do
  begin
    candidate := FNext;
    Inc(FNext);
    if FNext > FRangeEnd then
      FNext := FRangeStart;

    sNo := Format4(candidate);
    if not FInUse.ContainsKey(sNo) then
    begin
      FInUse.Add(sNo, True);
      AScreenNo := sNo;
      Exit(True);
    end;
  end;
  // 화면번호 고갈
  AScreenNo := '';
  Result := False;
end;

procedure TScreenNoManager.Release(const AScreenNo: string);
begin
  FInUse.Remove(AScreenNo);
end;

function TScreenNoManager.UsedCount: Integer;
begin
  Result := FInUse.Count;
end;

function TScreenNoManager.Capacity: Integer;
begin
  Result := FRangeEnd - FRangeStart + 1;
end;

end.
