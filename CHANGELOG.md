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

## [0.5.0] - 2026-10-03

### 新增（feat）

#### 菜单管理模块
- 后端新增 `sys_menu` 模块：`SysMenu` 实体、树形构造工具 `MakeMenuTree`、`RouterVo`、Mapper + 映射文件、Service、`SysMenuController`
- 前端新增 `api/menu` 与 `composables/menu`（useMenu / useMenuTable）
- 新增菜单页面 `AddMenu.vue`、上级菜单选择组件 `ParentMenu.vue`，`MenuList.vue` 完成列表联调

#### 会员卡管理模块
- 后端新增 `member_card` 模块：`MemberCard` / `ListCard` 实体、Mapper + 映射文件、Service、`MemberCardController`
- 完成会员卡接口开发：新增、修改、删除、分页列表（按标题模糊查询）
- 前端新增 `api/member_card` 与 `composables/member_card`（useMember / useMemberTable）
- 新增会员卡页面 `AddCard.vue`，`CardType.vue` 完成列表联调

## [0.6.0] - 2026-10-04

### 新增（feat）

#### 会员管理模块
- 后端新增 `member` 模块：`Member` / `PageParam` / `JoinParam` / `RechargeParam` 实体、Mapper + 映射文件、Service、`MemberController`
- 完成会员接口开发：新增（会员卡号去重）、编辑、删除、分页列表（姓名/电话/卡号模糊查询）、按会员查询角色、启用中会员卡列表查询
- 前端新增 `api/member` 与 `composables/member`（useMember / useTable / useJoin / useRecharge）
- 新增会员页面 `AddMember.vue`，`MemberList.vue` 完成列表联调

#### 办卡与充值模块
- 后端新增 `member_apple`（办卡申请）、`member_recharge`（充值）、`member_role`（会员-角色中间表）模块：实体、Mapper + 映射文件、Service
- 完成办卡接口 `/api/member/joinApply`、充值接口 `/api/member/recharge`
- 前端新增办卡页面 `JoinApply.vue`、充值页面 `Recharge.vue`

### 工程优化（improve）
- 后端 Mapper 映射文件统一整理到 `resources/mapper/` 目录
- 修复用户分页参数 `nickNme` → `nickName` 拼写错误

## [0.7.0] - 2026-10-05

### 新增（feat）

#### 课程管理模块
- 后端新增 `course` 模块：`Course` / `CourseList` 实体、Mapper + 映射文件、Service、`CourseController`
- 完成课程接口开发：新增、编辑、删除、分页列表（按课程名 / 教练名模糊查询）
- 新增教练下拉接口 `/api/user/getTeacher`（筛选用户类型为教练的员工），用于添加课程时选择教练
- 前端新增 `api/course` 与 `composables/course`（useCourse / useTable / useEditor / useUpload / useSelectTeacher）
- 新增课程列表 `CourseList.vue` 与课程添加 `AddCourse.vue`（wangEditor 富文本编辑课程详情）
- 新增「我的课程」页面 `mycourse.vue`（页面框架）

#### 图片上传（MinIO）
- 后端整合 MinIO 对象存储（8.3.9）：`MinioConfig` / `MinioProp` / `MinioUtils`、上传接口 `/api/upload/uploadImage`
- `WebMvcConfig` 增加静态资源映射，支持图片访问
- 前端集成 wangEditor 富文本编辑器（课程详情编辑，支持图片上传）

### 工程优化（improve）
- 后端补充 minio / okhttp / easyexcel / fastjson 依赖
- 前端 vite 配置新增 `BASE_API` 全局变量，axios 请求地址统一管理

## [0.8.0] - 2026-10-06

### 新增（feat）

#### 器材管理模块
- 后端新增 `equipment` 模块：`Material` / `ListParam` 实体、Mapper + 映射文件、Service、`MaterialController`
- 完成器材接口开发：新增、编辑、删除、分页列表（按名称模糊查询）
- 前端新增 `api/material` 与 `composables/material`（useMaterial / useTable）
- 新增器材列表 `MaterialList.vue`、器材添加 `AddMaterial.vue`

