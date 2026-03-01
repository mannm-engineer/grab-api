-- ==================================
-- 🏎️ Driver Table
-- ==================================
DROP TABLE IF EXISTS driver_document_file;
DROP TABLE IF EXISTS driver_document;
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

-- ==================================
-- 📄 Driver Document Table
-- ==================================
CREATE TABLE driver_document
(
  id              BIGSERIAL PRIMARY KEY,
  driver_id       BIGINT       NOT NULL REFERENCES driver (id),
  type            VARCHAR(50)  NOT NULL,
  document_number VARCHAR(255) NOT NULL,
  expiry_date     DATE         NOT NULL
);

-- ==================================
-- 📎 Driver Document File Table
-- ==================================
CREATE TABLE driver_document_file
(
  id          BIGSERIAL PRIMARY KEY,
  document_id BIGINT       NOT NULL REFERENCES driver_document (id),
  file_url    VARCHAR(500) NOT NULL
);
