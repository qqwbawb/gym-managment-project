import http from "@/http";
import type { AddRoleModel, ListParam } from './RoleModel'

//新增
export const addApi = (param: AddRoleModel) => {
    return http.post("/api/role", param)
}

//查询角色列表
export const getListApi = (param: ListParam) => {
    return http.get("/api/role/list", param)
}

//删除
export const deleteApi = (roleId: String) => {
    return http.delete(`/api/role/${roleId}`)
}

//编辑
export const editApi = (param: AddRoleModel) => {
    return http.put("/api/role", param)
}