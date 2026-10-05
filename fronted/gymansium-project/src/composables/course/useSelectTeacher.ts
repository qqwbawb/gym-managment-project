import { ref } from 'vue'
import http from '@/http'

export default function useSelectTeacher() {
    //教练列表
    const teacherData = ref<{ list: any[] }>({ list: [] });
    //获取教练数据列表
    const listTeacher = async () => {
        let res = await http.get('/api/user/getTeacher');
        if (res && res.code == 200) {
            teacherData.value.list = res.data;
        }
    }
    return {
        teacherData,
        listTeacher
    }
}
