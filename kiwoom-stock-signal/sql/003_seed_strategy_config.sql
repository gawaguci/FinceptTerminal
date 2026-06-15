-- =============================================================
-- 전략 파라미터 초기값 (계획서 19.2 설정 예시 기준)
-- 주문 기능 기본값은 비활성(false)이다.
-- =============================================================

INSERT INTO strategy_config (config_key, config_value, description) VALUES
    ('kiwoom.orderEnabled',        'false',        '주문 기능 활성화 여부(기본 비활성)'),
    ('kiwoom.dryRun',              'true',         '주문 Dry-run 모드'),
    ('kiwoom.maxTrPerSecond',      '5',            'TR 초당 제한'),
    ('kiwoom.maxTrPerMinute',      '100',          'TR 분당 제한'),
    ('kiwoom.maxTrPerHour',        '1000',         'TR 시간당 제한'),
    ('strategy.macdFast',          '12',           'MACD Fast EMA'),
    ('strategy.macdSlow',          '26',           'MACD Slow EMA'),
    ('strategy.macdSignal',        '9',            'MACD Signal EMA'),
    ('strategy.rsiPeriod',         '14',           'RSI 기간'),
    ('strategy.minTradeAmount',    '3000000000',   '당일 거래대금 하한(원)'),
    ('strategy.volumeSpikeMultiplier', '1.5',      '거래량 급증 배수'),
    ('strategy.maxRsiForEntry',    '75',           '진입 허용 RSI 상한'),
    ('strategy.maxVwapGapRate',    '0.03',         'VWAP 허용 이격률'),
    ('risk.maxPositionRate',       '0.05',         '종목당 최대 투입 비율'),
    ('risk.maxDailyLossRate',      '0.01',         '1일 최대 손실 비율'),
    ('risk.maxLossPerTradeRate',   '0.003',        '1회 최대 손실 비율'),
    ('risk.maxHoldingCount',       '5',            '최대 보유 종목 수'),
    ('risk.cooldownMinutes',       '20',           '동일 종목 쿨다운(분)'),
    ('risk.newBuyCutoffTime',      '15:10:00',     '신규매수 제한 시각')
ON CONFLICT (config_key) DO NOTHING;
