<template>
    <SysDialog
      :title="dialog.title"
      :height="dialog.height"
      :width="dialog.width"
      :visible="dialog.visible"
      @close="onClose"
      @confirm="commit"
    >
      <template v-slot:content>
        <div>请选择会员卡类型</div>
        <el-divider style="margin: 10px 0px" />
        <el-select
          style="margin-top: 10px; width: 100%"
          v-model="parm.cardId"
          class="m-2"
          placeholder="请选择会员卡类型"
          size="large"
        >
          <el-option
            v-for="item in cardList"
            :key="item.cardId"
            :label="item.title"
            :value="item.cardId"
          />
        </el-select>
      </template>
    </SysDialog>
  </template>

<script setup lang="ts">
    import type { MemberType, ApplyCard } from "@/api/member/MemberModel";
    import SysDialog from "@/components/SysDialog.vue";
    import useDialog from "@/hooks/useDialog";
    import useJoin from "@/composables/member/useJoin";
    import { reactive } from "vue";
    import { ElMessage } from "element-plus";
    import { applySaveApi } from "@/api/member/index";
    //选择的卡类型
    const parm = reactive<ApplyCard>({
        cardId: "",
        memberId: "",
    });
    //弹框属性
    const { dialog, onClose, onShow } = useDialog();
    //会员卡数据
    const { cardList } = useJoin();
    //显示弹框
    const show = (row: MemberType) => {
        parm.memberId = row.memberId;
        dialog.title = "为【" + row.name + "】办卡";
        onShow();
    };
    //暴露给父组件调用
    defineExpose({
        show,
    });
    const eimits = defineEmits(['refresh'])
    //提交办卡
    const commit = async () => {
        if (!parm.cardId) {
            ElMessage.warning("请选择会员卡类型!");
            return;
        }
        let res = await applySaveApi(parm);
        if (res && res.code == 200) {
            ElMessage.success(res.msg);
            eimits('refresh')
            onClose()
        }
    };
</script>
 <style scoped></style>