/*
 ============================================================
  GymSystem - AI 健身教练模块 演示数据
 ============================================================
  依赖：先执行 ai_schema.sql 建表，再执行本脚本。

  用途：让「多层记忆」在演示时真实可见，而不是空表。
    - 会员画像：3 个真实会员各一份画像
    - 训练记录：会员 1（张三）近 7 天有 3 条记录，用于演示
      「近期练过的部位本周优先换别的」
    - 对话历史 + 历史计划：1 组完整示例，用于演示历史列表

  说明：
    - 会员 id 取自 gym_system_full_rebuild.sql 的 member 表
    - 本脚本只插入 AI 模块数据，不影响其它业务表
    - 日期使用 CURDATE() 相对计算，保证「近 7 天」永远是有效的
 ============================================================
*/

SET NAMES utf8mb4;
USE `gym_system`;

-- 幂等：清空 AI 模块数据后重新灌入
DELETE FROM `ai_user_profile`;
DELETE FROM `ai_training_log`;
DELETE FROM `ai_chat_message`;
DELETE FROM `ai_plan`;

-- ----------------------------
-- 1. 用户画像
-- 会员1 张三：减脂、初级、膝盖有伤（核心演示账号）
-- 会员3 王五：增肌、中级、无伤病
-- 会员8 吴九：塑形、初级、久坐腰背不适
-- ----------------------------
INSERT INTO `ai_user_profile`
  (`profile_id`, `member_id`, `goal`, `level`, `days_per_week`, `session_minutes`, `equipment`, `injuries`, `injury_note`) VALUES
  (1, 1, 'LOSE_FAT',     'BEGINNER',     3, 60, 'DUMBBELL,MACHINE,CARDIO',        'KNEE',           '右膝半月板旧伤，深蹲和跑跳时会疼'),
  (2, 3, 'BUILD_MUSCLE', 'INTERMEDIATE', 4, 75, 'BARBELL,DUMBBELL,MACHINE,CABLE',  '',               NULL),
  (3, 8, 'SHAPE',        'BEGINNER',     2, 45, 'BODYWEIGHT,DUMBBELL,MACHINE',     'LOWER_BACK',     '久坐办公，腰容易酸，医生建议避免硬拉类');

-- ----------------------------
-- 2. 训练记录（近 7 天，演示「近期练过什么」）
-- ----------------------------
INSERT INTO `ai_training_log`
  (`member_id`, `plan_id`, `train_date`, `body_part`, `exercise_summary`, `duration_min`) VALUES
  (1, NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'CHEST',   '哑铃卧推/哑铃飞鸟/俯卧撑',      55),
  (1, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'BACK',    '高位下拉/坐姿划船',             60),
  (1, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'CARDIO',  '椭圆机/动感单车',               45),
  (3, NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'LEG',     '杠铃深蹲/腿举/腿屈伸',          70),
  (3, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'CHEST',   '杠铃卧推/上斜哑铃卧推',         75);

-- ----------------------------
-- 3. 历史计划（列表演示）
-- plan_json 结构与后端 AiPlanResult 一致
-- ----------------------------
INSERT INTO `ai_plan`
  (`member_id`, `session_id`, `title`, `summary`, `plan_json`, `warnings`, `model`, `token_usage`, `cost_ms`, `create_time`) VALUES
  (1, 'seed-session-0001', '减脂入门·膝伤友好周计划',
   '每周3练，以器械和哑铃为主，避开深蹲与跑跳，单次约55分钟',
   '{"title":"减脂入门·膝伤友好周计划","days":[{"day":1,"bodyPart":"CHEST","exercises":[{"name":"哑铃卧推","sets":4,"reps":"10-12","restSec":90},{"name":"哑铃飞鸟","sets":3,"reps":"12-15","restSec":60},{"name":"器械夹胸","sets":3,"reps":"12-15","restSec":60}]},{"day":2,"bodyPart":"BACK","exercises":[{"name":"高位下拉","sets":4,"reps":"10-12","restSec":90},{"name":"坐姿划船","sets":4,"reps":"12-15","restSec":60}]},{"day":3,"bodyPart":"CARDIO","exercises":[{"name":"椭圆机","sets":1,"reps":"30分钟","restSec":0},{"name":"动感单车低阻力","sets":1,"reps":"20分钟","restSec":0}]}],"notes":"全程避免深蹲、跑跳与弓步蹲等膝关节屈伸负荷较大的动作，训练前后各做5分钟拉伸。"}',
   '[]', 'qwen2.5:7b', 1860, 8420, DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ----------------------------
-- 4. 对话历史（对应上面的计划）
-- ----------------------------
INSERT INTO `ai_chat_message` (`member_id`, `session_id`, `role`, `content`, `create_time`) VALUES
  (1, 'seed-session-0001', 'user', '我膝盖有旧伤，帮我排个每周三天的减脂计划，能用哑铃和器械就行', DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (1, 'seed-session-0001', 'assistant', '已为你生成《减脂入门·膝伤友好周计划》：每周 3 练，胸、背、有氧交替，全程避开深蹲与跑跳类动作，单次约 55 分钟。', DATE_SUB(NOW(), INTERVAL 3 DAY));
