<template>
  <div class="logincontainer">
    <el-form
      class="loginForm"
      :model="loginModel"
      ref="loginForm"
      :rules="rules"
      size="default"
    >
      <el-form-item style="display: flex; justify-content:center">
        <div class="loginTitle">系统登录</div>
      </el-form-item>
      <el-form-item>
        <el-input
          size="large"
          v-model="loginModel.username"
          placeholder="请输入账号"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-input
          size="large"
          v-model="loginModel.password"
          placeholder="请输入密码"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-row :gutter="20">
          <el-col :span="16" :offset="0">
            <el-input
              size="large"
              v-model="loginModel.code"
              placeholder="请输入验证码"
            ></el-input>
          </el-col>
          <el-col :span="8" :offset="0">
            <img :src="imgSrc" class="image" @click="getImage" />
          </el-col>
        </el-row>
      </el-form-item>
      <el-form-item label="用户类型">
        <el-radio-group v-model="loginModel.userType">
          <el-radio value="1">会员</el-radio>
          <el-radio value="2">员工</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item>
        <el-row style="width: 100%">
          <el-col
            :span="12"
            :offset="0"
            style="padding-right: 10px; padding-left: 0px"
          >
            <el-button
              style="width: 100%"
              size="large"
              type="primary"
              @click="onSubmit"
              >登录</el-button>
          </el-col>
          <el-col
            :span="12"
            :offset="0"
            style="padding-right: 0px; padding-left: 10px"
          >
            <el-button type="danger" plain style="width: 100%"size="large">重置</el-button>
          </el-col>
        </el-row>
      </el-form-item>
    </el-form>
  </div>
</template>
<script setup lang="ts">
import { reactive } from "vue";
import useImage from "@/composables/login/useImage";
import { loginApi } from "@/api/login/index";
import { useRouter } from "vue-router";
import { userStore } from "@/store/user";
const store = userStore()
const router = useRouter();
const { imgSrc, getImage } = useImage();
//表单对象
const loginModel = reactive({
  username: "",
  password: "",
  code: "",
  userType: "2",
});
//表单验证规则
const rules = reactive({});
//登录提交
const onSubmit = async () => {
  const res = await loginApi(loginModel);
  if (res && res.code == 200) {
    store.setToken(res.data.token)
    store.setUserId(res.data.userId)
    store.setUserType(loginModel.userType)
    //跳转到首页
    router.push({ path: "/" });
  }
};
</script>
<style scoped lang="scss">
.logincontainer {
  background-color: #ffff;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url("../../assets/bg.png");
  background-size: 100% 100%;
  .loginForm {
    border-radius: 10px;
    background-color: #fff;
    width: 430px;
    height: 340px;
    padding: 20px 35px;
    .loginTitle {
      width: 100%;
      font-size: 24px;
      font-weight: 600;
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .image {
      height: 40px;
      width: 100%;
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }
}
</style>