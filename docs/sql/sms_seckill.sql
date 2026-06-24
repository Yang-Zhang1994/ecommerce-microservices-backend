-- ============================================================================
-- Seckill (秒杀) — PostgreSQL seed data (admin tables only)
-- Database: ecommerce_sms (owned by gulimall-coupon admin CRUD)
--
-- gulimall-seckill has NO database: warm-up pulls sessions via HTTP from coupon,
-- orders are emitted to gulimall-order via RabbitMQ (oms_order there).
--
-- sms_seckill_order is deprecated; orders live in oms_order after MQ consume.
-- ============================================================================

-- Optional legacy table (no longer written by gulimall-seckill):
CREATE TABLE IF NOT EXISTS sms_seckill_order (
    id                    BIGSERIAL PRIMARY KEY,
    order_sn              VARCHAR(64),
    member_id             BIGINT,
    promotion_session_id  BIGINT,
    sku_id                BIGINT,
    seckill_price         NUMERIC(18, 4),
    seckill_count         INTEGER,
    status                INTEGER DEFAULT 0,   -- 0=unpaid, 1=paid
    create_time           TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_seckill_order_member ON sms_seckill_order (member_id);
CREATE INDEX IF NOT EXISTS idx_seckill_order_sn ON sms_seckill_order (order_sn);

-- ============================================================================
-- 2) Seed data (DEMO). Adjust sku_id values to real pms_sku_info ids in your
--    catalog (the product service uses a separate database, so pick ids that
--    actually exist, e.g. from the homepage / search results).
-- ============================================================================

-- One enabled session that is live for the whole of today.
-- status = 1 means enabled (the warm-up job only loads enabled sessions).
INSERT INTO sms_seckill_session (name, start_time, end_time, status, create_time)
VALUES (
    'Today flash sale',
    CURRENT_DATE,                                              -- 00:00 today
    CURRENT_DATE + INTERVAL '1 day' - INTERVAL '1 second',     -- 23:59:59 today
    1,
    NOW()
);

-- Link SKUs to that session. Replace the sku_id values (1, 2, 3...) with real ones.
-- promotion_session_id must equal the id of the session inserted above.
INSERT INTO sms_seckill_sku_relation
    (promotion_id, promotion_session_id, sku_id, seckill_price, seckill_count, seckill_limit, seckill_sort)
SELECT
    0,
    (SELECT id FROM sms_seckill_session WHERE name = 'Today flash sale' ORDER BY id DESC LIMIT 1),
    v.sku_id,
    v.seckill_price,
    v.seckill_count,
    v.seckill_limit,
    v.seckill_sort
FROM (VALUES
    (1::BIGINT, 17.90::NUMERIC, 100::NUMERIC, 1::NUMERIC, 0),
    (2::BIGINT, 83.90::NUMERIC, 50::NUMERIC,  1::NUMERIC, 1),
    (3::BIGINT, 399.00::NUMERIC, 30::NUMERIC, 2::NUMERIC, 2)
) AS v(sku_id, seckill_price, seckill_count, seckill_limit, seckill_sort);

-- After seeding, restart gulimall-coupon (or wait for the cron) so the warm-up
-- job loads these into Redis. Then they appear on the mall homepage flash-sale block.
