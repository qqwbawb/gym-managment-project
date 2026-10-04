//查询参数类型
export type ListParam = {
    title: string,
    currentPage: number,
    pageSize: number,
    total: number
}
//会员卡数据类型
export type CardType = {
    type: string,
    title: string,
    cardType: string,
    cardId: string,
    cardDay: number,
    price: string,
    status: string
}
