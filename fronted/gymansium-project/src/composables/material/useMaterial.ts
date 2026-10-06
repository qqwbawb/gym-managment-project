import { deleteApi } from "@/api/material";
import type { MaterialType } from "@/api/material/MaterialModel";
import useInstance from "@/hooks/useInstance";
import { EditType } from "@/type/BaseEnum";
import type { FuncList } from "@/type/BaseType";
import { ElMessage } from "element-plus";
import { ref } from "vue";

export default function useMaterial(getList: FuncList) {
    const { global } = useInstance()
    //弹框属性
    const addRef = ref<{ show: (type: EditType, row?: MaterialType) => void }>()
    //新增
    const addBtn = () => {
        addRef.value?.show(EditType.ADD)
    }
    //编辑
    const editBtn = (row: MaterialType) => {
        addRef.value?.show(EditType.EDIT, row)
    }
    //删除
    const deleteBtn = async (row: MaterialType) => {
        let confirm = await global.$myconfirm("确定删除吗？")
        if (confirm) {
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