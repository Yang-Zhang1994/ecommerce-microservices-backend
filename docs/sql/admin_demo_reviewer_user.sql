-- Read-only admin user for resume reviewers (renren-fast / ecommerce_admin).
-- Login: demo / Demo2025!  —  Shiro SHA-256(salt + password).
-- Re-run safe: upserts user + role and refreshes role_menu grants.

BEGIN;

INSERT INTO sys_role (role_id, role_name, remark, create_user_id, create_time)
VALUES (100, 'Demo Reviewer', 'Read-only mall admin for resume reviewers', 1, NOW())
ON CONFLICT (role_id) DO UPDATE
  SET role_name = EXCLUDED.role_name,
      remark    = EXCLUDED.remark;

INSERT INTO sys_user (username, password, salt, email, mobile, status, create_user_id, create_time)
VALUES (
  'demo',
  '2408dbdd7151473e379de7ca0a954942c9f1a50440bfb0ec850954cfc51d1cff',
  'demoReviewerSalt2025',
  'demo@yangzhangtech.online',
  '0000000000',
  1,
  1,
  NOW()
)
ON CONFLICT (username) DO UPDATE
  SET password = EXCLUDED.password,
      salt     = EXCLUDED.salt,
      status   = 1;

DELETE FROM sys_user_role
WHERE user_id = (SELECT user_id FROM sys_user WHERE username = 'demo');

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 100
FROM sys_user u
WHERE u.username = 'demo';

DELETE FROM sys_role_menu WHERE role_id = 100;

-- Mall menus only: exclude 系统管理 subtree; grant folders/pages + list/info button perms.
WITH RECURSIVE sys_mgmt AS (
  SELECT menu_id FROM sys_menu WHERE menu_id = 1
  UNION ALL
  SELECT m.menu_id FROM sys_menu m
  INNER JOIN sys_mgmt s ON m.parent_id = s.menu_id
)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 100, m.menu_id
FROM sys_menu m
WHERE m.menu_id NOT IN (SELECT menu_id FROM sys_mgmt)
  AND (
    m.type IN (0, 1)
    OR (
      m.type = 2
      AND COALESCE(m.perms, '') <> ''
      AND m.perms !~ '(save|update|delete|:all)'
      AND (m.perms LIKE '%:list%' OR m.perms LIKE '%:info%' OR m.perms LIKE '%:select%')
    )
  );

COMMIT;
