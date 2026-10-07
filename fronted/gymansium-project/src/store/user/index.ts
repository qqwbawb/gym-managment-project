import { defineStore } from "pinia";

//定义store
export const userStore = defineStore('userStore', {
    state: () => {
        return {
            userId: '',
            token: ''
        }
    },
    getters: {
        getUserId(state) {
            return state.userId
        },
        getToken(state) {
            return state.token
        }
    },
    actions: {
        setUserId(userId: string) {
            this.userId = userId
        },
        setToken(token: string) {
            this.token = token
        }
    },
    persist: {
        key: 'userStore',
        storage: localStorage,
        paths: ['userId', 'token']
    }
})