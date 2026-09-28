import type { ListParam } from "@/api/role/RoleModel";
import { nextTick, onMounted, reactive, ref } from "vue";
import { getListApi } from '@/api/role/index'

export default function useTable() {

    //表格高度
    const tableHeight = ref(0)
    //表格数据
    const tableList = reactive({
        list: []
    })

    //查询列表参数
    const listParam = reactive<ListParam>({
        roleName: '',
        currentPage: 1,
        pageSize: 10,
        total: 0
    })

    //列表查询
    const getList = async () => {
        let res = await getListApi(listParam)
        if (res && res.code == 200) {
            console.log(res)
            //设置表格数据
            tableList.list = res.data.records
            //设置总记录
            listParam.total = res.data.total
        }
    }
    //搜索
    const searchBtn = () => {
        getList()
    }
    //重置
    const resetBtn = () => {
        listParam.roleName = ''
        getList()
    }
    //页面容量发生改变时触发
    const sizeChange = (val: number) => {
        listParam.pageSize = val
        getList()
    }

    //页码改变时触发
    const currentChange = (val: number) => {
        listParam.currentPage = val
        getList()
    }

    //刷新列表
    const refresh = () => {
        getList()
    }

    //页面打开时触发查询操作
    onMounted(() => {
        getList()
        nextTick(() => {
            tableHeight.value = window.innerHeight - 230
        })
    })

    return {
        listParam,
        getList,
        searchBtn,
        resetBtn,
        tableList,
        sizeChange,
        currentChange,
        tableHeight,
        refresh
    }
}