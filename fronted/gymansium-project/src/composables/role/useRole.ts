import type { AddRoleModel } from '@/api/role/RoleModel'
import { reactive, ref } from 'vue'


export default function useRole() {

    //获取子组件暴露的弹框属性
    const addRef = ref<{ show: () => void }>();

    //新增
    const addBtn = () => {
        addRef.value?.show()  //点击新增按钮，弹框
    }

    //编辑
    const editBtn = () => {

    }

    //删除
    const deleteBtn = () => {

    }

    return {
        addBtn,
        editBtn,
        deleteBtn,
        addRef
    }

}