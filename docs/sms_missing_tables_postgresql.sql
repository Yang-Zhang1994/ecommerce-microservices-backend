-- =============================================================================
-- gulimall-coupon (ecommerce_sms): missing tables per entity
-- Run this on database: ecommerce_sms (PostgreSQL)
-- Existing tables (no need to create): sms_coupon, sms_coupon_history, sms_home_adv,
--   sms_seckill_promotion, sms_sku_full_reduction, sms_sku_ladder, sms_spu_bounds
-- =============================================================================

-- 1. 优惠券分类关联
CREATE TABLE IF NOT EXISTS sms_coupon_spu_category_relation (
  id           BIGSERIAL PRIMARY KEY,
  coupon_id    BIGINT,
  category_id  BIGINT,
  category_name VARCHAR(255)
);

-- 2. 优惠券与产品关联
CREATE TABLE IF NOT EXISTS sms_coupon_spu_relation (
  id        BIGSERIAL PRIMARY KEY,
  coupon_id BIGINT,
  spu_id    BIGINT,
  spu_name  VARCHAR(255)
);

-- 3. 首页专题
CREATE TABLE IF NOT EXISTS sms_home_subject (
  id         BIGSERIAL PRIMARY KEY,
  name       VARCHAR(255),
  title      VARCHAR(255),
  sub_title  VARCHAR(255),
  status     INT,
  url        VARCHAR(500),
  sort       INT,
  img        VARCHAR(500)
);

-- 4. 专题商品
CREATE TABLE IF NOT EXISTS sms_home_subject_spu (
  id          BIGSERIAL PRIMARY KEY,
  name        VARCHAR(255),
  subject_id  BIGINT,
  spu_id      BIGINT,
  sort        INT
);

-- 5. 商品会员价格
CREATE TABLE IF NOT EXISTS sms_member_price (
  id                BIGSERIAL PRIMARY KEY,
  sku_id            BIGINT,
  member_level_id   BIGINT,
  member_level_name VARCHAR(255),
  member_price     NUMERIC(19, 2),
  add_other         INT
);

-- 6. 秒杀活动场次
CREATE TABLE IF NOT EXISTS sms_seckill_session (
  id          BIGSERIAL PRIMARY KEY,
  name        VARCHAR(255),
  start_time  TIMESTAMP,
  end_time    TIMESTAMP,
  status      INT,
  create_time TIMESTAMP
);

-- 7. 秒杀商品通知订阅 (column name subcribe_time matches entity)
CREATE TABLE IF NOT EXISTS sms_seckill_sku_notice (
  id            BIGSERIAL PRIMARY KEY,
  member_id     BIGINT,
  sku_id        BIGINT,
  session_id    BIGINT,
  subcribe_time TIMESTAMP,
  send_time     TIMESTAMP,
  notice_type   INT
);

-- 8. 秒杀活动商品关联
CREATE TABLE IF NOT EXISTS sms_seckill_sku_relation (
  id                    BIGSERIAL PRIMARY KEY,
  promotion_id           BIGINT,
  promotion_session_id   BIGINT,
  sku_id                 BIGINT,
  seckill_price          NUMERIC(19, 2),
  seckill_count          NUMERIC(19, 2),
  seckill_limit          NUMERIC(19, 2),
  seckill_sort           INT
);
