//列表参数类型
export type LostParm = {
    currentPage: number,
    pageSize: number,
    lostName: string,
    total: number
}
//失物数据类型
export type LostType = {
    type: string,
    lostId: string,
    lostName: string,
    foundTime: string,
    foundAddress: string,
    foundPerson: string,
    foundPhone: string,
    status: string,
    lostPerson: string,
}