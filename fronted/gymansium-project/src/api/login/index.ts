import http from "@/http"
import type { LoginParam } from "./LoginModel"

//获取验证码
export const getImageApi = () => {
    return http.post("/api/login/image")
}

//登录
export const loginApi = (param: LoginParam) => {
    return http.post("/api/login/login", param)
}