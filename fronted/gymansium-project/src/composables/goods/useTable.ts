import type { GoodsParam } from "@/api/goods/GoodsModel";
import { nextTick, onMounted, reactive, ref } from "vue";
import { listApi } from "@/api/goods";
export default function useTable() {
    //表格高度
    const tableHeight = ref(0)
    //表格数据
    const tableData = reactive({
        list: []
    })
    //列表查询的参数
    const listParam = reactive<GoodsParam>({
        pageSize: 10,
        currentPage: 1,
        name: '',
        total: 0
    })
    //商品列表
    const getList = async () => {
        let res = await listApi(listParam)
        if (res && res.code == 200) {
            console.log(res)
            //设置表格数据
            tableData.list = res.data.records;
            //设置分页总数
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
        listParam.currentPage = 1
        getList()
    }
    //页容量改变触发
    const sizeChange = (size: number) => {
        listParam.pageSize = size;
        getList()
    }
    //页数改变触发
    const currentChange = (page: number) => {
        listParam.currentPage = page;
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
        listParam,
        searchBtn,
        resetBtn,
        tableData,
        getList,
        sizeChange,
        currentChange,
        tableHeight,
        reFresh
    }
}