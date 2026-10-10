import { getMenuListApi } from "@/api/login"
import type { InfoParam } from "@/api/login/LoginModel"
import { defineStore } from "pinia"
import type { RouteRecordRaw } from "vue-router"
import Layout from '@/layout/Index.vue'
import center from "@/layout/center/center.vue"

//获取views目录下面的所有组件信息
const modules = import.meta.glob('../../views/**/*.vue')
//定义store
export const menuStore = defineStore('menuStore', {

    state: () => {
        return {
            menuList: []
        }
    },
    getters: {
        getMenuList(state) {
            return state.menuList
        }
    },
    actions: {
        getMenu(router: any, param: InfoParam) {
            return new Promise((resolve, reject) => {
                getMenuListApi(param).then((res) => {
                    let accessRoute;
                    if (res && res.code == 200) {
                        //动态生成路由信息
                        accessRoute = generateRoutes(res.data, router)
                        const desk = [
                            {
                                path: "/dashboard",
                                component: "Layout",
                                name: "dashboard",
                                meta: {
                                    title: "首页",
                                    icon: "HomeFilled",
                                    roles: ["sys:dashboard"],
                                },
                                children: []
                            }
                        ] as any
                        this.menuList = desk.concat(accessRoute)
                    }
                    resolve(this.menuList)
                }).catch((error) => {
                    reject(error)
                })
            })
        }
    },
    persist: false

})
//动态生成路由的方法
export function generateRoutes(routes: RouteRecordRaw[], router: any) {
    //定义接收生成的菜单
    const res: Array<RouteRecordRaw> = [];
    routes.forEach((route: any) => {
        //把router里面的数据放到tmp里面
        const tmp = { ...route }
        const component = tmp.component
        if (route.component) {
            if (component == 'Layout') {
                tmp.component = Layout
            } else {
                tmp.component = modules[`../../views${component}.vue`]
            }
        }
        //如果存在下级菜单
        if (tmp.children && tmp.children.length > 0) {
            if (route.component != 'Layout') {
                tmp.component = center
            }
            //递归调用
            tmp.children = generateRoutes(tmp.children, router)
        }
        //动态添加路由
        router.addRoute(tmp)
        res.push(tmp)
    })
    return res
}