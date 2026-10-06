INSERT INTO promo_codes (code, discount_type, discount_value, valid_from, valid_until, active, usage_limit, min_price) VALUES
    ('WELCOME10', 'PERCENT', 10,        DATE '2020-01-01', DATE '2099-12-31', TRUE,  NULL, NULL),
    ('FLAT5',     'FIXED',   5.00,      DATE '2020-01-01', DATE '2099-12-31', TRUE,  NULL, NULL),
    ('HUGE',      'FIXED',   100000.00, DATE '2020-01-01', DATE '2099-12-31', TRUE,  NULL, NULL),
    ('PAUSED',    'PERCENT', 20,        DATE '2020-01-01', DATE '2099-12-31', FALSE, NULL, NULL),
    ('OLDCODE',   'PERCENT', 20,        DATE '2020-01-01', DATE '2020-12-31', TRUE,  NULL, NULL),
    ('FUTURE',    'PERCENT', 20,        DATE '2099-01-01', DATE '2099-12-31', TRUE,  NULL, NULL),
    ('ONCE',      'PERCENT', 50,        DATE '2020-01-01', DATE '2099-12-31', TRUE,  1,    NULL),
    ('BIG15',     'PERCENT', 15,        DATE '2020-01-01', DATE '2099-12-31', TRUE,  NULL, 100000.00);
