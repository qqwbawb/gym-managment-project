import type { AddUserModel } from "@/api/user/UserModel"
import { EditType } from "@/type/BaseEnum"
import { ref } from "vue"
import { deleteApi, resetPwdApi } from "@/api/user"
import { ElMessage } from "element-plus"
import type { FuncList } from "@/type/BaseType"
import useInstance from "@/hooks/useInstance"

export default function useUser(getList: FuncList) {
    const { global } = useInstance()
    //新增弹框属性
    const addRef = ref<{ show: (type: string, row?: AddUserModel) => void }>()

    //新增
    const addBtn = () => {
        //父组件调用子组件中的show方法
        addRef.value?.show(EditType.ADD)
    }

    //编辑
    const editBtn = (row: AddUserModel) => {
        addRef.value?.show(EditType.EDIT, row)
    }

    //删除
    const deleteBtn = async (row: AddUserModel) => {
        const confirm = await global.$myconfirm('确定要删除吗？')
        if (confirm) {
            let res = await deleteApi(row.userId)
            if (res && res.code == 200) {
                ElMessage.success(res.msg)
                //刷新表格
                getList()
            }
        }
    }

    //重置密码
    const resetPwdBtn = async (row: AddUserModel) => {
        let confirm = await global.$myconfirm('确定重置密码吗？')
        if (confirm) {
            let res = await resetPwdApi(row)
            if (res && res.code == 200) {
                ElMessage.success(res.msg)
                //刷新表格
                getList()
            }
        }
    }

    return {
        addBtn,
        editBtn,
        deleteBtn,
        resetPwdBtn,
        addRef
    }
}