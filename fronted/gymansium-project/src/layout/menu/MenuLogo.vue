<template>
  <div class="logo">
    <img :src="MenuLogo" />
    <span v-if="show" class="logo-title">{{ title }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import MenuLogo from "@/assets/MenuLogo.png";
const title = ref("健身房管理系统");
const props = defineProps({
  isCollapsed: {
    required: true,
    type: Boolean,
  },
});
const show = ref(true);
watch(
  () => props.isCollapsed,
  (collapsed: boolean) => {
    if (!collapsed) {
      setTimeout(() => {
        show.value = !collapsed;
      }, 300);
    } else {
      show.value = !collapsed;
    }
  }
);
</script>

<style scoped lang="scss">
.logo {
  display: flex;
  width: 100%;
  height: 60px;
  line-height: 60px;
  background: #2b2f3a;
  text-align: center;
  cursor: pointer;
  align-items: center;
  img {
    width: 36px;
    height: 36px;
    margin-right: 12px;
    margin-left: 20px;
  }
  .logo-title {
    color: #fff;
    font-weight: 800;
    line-height: 60px;
    font-size: 22px;
    font-family: FangSong;
  }
}
</style>
