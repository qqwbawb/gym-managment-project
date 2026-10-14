//课程订单列表查询参数（模仿商品订单 OrderListParam）
export type CourseOrderListParam = {
    courseName: string,
    currentPage: number,
    pageSize: number,
    total: number
}

//课程订单列表数据（member_course 关联 member 带出报名会员）
export type CourseOrderType = {
    memberCourseId: string,
    courseId: string,
    memberId: string,
    teacherId: string,
    courseName: string,
    coursePrice: number,
    courseDetails: string,
    image: string,
    courseHour: number,
    teacherName: string,
    memberName: string,
    memberUsername: string
}

//课程下单参数（模仿商品订单：memberId + courseList）
export type CourseDownParam = {
    memberId: string,
    courseList: Array<{
        courseId: string
    }>
}
