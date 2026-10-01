import { reactive, ref } from 'vue';
import { getSelectApi } from '@/api/user';
import type { SelectRole } from '@/api/user/UserModel'
import { getRoleApi } from "@/api/user"

export default function useSelectRole() {

    const roleId = ref('')

    //定义角色数据
    const roleData = reactive<SelectRole>({
        list: []
    })

    //获取列表
    const listRole = async () => {
        let res = await getSelectApi()
        if (res && res.code == 200) {
            roleData.list = res.data
        }
    }

    //根据用户id查询角色
    const getRole = async (userId: string) => {
        roleId.value = ''
        let res = await getRoleApi(userId)
        if (res && res.code == 200 && res.data) {
            roleId.value = res.data.roleId
        }
    }

    return {
        roleData,
        listRole,
        getRole,
        roleId
    }

}