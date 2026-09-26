<template>
  <el-breadcrumb class="bred" separator="/">
    <el-breadcrumb-item v-for="item in tabs">{{ item.meta.title }}</el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import type { Ref } from "vue";
import { useRoute } from "vue-router";
import type { RouteLocationMatched } from "vue-router";

//面包屑数据
const tabs: Ref<RouteLocationMatched[]> = ref([]);
//获取当前路由
const route = useRoute();

const getBredcrumb = () => {
  //从路由里面获取所有有meta和title
  let mached = route.matched.filter((item) => item.meta && item.meta.title);
  //console.log(mached)
  //判断第一个是否是首页,不是,构造一个
  const first = mached[0];
  if (first.path !== "/dashboard") {
    mached = [{ path: "/dashboard", meta: { title: "首页" } } as any].concat(mached);
  }
  tabs.value = mached;
};

getBredcrumb();
watch(
  () => route.path,
  () => getBredcrumb()
);
</script>

<style scoped lang="scss">
//修改字体颜色
:deep(.el-breadcrumb__inner) {
  color: #fff !important;
}
.bred {
  margin-left: 20px;
}
</style>
