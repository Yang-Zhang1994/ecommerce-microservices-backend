-- Mobile Phones (225): canonical sale attrs = Color(6) + RAM(14) + Capacity(8).
-- Prefer scripts/migrate-mobile-phone-sale-attrs-unified.py (API) for live DB.
-- This SQL documents attr definitions only.

UPDATE pms_attr SET attr_name = 'Color', attr_type = 0, enable = 1, value_type = 1,
  value_select = 'Black;White;Blue;Silver;Gold;Green;Pink', search_type = 0
WHERE attr_id = 6 AND catelog_id = 225;

UPDATE pms_attr SET attr_name = 'RAM', attr_type = 0, enable = 1, value_type = 1,
  value_select = '4GB;8GB;12GB;16GB;24GB', search_type = 1
WHERE attr_id = 14 AND catelog_id = 225;

UPDATE pms_attr SET attr_name = 'Capacity', attr_type = 0, enable = 1, value_type = 1,
  value_select = '128GB;256GB;512GB;1TB', search_type = 1
WHERE attr_id = 8 AND catelog_id = 225;

UPDATE pms_attr SET attr_name = 'Edition', enable = 0, attr_type = 0
WHERE attr_id = 9 AND catelog_id = 225;
