import { listApi } from "@/api/order";
import type { OrderListParam } from "@/api/order/OrderModel";
import { nextTick, onMounted, reactive, ref } from "vue";


export default function useOrderTable() {

    //表格高度
    const orderTableHeight = ref(0)
    //表格数据
    const tableData = reactive({
        list: []
    })
    //列表参数
    const listParam = reactive<OrderListParam>({
        name: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        controlName: ''
    })

    //获取订单列表
    const getList = async () => {
        let res = await listApi(listParam)
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
        getList()
    }

    //页面容量改变
    const orderSizeChange = (size: number) => {
        listParam.pageSize = size
        getList()
    }

    //页码改变
    const orderCurrentChange = (page: number) => {
        listParam.currentPage = page
        getList()
    }

    onMounted(() => {
        nextTick(() => {
            orderTableHeight.value = window.innerHeight - 230
        })
        getList()
    })

    return {
        listParam,
        searchBtn,
        resetBtn,
        getList,
        orderCurrentChange,
        orderSizeChange,
        tableData,
        orderTableHeight
    }
}