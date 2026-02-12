-- ==================================
-- 🏎️ Driver Table
-- ==================================
DROP TABLE IF EXISTS driver;
CREATE TABLE driver
(
  id            BIGSERIAL PRIMARY KEY,
  full_name     VARCHAR(255)     NOT NULL,
  mobile_phone  VARCHAR(50)      NOT NULL UNIQUE,
  status        VARCHAR(50)      NOT NULL,
  age           INTEGER          NOT NULL,
  rating        DOUBLE PRECISION NOT NULL,
  is_verified   BOOLEAN          NOT NULL,
  balance       NUMERIC(12, 2)   NOT NULL,
  date_of_birth DATE             NOT NULL
);
