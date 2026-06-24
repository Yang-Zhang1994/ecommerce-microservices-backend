-- Local outbox (PostgreSQL). Run on ecommerce_oms (or your OMS database) if table does not exist.
-- 0 = pending send, 2 = awaiting publisher confirm, 1 = broker confirmed; consumers remain idempotent for duplicates.

CREATE TABLE IF NOT EXISTS oms_mq_message (
    message_id     VARCHAR(32) PRIMARY KEY,
    content        TEXT,
    to_exchange    VARCHAR(255),
    routing_key      VARCHAR(255),
    class_type     VARCHAR(255),
    message_status SMALLINT NOT NULL DEFAULT 0,
    create_time    TIMESTAMPTZ,
    update_time    TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_oms_mq_message_status ON oms_mq_message (message_status);
