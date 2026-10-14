import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/Index.vue'

/**
 * 静态路由（constantRoutes）
 *
 * 本项目的业务路由由后端 sys_menu 动态下发（见 store/menu/index.ts 的 getMenu），
 * 登录后通过 router.addRoute 注册。这里只保留不依赖权限的公共路由：
 *   - /login        登录页
 *   - /dashboard    首页（所有角色都可访问）
 *
 * 原文件下方曾存在一份 227 行的静态 routes 定义，与动态菜单重复且从未被
 * createRouter 使用（属于死代码），已移除以避免误导后续维护者。
 */
export const constantRoutes: Array<RouteRecordRaw> = [
  {
    path: "/login",
    component: () => import('@/views/login/Login.vue'),
    name: "login"
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        component: () => import('@/layout/dashboard/Index.vue'),
        name: 'dashboard',
        meta: {
          title: '首页',
          icon: 'HomeFilled'
        }
      }
    ]
  },
]
const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

export default router
