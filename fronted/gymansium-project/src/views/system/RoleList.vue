<template>
    <el-main>
      <!-- 搜索栏 -->
      <el-form :model="listParam" :inline="true" size="default">
        <el-form-item>
          <el-input
            placeholder="请输入角色名称"
            v-model="listParam.roleName"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="searchBtn" :icon="Search">查询</el-button>
          <el-button @click="resetBtn" plain :icon="Close"type="danger">重置</el-button>
          <el-button type="primary" :icon="Plus" @click="addBtn">新增</el-button>
        </el-form-item>
      </el-form>
    <!-- 新增、编辑弹框 -->
    <role-add ref="addRef" ></role-add>
    </el-main>
   </template>
   <script setup lang="ts">
   import { Search, Edit, Plus, Close, Delete } from '@element-plus/icons-vue';
   import useTable from "@/composables/role/useTable";
   import useRole from "@/composables/role/useRole";
   import RoleAdd from "./RoleAdd.vue";
   //表格相关操作
  const { listParam, searchBtn, resetBtn } = useTable();
   //新增、编辑、删除
  const { addBtn, editBtn, deleteBtn,addRef } = useRole();
</script>
<style scoped></style>