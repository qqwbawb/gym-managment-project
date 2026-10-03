//定义菜单类型
export type MenuType = {
    editType: string,
    menuId: string,
    parentId: string | number,
    title: string,
    code: string,
    name: string,
    path: string,
    url: string,
    type: string,
    icon: string,
    parentName: string,
    orderNum: string,
    open: boolean
}

//定义选中上级菜单的数据类型
export type SelectNode = {
    parentId: string | number,
    parentName: string
}