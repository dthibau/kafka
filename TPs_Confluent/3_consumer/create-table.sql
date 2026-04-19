drop table if exists positions;

CREATE TABLE IF NOT EXISTS positions (
    id            SERIAL PRIMARY KEY,
    courier_id    VARCHAR(100),
    kafka_offset  BIGINT,
    latitude      DOUBLE PRECISION,
    longitude     DOUBLE PRECISION,
    created_at    TIMESTAMP DEFAULT NOW(),
    CONSTRAINT uq_courier_offset UNIQUE (courier_id, kafka_offset)
);

