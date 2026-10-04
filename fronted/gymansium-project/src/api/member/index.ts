import http from "@/http"
import type { ApplyCard, MemberParam, MemberType, Recharge } from "./MemberModel"

export const addApi = (param: MemberType) => {
    return http.post("/api/member", param)
}

export const getListApi = (param: MemberParam) => {
    return http.get("/api/member/list", param)
}

export const editApi = (param: MemberType) => {
    return http.put("/api/member", param)
}

export const deleteApi = (memberId: string) => {
    return http.delete(`/api/member/${memberId}`)
}

//根据会员id查询角色信息
export const getRoleByMemberIdApi = (memberId: string) => {
    return http.get("/api/member/getRoleByMemberId", { memberId: memberId })
}

//查询会员卡列表
export const getCardListApi = () => {
    return http.get("/api/member/getCardList")
}

//办卡
export const applySaveApi = (param: ApplyCard) => {
    return http.post("/api/member/joinApply", param)
}

//充值
export const rechargeApi = (param: Recharge) => {
    return http.post("/api/member/recharge", param)
}