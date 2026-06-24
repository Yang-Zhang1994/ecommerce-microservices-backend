-- PostgreSQL: add warehouse + lock lifecycle columns to stock work-order detail (align with standard Gulimall schema).
-- Run against your WMS database (e.g. ecommerce_wms).
-- Safe to run multiple times.

ALTER TABLE wms_ware_order_task_detail
    ADD COLUMN IF NOT EXISTS ware_id BIGINT;

ALTER TABLE wms_ware_order_task_detail
    ADD COLUMN IF NOT EXISTS lock_status INTEGER DEFAULT 1;

COMMENT ON COLUMN wms_ware_order_task_detail.ware_id IS 'Warehouse id for this locked line';
COMMENT ON COLUMN wms_ware_order_task_detail.lock_status IS '1 locked, 2 unlocked, 3 deducted';
