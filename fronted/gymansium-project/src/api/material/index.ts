import http from "@/http";
import type { ListParam, MaterialType } from "./MaterialModel";

export const addApi = (param: MaterialType) => {
    return http.post("/api/material", param)
}

export const editApi = (param: MaterialType) => {
    return http.put("/api/material", param)
}

export const deleteApi = (id: string) => {
    return http.delete(`/api/material/${id}`)
}

export const getListApi = (param: ListParam) => {
    return http.get("/api/material/list", param)
}