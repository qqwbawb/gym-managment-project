# 更新日志（CHANGELOG）

本项目所有重要变更均记录在此文件中，格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/) 与 [语义化版本](https://semver.org/lang/zh-CN/)。

## [0.1.0] - 2026-09-26

### 新增（feat）

#### 前端
- 初始化 Vue3 + TypeScript + Vite 工程，集成 Element Plus、Pinia、Vue Router、Axios、Sass
- 完成主界面布局：左侧菜单（MenuBar/MenuItem/MenuLogo）、顶部 Header、面包屑、Tabs 选项卡、菜单收缩
- 完成路由配置（系统管理 / 会员管理 / 首页）与对应页面框架
- 封装 Axios 请求模块（baseURL、Token 携带、统一错误处理）
- 封装弹框组件 `SysDialog` 与 `useDialog` Hook
- 搭建 Pinia 状态管理（collapse 菜单收缩、tabs 标签页）

#### 后端
- 创建 Maven 多模块父工程（`gymnasium-common` + `gymnasium-service-web`）
- 集成 Spring Boot 2.7.18、MyBatis-Plus 3.4.1、Druid 连接池、Swagger2
- 统一返回结果封装：`ResultVo`、`ResultUtils`、`StatusCode`
- 配置 MyBatis-Plus 分页插件与 Mapper 扫描
- 配置开发环境数据源（`application-dev.yml`，数据库 `gym_system`）

### 优化（improve）
- 后端依赖统一版本管理（dependencyManagement）
- 前端路径别名 `@` 指向 `src`

## [0.2.0] - 2026-09-27

### 新增（feat）

#### 角色管理模块
- 后端完成角色管理 CRUD：`SysRole` / `RoleParm` 实体、`SysRoleMapper` + 映射文件、`SysRoleService` 业务层、`SysRoleController`
- 前端新增 `api/role`（接口请求封装）与 `composables/role`（业务逻辑）两层
- 前端新增角色新增页面 `RoleAdd.vue`，角色列表页 `RoleList.vue` 完成接口对接与弹框集成

#### 工程配置
- 正式整合 Swagger2 接口文档：升级为 springfox-swagger2 / swagger-ui 2.9.2，新增 `SwaggerConfig`，启动类开启 `@EnableSwagger2`
- 新增跨域配置 `WebMvcConfig`，解决前后端联调跨域问题

### 优化（improve）
- MySQL 驱动升级至 8.0.33，数据源 URL 补充时区与编码参数（serverTimezone / utf8）
- MyBatis-Plus 开启自动驼峰映射，Mapper 扫描适配多业务模块分包（`com.xq.*.*.mapper`）
- 配置 `spring.mvc.pathmatch.matching-strategy=ant_path_matcher`，解决 Spring Boot 2.7 与 Swagger 2.9.2 兼容问题
- 控制台日志输出改为彩色分级格式

## [未发布]

### 规划（planned）
- 数据库初始化脚本（docs/sql/）
- 业务模块开发：用户管理、菜单管理、会员卡、会员、办卡充值、课程、器材、商品、失物招领、反馈
- 登录认证（验证码 + JWT）、权限树分配、动态菜单
- 数据统计看板（ECharts）、Spring Security 认证授权
