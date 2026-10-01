import http from "@/http";
import type { AddUserModel, ListParam } from './UserModel'

//查询角色列表
export const getSelectApi = () => {
    return http.get("/api/role/getSelect")
}

//新增
export const addApi = (param: AddUserModel) => {
    return http.post("/api/user", param)
}

//查询用户列表
export const getListApi = (param: ListParam) => {
    return http.get("/api/user/list", param)
}

//编辑
export const editApi = (param: AddUserModel) => {
    return http.put("/api/user", param)
}

//删除
export const deleteApi = (userId: string) => {
    return http.delete(`/api/user/${userId}`)
}

//根据用户id查询角色
export const getRoleApi = (userId: string) => {
    return http.get("/api/user/role", { userId: userId })
}

//重置密码
export const resetPwdApi = (param: AddUserModel) => {
    return http.put("/api/user/resetPwd", param)
}