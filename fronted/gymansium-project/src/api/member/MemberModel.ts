//定义member类型
export type MemberType = {
    type: string,
    memberId: string,
    sex: string
    name: string,
    phone: string,
    age: string,
    birthday: string,
    height: string,
    weight: string,
    waist: string,
    joinTime: string,
    endTime: string,
    username: string,
    password: string,
    status: string,
    roleId: string
}
//分页查询数据类型
export type MemberParam = {
    name: string,
    phone: string,
    username: string,
    currentPage: number,
    pageSize: number,
    total: number
}
//办卡数据类型
export type ApplyCard = {
    memberId: string,
    cardId: string
}
//充值数据类型
export type Recharge = {
    memberId: string
    money: number
    userId: string
}