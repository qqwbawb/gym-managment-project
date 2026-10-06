import { nextTick, onMounted, reactive, ref } from "vue";
import type { LostParm } from '@/api/lost/LostModel'
import { getListApi } from "@/api/lost";
export default function useTable() {
    //表格高度
    const tableHeight = ref(0)
    //定义表格数据
    const tableData = reactive({
        list: []
    })
    //列表参数
    const listParm = reactive<LostParm>({
        currentPage: 1,
        pageSize: 10,
        lostName: '',
        total: 0
    })
    //重置
    const resetBtn = () => {
        listParm.lostName = ''
        listParm.currentPage = 1;
        getList()
    }
    //搜索
    const searchBtn = () => {
        getList()
    }
    //列表
    const getList = async () => {
        let res = await getListApi(listParm)
        if (res && res.code == 200) {
            console.log(res)
            tableData.list = res.data.records;
            listParm.total = res.data.total
        }
    }
    //页容量改变时触发
    const sizeChange = (size: number) => {
        listParm.pageSize = size;
        getList()
    }
    //页数改变时触发
    const currentChange = (page: number) => {
        listParm.currentPage = page;
        getList()
    }
    //刷新表格
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
        listParm,
        resetBtn,
        searchBtn,
        getList,
        tableData,
        sizeChange,
        currentChange,
        tableHeight,
        reFresh
    }
}