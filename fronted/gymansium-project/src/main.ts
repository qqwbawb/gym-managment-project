import { createApp } from 'vue'
import App from './App.vue'
import router from '@/router/index'
//引入element plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
//引入Pinia构造函数
import { createPinia } from 'pinia'
// 实例化 Pinia
const pinia = createPinia()
//国际化
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import objCoppy from './utils/objCopy'
//信息确认框
import myconfirm from './utils/myconfirm'
const app = createApp(App);
app.use(router).use(ElementPlus, {
  locale: zhCn,
}).use(pinia)
app.mount('#app')
//挂载全局属性
app.config.globalProperties.$objCoppy = objCoppy;
app.config.globalProperties.$myconfirm = myconfirm;
//全局注册图标组件
for (const [key, component] of
  Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}