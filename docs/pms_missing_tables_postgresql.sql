-- ============================================================
-- PMS 缺失表 PostgreSQL DDL（在 ecommerce_pms 库的 public schema 下执行）
-- 根据 gulimall-product 实体类生成，对应表：pms_comment_replay, pms_product_attr_value,
--   pms_sku_images, pms_sku_sale_attr_value, pms_spu_comment, pms_spu_images, pms_spu_info_desc
-- ============================================================

-- 1. 商品评价回复关系 (CommentReplayEntity)
CREATE TABLE IF NOT EXISTS pms_comment_replay (
    id          BIGSERIAL PRIMARY KEY,
    comment_id  BIGINT,
    reply_id    BIGINT
);
COMMENT ON TABLE pms_comment_replay IS 'Comment reply relation';
COMMENT ON COLUMN pms_comment_replay.id IS 'Primary key';
COMMENT ON COLUMN pms_comment_replay.comment_id IS 'Comment ID';
COMMENT ON COLUMN pms_comment_replay.reply_id IS 'Reply ID';
CREATE INDEX IF NOT EXISTS idx_pms_comment_replay_comment_id ON pms_comment_replay (comment_id);

-- 2. SPU 属性值 (ProductAttrValueEntity)
CREATE TABLE IF NOT EXISTS pms_product_attr_value (
    id          BIGSERIAL PRIMARY KEY,
    spu_id      BIGINT NOT NULL,
    attr_id     BIGINT NOT NULL,
    attr_name   VARCHAR(255),
    attr_value  VARCHAR(1000),
    attr_sort   INTEGER,
    quick_show  INTEGER
);
COMMENT ON TABLE pms_product_attr_value IS 'SPU attribute values';
COMMENT ON COLUMN pms_product_attr_value.quick_show IS '0-no 1-yes, show on intro';
CREATE INDEX IF NOT EXISTS idx_pms_product_attr_value_spu_id ON pms_product_attr_value (spu_id);
CREATE INDEX IF NOT EXISTS idx_pms_product_attr_value_attr_id ON pms_product_attr_value (attr_id);

-- 3. SKU 图片 (SkuImagesEntity)
CREATE TABLE IF NOT EXISTS pms_sku_images (
    id          BIGSERIAL PRIMARY KEY,
    sku_id      BIGINT NOT NULL,
    img_url     VARCHAR(500),
    img_sort    INTEGER,
    default_img INTEGER
);
COMMENT ON TABLE pms_sku_images IS 'SKU images';
COMMENT ON COLUMN pms_sku_images.default_img IS '0-not default 1-default';
CREATE INDEX IF NOT EXISTS idx_pms_sku_images_sku_id ON pms_sku_images (sku_id);

-- 4. SKU 销售属性值 (SkuSaleAttrValueEntity)
CREATE TABLE IF NOT EXISTS pms_sku_sale_attr_value (
    id          BIGSERIAL PRIMARY KEY,
    sku_id      BIGINT NOT NULL,
    attr_id     BIGINT NOT NULL,
    attr_name   VARCHAR(255),
    attr_value  VARCHAR(255),
    attr_sort   INTEGER
);
COMMENT ON TABLE pms_sku_sale_attr_value IS 'SKU sale attribute values';
CREATE INDEX IF NOT EXISTS idx_pms_sku_sale_attr_value_sku_id ON pms_sku_sale_attr_value (sku_id);
CREATE INDEX IF NOT EXISTS idx_pms_sku_sale_attr_value_attr_id ON pms_sku_sale_attr_value (attr_id);

-- 5. 商品评价 (SpuCommentEntity)
CREATE TABLE IF NOT EXISTS pms_spu_comment (
    id              BIGSERIAL PRIMARY KEY,
    sku_id          BIGINT,
    spu_id          BIGINT,
    spu_name        VARCHAR(255),
    member_nick_name VARCHAR(255),
    star            INTEGER,
    member_ip       VARCHAR(64),
    create_time     TIMESTAMP,
    show_status     INTEGER,
    spu_attributes  VARCHAR(500),
    likes_count     INTEGER,
    reply_count     INTEGER,
    resources       TEXT,
    content         TEXT,
    member_icon      VARCHAR(500),
    comment_type    INTEGER
);
COMMENT ON TABLE pms_spu_comment IS 'SPU comments';
COMMENT ON COLUMN pms_spu_comment.show_status IS '0-hide 1-show';
COMMENT ON COLUMN pms_spu_comment.comment_type IS '0-direct comment 1-reply';
CREATE INDEX IF NOT EXISTS idx_pms_spu_comment_spu_id ON pms_spu_comment (spu_id);
CREATE INDEX IF NOT EXISTS idx_pms_spu_comment_sku_id ON pms_spu_comment (sku_id);

-- 6. SPU 图片 (SpuImagesEntity)
CREATE TABLE IF NOT EXISTS pms_spu_images (
    id          BIGSERIAL PRIMARY KEY,
    spu_id      BIGINT NOT NULL,
    img_name    VARCHAR(255),
    img_url     VARCHAR(500),
    img_sort    INTEGER,
    default_img INTEGER
);
COMMENT ON TABLE pms_spu_images IS 'SPU images';
CREATE INDEX IF NOT EXISTS idx_pms_spu_images_spu_id ON pms_spu_images (spu_id);

-- 7. SPU 介绍 (SpuInfoDescEntity) — 主键为 spu_id，无自增
CREATE TABLE IF NOT EXISTS pms_spu_info_desc (
    spu_id   BIGINT PRIMARY KEY,
    decript  TEXT
);
COMMENT ON TABLE pms_spu_info_desc IS 'SPU description';
