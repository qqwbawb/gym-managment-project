import http from "@/http";
import type { SuggestParm, SuggestType } from "./SuggestModel";
//新增
export const addApi = (parm: SuggestType) => {
    return http.post("/api/suggest", parm)
}
//列表
export const listApi = (parm: SuggestParm) => {
    return http.get("/api/suggest/list", parm)
}
//编辑
export const editApi = (parm: SuggestType) => {
    return http.put('/api/suggest', parm)
}
//删除
export const deleteApi = (id: string) => {
    return http.delete(`/api/suggest/${id}`)
}