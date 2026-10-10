# 健身房管理系统（GymSystem）

基于 **Spring Boot + Vue 3** 的前后端分离健身房管理系统，采用「先搭框架、再按模块迭代」的开发方式。当前已完成前后端基础框架搭建，业务功能按开发路线图逐步实现，每个阶段均可独立运行、独立验证。

## 技术栈

### 后端（backend）

| 技术 | 版本 | 用途 |
|------|------|------|
| JDK | 1.8 | 运行环境 |
| Spring Boot | 2.7.18 | 基础框架 |
| Maven | 3.6+ | 多模块构建 |
| MyBatis-Plus | 3.4.1 | ORM / 分页插件 |
| MySQL | 8.0 | 数据库 |
| Druid | 1.2.1 | 数据库连接池 |
| Swagger2（springfox） | 2.9.2 | 接口文档 |
| java-jwt | 3.10.3 | 登录令牌（JWT） |
| kaptcha | 2.3.2 | 图形验证码 |
| EasyPoi | 4.2.0 | Excel 导入导出 |

### 前端（fronted）

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5 | 前端框架 |
| TypeScript | 6.0 | 类型系统 |
| Vite | 8.3 | 构建工具 |
| Element Plus | 2.14 | UI 组件库 |
| Pinia | 4.0 | 状态管理 |
| Vue Router | 4.6 | 路由 |
| Axios | 1.20 | HTTP 请求 |
| Sass | 1.104 | 样式预处理器 |

## 项目结构

```
GymSystem
├── backend/                          # 后端（Maven 多模块）
│   └── gymnasium-parent-project/     # 父工程
│       ├── gymnasium-common/         # 公共模块：统一返回封装、状态码
│       └── gymnasium-service-web/    # Web 服务模块：启动类、配置、业务接口
├── fronted/                          # 前端
│   └── gymansium-project/            # Vue3 工程
│       └── src/
│           ├── api/                  # 接口请求封装
│           ├── components/           # 公共组件（SysDialog 弹框等）
│           ├── composables/          # 组合式函数（业务逻辑）
│           ├── hooks/                # 自定义 Hook（useDialog 等）
│           ├── http/                 # Axios 封装（拦截器、Token）
│           ├── layout/               # 主布局（菜单/头部/面包屑/Tabs）
│           ├── router/               # 路由配置
│           ├── store/                # Pinia 状态管理
│           └── views/                # 页面组件（system/member 等）
└── docs/                             # 项目文档（SQL 脚本、说明）
```

## 当前开发进度

> 开发方式：先搭建可运行的前后端框架，再按业务模块逐个迭代，避免一次性堆砌代码。

### 已完成：阶段一（基础框架）

**后端**
- Maven 多模块工程搭建（`gymnasium-common` 公共模块 + `gymnasium-service-web` 服务模块）
- 统一返回结果封装：`ResultVo` / `ResultUtils` / `StatusCode`
- 整合 MyBatis-Plus（分页插件）、Druid 连接池、Swagger2 接口文档依赖
- 开发环境配置分离（`application.yml` + `application-dev.yml`）

**前端**
- Vue3 + TypeScript + Vite 工程，集成 Element Plus / Pinia / Vue Router / Axios / Sass
- 主界面布局：侧边菜单、顶部 Header、面包屑导航、Tabs 标签页、菜单收缩
- Axios 请求封装（拦截器、Token 携带、统一错误处理）
- 封装通用弹框组件 `SysDialog` 与 `useDialog` Hook
- 系统管理 / 会员管理页面框架（员工、角色、菜单、会员卡、会员、我的充值）

### 已完成：阶段二 —— 角色管理、用户管理、菜单管理模块

**角色管理**
- 后端角色管理 CRUD 接口（实体 / Mapper / Service / Controller），含角色下拉接口 `/api/role/getSelect`
- 前端角色列表：表格展示、搜索、重置、分页、高度自适应；新增 / 编辑弹框（类型选择、回显）、删除确认
- 正式整合 Swagger2 接口文档（2.9.2）与跨域配置

