-- 属性与属性分组关联表（gulimall_pms）
-- 用于 Query All 等接口查询属性分组名称，缺失此表会报 relation "pms_attr_attrgroup_relation" does not exist

CREATE TABLE IF NOT EXISTS pms_attr_attrgroup_relation (
    id              BIGSERIAL PRIMARY KEY,
    attr_id         BIGINT NOT NULL,
    attr_group_id   BIGINT NOT NULL,
    attr_sort       INTEGER
);

CREATE INDEX IF NOT EXISTS idx_pms_attr_attrgroup_relation_attr_id ON pms_attr_attrgroup_relation (attr_id);
CREATE INDEX IF NOT EXISTS idx_pms_attr_attrgroup_relation_attr_group_id ON pms_attr_attrgroup_relation (attr_group_id);

COMMENT ON TABLE pms_attr_attrgroup_relation IS 'Attribute & Attribute Group relation';
COMMENT ON COLUMN pms_attr_attrgroup_relation.id IS 'Primary key';
COMMENT ON COLUMN pms_attr_attrgroup_relation.attr_id IS 'Attribute ID';
COMMENT ON COLUMN pms_attr_attrgroup_relation.attr_group_id IS 'Attribute group ID';
COMMENT ON COLUMN pms_attr_attrgroup_relation.attr_sort IS 'Sort order within group';
