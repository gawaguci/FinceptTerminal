unit TrRateLimiter;

{
  TR 호출 제한 관리 유닛 (계획서 8.5)

  키움 OpenAPI+ TR 제한:
    - 초당 5건, 분당 100건, 시간당 1000건

  제공 기능:
    - CanRequest:        현재 요청 가능 여부
    - MarkRequest:       요청 발생 기록
    - NextAvailableTime: 다음 요청 가능 시각

  주의: 본 유닛은 순수 로직으로 작성하여 단위 테스트가 가능하도록 한다.
        키움 OCX를 직접 호출하지 않는다.
}

interface

uses
  System.SysUtils, System.DateUtils, System.Generics.Collections;

type
  // 요청 유형 구분 (조회/주문 등을 구분하여 정책 분리 가능)
  TTrRequestType = (trtQuery, trtAccount, trtChart, trtOrder, trtEtc);

  // 제한 판정 결과
  TRateDecision = (rdAllow, rdWait, rdReject);

  TTrRateLimiter = class
  private
    FMaxPerSecond: Integer;
    FMaxPerMinute: Integer;
    FMaxPerHour: Integer;
    // 최근 요청 시각 목록 (오래된 항목은 정리)
    FTimestamps: TList<TDateTime>;
    procedure Prune(const ANow: TDateTime);
    function CountWithin(const ANow: TDateTime; ASeconds: Integer): Integer;
  public
    constructor Create(AMaxPerSecond: Integer = 5;
                       AMaxPerMinute: Integer = 100;
                       AMaxPerHour: Integer = 1000);
    destructor Destroy; override;

    // 요청 가능 여부 판정
    function CanRequest(const ANow: TDateTime): Boolean;
    function Decide(const ANow: TDateTime): TRateDecision;

    // 요청 발생을 기록한다. 호출 전 CanRequest로 확인 권장.
    procedure MarkRequest(const ANow: TDateTime);

    // 다음 요청 가능 시각. 지금 가능하면 ANow를 반환.
    function NextAvailableTime(const ANow: TDateTime): TDateTime;
  end;

implementation

constructor TTrRateLimiter.Create(AMaxPerSecond, AMaxPerMinute, AMaxPerHour: Integer);
begin
  inherited Create;
  FMaxPerSecond := AMaxPerSecond;
  FMaxPerMinute := AMaxPerMinute;
  FMaxPerHour := AMaxPerHour;
  FTimestamps := TList<TDateTime>.Create;
end;

destructor TTrRateLimiter.Destroy;
begin
  FTimestamps.Free;
  inherited;
end;

procedure TTrRateLimiter.Prune(const ANow: TDateTime);
var
  i: Integer;
  Threshold: TDateTime;
begin
  // 1시간 이전 기록은 더 이상 어떤 윈도우에도 영향이 없으므로 제거
  Threshold := IncSecond(ANow, -3600);
  for i := FTimestamps.Count - 1 downto 0 do
    if FTimestamps[i] < Threshold then
      FTimestamps.Delete(i);
end;

function TTrRateLimiter.CountWithin(const ANow: TDateTime; ASeconds: Integer): Integer;
var
  ts: TDateTime;
  Threshold: TDateTime;
begin
  Result := 0;
  Threshold := IncSecond(ANow, -ASeconds);
  for ts in FTimestamps do
    if ts > Threshold then
      Inc(Result);
end;

function TTrRateLimiter.CanRequest(const ANow: TDateTime): Boolean;
begin
  Prune(ANow);
  Result := (CountWithin(ANow, 1) < FMaxPerSecond)
        and (CountWithin(ANow, 60) < FMaxPerMinute)
        and (CountWithin(ANow, 3600) < FMaxPerHour);
end;

function TTrRateLimiter.Decide(const ANow: TDateTime): TRateDecision;
begin
  if CanRequest(ANow) then
    Result := rdAllow
  // 시간당 한도 초과는 대기 시간이 길어 거절로 본다.
  else if CountWithin(ANow, 3600) >= FMaxPerHour then
    Result := rdReject
  else
    Result := rdWait;
end;

procedure TTrRateLimiter.MarkRequest(const ANow: TDateTime);
begin
  FTimestamps.Add(ANow);
  Prune(ANow);
end;

function TTrRateLimiter.NextAvailableTime(const ANow: TDateTime): TDateTime;
var
  ts, earliest: TDateTime;
begin
  Prune(ANow);
  if CanRequest(ANow) then
    Exit(ANow);

  // 초당 한도 위반 시: 1초 윈도우 내 가장 이른 요청이 빠지는 시점
  if CountWithin(ANow, 1) >= FMaxPerSecond then
  begin
    earliest := ANow;
    for ts in FTimestamps do
      if (ts > IncSecond(ANow, -1)) and (ts < earliest) then
        earliest := ts;
    Exit(IncSecond(earliest, 1));
  end;

  // 분당 한도 위반 시
  if CountWithin(ANow, 60) >= FMaxPerMinute then
  begin
    earliest := ANow;
    for ts in FTimestamps do
      if (ts > IncSecond(ANow, -60)) and (ts < earliest) then
        earliest := ts;
    Exit(IncSecond(earliest, 60));
  end;

  // 시간당 한도 위반 시
  earliest := ANow;
  for ts in FTimestamps do
    if (ts > IncSecond(ANow, -3600)) and (ts < earliest) then
      earliest := ts;
  Result := IncSecond(earliest, 3600);
end;

end.
