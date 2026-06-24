-- Mobile Phones (catalog 225): RAM/Capacity as sale attrs for SKU matrix (Publish SPU step 3).
-- Run after fix-mobile-phone-attrs.sql. Safe to re-run.

-- RAM (attr_id 14): base -> sale, multi-select in step 3
UPDATE pms_attr SET
  attr_name = 'RAM',
  attr_type = 0,
  value_type = 1,
  value_select = '8GB;12GB;16GB;24GB',
  search_type = 1,
  show_desc = 0,
  icon = '',
  enable = 1
WHERE attr_id = 14 AND catelog_id = 225;

-- Base Storage (attr_id 15): disable; use sale Capacity (attr_id 8) for 128GB/256GB SKUs
UPDATE pms_attr SET
  attr_name = 'Storage',
  enable = 0,
  search_type = 0,
  show_desc = 0
WHERE attr_id = 15 AND catelog_id = 225;

-- Capacity: sale attr, multi-select
UPDATE pms_attr SET
  attr_name = 'Capacity',
  attr_type = 0,
  value_type = 1,
  value_select = '128GB;256GB;512GB;1TB',
  search_type = 1,
  show_desc = 0,
  icon = '',
  enable = 1
WHERE attr_id = 8 AND catelog_id = 225;

-- Color: ensure enabled sale attr
UPDATE pms_attr SET
  attr_type = 0,
  value_type = 1,
  enable = 1
WHERE attr_id = 6 AND catelog_id = 225;

-- Remove base-attribute group links for attrs now used as sale-only
DELETE FROM pms_attr_attrgroup_relation WHERE attr_id IN (14, 15);

-- Optional: drop stale SPU base values for RAM/Storage (variants live on SKU sale attrs)
DELETE FROM pms_product_attr_value WHERE attr_id IN (14, 15);
