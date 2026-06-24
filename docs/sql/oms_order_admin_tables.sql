-- OMS tables for renren admin: Return Settings, Refund, Return Apply, Return Reason
-- Database: ecommerce_oms (PostgreSQL / RDS)

CREATE TABLE IF NOT EXISTS oms_order_setting (
    id BIGSERIAL PRIMARY KEY,
    flash_order_overtime INT,
    normal_order_overtime INT,
    confirm_overtime INT,
    finish_overtime INT,
    comment_overtime INT,
    member_level INT
);

CREATE TABLE IF NOT EXISTS oms_order_return_reason (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    sort INT,
    status INT,
    create_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS oms_refund_info (
    id BIGSERIAL PRIMARY KEY,
    order_return_id BIGINT,
    refund DECIMAL(18, 4),
    refund_sn VARCHAR(64),
    refund_status INT,
    refund_channel INT,
    refund_content VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS oms_order_return_apply (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT,
    sku_id BIGINT,
    order_sn VARCHAR(64),
    create_time TIMESTAMP,
    member_username VARCHAR(64),
    return_amount DECIMAL(18, 4),
    return_name VARCHAR(64),
    return_phone VARCHAR(32),
    status INT,
    handle_time TIMESTAMP,
    sku_img VARCHAR(500),
    sku_name VARCHAR(255),
    sku_brand VARCHAR(255),
    sku_attrs_vals VARCHAR(2000),
    sku_count INT,
    sku_price DECIMAL(18, 4),
    sku_real_price DECIMAL(18, 4),
    reason VARCHAR(500),
    description VARCHAR(2000),
    desc_pics VARCHAR(2000),
    handle_note VARCHAR(500),
    handle_man VARCHAR(64),
    receive_man VARCHAR(64),
    receive_time TIMESTAMP,
    receive_note VARCHAR(500),
    receive_phone VARCHAR(32),
    company_address VARCHAR(500)
);

-- Optional seed for Return Settings (one default row)
INSERT INTO oms_order_setting (
    flash_order_overtime, normal_order_overtime, confirm_overtime,
    finish_overtime, comment_overtime, member_level
)
SELECT 30, 60, 7, 14, 7, 0
WHERE NOT EXISTS (SELECT 1 FROM oms_order_setting LIMIT 1);
