/*
 ============================================================
  GymSystem - AI 健身教练模块 菜单与权限
 ============================================================
  依赖：先执行 ai_schema.sql。

  设计说明：
    1. 只给「会员」角色（sys_role.role_id = 5）分配 AI 菜单，
       不分配给管理员 / 员工 / 教练，与「本模块仅对会员开放」一致；
    2. 【不硬编码 menu_id】——原先写死 66-69，一旦复用已有库就会主键冲突。
       现在依赖表自增，再用 code 回查拿到实际 id，脚本可以重复执行、
       也可以在任何 id 起点的库上执行；
    3. 可重复执行：先按 code 删除旧记录再插入。
 ============================================================
*/

SET NAMES utf8mb4;
USE `gym_system`;

-- 幂等：删掉可能已存在的 AI 菜单及其角色关联
DELETE FROM `sys_role_menu`
 WHERE `menu_id` IN (SELECT `menu_id` FROM `sys_menu`
                      WHERE `code` IN ('sys:aiRoot','sys:aiCoach','sys:aiProfile','sys:aiPlanList'));

DELETE FROM `sys_menu`
 WHERE `code` IN ('sys:aiRoot','sys:aiCoach','sys:aiProfile','sys:aiPlanList');

-- ----------------------------
-- 1. 顶级目录（parent_id = 0）
-- ----------------------------
INSERT INTO `sys_menu`
  (`parent_id`, `title`, `code`, `name`, `path`, `url`, `type`, `icon`, `parent_name`, `order_num`, `create_time`, `update_time`)
VALUES
  (0, 'AI 健身教练', 'sys:aiRoot', 'aiRoot', '/aiRoot', '', '0', 'MagicStick', '顶级菜单', 60, NOW(), NULL);

-- 记住父菜单 id，供下面三个子菜单引用
SET @ai_root_id = (SELECT `menu_id` FROM `sys_menu` WHERE `code` = 'sys:aiRoot');

-- ----------------------------
-- 2. 三个页面菜单
--    url 必须与前端文件位置严格对应：views/ai/xxx.vue → /ai/xxx
-- ----------------------------
INSERT INTO `sys_menu`
  (`parent_id`, `title`, `code`, `name`, `path`, `url`, `type`, `icon`, `parent_name`, `order_num`, `create_time`, `update_time`)
VALUES
  (@ai_root_id, 'AI 教练',   'sys:aiCoach',    'aiCoach',    '/aiCoach',    '/ai/AiCoach',    '1', 'ChatDotRound', 'AI 健身教练', 61, NOW(), NULL),
  (@ai_root_id, '健身画像',  'sys:aiProfile',  'aiProfile',  '/aiProfile',  '/ai/AiProfile',  '1', 'User',         'AI 健身教练', 62, NOW(), NULL),
  (@ai_root_id, '历史计划',  'sys:aiPlanList', 'aiPlanList', '/aiPlanList', '/ai/AiPlanList', '1', 'Document',     'AI 健身教练', 63, NOW(), NULL);

-- ----------------------------
-- 3. 只给会员角色（role_id = 5）分配
-- ----------------------------
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 5, `menu_id` FROM `sys_menu`
 WHERE `code` IN ('sys:aiRoot','sys:aiCoach','sys:aiProfile','sys:aiPlanList');