#### 商品管理模块
- 后端新增 `goods` 模块：`Goods` / `GoodsParam` 实体、Mapper + 映射文件、Service、`GoodsController`
- 完成商品接口开发：新增、编辑、删除、分页列表（按名称模糊查询）
- 前端新增 `api/goods` 与 `composables/goods`（useGoods / useTable）
- 新增商品列表 `GoodsList.vue`、商品添加 `AddGoods.vue`

#### 失物招领模块
- 后端新增 `lost` 模块：`Lost` / `LostParam` 实体、Mapper + 映射文件、Service、`LostController`
- 完成失物招领接口开发：新增、编辑、删除、分页列表（按失物名称模糊查询）
- 前端新增 `api/lost` 与 `composables/lost`（useLost / useTable）
- 新增失物列表 `LostList.vue`、失物添加 `AddLost.vue`、失物招领人页面 `LostPerson.vue`

#### 建议反馈模块
- 后端新增 `suggest` 模块：`Suggest` / `SuggestParam` 实体、Mapper + 映射文件、Service、`SuggestController`
- 完成反馈接口开发：新增（自动记录反馈时间）、编辑、删除、分页列表（按标题模糊查询、按时间倒序）
- 前端新增 `api/suggest` 与 `composables/suggest`（useSuggest / useTable）
- 新增反馈列表 `SuggestList.vue`、反馈添加 `AddSuggest.vue`

### 工程优化（improve）
- 补充 wangEditor 类型声明（`types/wangeditor.d.ts`）
- 新增器材管理、商品管理、失物招领、建议反馈路由与侧边菜单

## [0.9.0] - 2026-10-07

### 新增（feat）

#### 登录认证模块
- 后端新增 `login` 模块：`LoginController`（验证码生成 `/api/login/image`、登录 `/api/login/login`，支持会员 / 员工双账号体系）、`LoginParam` / `LoginResult` 实体
- 整合 kaptcha 图形验证码（`ImageConfig` 配置，验证码存入 Session，Base64 返回前端）
- 新增 JWT 工具类 `JwtUtils`（java-jwt），登录成功后签发 Token；`application-dev.yml` 新增 JWT 配置（颁发者 / 密钥 / 过期时间）
- 登录校验：MD5 密码比对，按用户类型区分会员（member）与员工（sys_user）账号
- 会员新增接口补充密码 MD5 加密，为会员登录做准备
- 前端新增登录页 `Login.vue`、`api/login`、`composables/login`（useImage 验证码获取）、Pinia 用户状态 `store/user`（登录信息持久化）
- 新增 `/login` 路由与全局路由守卫（未登录自动跳转登录页）
- 集成 pinia-plugin-persistedstate 状态持久化；axios 请求 Token 改从用户状态读取、开启 withCredentials 保持 Session

### 工程优化（improve）
- 统一返回结果字段 `message` → `msg`，与前端请求封装对齐

## [0.10.0] - 2026-10-09

### 新增（feat）

#### 角色权限分配模块（权限树）
- 后端新增 `sys_role_menu`（角色-菜单中间表）模块：`RoleMenu` / `SaveMenuParam` 实体、Mapper + 映射文件、Service
- 新增权限分配接口：`/api/role/getMenuTree`（权限树回显：超级管理员查询全部菜单，普通用户查询本人可见菜单，并回显角色已分配菜单）、`/api/role/saveRoleMenu`（保存角色菜单分配）
- `sys_menu` 模块补充按用户 / 按角色查询菜单树接口，`MakeMenuTree` 组装权限树数据
- 前端新增 `composables/role/useAssign.ts` 与权限分配弹框 `AssignRole.vue`，角色列表 `RoleList.vue` 增加分配入口

### 工程优化（improve）
- 用户实体管理员标识字段统一为 `isAdmin`（修复原 `idAdmin` 拼写），用于权限树差异化查询

## [未发布]

### 规划（planned）
- 数据库初始化脚本（docs/sql/）
- 业务模块开发：选课报名、订单、我的充值
- 动态菜单
- 数据统计看板（ECharts）、Spring Security 认证授权
