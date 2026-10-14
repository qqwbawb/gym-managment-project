import { courseDownApi } from "@/api/order";
import { listApi } from "@/api/course";
import { getListApi } from "@/api/member";
import type { CourseType } from "@/api/course/CourseModel";
import type { MemberType } from "@/api/member/MemberModel";
import { ElMessage } from "element-plus";
import { nextTick, onMounted, reactive, ref } from "vue";

/**
 * 课程下单抽屉（模仿商品订单 useFaDrawer + useChildDrawer 的组合）。
 * 流程：点击下单 → 选择报名会员 → 内嵌抽屉选择课程加入选课车 → 确定提交。
 */
export default function useCourseOrderDrawer(getList: () => void) {
    //外层下单抽屉
    const faDrawer = ref(false)
    //选择会员内嵌抽屉
    const memberDrawer = ref(false)
    //选择课程内嵌抽屉
    const courseDrawer = ref(false)
    //已选中的报名会员
    const selectedMember = ref<MemberType | null>(null)

    //课程列表参数（内嵌抽屉）
    const courseParam = reactive({
        courseName: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        teacherName: ''
    })
    //课程表格数据
    const courseTableList = reactive({
        list: []
    })
    //选课车
    const selectCourses = reactive({
        list: [] as CourseType[]
    })

    //会员列表参数（内嵌抽屉）
    const memberParam = reactive({
        name: '',
        phone: '',
        username: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        userId: '',
        userType: '2'
    })
    //会员表格数据
    const memberTableList = reactive({
        list: []
    })

    const courseHeight = ref(0)
    const memberHeight = ref(0)

    //获取课程列表
    const getCourseList = async () => {
        let res = await listApi(courseParam)
        if (res && res.code == 200) {
            courseTableList.list = res.data.records
            courseParam.total = res.data.total
        }
    }

    //获取会员列表（管理端：全部会员）
    const getMemberList = async () => {
        let res = await getListApi(memberParam)
        if (res && res.code == 200) {
            memberTableList.list = res.data.records
            memberParam.total = res.data.total
        }
    }

    //点击下单，打开抽屉并清空上一次选择
    const order = () => {
        selectCourses.list = []
        selectedMember.value = null
        faDrawer.value = true
    }

    //取消，关闭抽屉
    const cancelClick = () => {
        faDrawer.value = false
    }

    //打开选择会员抽屉
    const memberDrawerShow = () => {
        getMemberList()
        memberDrawer.value = true
    }

    //关闭选择会员抽屉
    const memberDrawerClose = () => {
        memberDrawer.value = false
    }

    //选中会员
    const selectMemberBtn = (row: MemberType) => {
        selectedMember.value = row
        memberDrawer.value = false
        ElMessage.success("已选择会员：" + row.name)
    }

    //打开选择课程抽屉
    const courseDrawerShow = () => {
        getCourseList()
        courseDrawer.value = true
    }

    //关闭选择课程抽屉
    const courseDrawerClose = () => {
        courseDrawer.value = false
    }

    //课程加入选课车
    const addCourseBtn = (row: CourseType) => {
        const flag = selectCourses.list.some(item => item.courseId === row.courseId)
        if (flag) {
            ElMessage.warning("该课程已经添加到选课车")
            return
        }
        selectCourses.list.push(row)
        ElMessage.success("加入选课车成功")
    }

    //选课车删除
    const deleteCourseBtn = (row: CourseType) => {
        selectCourses.list = selectCourses.list.filter(item => item.courseId != row.courseId)
    }

    //会员搜索
    const searchMemberBtn = () => {
        getMemberList()
    }
    //会员重置
    const resetMemberBtn = () => {
        memberParam.name = ''
        memberParam.phone = ''
        memberParam.username = ''
        getMemberList()
    }
    //会员分页
    const memberSizeChange = (size: number) => {
        memberParam.pageSize = size
        getMemberList()
    }
    const memberCurrentChange = (page: number) => {
        memberParam.currentPage = page
        getMemberList()
    }

    //课程搜索
    const searchCourseBtn = () => {
        getCourseList()
    }
    //课程重置
    const resetCourseBtn = () => {
        courseParam.courseName = ''
        getCourseList()
    }
    //课程分页
    const courseSizeChange = (size: number) => {
        courseParam.pageSize = size
        getCourseList()
    }
    const courseCurrentChange = (page: number) => {
        courseParam.currentPage = page
        getCourseList()
    }

    //提交下单
    const commit = async () => {
        if (!selectedMember.value) {
            ElMessage.error("请先选择报名会员!")
            return
        }
        if (selectCourses.list.length == 0) {
            ElMessage.error("请选择课程!")
            return
        }
        //组装数据
        let courseList = selectCourses.list.map((item) => {
            return { courseId: item.courseId }
        })
        let parm = {
            memberId: selectedMember.value.memberId,
            courseList: courseList,
        }
        let res = await courseDownApi(parm)
        if (res && res.code == 200) {
            getList()
            ElMessage.success(res.msg)
            faDrawer.value = false
        } else if (res) {
            //余额不足 / 已报名等业务失败：展示后端返回的错误信息
            ElMessage.error(res.msg)
        }
    }

    onMounted(() => {
        nextTick(() => {
            courseHeight.value = window.innerHeight - 230
            memberHeight.value = window.innerHeight - 230
        })
        getCourseList()
    })

    return {
        faDrawer,
        memberDrawer,
        courseDrawer,
        selectedMember,
        order,
        cancelClick,
        commit,
        memberDrawerShow,
        memberDrawerClose,
        selectMemberBtn,
        courseDrawerShow,
        courseDrawerClose,
        addCourseBtn,
        deleteCourseBtn,
        //会员
        memberParam,
        memberTableList,
        searchMemberBtn,
        resetMemberBtn,
        memberSizeChange,
        memberCurrentChange,
        memberHeight,
        //课程
        courseParam,
        courseTableList,
        searchCourseBtn,
        resetCourseBtn,
        courseSizeChange,
        courseCurrentChange,
        courseHeight,
        selectCourses,
    }
}
