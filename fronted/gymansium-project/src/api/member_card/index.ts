import http from "@/http";
import type { CardType, ListParam } from "./MemberModel";

//新增
export const addApi = (param: CardType) => {
    return http.post("/api/memberCard", param)
}

//查询列表
export const getListApi = (param: ListParam) => {
    return http.get("/api/memberCard/list", param)
}

//修改
export const editApi = (param: CardType) => {
    return http.put("/api/memberCard", param)
}

//删除
export const deleteApi = (cardId: string) => {
    return http.delete(`/api/memberCard/${cardId}`)
}