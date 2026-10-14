import { getInfoApi } from "@/api/login";
import { defineStore } from "pinia";

//定义store
export const userStore = defineStore('userStore', {
    state: () => {
        return {
            userId: '',
            token: '',
            userType: '',
            codeList: []
        }
    },
    getters: {
        getUserId(state) {
            return state.userId
        },
        getToken(state) {
            return state.token
        },
        getUserType(state) {
            return state.userType
        }
    },
    actions: {
        setUserId(userId: string) {
            this.userId = userId
        },
        setToken(token: string) {
            this.token = token
        },
        setUserType(userType: string) {
            this.userType = userType
        },
        //清空登录状态
        //注意：只调用 localStorage.clear() 是清不掉内存里的 token 的（pinia 的 state 仍在），
        //会导致路由守卫继续认为「已登录」，进而无限重定向。必须显式重置 state。
        clearUser() {
            this.userId = ''
            this.token = ''
            this.userType = ''
            this.codeList = []
            localStorage.clear()
        },
        getInfo() {
            return new Promise((resolve, reject) => {
                getInfoApi({ userType: this.userType, userId: this.userId }).then((res) => {
                    if (res && res.code == 200) {
                        this.codeList = res.data.permissions
                    }
                    resolve(this.codeList)
                }).catch((error) => {
                    reject(error)
                })
            })
        }
    },
    persist: true
})