import { listApi } from "@/api/goods"
import type { AddCar, GoodsParam, GoodsType } from "@/api/goods/GoodsModel"
import { ElMessage } from "element-plus"
import { nextTick, onMounted, reactive, ref } from "vue"

export default function useChildDrawer() {
    const innerDrawer = ref(false)

    //关闭商品列表的内嵌抽屉
    const innerCloseClick = () => {
        innerDrawer.value = false
    }

    //表格高度
    const goodsHeight = ref(0)
    //选择的商品数据
    const selectGoods = reactive<AddCar>({
        list: []
    })
    //商品列表参数
    const goodsParam = reactive<GoodsParam>({
        name: '',
        currentPage: 1,
        pageSize: 10,
        total: 0
    })
    //商品表格中的数据
    const tableList = reactive({
        list: []
    })

    //获取商品列表
    const getList = async () => {
        let res = await listApi(goodsParam)
        if (res && res.code == 200) {
            tableList.list = res.data.records
            goodsParam.total = res.data.total
        }
    }

    //搜索
    const searchGoodsBtn = () => {
        getList()
    }

    //重置
    const resetGoodsBtn = () => {
        goodsParam.name = ''
        getList()
    }

    //列表打开时展示
    const innerDrawerShow = () => {
        getList()
        innerDrawer.value = true;
    }

    //页面容量改变
    const goodsSizeChange = (size: number) => {
        goodsParam.pageSize = size
        getList()
    }

    //页码改变
    const goodsCurrentChange = (page: number) => {
        goodsParam.currentPage = page
        getList()
    }

    //加入购物车
    const addCarBtn = (row: GoodsType) => {
        row.num = 1
        const flag = selectGoods.list.some(item => item.goodsId === row.goodsId)
        if (flag) {
            ElMessage.warning("该商品已经添加到购物车")
            return
        }
        selectGoods.list.push(row)
        ElMessage.success("加入购物车成功")
    }

    onMounted(() => {
        nextTick(() => {
            goodsHeight.value = window.innerHeight - 230
        })
        getList()
    })

    return {
        innerDrawer,
        innerCloseClick,
        getList,
        searchGoodsBtn,
        resetGoodsBtn,
        goodsParam,
        selectGoods,
        tableList,
        addCarBtn,
        goodsCurrentChange,
        goodsSizeChange,
        goodsHeight,
        innerDrawerShow
    }
}