import http from "@/http";
import type { AddRoleModel } from './RoleModel'

//新增
export const addApi = (param: AddRoleModel) => {
    return http.post("/api/role", param)
}