<template>
  <menu-logo :isCollapsed="isCollapse"></menu-logo>
  <el-menu
    :collapse="isCollapse"
    :default-active="activeIndex"
    class="el-menu-vertical-demo"
    unique-opened
    background-color="#304156"
    router
  >
    <menu-item :menuList="menuList"></menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { reactive, computed } from "vue";
import MenuItem from "@/layout/menu/MenuItem.vue";
import MenuLogo from "@/layout/menu/MenuLogo.vue";
import { useRoute } from "vue-router";
import { collapseStore } from "@/store/collapse/index";

const colstore = collapseStore();
const isCollapse = computed(() => {
  return colstore.getCollapse;
});
const route = useRoute();
//获取激活的菜单
const activeIndex = computed(() => {
  const { path } = route;
  return path;
});
//树形的菜单数据
//reactive:定义响应式的数据（复杂类型，对象）
//ref:定义响应式的数据 （基本类型） let count = ref(0)
let menuList = reactive([
  {
    path: "/system",
    component: "Layout",
    name: "system",
    meta: {
      title: "系统管理",
      icon: "Setting",
      roles: ["sys:manage"],
    },
    children: [
      {
        path: "/userList",
        component: "/system/User/UserList",
        name: "userList",
        meta: {
          title: "员工管理",
          icon: "UserFilled",
          roles: ["sys:user"],
        },
      },
      {
        path: "/roleList",
        component: "/system/Role/RoleList",
        name: "roleList",
        meta: {
          title: "角色管理",
          icon: "User",
          roles: ["sys:role"],
        },
      },
      {
        path: "/menuList",
        component: "/system/Menu/MenuList",
        name: "menuList",
        meta: {
          title: "菜单管理",
          icon: "Menu",
          roles: ["sys:menu"],
        },
      },
    ],
  },
  {
    path: "/memberRoot",
    component: "Layout",
    name: "memberRoot",
    meta: {
      title: "会员管理",
      icon: "UserFilled",
      roles: ["sys:memberRoot"],
    },
    children: [
      {
        path: "/cardType",
        component: "/member/CardType",
        name: "cardType",
        meta: {
          title: "会员卡类型",
          icon: "Postcard",
          roles: ["sys:cardType"],
        },
      },
      {
        path: "/memberList",
        component: "/member/MemberList",
        name: "memberList",
        meta: {
          title: "会员管理",
          icon: "Wallet",
          roles: ["sys:memberList"],
        },
      },
      {
        path: "/myFee",
        component: "/system/MyFee",
        name: "myFee",
        meta: {
          title: "我的充值",
          icon: "CreditCard",
          roles: ["sys:myFee"],
        },
      },
    ],
  },
]);
</script>

<style scoped lang="scss">
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 230px;
  min-height: 400px;
}
.el-menu {
  border-right: none;
}
:deep(.el-sub-menu .el-sub-menu__title) {
  color: #f4f4f5 !important;
}
:deep(.el-menu .el-menu-item) {
  color: #bfcbd9;
}
/* 菜单点中文字的颜色 */
:deep(.el-menu-item.is-active) {
  color: #409eff !important;
}
/* 当前打开菜单的所有子菜单颜色 */
:deep(.is-opened .el-menu-item) {
  background-color: #1f2d3d !important;
}
/* 鼠标移动菜单的颜色 */
:deep(.el-menu-item:hover) {
  background-color: #001528 !important;
}
</style>
