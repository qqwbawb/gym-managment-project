//分页数据类型
export type ListParam = {
    name: string,
    currentPage: number,
    pageSize: number,
    total: number
}

//设备数据类型
export type MaterialType = {
    id: string,
    name: string,
    details: string,
    numTotal: number,
    type: string
}