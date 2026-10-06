CREATE TABLE promo_codes (
    code           VARCHAR(32)    PRIMARY KEY,
    discount_type  VARCHAR(10)    NOT NULL CHECK (discount_type IN ('PERCENT', 'FIXED')),
    -- A whole percentage (1-100) for PERCENT codes, an amount for FIXED codes.
    discount_value NUMERIC(10, 2) NOT NULL CHECK (discount_value > 0),
    valid_from     DATE           NOT NULL,
    valid_until    DATE           NOT NULL,
    active         BOOLEAN        NOT NULL,
    usage_limit    INT,
    min_price      NUMERIC(10, 2)
);

ALTER TABLE bookings ADD COLUMN original_price NUMERIC(10, 2);
UPDATE bookings SET original_price = total_price;
ALTER TABLE bookings ALTER COLUMN original_price SET NOT NULL;

ALTER TABLE bookings ADD COLUMN discount_amount NUMERIC(10, 2) DEFAULT 0 NOT NULL;

ALTER TABLE bookings ADD COLUMN promo_code VARCHAR(32);
ALTER TABLE bookings ADD CONSTRAINT fk_bookings_promo_code FOREIGN KEY (promo_code) REFERENCES promo_codes (code);
CREATE INDEX idx_bookings_promo_code ON bookings (promo_code);
