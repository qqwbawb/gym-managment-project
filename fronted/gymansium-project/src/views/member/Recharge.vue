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
        <el-form
          :model="addModel"
          ref="addFormRef"
          :rules="rules"
          label-width="80px"
          size="default"
        >
          <el-form-item prop="money" label="充值金额">
            <el-input type="number" v-model="addModel.money"></el-input>
          </el-form-item>
        </el-form>
      </template>
    </SysDialog>
  </template>

<script setup lang="ts">
    import type { MemberType, Recharge } from "@/api/member/MemberModel";
    import SysDialog from "@/components/SysDialog.vue";
    import useDialog from "@/hooks/useDialog";
    import { ElMessage } from "element-plus";
    import type { FormInstance } from "element-plus";
    import { reactive, ref } from "vue";
    import { rechargeApi } from "@/api/member/index";
    const addFormRef = ref<FormInstance>();
    //表单绑定值
    const addModel = reactive<Recharge>({
        userId: "",
        memberId: "",
        money: 0,
    });
    const validMoney = (_rule: any, value: any, callback: any) =>
{
        if (value <= 0) {
            callback(new Error("充值金额不能小于0"));
        } else {
            callback();
        }
    };
    //表单验证规则
    const rules = reactive({
        money: [
            {
                required: true,
                trigger: "change",
                validator: validMoney,
            },
        ],
    });
    //弹框属性
    const { dialog, onClose, onShow } = useDialog();
    //显示弹框
    const show = (row: MemberType) => {
        dialog.title = "为【" + row.name + "】充值";
        addModel.memberId = row.memberId
        onShow();
        addFormRef.value?.resetFields();
    };
    //暴露出去
    defineExpose({
        show,
    });
    //注册事件
    const emits = defineEmits(['refresh'])
    //提交表单
    const commit = () => {
        addFormRef.value?.validate(async (valid) => {
            if (valid) {
                let res = await rechargeApi(addModel);
                if (res && res.code == 200) {
                    ElMessage.success(res.msg)
                    emits('refresh')
                    onClose();
                }
            }
        });
    };
</script>

<style scoped></style>