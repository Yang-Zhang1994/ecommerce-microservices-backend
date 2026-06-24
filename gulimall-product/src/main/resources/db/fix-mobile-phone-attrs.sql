-- Mobile Phones (catelog_id = 225) base attribute cleanup
-- value_type: 0 = single value per SKU, 1 = multiple values per SKU
-- value_select: semicolon-separated options

UPDATE pms_attr SET
  attr_name = 'Screen Size',
  value_type = 0,
  value_select = '6.1 inch;6.3 inch;6.5 inch;6.7 inch;6.8 inch',
  search_type = 1,
  show_desc = 1,
  icon = '0'
WHERE attr_id = 10;

UPDATE pms_attr SET
  attr_name = 'Screen Material',
  value_type = 0,
  value_select = 'OLED;AMOLED;LTPO AMOLED;IPS LCD',
  search_type = 1,
  show_desc = 1,
  icon = '1'
WHERE attr_id = 11;

UPDATE pms_attr SET
  attr_name = 'Process Node',
  value_type = 0,
  value_select = '3nm;4nm;5nm;6nm',
  search_type = 0,
  show_desc = 0,
  icon = '1'
WHERE attr_id = 12;

UPDATE pms_attr SET
  attr_name = 'Processor',
  value_type = 0,
  value_select = 'Snapdragon 8 Gen 3;Snapdragon 8 Gen 2;Apple A18 Pro;Apple A17 Pro;Dimensity 9300;Exynos 2400',
  search_type = 1,
  show_desc = 1,
  icon = '2'
WHERE attr_id = 13;

UPDATE pms_attr SET
  attr_name = 'Release Year',
  value_type = 0,
  value_select = '2022;2023;2024;2025;2026',
  search_type = 1,
  show_desc = 0,
  icon = '0'
WHERE attr_id = 1;

UPDATE pms_attr SET
  attr_name = 'Network',
  value_type = 1,
  value_select = '5G;4G LTE;Wi-Fi 6;Wi-Fi 6E;NFC',
  search_type = 1,
  show_desc = 0,
  icon = '1'
WHERE attr_id = 5;

UPDATE pms_attr SET
  attr_name = 'Body Material',
  value_type = 0,
  value_select = 'Glass;Aluminum Alloy;Stainless Steel;Ceramic;Plastic',
  search_type = 1,
  show_desc = 1,
  icon = '3'
WHERE attr_id = 7;

UPDATE pms_attr_group SET
  attr_group_name = 'Screen',
  descript = 'Display specifications'
WHERE attr_group_id = 1 AND catelog_id = 225;

UPDATE pms_attr_group SET
  attr_group_name = 'Basic Info',
  descript = 'General specifications'
WHERE attr_group_id = 2 AND catelog_id = 225;

UPDATE pms_attr_group SET
  attr_group_name = 'Chip',
  descript = 'Processor specifications'
WHERE attr_group_id = 3 AND catelog_id = 225;

-- Ensure attr ↔ group relations (idempotent)
INSERT INTO pms_attr_attrgroup_relation (attr_id, attr_group_id, attr_group_sort)
SELECT 10, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM pms_attr_attrgroup_relation WHERE attr_id = 10);
INSERT INTO pms_attr_attrgroup_relation (attr_id, attr_group_id, attr_group_sort)
SELECT 11, 1, 1 WHERE NOT EXISTS (SELECT 1 FROM pms_attr_attrgroup_relation WHERE attr_id = 11);
INSERT INTO pms_attr_attrgroup_relation (attr_id, attr_group_id, attr_group_sort)
SELECT 12, 3, 1 WHERE NOT EXISTS (SELECT 1 FROM pms_attr_attrgroup_relation WHERE attr_id = 12);
INSERT INTO pms_attr_attrgroup_relation (attr_id, attr_group_id, attr_group_sort)
SELECT 13, 3, 0 WHERE NOT EXISTS (SELECT 1 FROM pms_attr_attrgroup_relation WHERE attr_id = 13);
INSERT INTO pms_attr_attrgroup_relation (attr_id, attr_group_id, attr_group_sort)
SELECT 1, 2, 0 WHERE NOT EXISTS (SELECT 1 FROM pms_attr_attrgroup_relation WHERE attr_id = 1);
INSERT INTO pms_attr_attrgroup_relation (attr_id, attr_group_id, attr_group_sort)
SELECT 5, 2, 1 WHERE NOT EXISTS (SELECT 1 FROM pms_attr_attrgroup_relation WHERE attr_id = 5);
INSERT INTO pms_attr_attrgroup_relation (attr_id, attr_group_id, attr_group_sort)
SELECT 7, 2, 2 WHERE NOT EXISTS (SELECT 1 FROM pms_attr_attrgroup_relation WHERE attr_id = 7);

-- ---------------------------------------------------------------------------
-- RAM/Capacity as sale attrs: run scripts/setup-mobile-phone-sale-attrs.sh and
-- gulimall-product/src/main/resources/db/mobile-phone-sale-attrs-ram.sql
-- Base step 2: specs (Screen, Network, Battery, …). Step 3 sale: Color, RAM, Capacity.
-- ---------------------------------------------------------------------------

-- Sale attribute cleanup (attr_type = 0)
UPDATE pms_attr SET attr_name = 'Color', value_type = 1,
  value_select = 'Black;White;Blue;Silver;Gold;Green', search_type = 0, show_desc = 0, icon = '2'
WHERE attr_id = 6 AND catelog_id = 225;

UPDATE pms_attr SET attr_name = 'Capacity', value_type = 1,
  value_select = '128GB;256GB;512GB;1TB', search_type = 0, show_desc = 0, icon = '', enable = 1
WHERE attr_id = 8 AND catelog_id = 225;

UPDATE pms_attr SET attr_name = 'Edition', value_type = 1,
  value_select = 'Standard;Pro;Ultra', search_type = 0, show_desc = 0, icon = '', enable = 0
WHERE attr_id = 9 AND catelog_id = 225;

UPDATE pms_attr SET attr_name = 'Color', value_type = 1,
  value_select = 'Black;White;Blue;Silver;Gold;Green;Pink', search_type = 0, show_desc = 0, icon = '', enable = 1
WHERE attr_id = 6 AND catelog_id = 225;
