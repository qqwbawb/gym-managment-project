/*
 ============================================================
  GymSystem - AI 健身教练模块 建表脚本
 ============================================================
  说明：
    本脚本只创建 AI 模块的 4 张表，可重复执行（先 DROP 再 CREATE）。
    演示数据见同目录 ai_demo_seed.sql。

  表清单：
    ai_user_profile   用户画像（目标/水平/频次/时长/器械/伤病）
    ai_training_log   训练打卡记录（记忆来源：近 7 天练了什么）
    ai_chat_message   对话历史（多轮上下文）
    ai_plan           生成的训练计划（结构化 JSON + 成本观测字段）

  约定：
    1. member_id 对应 member.member_id（会员身份，不接受前端传入，取自登录态）
    2. 时间字段统一 create_time / update_time，与项目其它表保持一致
    3. 字符集 utf8mb4 / utf8mb4_bin，与现有表一致
 ============================================================
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `gym_system`;

-- ----------------------------
-- 1. 用户画像
-- 注意：一个会员一条画像，user_id 唯一
-- ----------------------------
DROP TABLE IF EXISTS `ai_user_profile`;
CREATE TABLE `ai_user_profile`  (
  `profile_id`        int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id`         int(11) NOT NULL COMMENT '会员id（member.member_id）',
  `goal`              varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '目标: LOSE_FAT 减脂 / BUILD_MUSCLE 增肌 / ENDURANCE 耐力 / SHAPE 塑形',
  `level`             varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '水平: BEGINNER / INTERMEDIATE / ADVANCED',
  `days_per_week`     tinyint(4) NULL DEFAULT 3 COMMENT '每周训练次数',
  `session_minutes`   tinyint(4) NULL DEFAULT 60 COMMENT '单次训练时长(分钟)',
  `equipment`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '可用器械,逗号分隔: BARBELL,DUMBBELL,MACHINE,CABLE,BODYWEIGHT,CARDIO',
  `injuries`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '伤病/禁忌部位,逗号分隔: KNEE,SHOULDER,LOWER_BACK,ELBOW,WRIST,ANKLE,NECK',
  `injury_note`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '伤病补充说明（用户自由填写）',
  `create_time`       datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`       datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`profile_id`) USING BTREE,
  UNIQUE INDEX `uk_ai_profile_member`(`member_id`) USING BTREE COMMENT '一个会员一条画像'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic COMMENT = 'AI 用户画像';

-- ----------------------------
-- 2. 训练记录（记忆来源）
-- 会员在计划卡片上点「完成今天训练」即写入一条
-- ----------------------------
DROP TABLE IF EXISTS `ai_training_log`;
CREATE TABLE `ai_training_log`  (
  `log_id`        int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id`     int(11) NOT NULL COMMENT '会员id',
  `plan_id`       int(11) NULL DEFAULT NULL COMMENT '来源计划id（ai_plan.plan_id），手动打卡可为空',
  `train_date`    date NOT NULL COMMENT '训练日期',
  `body_part`     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '训练部位: CHEST/BACK/LEG/SHOULDER/ARM/CORE/CARDIO',
  `exercise_summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '动作摘要，如「哑铃卧推/飞鸟」',
  `duration_min`  smallint(6) NULL DEFAULT NULL COMMENT '实际训练时长(分钟)',
  `create_time`   datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_ai_log_member_date`(`member_id`, `train_date`) USING BTREE COMMENT '按会员+日期查近期记录',
  UNIQUE INDEX `uk_ai_log_member_plan_date`(`member_id`, `plan_id`, `train_date`) USING BTREE COMMENT '同一计划同一天只能打卡一次（plan_id 为 NULL 的手动打卡不受限，MySQL 唯一索引允许多个 NULL）'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic COMMENT = 'AI 训练记录';

-- ----------------------------
-- 3. 对话历史（多轮上下文）
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message`  (
  `message_id`    int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id`     int(11) NOT NULL COMMENT '会员id',
  `session_id`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '会话id（一次连续对话）',
  `role`          varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'user / assistant',
  `content`       text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '消息内容',
  `create_time`   datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_ai_msg_member_session`(`member_id`, `session_id`, `message_id`) USING BTREE COMMENT '查某会话最近N轮'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic COMMENT = 'AI 对话记录';

-- ----------------------------
-- 4. 生成的训练计划
-- ----------------------------
DROP TABLE IF EXISTS `ai_plan`;
CREATE TABLE `ai_plan`  (
  `plan_id`       int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id`     int(11) NOT NULL COMMENT '会员id',
  `session_id`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '来源会话id',
  `title`         varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '计划标题',
  `plan_json`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '完整计划(JSON)',
  `summary`       varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '一句话摘要（列表展示用）',
  `warnings`      text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '规则引擎校验告警(JSON数组)。用 text 而非 varchar(1000)：每条修复说明是完整中文句子，多条累加很容易超长，超长会导致 insert 失败、整次生成回滚',
  `model`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '模型名称',
  `token_usage`   int(11) NULL DEFAULT NULL COMMENT 'token 消耗',
  `cost_ms`       int(11) NULL DEFAULT NULL COMMENT '生成耗时(毫秒)',
  `create_time`   datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`plan_id`) USING BTREE,
  INDEX `idx_ai_plan_member`(`member_id`, `create_time`) USING BTREE COMMENT '按会员查历史计划'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic COMMENT = 'AI 生成的训练计划';

SET FOREIGN_KEY_CHECKS = 1;
