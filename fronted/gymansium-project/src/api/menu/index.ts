import http from "@/http"
import type { MenuType } from "./MenuModel"

//查询上级菜单
export const getParentApi = () => {
    return http.get("/api/menu/parent")
}

//新增菜单
export const addApi = (param: MenuType) => {
    return http.post("/api/menu", param)
}

//查询菜单列表
export const getListApi = () => {
    return http.get("/api/menu/list")
}

//编辑菜单
export const editApi = (param: MenuType) => {
    return http.put("/api/menu", param)
}

//删除菜单
export const deleteApi = (menuId: String | number) => {
    return http.delete(`/api/menu/${menuId}`)
}