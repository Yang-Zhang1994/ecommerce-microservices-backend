-- ecommerce_oms: Stripe webhook idempotency + audit log (gulimall-order)
CREATE TABLE IF NOT EXISTS stripe_webhook_event (
    id            BIGSERIAL PRIMARY KEY,
    event_id      VARCHAR(128) NOT NULL,
    event_type    VARCHAR(128),
    payload       TEXT NOT NULL,
    status        VARCHAR(32) NOT NULL,
    error_message VARCHAR(512),
    received_at   TIMESTAMPTZ NOT NULL,
    processed_at  TIMESTAMPTZ,
    CONSTRAINT uk_stripe_event_id UNIQUE (event_id)
);

CREATE INDEX IF NOT EXISTS idx_stripe_webhook_event_status ON stripe_webhook_event (status);
CREATE INDEX IF NOT EXISTS idx_stripe_webhook_event_received_at ON stripe_webhook_event (received_at DESC);
