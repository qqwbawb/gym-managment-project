import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/Index.vue'

const routes: Array<RouteRecordRaw> = [
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
  {
    path: '/system',
    component: Layout,
    name: 'system',
    meta: {
      title: '系统管理',
      icon: 'Setting',
      roles: ['sys:manage']
    },
    children: [
      {
        path: '/userList',
        component: () => import('@/views/system/UserList.vue'),
        name: 'userList',
        meta: {
          title: '员工管理',
          icon: 'UserFilled',
          roles: ['sys:user']
        }
      },
      {
        path: '/roleList',
        component: () => import('@/views/system/RoleList.vue'),
        name: 'roleList',
        meta: {
          title: '角色管理',
          icon: 'User',
          roles: ['sys:role']
        }
      },
      {
        path: '/menuList',
        component: () => import('@/views/system/MenuList.vue'),
        name: 'menuList',
        meta: {
          title: '菜单管理',
          icon: 'Menu',
          roles: ['sys:menu']
        }
      }
    ]
  },
  {
    path: '/memberRoot',
    component: Layout,
    name: 'memberRoot',
    meta: {
      title: '会员管理',
      icon: 'UserFilled',
      roles: ['sys:memberRoot']
    },
    children: [
      {
        path: '/cardType',
        component: () => import('@/views/member/CardType.vue'),
        name: 'cardType',
        meta: {
          title: '会员卡类型',
          icon: 'Postcard',
          roles: ['sys:cardType']
        }
      },
      {
        path: '/memberList',
        component: () => import('@/views/member/MemberList.vue'),
        name: 'memberList',
        meta: {
          title: '会员管理',
          icon: 'Wallet',
          roles: ['sys:memberList']
        }
      },
      {
        path: '/myFee',
        component: () => import('@/views/member/MyFee.vue'),
        name: 'myFee',
        meta: {
          title: '我的充值',
          icon: 'CreditCard',
          roles: ['sys:myFee']
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
