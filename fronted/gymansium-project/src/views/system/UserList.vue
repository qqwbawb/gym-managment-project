<template>
  <div>
    <el-button type="primary" size="default" @click="addBtn">新增</el-button>
    <sys-dialog
      :visible="dialog.visible"
      @close="onClose"
      @confirm="onConfirm"
    >
      <template #content>
        <div>我是弹框</div>
      </template>
    </sys-dialog>
  </div>
</template>

<script setup lang="ts">
import SysDialog from "@/components/SysDialog.vue";
import useDialog from "@/hooks/useDialog";

//弹框属性
const { dialog, onClose, onConfirm } = useDialog()

const addBtn = () => {
  dialog.visible = true;
};
</script>

<style scoped></style>
