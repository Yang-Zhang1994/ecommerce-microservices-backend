-- One-time cleanup for orphan rows left by older delete logic (no pms_spu_info / pms_sku_info parent).
-- Review each block before running in production.

-- Example: SPU 14 / SKU 23,24 (adjust ids as needed)
-- DELETE FROM pms_sku_images WHERE sku_id IN (23, 24);
-- DELETE FROM pms_sku_sale_attr_value WHERE sku_id IN (23, 24);
-- DELETE FROM pms_spu_images WHERE spu_id = 14;
-- DELETE FROM pms_product_attr_value WHERE spu_id = 14;
-- DELETE FROM pms_spu_info_desc WHERE spu_id = 14;
-- DELETE FROM pms_spu_comment WHERE spu_id = 14 OR sku_id IN (23, 24);

-- Warehouse DB (ecommerce_wms):
-- DELETE FROM wms_ware_sku WHERE sku_id IN (23, 24);

-- Coupon DB (ecommerce_sms):
-- DELETE FROM sms_spu_bounds WHERE spu_id = 14;
-- DELETE FROM sms_coupon_spu_relation WHERE spu_id = 14;
-- DELETE FROM sms_home_subject_spu WHERE spu_id = 14;
-- DELETE FROM sms_sku_full_reduction WHERE sku_id IN (23, 24);
-- DELETE FROM sms_sku_ladder WHERE sku_id IN (23, 24);
-- DELETE FROM sms_member_price WHERE sku_id IN (23, 24);