**用户管理（员工管理）**
- 后端 `sys_user` + `sys_user_role`（用户-角色中间表）模块：新增（MD5 加密 + 重名校验）、编辑、删除、分页列表（密码脱敏）、重置密码
- 前端员工列表：搜索（电话/姓名）、表格展示（性别/类型/状态标签）、分页、编辑/删除/重置密码操作、权限指令 `v-permission`
- 新增员工页面 `AddUser.vue`（含角色选择）

**菜单管理**
- 后端 `sys_menu` 模块：`SysMenu` 实体、树形构造工具 `MakeMenuTree`、Mapper + 映射文件、Service、Controller
- 前端菜单列表联调、新增菜单 `AddMenu.vue`、上级菜单选择 `ParentMenu.vue`

**前端公共能力**
- Element Plus 中文国际化、`$objCoppy` / `$myconfirm` 全局工具、`useInstance` Hook、枚举定义

### 已完成：阶段三 —— 会员卡类型、会员管理、办卡充值

**会员卡类型管理**
- 后端 `member_card` 模块：`MemberCard` / `ListCard` 实体、Mapper + 映射文件、Service、Controller
- 会员卡接口：新增、修改、删除、分页列表（按标题模糊查询）
- 前端会员卡列表 `CardType.vue` 联调、新增/编辑页面 `AddCard.vue`

**会员管理**
- 后端 `member` 模块：`Member` / `JoinParam` / `RechargeParam` 实体、Mapper + 映射文件、Service、Controller
- 会员接口：新增（卡号去重）、编辑、删除、分页列表（姓名/电话/卡号模糊查询）、按会员查询角色
- 前端会员列表 `MemberList.vue` 联调、新增会员 `AddMember.vue`

**办卡与充值**
- 后端 `member_apple`（办卡申请）、`member_recharge`（充值）、`member_role`（会员-角色中间表）模块
- 办卡接口 `/api/member/joinApply`（选择启用中会员卡）、充值接口 `/api/member/recharge`
- 前端办卡页面 `JoinApply.vue`、充值页面 `Recharge.vue`

### 已完成：阶段四（部分）—— 课程管理与图片上传

**课程管理**
- 后端 `course` 模块：`Course` / `CourseList` 实体、Mapper + 映射文件、Service、Controller
- 课程接口：新增、编辑、删除、分页列表（按课程名 / 教练名模糊查询）
- 教练下拉接口 `/api/user/getTeacher`，添加课程时选择教练
- 前端课程列表 `CourseList.vue`、课程添加 `AddCourse.vue`（wangEditor 富文本编辑课程详情）
- 新增「我的课程」页面 `mycourse.vue`（页面框架）

**图片上传（MinIO）**
- 后端整合 MinIO 对象存储（8.3.9）：`MinioConfig` / `MinioProp` / `MinioUtils`、上传接口 `/api/upload/uploadImage`
- `WebMvcConfig` 增加静态资源映射，支持图片访问
- 前端集成 wangEditor 富文本编辑器，课程详情支持图片上传

### 已完成：阶段五（部分）—— 器材管理、商品管理

**器材管理**
- 后端 `equipment` 模块：`Material` / `ListParam` 实体、Mapper + 映射文件、Service、Controller
- 器材接口：新增、编辑、删除、分页列表（按名称模糊查询）
- 前端器材列表 `MaterialList.vue`、器材添加 `AddMaterial.vue`

**商品管理**
- 后端 `goods` 模块：`Goods` / `GoodsParam` 实体、Mapper + 映射文件、Service、Controller
- 商品接口：新增、编辑、删除、分页列表（按名称模糊查询）
- 前端商品列表 `GoodsList.vue`、商品添加 `AddGoods.vue`

**失物招领**
- 后端 `lost` 模块：`Lost` / `LostParam` 实体、Mapper + 映射文件、Service、Controller
- 失物接口：新增、编辑、删除、分页列表（按失物名称模糊查询）
- 前端失物列表 `LostList.vue`、失物添加 `AddLost.vue`、失物招领人页面 `LostPerson.vue`

