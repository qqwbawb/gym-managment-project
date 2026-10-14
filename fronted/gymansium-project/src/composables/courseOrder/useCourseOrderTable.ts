import { courseOrderListApi } from "@/api/order";
import type { CourseOrderListParam } from "@/api/order/CourseOrderModel";
import { nextTick, onMounted, reactive, ref } from "vue";

/**
 * 课程订单列表（模仿商品订单 useOrderTable）
 */
export default function useCourseOrderTable() {

    //表格高度
    const courseOrderTableHeight = ref(0)
    //表格数据
    const tableData = reactive({
        list: []
    })
    //列表参数
    const listParam = reactive<CourseOrderListParam>({
        courseName: '',
        currentPage: 1,
        pageSize: 10,
        total: 0
    })

    //获取课程订单列表
    const getList = async () => {
        let res = await courseOrderListApi(listParam)
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
        listParam.courseName = ''
        getList()
    }

    //页面容量改变
    const courseOrderSizeChange = (size: number) => {
        listParam.pageSize = size
        getList()
    }

    //页码改变
    const courseOrderCurrentChange = (page: number) => {
        listParam.currentPage = page
        getList()
    }

    onMounted(() => {
        nextTick(() => {
            courseOrderTableHeight.value = window.innerHeight - 230
        })
        getList()
    })

    return {
        listParam,
        searchBtn,
        resetBtn,
        getList,
        courseOrderCurrentChange,
        courseOrderSizeChange,
        tableData,
        courseOrderTableHeight
    }
}
