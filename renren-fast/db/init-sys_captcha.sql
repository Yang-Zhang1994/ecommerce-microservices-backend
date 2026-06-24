-- 验证码接口 500 时，在 ecommerce_admin 库中执行此脚本创建 sys_captcha 表
-- 使用: psql -h <RDS端点> -U postgres -d ecommerce_admin -f init-sys_captcha.sql
CREATE TABLE IF NOT EXISTS sys_captcha (
  uuid varchar(36) NOT NULL,
  code varchar(6) NOT NULL,
  expire_time timestamp,
  PRIMARY KEY (uuid)
);
