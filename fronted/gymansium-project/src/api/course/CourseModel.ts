//课程类型
export type CourseType = {
    type: string,
    courseId: string,
    courseName: string,
    image: string,
    teacherName: string,
    courseHour: number,
    courseDetails: string,
    coursePrice: number
}
//分页查询类型
export type CourseListParam = {
    courseName: string,
    currentPage: number,
    pageSize: number,
    total: number,
    teacherName: string
}