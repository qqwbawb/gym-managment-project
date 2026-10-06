//商品类型
export type GoodsType = {
    type: string,
    goodsId: string,
    name: string,
    details: string,
    image: string,
    unit: string,
    specs: string,
    price: number,
    store: number
}

//商品查询数据类型
export type GoodsParam = {
    currentPage: number,
    pageSize: number,
    total: number,
    name: string
}