**建议反馈**
- 后端 `suggest` 模块：`Suggest` / `SuggestParam` 实体、Mapper + 映射文件、Service、Controller
- 反馈接口：新增（自动记录反馈时间）、编辑、删除、分页列表（按标题模糊查询、按时间倒序）
- 前端反馈列表 `SuggestList.vue`、反馈添加 `AddSuggest.vue`

### 已完成：阶段六（部分）—— 登录认证、权限分配、动态菜单

**登录认证**
- 后端 `login` 模块：验证码生成 `/api/login/image`、登录 `/api/login/login`，支持会员 / 员工双账号体系
- 整合 kaptcha 图形验证码（Session 校验，Base64 返回前端）
- JWT 工具类 `JwtUtils` + JWT 配置（颁发者 / 密钥 / 过期时间），登录成功后签发 Token
- 登录校验：MD5 密码比对，按用户类型区分会员（member）与员工（sys_user）账号
- 前端登录页 `Login.vue`、Pinia 用户状态（持久化）、全局路由守卫（未登录自动跳转登录页）
- 会员新增接口密码 MD5 加密，为会员登录做准备

**角色权限分配（权限树）**
- 后端 `sys_role_menu`（角色-菜单中间表）模块：权限树回显 `/api/role/getMenuTree`、保存分配 `/api/role/saveRoleMenu`
- 权限树差异化：超级管理员查询全部菜单，普通用户查询本人可见菜单，并回显角色已分配菜单
- 前端权限分配弹框 `AssignRole.vue`，角色列表 `RoleList.vue` 增加分配入口

**动态菜单与用户信息**
- 后端新增 `/api/login/getInfo`（用户信息 + 权限标识）、`/api/login/getMenuList`（按用户动态菜单，组装路由树）
- 前端 `store/menu` + `permission.ts` 全局路由守卫：登录后动态注册路由，失败自动清理登录态
- 侧边菜单按权限动态渲染；新增个人中心 `center.vue`、退出登录 `LoginOut.vue`、头部用户头像下拉

### 开发路线图

| 阶段 | 内容 | 状态 |
|------|------|------|
| 阶段一 | 工程搭建与基础框架（后端多模块 + 前端布局） | 已完成 |
| 阶段二 | 系统管理模块：角色、员工、菜单管理 | 已完成 |
| 阶段三 | 会员业务：会员卡类型、会员信息、办卡、充值 | 已完成 |
| 阶段四 | 课程管理：课程维护与图片上传（已完成）、选课报名与我的课程 | 开发中 |
| 阶段五 | 业务扩展：器材（已完成）、商品（已完成）、失物招领（已完成）、反馈（已完成）、订单 | 开发中 |
| 阶段六 | 登录认证（已完成）、权限树分配（已完成）、动态菜单（已完成）、Spring Security、首页看板 | 开发中 |

## 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0（数据库名：`gym_system`）
- Node.js 18+、npm

## 快速启动

### 1. 初始化数据库

1. 创建数据库：`CREATE DATABASE gym_system DEFAULT CHARACTER SET utf8mb4;`
2. 执行 `docs/sql/` 下的初始化脚本（建表 + 初始数据，随开发阶段逐步补充）。

### 2. 启动后端

1. 修改 `backend/gymnasium-parent-project/gymnasium-service-web/src/main/resources/application-dev.yml` 中的数据库账号密码；
2. IDEA 中打开 `backend/gymnasium-parent-project`，运行 `GYMServiceWebApplication` 启动类；
3. 启动成功后访问接口文档：`http://localhost:8089/swagger-ui/index.html`。

### 3. 启动前端

```bash
cd fronted/gymansium-project
npm install        # 首次运行需要
npm run dev        # 启动开发服务，自动打开 http://localhost:8080
```

## 开发日志

每个阶段的迭代记录见 [CHANGELOG.md](./CHANGELOG.md)，与 Git 提交历史一一对应。

## License

本项目使用 [MIT](./LICENSE) 协议开源。
