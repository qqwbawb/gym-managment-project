import { getListApi } from "@/api/member";
import type { MemberParam } from "@/api/member/MemberModel";
import { nextTick, onMounted, reactive, ref } from "vue";

export default function useTable() {

    const tableHeight = ref(0)
    const tableList = reactive({
        list: []
    })

    const listParam = reactive<MemberParam>({
        name: '',
        phone: '',
        username: '',
        currentPage: 1,
        pageSize: 10,
        total: 0
    })

    const getList = async () => {
        let res = await getListApi(listParam)
        if (res && res.code == 200) {
            tableList.list = res.data.records
            listParam.total = res.data.total
        }
    }

    const searchBtn = () => {
        getList()
    }

    const resetBtn = () => {
        listParam.name = ''
        listParam.phone = ''
        listParam.username = ''
        getList()
    }

    const sizeChange = (size: number) => {
        listParam.pageSize = size
        getList()
    }

    const currentChange = (page: number) => {
        listParam.currentPage = page
        getList()
    }

    const refresh = () => {
        getList()
    }

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
        tableHeight,
        tableList,
        sizeChange,
        currentChange,
        refresh
    }
}