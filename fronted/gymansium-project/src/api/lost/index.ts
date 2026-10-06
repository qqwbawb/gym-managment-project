import http from "@/http";
import type { LostParm, LostType } from "./LostModel";
//新增
export const addApi = (parm: LostType) => {
    return http.post('/api/lost', parm)
}
//列表
export const getListApi = (parm: LostParm) => {
    return http.get("/api/lost/list", parm)
}
//编辑
export const editApi = (parm: LostType) => {
    return http.put('/api/lost', parm)
}
//删除
export const deleteApi = (lostId: string) => {
    return http.delete(`/api/lost/${lostId}`)
}