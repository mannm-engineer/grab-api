-- ==================================
-- 🏎️ DRIVER TABLE
-- ==================================
DROP TABLE IF EXISTS driver;
CREATE TABLE driver
(
    id           BIGSERIAL PRIMARY KEY,
    location_lat DOUBLE PRECISION NULL,
    location_lng DOUBLE PRECISION NULL
);

-- 📝 Insert dummy drivers
INSERT INTO driver DEFAULT VALUES;
INSERT INTO driver DEFAULT VALUES;
