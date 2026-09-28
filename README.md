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

### 已完成：阶段二（部分）—— 角色管理模块

**后端**
- 角色管理 CRUD 接口（实体 / Mapper / Service / Controller）
- 正式整合 Swagger2 接口文档（2.9.2）与跨域配置

**前端**
- 角色列表：表格展示、搜索、重置、分页、高度自适应
- 新增 / 编辑弹框：角色类型选择（员工 / 会员）、编辑回显
- 删除确认提示，操作成功后自动刷新列表
- 前端公共能力：Element Plus 中文国际化、`$objCoppy` / `$myconfirm` 全局工具、`useInstance` Hook、枚举定义

### 开发路线图

| 阶段 | 内容 | 状态 |
|------|------|------|
| 阶段一 | 工程搭建与基础框架（后端多模块 + 前端布局） | 已完成 |
| 阶段二 | 系统管理模块：角色管理（已完成）、员工、菜单 CRUD 与权限分配 | 开发中 |
| 阶段三 | 会员业务：会员卡类型、会员信息、办卡、充值 | 待开发 |
| 阶段四 | 课程管理：课程维护、图片上传、选课报名 | 待开发 |
| 阶段五 | 业务扩展：器材、商品、订单、失物招领、反馈 | 待开发 |
| 阶段六 | 登录认证（验证码 + JWT）、Spring Security 权限、首页数据看板 | 待开发 |

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
