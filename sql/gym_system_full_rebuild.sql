/*
 ============================================================
 健身房管理系统 - 数据库全量重建脚本（已适配 gym_system）
 ============================================================
 源脚本 : 25章节及之后数据库脚本.sql  (Navicat, Source Schema: gym-pro)
 适配目标: gym_system @ localhost:3306  (MySQL 8.0.46)
 适配改动:
   1. 显式 USE `gym_system`，防止误跑到其它库
   2. 保留 DROP TABLE IF EXISTS（全量重建，覆盖现有数据）
   3. 字符集统一 utf8mb4 / utf8mb4_bin，与后端 characterEncoding=utf8 匹配
   4. 显式关闭/开启外键检查，保证删除与插入顺序无依赖问题
 执行前请确认已备份（E:\GymSystem\backup\）
 ============================================================
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `gym_system`;

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `course_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '课程id',
  `course_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '课程名称',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '封面图',
  `teacher_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '授课教师',
  `course_hour` int(11) NULL DEFAULT NULL COMMENT '课时',
  `course_details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '课程详情',
  `course_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '课程价格',
  `teacher_id` int(11) NULL DEFAULT NULL COMMENT '教师',
  PRIMARY KEY (`course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course
-- ----------------------------
INSERT INTO `course` VALUES (1, '跆拳道', 'http://localhost:8089/images/cdb847d1-0503-4b24-b99d-9f37ae65a4e5.jpg', '李丽', 50, '<p><img src=\"http://localhost:8089/images/9c011f9d-f86c-45a5-9377-49e9dc8ce48b.png\" alt=\"\" data-href=\"\" style=\"\"/></p>', 500.00, 5);
INSERT INTO `course` VALUES (2, '篮球专训11', 'http://localhost:8089/images/7c01a1f5-a1c6-4bd1-8508-e859062c7041.jpg', '张明', 60, '<p>非常精彩的技术讲解，演示，实际操练11;</p><p><img src=\"http://localhost:8089/images/e2cab6c1-b6d8-4368-8bd5-52bafa69b384.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', 10001.00, 4);
INSERT INTO `course` VALUES (3, '99', 'http://localhost:8089/images/0890a18a-e906-49f2-b1f5-2a59d0bfd635.jpg', '李丽', 99, '<p><img src=\"http://localhost:8089/images/f62b0eb2-cb2e-4c17-b094-ff00b67af453.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p><p>非常好！</p>', 99.00, 5);
INSERT INTO `course` VALUES (4, '足球专训', 'http://localhost:8089/images/d0674c5b-2ef8-466d-bb0c-ee51c568b6a8.jpg', '张明', 60, '<p>实际操练，非常好!</p><p style=\"text-align: center;\"><img src=\"http://localhost:8089/images/a50ffd0a-0d1c-42c3-bf8b-9e8872fa062d.webp\" alt=\"\" data-href=\"\" style=\"\"></p>', 666.00, 4);
INSERT INTO `course` VALUES (5, '测试', 'http://localhost:8089/images/1ef1fe21-be21-48c2-aecb-c1cd2094b7e3.jpg', '李四', 100, '<p>测试</p>', 2000.00, 2);
INSERT INTO `course` VALUES (6, '测试1', 'http://localhost:8089/images/faa1e2b9-1741-4910-b2cc-00c0f1c9aa0c.jpg', '张明', 20, '<p>测试1</p>', 200.00, 4);
INSERT INTO `course` VALUES (8, '444', 'http://localhost:8089/images/d29d52c8-6167-4c44-b0cb-08ee34c7034b.jpg', '李丽', 44, '<p>111</p><p><img src=\"http://localhost:8089/images/7cf6c471-23e0-4aa8-9cdc-8e3230690479.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', 444.00, 5);
INSERT INTO `course` VALUES (9, '666', 'http://localhost:8089/images/0235ba9e-5855-4288-bd90-631e47c64beb.jpg', '李丽', 66, '<p>6666</p><p><img src=\"http://localhost:8089/images/95bdad4a-2aca-4134-a262-0390fb5f7b26.png\" alt=\"\" data-href=\"\" style=\"\"/></p>', 666.00, 5);
INSERT INTO `course` VALUES (10, '测试课程', 'http://localhost:8089/images/33bcdb29-0e76-4ae8-a24c-59d237d513d8.jpeg', '999', 30, '<p>测试</p>', 1000.00, 15);

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `goods_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `price` decimal(18, 2) NULL DEFAULT NULL COMMENT '价格',
  `store` int(11) NULL DEFAULT NULL COMMENT '库存',
  `image` varchar(220) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '图片',
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '描述',
  `unit` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '单位',
  `specs` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '规格',
  PRIMARY KEY (`goods_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, '冰红茶', 6.00, 97, 'http://localhost:8089/images/6f889207-6017-4bc3-8a2c-81a95e203c08.jpg', '<p>呵呵</p><p><img src=\"http://localhost:8089/images/777054cb-62a3-4bed-84d5-6211d905ece6.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', '瓶', '500ml/瓶');
INSERT INTO `goods` VALUES (2, '测试666', 1166.00, 1165, 'http://localhost:8089/images/014293e5-da41-417e-877c-1c21af364300.jpg', '<p>412434</p><p><img src=\"http://localhost:8089/images/3818e664-9ccb-46b0-b09a-63dc0c25659c.webp\" alt=\"\" data-href=\"\" style=\"\"/></p>', '1166', '1166');
INSERT INTO `goods` VALUES (8, '22', 22.00, 2, 'http://localhost:8089/images/27662d82-9647-4753-a929-4434e6dd0fcd.jpg', '<p>44444</p>', '22', '22');
INSERT INTO `goods` VALUES (9, '33', 33.00, 33, 'http://localhost:8089/images/27cd0d92-efaa-4919-a0cc-9a8544418d91.jpg', '<p>33333</p>', '33', '33');
INSERT INTO `goods` VALUES (10, '666', 666.00, 66, 'http://localhost:8089/images/503ffb54-b262-4940-8b07-90aa8263837c.jpg', '<p>66</p><p><img src=\"http://localhost:8089/images/9c799828-0976-4761-9fab-50e8f2c4dcc5.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', '66', '66');
INSERT INTO `goods` VALUES (11, '苹果', 11.00, 1, 'http://localhost:8089/images/e731b1f6-6ea7-4cf9-9d4b-3d8b57f10182.jpg', '<p>1</p>', '1', '1');

-- ----------------------------
-- Table structure for goods_order
-- ----------------------------
DROP TABLE IF EXISTS `goods_order`;
CREATE TABLE `goods_order`  (
  `order_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `goods_id` int(11) NULL DEFAULT NULL COMMENT '商品id',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `price` decimal(18, 2) NULL DEFAULT NULL COMMENT '价格',
  `image` varchar(220) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '图片',
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '描述',
  `unit` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '单位',
  `specs` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '规格',
  `num` int(11) NULL DEFAULT NULL COMMENT '数量',
  `total_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '总价',
  `control_user` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '操作人',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '下单时间',
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods_order
-- ----------------------------
INSERT INTO `goods_order` VALUES (8, 1, '冰红茶', 6.00, 'http://localhost:8089/images/6f889207-6017-4bc3-8a2c-81a95e203c08.jpg', '<p>呵呵</p><p><img src=\"http://localhost:8089/images/777054cb-62a3-4bed-84d5-6211d905ece6.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', '瓶', '500ml/瓶', 2, 12.00, 'admin', '2022-07-05 18:27:23');
INSERT INTO `goods_order` VALUES (9, 2, '测试666', 1166.00, 'http://localhost:8089/images/014293e5-da41-417e-877c-1c21af364300.jpg', '<p>412434</p><p><img src=\"http://localhost:8089/images/3818e664-9ccb-46b0-b09a-63dc0c25659c.webp\" alt=\"\" data-href=\"\" style=\"\"/></p>', '1166', '1166', 2, 2332.00, 'admin', '2022-07-06 18:27:23');
INSERT INTO `goods_order` VALUES (10, 8, '22', 22.00, 'http://localhost:8089/images/27662d82-9647-4753-a929-4434e6dd0fcd.jpg', '<p>44444</p>', '22', '22', 1, 22.00, 'admin', '2022-07-06 18:27:35');
INSERT INTO `goods_order` VALUES (11, 9, '33', 33.00, 'http://localhost:8089/images/27cd0d92-efaa-4919-a0cc-9a8544418d91.jpg', '<p>33333</p>', '33', '33', 1, 33.00, 'admin', '2022-07-06 18:27:35');
INSERT INTO `goods_order` VALUES (12, 1, '冰红茶', 6.00, 'http://localhost:8089/images/6f889207-6017-4bc3-8a2c-81a95e203c08.jpg', '<p>呵呵</p><p><img src=\"http://localhost:8089/images/777054cb-62a3-4bed-84d5-6211d905ece6.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', '瓶', '500ml/瓶', 2, 12.00, 'admin', '2022-07-06 18:27:52');
INSERT INTO `goods_order` VALUES (13, 2, '测试666', 1166.00, 'http://localhost:8089/images/014293e5-da41-417e-877c-1c21af364300.jpg', '<p>412434</p><p><img src=\"http://localhost:8089/images/3818e664-9ccb-46b0-b09a-63dc0c25659c.webp\" alt=\"\" data-href=\"\" style=\"\"/></p>', '1166', '1166', 2, 2332.00, 'admin', '2022-07-06 18:27:52');
INSERT INTO `goods_order` VALUES (14, 8, '22', 22.00, 'http://localhost:8089/images/27662d82-9647-4753-a929-4434e6dd0fcd.jpg', '<p>44444</p>', '22', '22', 2, 44.00, 'admin', '2022-07-06 18:27:52');
INSERT INTO `goods_order` VALUES (15, 9, '33', 33.00, 'http://localhost:8089/images/27cd0d92-efaa-4919-a0cc-9a8544418d91.jpg', '<p>33333</p>', '33', '33', 2, 66.00, 'admin', '2022-07-06 18:27:52');
INSERT INTO `goods_order` VALUES (16, 1, '冰红茶', 6.00, 'http://localhost:8089/images/6f889207-6017-4bc3-8a2c-81a95e203c08.jpg', '<p>呵呵</p><p><img src=\"http://localhost:8089/images/777054cb-62a3-4bed-84d5-6211d905ece6.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', '瓶', '500ml/瓶', 1, 6.00, '张三', '2022-07-07 21:38:05');
INSERT INTO `goods_order` VALUES (17, 1, '冰红茶', 6.00, 'http://localhost:8089/images/6f889207-6017-4bc3-8a2c-81a95e203c08.jpg', '<p>呵呵</p><p><img src=\"http://localhost:8089/images/777054cb-62a3-4bed-84d5-6211d905ece6.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', '瓶', '500ml/瓶', 4, 24.00, 'admin', '2022-07-11 10:14:38');
INSERT INTO `goods_order` VALUES (18, 2, '测试666', 1166.00, 'http://localhost:8089/images/014293e5-da41-417e-877c-1c21af364300.jpg', '<p>412434</p><p><img src=\"http://localhost:8089/images/3818e664-9ccb-46b0-b09a-63dc0c25659c.webp\" alt=\"\" data-href=\"\" style=\"\"/></p>', '1166', '1166', 3, 3498.00, 'admin', '2022-07-11 10:14:38');
INSERT INTO `goods_order` VALUES (19, 1, '冰红茶', 6.00, 'http://localhost:8089/images/6f889207-6017-4bc3-8a2c-81a95e203c08.jpg', '<p>呵呵</p><p><img src=\"http://localhost:8089/images/777054cb-62a3-4bed-84d5-6211d905ece6.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', '瓶', '500ml/瓶', 1, 6.00, 'admin', '2023-11-11 22:22:18');
INSERT INTO `goods_order` VALUES (20, 1, '冰红茶', 6.00, 'http://localhost:8089/images/6f889207-6017-4bc3-8a2c-81a95e203c08.jpg', '<p>呵呵</p><p><img src=\"http://localhost:8089/images/777054cb-62a3-4bed-84d5-6211d905ece6.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', '瓶', '500ml/瓶', 1, 6.00, 'admin1', '2023-11-13 10:21:12');
INSERT INTO `goods_order` VALUES (21, 2, '测试666', 1166.00, 'http://localhost:8089/images/014293e5-da41-417e-877c-1c21af364300.jpg', '<p>412434</p><p><img src=\"http://localhost:8089/images/3818e664-9ccb-46b0-b09a-63dc0c25659c.webp\" alt=\"\" data-href=\"\" style=\"\"/></p>', '1166', '1166', 1, 1166.00, 'admin1', '2023-11-13 10:21:12');
INSERT INTO `goods_order` VALUES (22, 1, '冰红茶', 6.00, 'http://localhost:8089/images/6f889207-6017-4bc3-8a2c-81a95e203c08.jpg', '<p>呵呵</p><p><img src=\"http://localhost:8089/images/777054cb-62a3-4bed-84d5-6211d905ece6.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', '瓶', '500ml/瓶', 1, 6.00, '张明', '2024-03-10 10:53:36');

-- ----------------------------
-- Table structure for lost
-- ----------------------------
DROP TABLE IF EXISTS `lost`;
CREATE TABLE `lost`  (
  `lost_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `lost_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `found_time` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '捡到时间',
  `found_addres` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '捡到地址',
  `found_person` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '捡到人',
  `found_phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '捡到人电话',
  `status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT ' 认领状态 0：未认领 1：已认领',
  `lost_person` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '认领人',
  PRIMARY KEY (`lost_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lost
-- ----------------------------
INSERT INTO `lost` VALUES (1, '钱包', '2022-06-28', '大厅', '李四', '18787171906', '1', '张明');
INSERT INTO `lost` VALUES (2, '手机', '2022-06-02', '厕所', '张三', '187871719036', '1', '赵柳');
INSERT INTO `lost` VALUES (3, '测试', '2022-06-28', '11', '11', '11', '1', '李记');
INSERT INTO `lost` VALUES (5, '777', '2022-06-08', '77', '77', '77', '1', '77');

-- ----------------------------
-- Table structure for material
-- ----------------------------
DROP TABLE IF EXISTS `material`;
CREATE TABLE `material`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '器材id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '器材名称',
  `num_total` int(11) NULL DEFAULT NULL COMMENT '数量',
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '简介',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of material
-- ----------------------------
INSERT INTO `material` VALUES (1, '跑步机', 10, '跑步机');
INSERT INTO `material` VALUES (2, '篮球', 20, '篮球');
INSERT INTO `material` VALUES (3, '测试', 12, '测试');

-- ----------------------------
-- Table structure for member
-- ----------------------------
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member`  (
  `member_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '会员id',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '姓名',
  `sex` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '性别',
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '电话',
  `age` int(11) NULL DEFAULT NULL COMMENT '年龄',
  `birthday` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '生日',
  `height` int(11) NULL DEFAULT NULL COMMENT '身高',
  `weight` int(11) NULL DEFAULT NULL COMMENT '体重',
  `waist` int(11) NULL DEFAULT NULL COMMENT '腰围',
  `join_time` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '加入时间',
  `end_time` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '到期时间',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会员卡号',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '密码',
  `status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '状态 1：启用  0：停用',
  `money` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '充值金额',
  `card_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会员类型',
  `card_day` int(11) NULL DEFAULT NULL COMMENT '天数',
  `price` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '会员价格',
  `is_account_non_expired` tinyint(4) NULL DEFAULT NULL COMMENT '帐户是否过期(1 未过期，0已过期)',
  `is_account_non_locked` tinyint(4) NULL DEFAULT NULL COMMENT '帐户是否被锁定(1 未锁定，0已锁定)',
  `is_credentials_non_expired` tinyint(4) NULL DEFAULT NULL COMMENT '密码是否过期(1 未过期，0已过期)',
  `is_enabled` tinyint(4) NULL DEFAULT NULL COMMENT '帐户是否可用(1 可用，0 删除用户)',
  PRIMARY KEY (`member_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of member
-- ----------------------------
INSERT INTO `member` VALUES (1, '张三', '0', '18787171906', 100, '2022-06-15', 180, 200, 100, '2022-06-16', '2022-08-06', '2022001', '$2a$10$myAOP14vKQVBF5EoX2QJPOpO1fU6mLOLZHIABGNz9KdmFPaG1zNKG', '1', 4256.00, '1月卡', 30, 999.00, 1, 1, 1, 1);
INSERT INTO `member` VALUES (2, '李四', '1', '18787171906', NULL, '', NULL, NULL, NULL, '2022-06-07', '2022-06-24', '2022002', '$2a$10$0HVjw0UCs.eFOEti4TP4JurkdjO6pGaY65XNLDojEYX7DV/zQSVC.', '1', 1300.00, '7天卡', 7, 500.00, 1, 1, 1, 1);
INSERT INTO `member` VALUES (3, '王五', '0', '187871523300', NULL, '', NULL, NULL, NULL, '2022-06-05', '2022-09-05', '2022003', '$2a$10$h84zWzUeB9xFtUjA7Wn7T.RoIUzivRfdSHrIFtHk0cI1nL9JsP11O', '1', 1300.00, '1月卡', 30, 999.00, 1, 1, 1, 1);
INSERT INTO `member` VALUES (4, '赵柳', '1', '18787171906', 60, '', 175, 60, 50, '2022-07-06', '2022-08-07', '4561313213213', '$2a$10$VwxPPOZ1gWeoc5CqNhRAXu6gtwlVuQ05BtPgclYYzmBvdRf3zIK26', '1', 801.00, '1月卡', 30, 999.00, 1, 1, 1, 1);
INSERT INTO `member` VALUES (6, '9999', '0', '9999', NULL, '', NULL, NULL, NULL, '2022-07-04', '2022-08-10', '999999', '$2a$10$l/20uClzP/.tQfT/j0G39uD9pihFLwVK5f5vBDnEDgWsQyLsB63Hq', '1', 300.00, '1月卡', 30, 999.00, 1, 1, 1, 1);
INSERT INTO `member` VALUES (7, '测试', '0', '123', 1, '2023-11-11', 150, 150, NULL, '2023-11-11', '2023-12-11', '20231111', '$2a$10$dJq/Q7Xfy5pmlGzVKh1vBeC5569KDs.y/HLw1tI2MSJewNKNJJiNe', '1', 4500.00, '1月卡', 30, 999.00, 1, 1, 1, 1);
INSERT INTO `member` VALUES (8, '测试会员', '0', '11111', NULL, '', NULL, NULL, NULL, '2023-11-13', '2023-12-13', '20231113', '$2a$10$ZtJ/GCvJgZp7R4qw8yKd..LQ1w2kv73bqSadDUIcfv9vFzF9YvEZW', '1', 19401.00, '1月卡', 30, 999.00, 1, 1, 1, 1);
INSERT INTO `member` VALUES (9, '新测试会员1', '0', '1465464646', 100, '2024-03-11', 100, 100, 100, '2024-03-12', '2024-04-23', '2024001', '$2a$10$t85yDYkCYHO/1aK1JOhQkeBm7CSrYEmy54zNnDD/wxQgVH5IIAVZe', '1', 1534.00, '7天卡', 7, 500.00, 1, 1, 1, 1);

-- ----------------------------
-- Table structure for member_apply
-- ----------------------------
DROP TABLE IF EXISTS `member_apply`;
CREATE TABLE `member_apply`  (
  `apply_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id` int(11) NULL DEFAULT NULL COMMENT '会员id',
  `card_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会员类型',
  `card_day` int(11) NULL DEFAULT NULL COMMENT '天数',
  `price` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '价格',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`apply_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of member_apply
-- ----------------------------
INSERT INTO `member_apply` VALUES (1, 1, '7天卡', 7, 500.00, '2022-06-16 16:11:49', NULL);
INSERT INTO `member_apply` VALUES (2, 2, '1天体验卡', 1, 1.00, '2022-06-16 16:12:13', NULL);
INSERT INTO `member_apply` VALUES (3, 2, '7天卡', 7, 500.00, '2022-06-16 16:12:28', NULL);
INSERT INTO `member_apply` VALUES (4, 1, '7天卡', 7, 500.00, '2022-07-03 18:04:57', NULL);
INSERT INTO `member_apply` VALUES (5, 1, '7天卡', 7, 500.00, '2022-07-03 18:05:08', NULL);
INSERT INTO `member_apply` VALUES (6, 1, '1月卡', 30, 999.00, '2022-07-03 18:05:24', NULL);
INSERT INTO `member_apply` VALUES (7, 3, '1月卡', 30, 999.00, '2022-07-07 16:46:29', NULL);
INSERT INTO `member_apply` VALUES (8, 4, '1月卡', 30, 999.00, '2022-07-08 11:04:29', NULL);
-- 适配说明: 源脚本此行 member_id=5 在 member 表中不存在（member 仅定义 1,2,3,4,6,7,8,9），
--           属于源脚本脏数据。保留会导致该条办卡记录查不到会员，已注释掉。
-- INSERT INTO `member_apply` VALUES (9, 5, '1天体验卡', 1, 1.00, '2022-07-10 22:03:27', NULL);
INSERT INTO `member_apply` VALUES (10, 3, '1月卡', 30, 999.00, '2022-07-11 09:56:29', NULL);
INSERT INTO `member_apply` VALUES (11, 6, '1月卡', 30, 999.00, '2022-07-11 10:08:17', NULL);
INSERT INTO `member_apply` VALUES (12, 7, '1月卡', 30, 999.00, '2023-11-11 22:03:33', NULL);
INSERT INTO `member_apply` VALUES (13, 8, '1月卡', 30, 999.00, '2023-11-13 10:17:24', NULL);
INSERT INTO `member_apply` VALUES (14, 9, '1月卡', 30, 999.00, '2024-03-10 23:47:16', NULL);
INSERT INTO `member_apply` VALUES (15, 9, '7天卡', 7, 500.00, '2024-03-10 23:54:05', NULL);
INSERT INTO `member_apply` VALUES (16, 9, '7天卡', 7, 500.00, '2024-03-10 23:54:43', NULL);

-- ----------------------------
-- Table structure for member_card
-- ----------------------------
DROP TABLE IF EXISTS `member_card`;
CREATE TABLE `member_card`  (
  `card_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '标题',
  `card_type` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '卡类型  1:天卡  2：周卡 3：月卡 4：年卡',
  `price` decimal(18, 2) NULL DEFAULT NULL COMMENT '价格',
  `card_day` int(11) NULL DEFAULT NULL COMMENT '天数',
  `status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '状态 1：启用 0：停用',
  PRIMARY KEY (`card_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of member_card
-- ----------------------------
INSERT INTO `member_card` VALUES (1, '1天体验卡', '1', 1.00, 1, '1');
INSERT INTO `member_card` VALUES (2, '7天卡', '2', 500.00, 7, '1');
INSERT INTO `member_card` VALUES (3, '1月卡', '3', 999.00, 30, '1');
INSERT INTO `member_card` VALUES (4, '年卡', '4', 20000.00, 365, '1');

-- ----------------------------
-- Table structure for member_course
-- ----------------------------
DROP TABLE IF EXISTS `member_course`;
CREATE TABLE `member_course`  (
  `member_course_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` int(11) NULL DEFAULT NULL COMMENT '课程id',
  `course_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '课程名称',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '封面图',
  `teacher_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '授课教师',
  `course_hour` int(11) NULL DEFAULT NULL COMMENT '课时',
  `course_details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '课程详情',
  `course_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '课程价格',
  `teacher_id` int(11) NULL DEFAULT NULL COMMENT '教师id',
  `member_id` int(11) NULL DEFAULT NULL COMMENT '会员id',
  `status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '0' COMMENT '0:报名成功 1：退款',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`member_course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of member_course
-- ----------------------------
INSERT INTO `member_course` VALUES (4, 1, '跆拳道', 'http://localhost:8089/images/cdb847d1-0503-4b24-b99d-9f37ae65a4e5.jpg', '李丽', 50, '<p><img src=\"http://localhost:8089/images/9c011f9d-f86c-45a5-9377-49e9dc8ce48b.png\" alt=\"\" data-href=\"\" style=\"\"/></p>', 500.00, 5, 1, '1', NULL);
INSERT INTO `member_course` VALUES (5, 6, '测试1', 'http://localhost:8089/images/faa1e2b9-1741-4910-b2cc-00c0f1c9aa0c.jpg', '张明', 20, '<p>测试1</p>', 200.00, 4, 1, '1', NULL);
INSERT INTO `member_course` VALUES (6, 1, '跆拳道', 'http://localhost:8089/images/cdb847d1-0503-4b24-b99d-9f37ae65a4e5.jpg', '李丽', 50, '<p><img src=\"http://localhost:8089/images/9c011f9d-f86c-45a5-9377-49e9dc8ce48b.png\" alt=\"\" data-href=\"\" style=\"\"/></p>', 500.00, 5, 3, '0', NULL);
INSERT INTO `member_course` VALUES (7, 1, '跆拳道', 'http://localhost:8089/images/cdb847d1-0503-4b24-b99d-9f37ae65a4e5.jpg', '李丽', 50, '<p><img src=\"http://localhost:8089/images/9c011f9d-f86c-45a5-9377-49e9dc8ce48b.png\" alt=\"\" data-href=\"\" style=\"\"/></p>', 500.00, 5, 4, '0', NULL);
INSERT INTO `member_course` VALUES (8, 3, '99', 'http://localhost:8089/images/0890a18a-e906-49f2-b1f5-2a59d0bfd635.jpg', '李丽', 99, '<p><img src=\"http://localhost:8089/images/f62b0eb2-cb2e-4c17-b094-ff00b67af453.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p><p>非常好！</p>', 99.00, 5, 4, '0', NULL);
INSERT INTO `member_course` VALUES (9, 6, '测试1', 'http://localhost:8089/images/faa1e2b9-1741-4910-b2cc-00c0f1c9aa0c.jpg', '张明', 20, '<p>测试1</p>', 200.00, 4, 3, '0', NULL);
INSERT INTO `member_course` VALUES (10, 1, '跆拳道', 'http://localhost:8089/images/cdb847d1-0503-4b24-b99d-9f37ae65a4e5.jpg', '李丽', 50, '<p><img src=\"http://localhost:8089/images/9c011f9d-f86c-45a5-9377-49e9dc8ce48b.png\" alt=\"\" data-href=\"\" style=\"\"/></p>', 500.00, 5, 6, '0', NULL);
INSERT INTO `member_course` VALUES (11, 6, '测试1', 'http://localhost:8089/images/faa1e2b9-1741-4910-b2cc-00c0f1c9aa0c.jpg', '张明', 20, '<p>测试1</p>', 200.00, 4, 6, '0', NULL);
INSERT INTO `member_course` VALUES (12, 6, '测试1', 'http://localhost:8089/images/faa1e2b9-1741-4910-b2cc-00c0f1c9aa0c.jpg', '张明', 20, '<p>测试1</p>', 200.00, 4, 4, '0', NULL);
INSERT INTO `member_course` VALUES (13, 1, '跆拳道', 'http://localhost:8089/images/cdb847d1-0503-4b24-b99d-9f37ae65a4e5.jpg', '李丽', 50, '<p><img src=\"http://localhost:8089/images/9c011f9d-f86c-45a5-9377-49e9dc8ce48b.png\" alt=\"\" data-href=\"\" style=\"\"/></p>', 500.00, 5, 7, '0', NULL);
INSERT INTO `member_course` VALUES (14, 1, '跆拳道', 'http://localhost:8089/images/cdb847d1-0503-4b24-b99d-9f37ae65a4e5.jpg', '李丽', 50, '<p><img src=\"http://localhost:8089/images/9c011f9d-f86c-45a5-9377-49e9dc8ce48b.png\" alt=\"\" data-href=\"\" style=\"\"/></p>', 500.00, 5, 8, '0', NULL);
INSERT INTO `member_course` VALUES (15, 3, '99', 'http://localhost:8089/images/0890a18a-e906-49f2-b1f5-2a59d0bfd635.jpg', '李丽', 99, '<p><img src=\"http://localhost:8089/images/f62b0eb2-cb2e-4c17-b094-ff00b67af453.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p><p>非常好！</p>', 99.00, 5, 8, '0', NULL);
INSERT INTO `member_course` VALUES (16, 8, '444', 'http://localhost:8089/images/d29d52c8-6167-4c44-b0cb-08ee34c7034b.jpg', '李丽', 44, '<p>111</p><p><img src=\"http://localhost:8089/images/7cf6c471-23e0-4aa8-9cdc-8e3230690479.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>', 444.00, 5, 1, '0', '2024-03-10 10:48:32');
INSERT INTO `member_course` VALUES (17, 1, '跆拳道', 'http://localhost:8089/images/cdb847d1-0503-4b24-b99d-9f37ae65a4e5.jpg', '李丽', 50, '<p><img src=\"http://localhost:8089/images/9c011f9d-f86c-45a5-9377-49e9dc8ce48b.png\" alt=\"\" data-href=\"\" style=\"\"/></p>', 500.00, 5, 9, '1', '2024-03-10 23:55:36');
INSERT INTO `member_course` VALUES (18, 4, '足球专训', 'http://localhost:8089/images/d0674c5b-2ef8-466d-bb0c-ee51c568b6a8.jpg', '张明', 60, '<p>实际操练，非常好!</p><p style=\"text-align: center;\"><img src=\"http://localhost:8089/images/a50ffd0a-0d1c-42c3-bf8b-9e8872fa062d.webp\" alt=\"\" data-href=\"\" style=\"\"></p>', 666.00, 4, 9, '0', '2024-03-10 23:56:26');
INSERT INTO `member_course` VALUES (19, 10, '测试课程', 'http://localhost:8089/images/33bcdb29-0e76-4ae8-a24c-59d237d513d8.jpeg', '999', 30, '<p>测试</p>', 1000.00, 15, 9, '0', '2024-03-11 00:47:26');

-- ----------------------------
-- Table structure for member_recharge
-- ----------------------------
DROP TABLE IF EXISTS `member_recharge`;
CREATE TABLE `member_recharge`  (
  `recharge_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '充值id',
  `member_id` int(11) NULL DEFAULT NULL COMMENT '会员id',
  `money` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '充值金额',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`recharge_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of member_recharge
-- ----------------------------
INSERT INTO `member_recharge` VALUES (8, 1, 500.00, '2022-07-03 15:07:04', 'admin');
INSERT INTO `member_recharge` VALUES (9, 2, 1000.00, '2022-07-03 15:07:12', 'admin');
INSERT INTO `member_recharge` VALUES (10, 3, 2000.00, '2022-07-03 15:07:20', 'admin');
INSERT INTO `member_recharge` VALUES (11, 1, 4000.00, '2022-07-03 15:07:29', 'admin');
INSERT INTO `member_recharge` VALUES (12, 3, 1000.00, '2022-07-07 16:46:21', 'admin');
INSERT INTO `member_recharge` VALUES (13, 4, 600.00, '2022-07-08 11:04:36', 'admin');
INSERT INTO `member_recharge` VALUES (15, 6, 1000.00, '2022-07-11 10:08:27', 'admin');
INSERT INTO `member_recharge` VALUES (16, 4, 1000.00, '2023-11-11 21:57:12', 'admin1');
INSERT INTO `member_recharge` VALUES (17, 7, 5000.00, '2023-11-11 22:03:42', 'admin1');
INSERT INTO `member_recharge` VALUES (18, 8, 20000.00, '2023-11-13 10:17:46', 'admin1');
INSERT INTO `member_recharge` VALUES (19, 9, 100.00, '2024-03-11 00:44:07', 'admin1');
INSERT INTO `member_recharge` VALUES (20, 9, 100.00, '2024-03-11 00:44:30', 'admin1');
INSERT INTO `member_recharge` VALUES (21, 9, 2000.00, '2024-03-11 00:47:04', 'admin1');

-- ----------------------------
-- Table structure for member_role
-- ----------------------------
DROP TABLE IF EXISTS `member_role`;
CREATE TABLE `member_role`  (
  `member_role_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '会员角色id',
  `member_id` int(11) NULL DEFAULT NULL COMMENT '会员id',
  `role_id` int(11) NULL DEFAULT NULL COMMENT '角色id',
  PRIMARY KEY (`member_role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of member_role
-- ----------------------------
INSERT INTO `member_role` VALUES (12, 2, 5);
INSERT INTO `member_role` VALUES (14, 3, 5);
INSERT INTO `member_role` VALUES (16, 1, 5);
INSERT INTO `member_role` VALUES (17, 4, 5);
INSERT INTO `member_role` VALUES (19, 6, 5);
INSERT INTO `member_role` VALUES (20, 7, 5);
INSERT INTO `member_role` VALUES (21, 8, 5);
INSERT INTO `member_role` VALUES (24, 9, 5);

-- ----------------------------
-- Table structure for suggest
-- ----------------------------
DROP TABLE IF EXISTS `suggest`;
CREATE TABLE `suggest`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '反馈内容',
  `date_time` datetime(0) NULL DEFAULT NULL COMMENT '反馈时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of suggest
-- ----------------------------
INSERT INTO `suggest` VALUES (1, '器材太少', '建议增加器材', '2022-06-29 19:07:32');
INSERT INTO `suggest` VALUES (4, '教练来的太迟', '教练来的太迟', '2022-06-29 19:51:14');
INSERT INTO `suggest` VALUES (5, '环境不太干净', '环境不太干净', '2022-06-29 19:53:04');
INSERT INTO `suggest` VALUES (6, '11', '111', '2023-11-13 10:19:31');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '菜单id',
  `parent_id` int(11) NOT NULL COMMENT '父级id',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '菜单名称',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '权限字段',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '路由名称',
  `path` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '路由path',
  `url` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '组件路径',
  `type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '类型(0 目录 1菜单，2按钮)',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '图标',
  `parent_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '上级菜单名称',
  `order_num` int(11) NULL DEFAULT NULL COMMENT '序号',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 66 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '系统管理', 'sys:manage', 'system', '/system', '', '0', 'Setting', '顶级菜单', 1, '2022-06-13 11:20:17', NULL);
INSERT INTO `sys_menu` VALUES (2, 1, '员工管理', 'sys:user', 'userList', '/userList', '/system/user/UserList', '1', 'UserFilled', '系统管理', 2, '2022-06-13 11:24:39', '2022-06-13 14:21:37');
INSERT INTO `sys_menu` VALUES (3, 1, '角色管理', 'sys:role', 'roleList', '/roleList', '/system/role/RoleList', '1', 'Wallet', '系统管理', 3, '2022-06-13 11:26:25', NULL);
INSERT INTO `sys_menu` VALUES (4, 0, '会员管理', 'sys:memberRoot', 'memberRoot', '/memberRoot', '', '0', 'Setting', '顶级菜单', 2, '2022-06-13 13:30:37', NULL);
INSERT INTO `sys_menu` VALUES (5, 4, '会员卡类型', 'sys:cardType', 'cardType', '/cardType', '/member/type/CardType', '1', 'UserFilled', '会员管理', 4, '2022-06-13 13:31:42', NULL);
INSERT INTO `sys_menu` VALUES (6, 4, '会员管理', 'sys:memberList', 'memberList', '/memberList', '/member/list/MemberList', '1', 'Wallet', '会员管理', 5, '2022-06-13 13:32:38', '2022-06-13 14:21:51');
INSERT INTO `sys_menu` VALUES (7, 4, '我的充值', 'sys:myFee', 'myFee', '/myFee', '/member/fee/MyFee', '1', 'Menu', '会员管理', 6, '2022-06-13 13:33:24', '2022-07-02 18:40:53');
INSERT INTO `sys_menu` VALUES (8, 2, '新增', 'sys:user:add', '', '', '', '2', '', '员工管理', 1, '2022-06-13 13:33:56', NULL);
INSERT INTO `sys_menu` VALUES (9, 2, '编辑', 'sys:user:edit', '', '', '', '2', '', '员工管理', 2, '2022-06-13 13:34:16', NULL);
INSERT INTO `sys_menu` VALUES (10, 2, '删除', 'sys:user:delete', '', '', '', '2', '', '员工管理', 3, '2022-06-13 13:34:34', '2022-06-13 14:21:06');
INSERT INTO `sys_menu` VALUES (11, 3, '新增', 'sys:role:add', '', '', '', '2', '', '角色管理', 1, '2022-06-13 13:35:01', NULL);
INSERT INTO `sys_menu` VALUES (12, 3, '编辑', 'sys:role:edit', '', '', '', '2', '', '角色管理', 2, '2022-06-13 13:35:17', NULL);
INSERT INTO `sys_menu` VALUES (13, 3, '删除', 'sys:role:delete', '', '', '', '2', '', '角色管理', 3, '2022-06-13 13:35:33', NULL);
INSERT INTO `sys_menu` VALUES (14, 5, '新增', 'sys:memberRoot:add', '', '', '', '2', '', '会员卡类型', 1, '2022-06-13 13:35:59', NULL);
INSERT INTO `sys_menu` VALUES (15, 5, '编辑', 'sys:memberRoot:edit', '', '', '', '2', '', '会员卡类型', 2, '2022-06-13 13:36:21', NULL);
INSERT INTO `sys_menu` VALUES (16, 5, '删除', 'sys:memberRoot:delete', '', '', '', '2', '', '会员卡类型', 3, '2022-06-13 13:36:35', NULL);
INSERT INTO `sys_menu` VALUES (17, 1, '菜单管理', 'sys:menu', 'menuList', '/menuList', '/system/menu/MenuList', '1', 'Menu', '系统管理', 7, '2022-07-02 18:19:56', NULL);
INSERT INTO `sys_menu` VALUES (18, 17, '新增', 'sys:menu:add', '', '', '', '2', '', '菜单管理', 8, '2022-07-02 18:20:23', NULL);
INSERT INTO `sys_menu` VALUES (19, 17, '编辑', 'sys:menu:edit', '', '', '', '2', '', '菜单管理', 9, '2022-07-02 18:20:41', NULL);
INSERT INTO `sys_menu` VALUES (20, 17, '删除', 'sys:menu:delete', '', '', '', '2', '', '菜单管理', 11, '2022-07-02 18:20:58', NULL);
INSERT INTO `sys_menu` VALUES (21, 6, '新增', 'sys:memberList:add', '', '', '', '2', '', '会员管理', 12, '2022-07-02 18:22:06', NULL);
INSERT INTO `sys_menu` VALUES (22, 6, '编辑', 'sys:memberList:edit', '', '', '', '2', '', '会员管理', 13, '2022-07-02 18:22:32', NULL);
INSERT INTO `sys_menu` VALUES (23, 6, '删除', 'sys:memberList:delete', '', '', '', '2', '', '会员管理', 14, '2022-07-02 18:22:49', NULL);
INSERT INTO `sys_menu` VALUES (24, 6, '充值', 'sys:memberList:rechart', '', '', '', '2', '', '会员管理', 15, '2022-07-02 18:23:41', NULL);
INSERT INTO `sys_menu` VALUES (25, 6, '办卡', 'sys:memberList:setCard', '', '', '', '2', '', '会员管理', 16, '2022-07-02 18:24:18', NULL);
INSERT INTO `sys_menu` VALUES (26, 0, '课程管理', 'sys:courseRoot', 'courseRoot', '/courseRoot', '', '0', 'ScaleToOriginal', '顶级菜单', 18, '2022-07-02 18:25:24', NULL);
INSERT INTO `sys_menu` VALUES (27, 26, '课程列表', 'sys:courseList', 'courseList', '/courseList', '/course/CourseList', '1', 'UserFilled', '课程管理', 19, '2022-07-02 18:26:24', NULL);
INSERT INTO `sys_menu` VALUES (28, 27, '新增', 'sys:courseList:add', '', '', '', '2', '', '课程列表', 20, '2022-07-02 18:26:45', NULL);
INSERT INTO `sys_menu` VALUES (29, 27, '编辑', 'sys:courseList:edit', '', '', '', '2', '', '课程列表', 21, '2022-07-02 18:27:02', NULL);
INSERT INTO `sys_menu` VALUES (30, 27, '删除', 'sys:courseList:delete', '', '', '', '2', '', '课程列表', 22, '2022-07-02 18:27:19', NULL);
INSERT INTO `sys_menu` VALUES (31, 26, '我的课程', 'sys:mycourse', 'mycourse', '/mycourse', '/mycourse/mycourse', '1', 'Wallet', '课程管理', 23, '2022-07-02 18:28:21', NULL);
INSERT INTO `sys_menu` VALUES (32, 0, '器材管理', 'sys:materialList', 'materialList', '/materialList', '/material/MaterialList', '1', 'UserFilled', '顶级菜单', 24, '2022-07-02 18:29:36', NULL);
INSERT INTO `sys_menu` VALUES (33, 32, '新增', 'sys:materialList:add', '', '', '', '2', '', '器材管理', 25, '2022-07-02 18:29:57', NULL);
INSERT INTO `sys_menu` VALUES (34, 32, '编辑', 'sys:materialList:edit', '', '', '', '2', '', '器材管理', 26, '2022-07-02 18:30:15', NULL);
INSERT INTO `sys_menu` VALUES (35, 32, '删除', 'sys:materialList:delete', '', '', '', '2', '', '器材管理', 27, '2022-07-02 18:30:31', NULL);
INSERT INTO `sys_menu` VALUES (36, 0, '商品管理', 'sys:goodsRoot', 'goodsRoot', '/goodsRoot', '', '0', 'Document', '顶级菜单', 30, '2022-07-02 18:31:25', NULL);
INSERT INTO `sys_menu` VALUES (37, 36, '商品列表', 'sys:goodsList', 'goodsList', '/goodsList', '/goods/GoodsList', '1', 'UserFilled', '商品管理', 31, '2022-07-02 18:32:18', NULL);
INSERT INTO `sys_menu` VALUES (38, 37, '新增', 'sys:goodsList:add', '', '', '', '2', '', '商品列表', 32, '2022-07-02 18:32:44', NULL);
INSERT INTO `sys_menu` VALUES (39, 37, '编辑', 'sys:goodsList:edit', '', '', '', '2', '', '商品列表', 33, '2022-07-02 18:33:01', NULL);
INSERT INTO `sys_menu` VALUES (40, 37, '删除', 'sys:goodsList:delete', '', '', '', '2', '', '商品列表', 34, '2022-07-02 18:33:19', NULL);
INSERT INTO `sys_menu` VALUES (41, 36, '订单管理', 'sys:orderList', 'orderList', '/orderList', '/order/OrderList', '1', 'UserFilled', '商品管理', 35, '2022-07-02 18:34:12', NULL);
INSERT INTO `sys_menu` VALUES (42, 51, '失物列表', 'sys:lostList', 'lostList', '/lostList', '/lost/LostList', '1', 'UserFilled', '失物招领1', 36, '2022-07-02 18:35:06', '2022-07-02 20:12:07');
INSERT INTO `sys_menu` VALUES (43, 42, '新增', 'sys:lostList:add', '', '', '', '2', '', '失物招领', 37, '2022-07-02 18:35:28', NULL);
INSERT INTO `sys_menu` VALUES (44, 42, '编辑', 'sys:lostList:edit', '', '', '', '2', '', '失物招领', 38, '2022-07-02 18:35:47', NULL);
INSERT INTO `sys_menu` VALUES (45, 42, '删除', 'sys:lostList:delete', '', '', '', '2', '', '失物招领', 39, '2022-07-02 18:36:03', NULL);
INSERT INTO `sys_menu` VALUES (46, 52, '反馈列表', 'sys:suggestList', 'suggestList', '/suggestList', '/suggest/SuggestList', '1', 'UserFilled', '反馈管理', 40, '2022-07-02 18:37:12', '2022-07-02 20:13:51');
INSERT INTO `sys_menu` VALUES (47, 46, '新增', 'sys:suggestList:add', '', '', '', '2', '', '反馈管理', 42, '2022-07-02 18:37:39', NULL);
INSERT INTO `sys_menu` VALUES (48, 46, '编辑', 'sys:suggestList:edit', '', '', '', '2', '', '反馈管理', 43, '2022-07-02 18:37:55', NULL);
INSERT INTO `sys_menu` VALUES (49, 46, '删除', 'sys:suggestList:delete', '', '', '', '2', '', '反馈管理', 44, '2022-07-02 18:38:11', NULL);
INSERT INTO `sys_menu` VALUES (50, 42, '查看', 'sys:lostList:look', '', '', '', '2', '', '失物招领', 45, '2022-07-02 20:00:09', NULL);
INSERT INTO `sys_menu` VALUES (51, 0, '失物招领', 'sys:lostRoot', 'lostRoot', '/lostRoot', '', '0', 'Document', '顶级菜单', 36, '2022-07-02 20:10:57', '2022-07-02 20:12:14');
INSERT INTO `sys_menu` VALUES (52, 0, '反馈管理', 'sys:suggestRoot', 'suggestRoot', '/suggestRoot', '/suggest/SuggestList', '0', 'Document', '顶级菜单', 40, '2022-07-02 20:13:40', NULL);
INSERT INTO `sys_menu` VALUES (53, 27, '查看', 'sys:courseList:look', '', '', '', '2', '', '课程列表', 50, '2022-07-03 21:17:50', NULL);
INSERT INTO `sys_menu` VALUES (54, 27, '报名', 'sys:courseList:join', '', '', '', '2', '', '课程列表', 51, '2022-07-03 21:40:15', NULL);
INSERT INTO `sys_menu` VALUES (55, 42, '认领', 'sys:lostList:get', '', '', '', '2', '', '失物列表', 55, '2022-07-04 15:55:26', NULL);
INSERT INTO `sys_menu` VALUES (56, 3, '分配权限', 'sys:role:assign', '', '', '', '2', '', '角色管理', 56, '2022-07-04 16:04:26', NULL);
INSERT INTO `sys_menu` VALUES (57, 6, '查看', 'sys:memberList:look', '', '', '', '2', '', '会员管理', 57, '2022-07-04 16:06:44', NULL);
INSERT INTO `sys_menu` VALUES (58, 31, '导出学生', 'sys:mycourse:export', '', '', '', '2', '', '我的课程', 57, '2022-07-04 19:29:37', NULL);
INSERT INTO `sys_menu` VALUES (59, 2, '员工重置密码', 'sys:user:resetPassword', '', '', '', '2', '', '员工管理', 60, '2022-07-07 18:19:42', NULL);
INSERT INTO `sys_menu` VALUES (60, 6, '会员重置密码', 'sys:member:resetPassword', '', '', '', '2', '', '会员管理', 61, '2022-07-07 18:20:21', NULL);
INSERT INTO `sys_menu` VALUES (61, 41, '下单', 'sys:orderList:down', '', '', '', '2', '', '订单管理', 70, '2022-07-10 16:26:27', NULL);
INSERT INTO `sys_menu` VALUES (62, 31, '列表', 'sys:mycourse:list', '', '', '', '2', '', '我的课程', 33, '2023-11-11 21:52:37', NULL);
INSERT INTO `sys_menu` VALUES (63, 31, '退款', 'sys:mycourse:return', '', '', '', '2', '', '我的课程', 11, '2024-03-07 16:20:22', '2024-03-09 21:15:59');
INSERT INTO `sys_menu` VALUES (64, 26, '课程订单', 'sys:courseOrder:index', 'courseOrder', '/courseOrder', '/course/Order', '1', 'Plus', '课程管理', 41, '2024-03-10 10:21:22', '2024-03-10 10:26:39');
INSERT INTO `sys_menu` VALUES (65, 64, '退款', 'sys:courseOrder:return', '', '', '', '2', '', '课程订单', NULL, '2024-03-10 10:22:51', NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '角色id',
  `role_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '角色名称',
  `remark` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `types` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '1：员员类型 2：会员类型 ',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (2, '系统管理员', '系统管理员', '2022-06-05 19:28:40', '2024-03-07 11:39:01', '1');
INSERT INTO `sys_role` VALUES (3, '员工', '员工', '2022-06-05 19:47:41', '2024-03-07 11:39:40', '1');
INSERT INTO `sys_role` VALUES (4, '教练', '教练', '2022-06-05 19:50:52', '2024-03-07 11:39:45', '1');
INSERT INTO `sys_role` VALUES (5, '会员', '会员', '2022-06-16 10:30:36', '2024-03-07 11:39:50', '2');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_menu_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` int(11) NULL DEFAULT NULL COMMENT '角色id',
  `menu_id` int(11) NULL DEFAULT NULL COMMENT '菜单id',
  PRIMARY KEY (`role_menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1423 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (911, 3, 5);
INSERT INTO `sys_role_menu` VALUES (912, 3, 14);
INSERT INTO `sys_role_menu` VALUES (913, 3, 15);
INSERT INTO `sys_role_menu` VALUES (914, 3, 16);
INSERT INTO `sys_role_menu` VALUES (915, 3, 6);
INSERT INTO `sys_role_menu` VALUES (916, 3, 21);
INSERT INTO `sys_role_menu` VALUES (917, 3, 22);
INSERT INTO `sys_role_menu` VALUES (918, 3, 23);
INSERT INTO `sys_role_menu` VALUES (919, 3, 24);
INSERT INTO `sys_role_menu` VALUES (920, 3, 25);
INSERT INTO `sys_role_menu` VALUES (921, 3, 57);
INSERT INTO `sys_role_menu` VALUES (922, 3, 60);
INSERT INTO `sys_role_menu` VALUES (923, 3, 36);
INSERT INTO `sys_role_menu` VALUES (924, 3, 37);
INSERT INTO `sys_role_menu` VALUES (925, 3, 38);
INSERT INTO `sys_role_menu` VALUES (926, 3, 39);
INSERT INTO `sys_role_menu` VALUES (927, 3, 40);
INSERT INTO `sys_role_menu` VALUES (928, 3, 41);
INSERT INTO `sys_role_menu` VALUES (929, 3, 61);
INSERT INTO `sys_role_menu` VALUES (930, 3, 51);
INSERT INTO `sys_role_menu` VALUES (931, 3, 42);
INSERT INTO `sys_role_menu` VALUES (932, 3, 43);
INSERT INTO `sys_role_menu` VALUES (933, 3, 44);
INSERT INTO `sys_role_menu` VALUES (934, 3, 45);
INSERT INTO `sys_role_menu` VALUES (935, 3, 50);
INSERT INTO `sys_role_menu` VALUES (936, 3, 55);
INSERT INTO `sys_role_menu` VALUES (937, 3, 52);
INSERT INTO `sys_role_menu` VALUES (938, 3, 46);
INSERT INTO `sys_role_menu` VALUES (939, 3, 47);
INSERT INTO `sys_role_menu` VALUES (940, 3, 48);
INSERT INTO `sys_role_menu` VALUES (941, 3, 49);
INSERT INTO `sys_role_menu` VALUES (942, 3, 4);
INSERT INTO `sys_role_menu` VALUES (1215, 5, 57);
INSERT INTO `sys_role_menu` VALUES (1216, 5, 7);
INSERT INTO `sys_role_menu` VALUES (1217, 5, 54);
INSERT INTO `sys_role_menu` VALUES (1218, 5, 62);
INSERT INTO `sys_role_menu` VALUES (1219, 5, 63);
INSERT INTO `sys_role_menu` VALUES (1220, 5, 50);
INSERT INTO `sys_role_menu` VALUES (1221, 5, 52);
INSERT INTO `sys_role_menu` VALUES (1222, 5, 46);
INSERT INTO `sys_role_menu` VALUES (1223, 5, 47);
INSERT INTO `sys_role_menu` VALUES (1224, 5, 48);
INSERT INTO `sys_role_menu` VALUES (1225, 5, 49);
INSERT INTO `sys_role_menu` VALUES (1226, 5, 4);
INSERT INTO `sys_role_menu` VALUES (1227, 5, 6);
INSERT INTO `sys_role_menu` VALUES (1228, 5, 26);
INSERT INTO `sys_role_menu` VALUES (1229, 5, 27);
INSERT INTO `sys_role_menu` VALUES (1230, 5, 31);
INSERT INTO `sys_role_menu` VALUES (1231, 5, 51);
INSERT INTO `sys_role_menu` VALUES (1232, 5, 42);
INSERT INTO `sys_role_menu` VALUES (1342, 2, 2);
INSERT INTO `sys_role_menu` VALUES (1343, 2, 8);
INSERT INTO `sys_role_menu` VALUES (1344, 2, 9);
INSERT INTO `sys_role_menu` VALUES (1345, 2, 10);
INSERT INTO `sys_role_menu` VALUES (1346, 2, 59);
INSERT INTO `sys_role_menu` VALUES (1347, 2, 3);
INSERT INTO `sys_role_menu` VALUES (1348, 2, 11);
INSERT INTO `sys_role_menu` VALUES (1349, 2, 12);
INSERT INTO `sys_role_menu` VALUES (1350, 2, 13);
INSERT INTO `sys_role_menu` VALUES (1351, 2, 56);
INSERT INTO `sys_role_menu` VALUES (1352, 2, 5);
INSERT INTO `sys_role_menu` VALUES (1353, 2, 14);
INSERT INTO `sys_role_menu` VALUES (1354, 2, 15);
INSERT INTO `sys_role_menu` VALUES (1355, 2, 16);
INSERT INTO `sys_role_menu` VALUES (1356, 2, 6);
INSERT INTO `sys_role_menu` VALUES (1357, 2, 21);
INSERT INTO `sys_role_menu` VALUES (1358, 2, 22);
INSERT INTO `sys_role_menu` VALUES (1359, 2, 23);
INSERT INTO `sys_role_menu` VALUES (1360, 2, 24);
INSERT INTO `sys_role_menu` VALUES (1361, 2, 25);
INSERT INTO `sys_role_menu` VALUES (1362, 2, 57);
INSERT INTO `sys_role_menu` VALUES (1363, 2, 60);
INSERT INTO `sys_role_menu` VALUES (1364, 2, 28);
INSERT INTO `sys_role_menu` VALUES (1365, 2, 29);
INSERT INTO `sys_role_menu` VALUES (1366, 2, 30);
INSERT INTO `sys_role_menu` VALUES (1367, 2, 53);
INSERT INTO `sys_role_menu` VALUES (1368, 2, 64);
INSERT INTO `sys_role_menu` VALUES (1369, 2, 65);
INSERT INTO `sys_role_menu` VALUES (1370, 2, 32);
INSERT INTO `sys_role_menu` VALUES (1371, 2, 33);
INSERT INTO `sys_role_menu` VALUES (1372, 2, 34);
INSERT INTO `sys_role_menu` VALUES (1373, 2, 35);
INSERT INTO `sys_role_menu` VALUES (1374, 2, 36);
INSERT INTO `sys_role_menu` VALUES (1375, 2, 37);
INSERT INTO `sys_role_menu` VALUES (1376, 2, 38);
INSERT INTO `sys_role_menu` VALUES (1377, 2, 39);
INSERT INTO `sys_role_menu` VALUES (1378, 2, 40);
INSERT INTO `sys_role_menu` VALUES (1379, 2, 41);
INSERT INTO `sys_role_menu` VALUES (1380, 2, 61);
INSERT INTO `sys_role_menu` VALUES (1381, 2, 51);
INSERT INTO `sys_role_menu` VALUES (1382, 2, 42);
INSERT INTO `sys_role_menu` VALUES (1383, 2, 43);
INSERT INTO `sys_role_menu` VALUES (1384, 2, 44);
INSERT INTO `sys_role_menu` VALUES (1385, 2, 45);
INSERT INTO `sys_role_menu` VALUES (1386, 2, 50);
INSERT INTO `sys_role_menu` VALUES (1387, 2, 55);
INSERT INTO `sys_role_menu` VALUES (1388, 2, 52);
INSERT INTO `sys_role_menu` VALUES (1389, 2, 46);
INSERT INTO `sys_role_menu` VALUES (1390, 2, 47);
INSERT INTO `sys_role_menu` VALUES (1391, 2, 48);
INSERT INTO `sys_role_menu` VALUES (1392, 2, 49);
INSERT INTO `sys_role_menu` VALUES (1393, 2, 1);
INSERT INTO `sys_role_menu` VALUES (1394, 2, 4);
INSERT INTO `sys_role_menu` VALUES (1395, 2, 26);
INSERT INTO `sys_role_menu` VALUES (1396, 2, 27);
INSERT INTO `sys_role_menu` VALUES (1397, 4, 28);
INSERT INTO `sys_role_menu` VALUES (1398, 4, 29);
INSERT INTO `sys_role_menu` VALUES (1399, 4, 30);
INSERT INTO `sys_role_menu` VALUES (1400, 4, 58);
INSERT INTO `sys_role_menu` VALUES (1401, 4, 62);
INSERT INTO `sys_role_menu` VALUES (1402, 4, 32);
INSERT INTO `sys_role_menu` VALUES (1403, 4, 33);
INSERT INTO `sys_role_menu` VALUES (1404, 4, 34);
INSERT INTO `sys_role_menu` VALUES (1405, 4, 35);
INSERT INTO `sys_role_menu` VALUES (1406, 4, 41);
INSERT INTO `sys_role_menu` VALUES (1407, 4, 61);
INSERT INTO `sys_role_menu` VALUES (1408, 4, 43);
INSERT INTO `sys_role_menu` VALUES (1409, 4, 44);
INSERT INTO `sys_role_menu` VALUES (1410, 4, 45);
INSERT INTO `sys_role_menu` VALUES (1411, 4, 50);
INSERT INTO `sys_role_menu` VALUES (1412, 4, 52);
INSERT INTO `sys_role_menu` VALUES (1413, 4, 46);
INSERT INTO `sys_role_menu` VALUES (1414, 4, 47);
INSERT INTO `sys_role_menu` VALUES (1415, 4, 48);
INSERT INTO `sys_role_menu` VALUES (1416, 4, 49);
INSERT INTO `sys_role_menu` VALUES (1417, 4, 26);
INSERT INTO `sys_role_menu` VALUES (1418, 4, 27);
INSERT INTO `sys_role_menu` VALUES (1419, 4, 31);
INSERT INTO `sys_role_menu` VALUES (1420, 4, 36);
INSERT INTO `sys_role_menu` VALUES (1421, 4, 51);
INSERT INTO `sys_role_menu` VALUES (1422, 4, 42);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '员工id',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '账户(员工编号)',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '密码',
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '邮箱',
  `sex` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT ' 性别  0：男 1：女',
  `salary` decimal(18, 2) NULL DEFAULT NULL COMMENT '工资',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '类型（1：员工 2：教练）',
  `status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '状态 0：停用 1：启用',
  `is_admin` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT ' 是否是管理员 （1：是 0 ：否）',
  `is_account_non_expired` tinyint(4) NULL DEFAULT NULL COMMENT '帐户是否过期(1 未过期，0已过期)',
  `is_account_non_locked` tinyint(4) NULL DEFAULT NULL COMMENT '帐户是否被锁定(1 未锁定，0已锁定)',
  `is_credentials_non_expired` tinyint(4) NULL DEFAULT NULL COMMENT '密码是否过期(1 未过期，0已过期)',
  `is_enabled` tinyint(4) NULL DEFAULT NULL COMMENT '帐户是否可用(1 可用，0 删除用户)',
  `nick_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '姓名',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'zs001', '$2a$10$n.qqOV6s9FVCAOFu41HASeQewB8aY5B8YMgCarI9PNOVN4HjoqdVG', '18687116223', '2383404558@qq.com', '0', 5000.00, '1', '1', '0', 1, 1, 1, 1, '张三', '2022-06-11 23:11:00', '2022-06-12 13:29:00');
INSERT INTO `sys_user` VALUES (2, 'ls001', '$2a$10$RuSq4KVFY64cG4au6v6viego5l694uJiNBpFEiaJ9pkhtHq73/LQ2', '18787871623', '2383404558@qq.com', '1', 6000.00, '2', '1', '0', 1, 1, 1, 1, '李四', '2022-06-12 11:50:16', NULL);
INSERT INTO `sys_user` VALUES (3, 'root', '$2a$10$jxLrPKHtYksGE1LdRb3g6.MkWtjxn./AoxU6QXrLQFumMlUwSrHpq', '18687116223', '2383404558@qq.com', '1', NULL, NULL, '1', '1', 1, 1, 1, 1, 'admin', NULL, NULL);
INSERT INTO `sys_user` VALUES (4, '2020001', '$2a$10$DeWn8iIjAJXNkVr6UPuB7.r6IB/6N9AHQyZnnkzX3oHLDNiJhVkDO', '18787171906', '', '0', 6000.00, '2', '1', '0', 1, 1, 1, 1, '张明', '2022-07-03 20:25:41', NULL);
INSERT INTO `sys_user` VALUES (5, '2022002', '$2a$10$3JqLa3vUoqd7Y7H7tyz67uHyvvbHk2bmzLW0HY4ux2vb1z8o143Jy', '18787171906', '', '1', 5000.00, '2', '1', '0', 1, 1, 1, 1, '李丽', '2022-07-03 20:26:08', '2022-07-08 11:55:40');
INSERT INTO `sys_user` VALUES (6, 'admin', '$2a$10$cxhsy4Vv45zfXynIIBZ8..u57XHMQA44ztRuwXos2u9IaOMbwnijq', '18687116223', '', '0', 10000.00, '1', '1', '0', 1, 1, 1, 1, 'admin1', '2022-07-04 19:08:05', NULL);
INSERT INTO `sys_user` VALUES (12, '77777', '$2a$10$Vow7F4MdKcgZ8mUL2BPpj.OGVAsCVjtAUk3Db6tpf05dL4H6pnLvG', '777777', '', '0', 7777.00, '1', '1', '0', 1, 1, 1, 1, '7776666', '2022-07-11 10:02:10', '2022-07-11 10:04:34');
INSERT INTO `sys_user` VALUES (13, 'test666', '$2a$10$16YI3gG.KQjwvr/GvBx7muGZTLF.zuP3Nyk8XStCJfOFL9mGQ.wz6', '***********', '', '0', 5000.00, '2', '1', '0', 1, 1, 1, 1, '测试', '2023-11-13 10:08:55', '2023-11-13 10:09:15');
INSERT INTO `sys_user` VALUES (14, 'test888', '$2a$10$RcPHHJ/bCP8kAUg8Ht4IyuADmBVKUt9Va.o1.Xz/VFdBs7IAdOE2q', '**********', '', '0', 8000.00, '1', '1', '0', 1, 1, 1, 1, 'test888', '2023-11-13 10:12:26', NULL);
INSERT INTO `sys_user` VALUES (15, '999', '$2a$10$zKpScwORaMdtWSkxskKoU.Q6v0yoOgbE2Df8VRbA1PAmO4GbX4HE2', '9999', '', '0', 999.00, '2', '1', '0', 1, 1, 1, 1, '999', '2024-03-09 22:12:32', '2024-03-11 00:45:37');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_role_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '员工id',
  `role_id` int(11) NULL DEFAULT NULL COMMENT '角色id',
  PRIMARY KEY (`user_role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
-- 适配说明: 源脚本中 (22, 12, 11) 的 role_id=11 在 sys_role 中不存在
--           (sys_role 仅定义 role_id 2,3,4,5)，属于源脚本的脏数据。
--           此处改为 3(员工)，使员工 77777 拥有合法角色，避免角色列表显示为空。
--           若不需要该员工，可整行删除。
INSERT INTO `sys_user_role` VALUES (2, 2, 4);
INSERT INTO `sys_user_role` VALUES (8, 1, 3);
INSERT INTO `sys_user_role` VALUES (9, 4, 4);
INSERT INTO `sys_user_role` VALUES (11, 6, 2);
INSERT INTO `sys_user_role` VALUES (12, 5, 4);
INSERT INTO `sys_user_role` VALUES (22, 12, 3);
INSERT INTO `sys_user_role` VALUES (24, 13, 4);
INSERT INTO `sys_user_role` VALUES (25, 14, 3);
INSERT INTO `sys_user_role` VALUES (30, 15, 4);

SET FOREIGN_KEY_CHECKS = 1;
