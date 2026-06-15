-- =============================================================
-- 키움 종목발굴/매수타이밍 프로그램 DB 스키마 (계획서 13장)
-- 대상: PostgreSQL 우선. SQLite 사용 시 일부 타입 조정 필요(주석 참고).
-- 주의: 계좌번호/인증정보 등 민감정보는 저장하지 않는다.
-- =============================================================

-- 종목 마스터
CREATE TABLE IF NOT EXISTS stock_master (
    stock_code   VARCHAR(20) PRIMARY KEY,
    stock_name   VARCHAR(100) NOT NULL,
    market_type  VARCHAR(10) NOT NULL,            -- KOSPI / KOSDAQ
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 실시간 수신 원본 틱 이벤트
CREATE TABLE IF NOT EXISTS tick_event (
    id                 BIGSERIAL PRIMARY KEY,      -- SQLite: INTEGER PRIMARY KEY AUTOINCREMENT
    stock_code         VARCHAR(20) NOT NULL,
    event_time         TIMESTAMP NOT NULL,
    current_price      BIGINT NOT NULL,            -- 원 단위 정수
    accumulated_volume BIGINT NOT NULL,
    received_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 1/3/5분 봉
CREATE TABLE IF NOT EXISTS minute_bar (
    id            BIGSERIAL PRIMARY KEY,
    stock_code    VARCHAR(20) NOT NULL,
    bar_interval  VARCHAR(4) NOT NULL,             -- M1 / M3 / M5
    bar_time      TIMESTAMP NOT NULL,
    open_price    BIGINT NOT NULL,
    high_price    BIGINT NOT NULL,
    low_price     BIGINT NOT NULL,
    close_price   BIGINT NOT NULL,
    volume        BIGINT NOT NULL,
    trade_amount  BIGINT NOT NULL,
    completed     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (stock_code, bar_interval, bar_time)
);

-- 지표 스냅샷
CREATE TABLE IF NOT EXISTS indicator_snapshot (
    id             BIGSERIAL PRIMARY KEY,
    stock_code     VARCHAR(20) NOT NULL,
    snapshot_time  TIMESTAMP NOT NULL,
    macd_value     NUMERIC(18, 6),
    macd_signal    NUMERIC(18, 6),
    macd_histogram NUMERIC(18, 6),
    rsi_value      NUMERIC(18, 6),
    vwap_price     NUMERIC(18, 2),
    ma20_value     NUMERIC(18, 2),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 후보 종목 점수
CREATE TABLE IF NOT EXISTS candidate_signal (
    id             BIGSERIAL PRIMARY KEY,
    stock_code     VARCHAR(20) NOT NULL,
    signal_time    TIMESTAMP NOT NULL,
    candidate_score INTEGER NOT NULL,
    grade          VARCHAR(20) NOT NULL,            -- INTEREST / WATCH / STRONG / NONE
    reason         TEXT NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 매수 신호 및 거절 사유 (계획서 13.2)
CREATE TABLE IF NOT EXISTS buy_signal_log (
    id                BIGSERIAL PRIMARY KEY,
    stock_code        VARCHAR(20) NOT NULL,
    signal_time       TIMESTAMP NOT NULL,
    current_price     BIGINT NOT NULL,
    candidate_score   INTEGER NOT NULL,
    macd_value        NUMERIC(18, 6),
    macd_signal       NUMERIC(18, 6),
    macd_histogram    NUMERIC(18, 6),
    rsi_value         NUMERIC(18, 6),
    vwap_price        NUMERIC(18, 2),
    decision          VARCHAR(30) NOT NULL,         -- BUY_CANDIDATE / REJECTED
    reject_reason     VARCHAR(40),
    reason            TEXT NOT NULL,
    order_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 주문 요청 이력
CREATE TABLE IF NOT EXISTS order_request_log (
    id            BIGSERIAL PRIMARY KEY,
    stock_code    VARCHAR(20) NOT NULL,
    side          VARCHAR(4) NOT NULL,              -- BUY / SELL
    quantity      INTEGER NOT NULL,
    price         BIGINT NOT NULL,
    market_order  BOOLEAN NOT NULL DEFAULT FALSE,
    requested_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 주문 접수/체결/거부 이벤트
CREATE TABLE IF NOT EXISTS order_event_log (
    id              BIGSERIAL PRIMARY KEY,
    order_no        VARCHAR(40),
    stock_code      VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL,           -- ACCEPTED / FILLED / PARTIAL / REJECTED / CANCELLED
    filled_quantity INTEGER NOT NULL DEFAULT 0,
    filled_price    BIGINT NOT NULL DEFAULT 0,
    message         TEXT,
    event_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 보유 종목 상태
CREATE TABLE IF NOT EXISTS position_snapshot (
    id            BIGSERIAL PRIMARY KEY,
    stock_code    VARCHAR(20) NOT NULL,
    quantity      INTEGER NOT NULL,
    avg_price     BIGINT NOT NULL,
    snapshot_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 리스크 차단 사유
CREATE TABLE IF NOT EXISTS risk_event_log (
    id            BIGSERIAL PRIMARY KEY,
    stock_code    VARCHAR(20),
    reject_reason VARCHAR(40) NOT NULL,
    detail        TEXT,
    event_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 전략 파라미터
CREATE TABLE IF NOT EXISTS strategy_config (
    config_key    VARCHAR(60) PRIMARY KEY,
    config_value  VARCHAR(200) NOT NULL,
    description   TEXT,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 시스템 이벤트(연결/재시작/API 제한 등)
CREATE TABLE IF NOT EXISTS system_event_log (
    id          BIGSERIAL PRIMARY KEY,
    event_type  VARCHAR(40) NOT NULL,
    detail      TEXT,
    event_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
