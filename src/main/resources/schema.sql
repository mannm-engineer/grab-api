-- ==================================
-- 🏎️ Driver Table
-- ==================================
DROP TABLE IF EXISTS driver;
CREATE TABLE driver
(
  id           BIGSERIAL PRIMARY KEY,
  full_name    VARCHAR(255) NOT NULL,
  mobile_phone VARCHAR(50)  NOT NULL UNIQUE,
  status       VARCHAR(50)  NOT NULL,
  -- 🔍 Audit fields
  created_at   TIMESTAMP    NOT NULL,
  created_by   VARCHAR(100) NOT NULL
);
