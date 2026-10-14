import http from "@/http"
import type { OrderListParam } from "./OrderModel"
import type { CourseOrderListParam } from "./CourseOrderModel"

//下单
export const dowmApi = (param: any) => {
    return http.post("/api/order/down", param)
}

//查询订单列表
export const listApi = (param: OrderListParam) => {
    return http.get("/api/order/list", param)
}

//课程订单：批量选课下单（模仿 dowmApi）
export const courseDownApi = (param: any) => {
    return http.post("/api/courseOrder/down", param)
}

//课程订单：查询课程订单列表
export const courseOrderListApi = (param: CourseOrderListParam) => {
    return http.get("/api/courseOrder/list", param)
}