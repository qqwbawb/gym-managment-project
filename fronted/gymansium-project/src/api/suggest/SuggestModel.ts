//列表查询的参数类型
export type SuggestParm = {
    currentPage: number,
    pageSize: number,
    title: string,
    total: number
}
//表单提交的数据类型
export type SuggestType = {
    type: string,
    id: string,
    title: string,
    content: string
}