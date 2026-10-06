import { getListApi } from "@/api/material";
import type { ListParam } from "@/api/material/MaterialModel";
import { nextTick, onMounted, reactive, ref } from "vue";

export default function useTable() {

    //定义表格高度
    const tableHeight = ref(0)
    //定义表格数据
    const tableData = reactive({
        list: []
    })

    //定义列表查询参数
    const listParam = reactive<ListParam>({
        name: '',
        currentPage: 1,
        pageSize: 10,
        total: 0
    })

    //获取表格数据
    const getList = async () => {
        let res = await getListApi(listParam)
        if (res && res.code == 200) {
            tableData.list = res.data.records
            listParam.total = res.data.total
        }
    }

    //搜索
    const searchBtn = () => {
        getList()
    }

    //重置
    const resetBtn = () => {
        listParam.name = ''
        listParam.currentPage = 1,
            getList()
    }

    //页码变化
    const currentChange = (page: number) => {
        listParam.currentPage = page
        getList()
    }
    //容量变化
    const sizeChange = (size: number) => {
        listParam.pageSize = size
        getList()
    }

    //刷新
    const reFresh = () => {
        getList()
    }

    onMounted(() => {
        nextTick(() => {
            tableHeight.value = window.innerHeight - 230
        })
        getList()
    })

    return {
        listParam,
        searchBtn,
        resetBtn,
        sizeChange,
        currentChange,
        tableData,
        tableHeight,
        reFresh,
        getList
    }
}