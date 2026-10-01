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

## [0.3.0] - 2026-09-28

### 新增（feat）

#### 角色管理模块功能完善
- 前端完成角色列表联调：表格展示、搜索、重置、分页（页码/容量切换）、列表高度自适应
- 新增/编辑弹框支持角色类型选择（员工类型 / 会员类型），编辑模式数据回显
- 完成新增、编辑、删除接口对接：删除带确认提示，操作成功后自动刷新列表并提示

#### 前端公共能力建设
- Element Plus 启用中文国际化（zhCn）
- 新增工具层 `src/utils/`：`objCopy` 对象拷贝、`myconfirm` 信息确认提示框（全局挂载 `$objCoppy` / `$myconfirm`）
- 新增 `useInstance` Hook，统一获取组件实例与全局属性
- 新增枚举定义 `BaseEnum`（EditType / Title / UserType），`BaseType` 补充通用函数类型

### 优化（improve）
- 调整 Element Plus 注册与全局属性挂载顺序
- 移除 tsconfig 中不再需要的 `erasableSyntaxOnly` 配置

## [0.4.0] - 2026-10-01

### 新增（feat）

#### 用户管理模块（员工管理）
- 后端新增 `sys_user` 模块：`SysUser` 实体、Mapper + 映射文件、Service 业务层、`SysUserController`
- 完成员工接口开发：新增（MD5 密码加密 + 重名校验）、编辑、删除、分页列表（密码脱敏）、重置密码（默认 123456）、按用户查询角色
- 后端新增 `sys_user_role` 模块（用户-角色中间表）：实体、Mapper + 映射文件、Service
- 前端新增 `api/user` 与 `composables/user`（useUser / useTable / useSelectRole）
- 新增员工页面 `AddUser.vue`；`UserList.vue` 完成列表联调：搜索（电话/姓名）、表格展示、分页、编辑/删除/重置密码操作、权限指令 `v-permission`

#### 角色模块补充
- 新增角色下拉接口 `/api/role/getSelect` 与 `SelectType` 实体，用于新增/编辑员工时选择角色

## [未发布]

### 规划（planned）
- 数据库初始化脚本（docs/sql/）
- 业务模块开发：菜单管理、会员卡、会员、办卡充值、课程、器材、商品、失物招领、反馈
- 登录认证（验证码 + JWT）、权限树分配、动态菜单
- 数据统计看板（ECharts）、Spring Security 认证授权
