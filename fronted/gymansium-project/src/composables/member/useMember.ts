import { deleteApi } from "@/api/member"
import type { MemberType } from "@/api/member/MemberModel"
import useInstance from "@/hooks/useInstance"
import { EditType } from "@/type/BaseEnum"
import type { FuncList } from "@/type/BaseType"
import { ElMessage } from "element-plus"
import { ref } from "vue"

export default function useMember(getList: FuncList) {

    const { global } = useInstance()
    const addRef = ref<{ show: (type: string, row?: MemberType) => void }>()

    const addBtn = () => {
        addRef.value?.show(EditType.ADD)
    }

    const editBtn = (row: MemberType) => {
        addRef.value?.show(EditType.EDIT, row)
    }

    const deleteBtn = async (row: MemberType) => {
        let confirm = await global.$myconfirm('确定删除该数据吗？')
        if (confirm) {
            let res = await deleteApi(row.memberId)
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