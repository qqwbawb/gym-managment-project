import type { GoodsParam, GoodsType } from "./GoodsModel"
import http from "@/http"
//新增
export const addApi = (parm: GoodsType) => {
    return http.post('/api/goods', parm)
}
//列表
export const listApi = (parm: GoodsParam) => {
    return http.get("/api/goods/list", parm)
}
//编辑
export const editApi = (parm: GoodsType) => {
    return http.put('/api/goods', parm)
}
//删除
export const deleteApi = (goodsId: string) => {
    return http.delete(`/api/goods/${goodsId}`)
}