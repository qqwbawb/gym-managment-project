# 健身房管理系统 — 前端（gymansium-project）

健身房管理系统前端工程，基于 **Vue 3 + TypeScript + Vite + Element Plus**。

## 技术栈

- Vue 3.5、TypeScript、Vite 8
- Element Plus 2.14（UI 组件库）、@element-plus/icons-vue
- Pinia（状态管理）、Vue Router 4（路由）
- Axios（HTTP 请求封装）
- Sass（样式预处理器）

## 环境要求

- Node.js 18+、npm

## 安装与启动

```bash
npm install        # 安装依赖
npm run dev        # 启动开发服务，自动打开 http://localhost:8080
npm run build      # 打包构建（产物输出至 dist/）
npm run preview    # 预览构建产物
```

## 目录结构

```
src/
├── api/           # 接口请求封装（按业务模块）
├── components/    # 公共组件（SysDialog 等）
├── composables/   # 组合式函数（业务逻辑）
├── hooks/         # 自定义 Hook（useDialog 等）
├── http/          # Axios 封装（拦截器、Token 携带、统一错误处理）
├── layout/        # 主布局（菜单/Header/面包屑/Tabs）
├── router/        # 路由配置
├── store/         # Pinia 状态管理
├── type/          # 公共类型定义
└── views/         # 页面组件（system 系统管理 / member 会员管理）
```

## 开发约定

- 路径别名：`@` 指向 `src`
- 后端接口基地址：`src/http/index.ts` 中的 `baseURL`（默认 `http://localhost:8089`）
- 接口返回统一格式：`{ code, msg, data }`，code 为 200 表示成功
- 新增页面流程：路由配置 → 页面组件 → api 接口封装 → composables 业务逻辑

## 接口文档

后端启动后访问 `http://localhost:8089/swagger-ui/index.html`。

> 项目整体说明见仓库根目录 [README.md](../../README.md)。
