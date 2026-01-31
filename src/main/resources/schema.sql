-- ==================================
-- 🏎️ Driver Table
-- ==================================
DROP TABLE IF EXISTS driver;
CREATE TABLE driver
(
  id           BIGSERIAL PRIMARY KEY,
  full_name    VARCHAR(255) NOT NULL,
  mobile_phone VARCHAR(50)  NOT NULL UNIQUE,
  location_lat DOUBLE PRECISION,
  location_lng DOUBLE PRECISION,
  status       VARCHAR(50)  NOT NULL,
  -- 🔍 Audit fields
  created_at   TIMESTAMP    NOT NULL,
  created_by   VARCHAR(100) NOT NULL
);

-- ==================================
-- 📤 Outbox Event Table
-- ==================================
DROP TABLE IF EXISTS outbox_event;
CREATE TABLE outbox_event
(
  id         BIGSERIAL PRIMARY KEY,
  topic      VARCHAR(255) NOT NULL,
  event_key  VARCHAR(255) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  payload    TEXT         NOT NULL,
  created_at TIMESTAMP    NOT NULL
);
