-- ==================================
-- 🏎️ Driver Table
-- ==================================
DROP TABLE IF EXISTS driver;
CREATE TABLE driver
(
  id           BIGSERIAL PRIMARY KEY,
  map_id       UUID         NOT NULL,
  full_name    VARCHAR(255) NOT NULL,
  mobile_phone VARCHAR(50)  NOT NULL,
  location_lat DOUBLE PRECISION,
  location_lng DOUBLE PRECISION,
  status       VARCHAR(50)  NOT NULL,
  -- 🔍 Audit fields
  created_at   TIMESTAMP    NOT NULL,
  created_by   VARCHAR(100) NOT NULL,
  updated_at   TIMESTAMP,
  updated_by   VARCHAR(100)
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

-- ==================================
-- 🚕 RIDE TABLE
-- ==================================
DROP TABLE IF EXISTS ride;
CREATE TABLE ride
(
  id           BIGSERIAL PRIMARY KEY,
  map_id       UUID             NOT NULL,
  passenger_id UUID             NOT NULL,

  -- 📍 Pickup location
  pickup_lat   DOUBLE PRECISION NOT NULL,
  pickup_lng   DOUBLE PRECISION NOT NULL,

  -- 🎯 Dropoff location
  dropoff_lat  DOUBLE PRECISION NOT NULL,
  dropoff_lng  DOUBLE PRECISION NOT NULL,

  status       VARCHAR(50)      NOT NULL
);
