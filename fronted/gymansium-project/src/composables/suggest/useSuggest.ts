import type { SuggestType } from "@/api/suggest/SuggestModel"
import { EditType } from "@/type/BaseEnum"
import { ref } from "vue"
import { deleteApi } from "@/api/suggest"
import useInstance from "@/hooks/useInstance"
import { ElMessage } from "element-plus"
import type { FuncList } from "@/type/BaseType"
export default function useSuggest(getList: FuncList) {
    const { global } = useInstance()
    const addRef = ref<{ show: (type: string, row?: SuggestType) => void }>()
    //新增
    const addBtn = () => {
        addRef.value?.show(EditType.ADD)
    }
    //编辑
    const editBtn = (row: SuggestType) => {
        addRef.value?.show(EditType.EDIT, row)
    }
    //删除
    const deleteBtn = async (row: SuggestType) => {
        const confrim = await global.$myconfirm('确定删除该数据吗?')
        if (confrim) {
            let res = await deleteApi(row.id)
            if (res && res.code == 200) {
                ElMessage.success(res.msg)
                getList()
            }
        }
    }
    return {
        addBtn,
        editBtn,
        deleteBtn,
        addRef
    }
}