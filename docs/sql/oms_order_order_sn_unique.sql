-- PostgreSQL: enforce unique order number on oms_order.order_sn
-- Run manually in DB (recommended in a maintenance window).
--
-- 1) Check duplicate order_sn values first.
SELECT order_sn, COUNT(*) AS cnt
FROM oms_order
GROUP BY order_sn
HAVING COUNT(*) > 1;

-- 2) If duplicates exist, keep the earliest row by id and delete the rest.
--    Review carefully before running in production.
DELETE FROM oms_order o
USING oms_order d
WHERE o.order_sn = d.order_sn
  AND o.id > d.id;

-- 3) Optional cleanup: normalize null/blank order_sn if they exist.
-- UPDATE oms_order SET order_sn = concat('FIX-', id) WHERE order_sn IS NULL OR btrim(order_sn) = '';

-- 4) Add unique constraint + unique index.
ALTER TABLE oms_order
    ALTER COLUMN order_sn SET NOT NULL;

ALTER TABLE oms_order
    ADD CONSTRAINT uk_oms_order_order_sn UNIQUE (order_sn);

