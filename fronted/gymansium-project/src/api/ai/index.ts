import http, { BASE_API, handleAuthExpired } from "@/http";
import { userStore } from "@/store/user";
import type {
    AiPlanParam,
    AiProfileParam,
    AiTrainingLogParam
} from "./AiModel";

// ===== 用户画像 =====
//查询我的画像
export const getProfileApi = () => {
    return http.get("/api/ai/profile")
}
//保存我的画像
export const saveProfileApi = (parm: AiProfileParam) => {
    return http.post("/api/ai/profile", parm)
}
//获取表单选项字典
export const getOptionsApi = () => {
    return http.get("/api/ai/profile/options")
}

// ===== 对话生成计划 =====
//同步生成（非流式）
export const chatApi = (parm: AiPlanParam) => {
    return http.post("/api/ai/chat", parm)
}

// ===== 历史与记录 =====
//我的历史计划
export const pagePlansApi = (parm: { current: number, size: number }) => {
    return http.get("/api/ai/plans", parm)
}
//计划详情
export const getPlanDetailApi = (planId: number) => {
    return http.get(`/api/ai/plans/${planId}`)
}
//会话消息
export const listMessagesApi = (sessionId: string) => {
    return http.get("/api/ai/messages", { sessionId })
}
//训练打卡
export const addTrainingLogApi = (parm: AiTrainingLogParam) => {
    return http.post("/api/ai/training-log", parm)
}
//我的训练记录
export const listTrainingLogApi = (limit = 10) => {
    return http.get("/api/ai/training-log", { limit })
}
//删除训练记录
export const deleteTrainingLogApi = (logId: number) => {
    return http.delete(`/api/ai/training-log/${logId}`)
}

// ===== SSE 流式生成 =====
export type StreamCallbacks = {
    //收到增量文本
    onToken?: (text: string) => void
    //收到完整结构化计划
    onResult?: (payload: any) => void
    //出错
    onError?: (message: string) => void
    //结束
    onDone?: () => void
}

/**
 * SSE 流式生成计划。
 *
 * 为什么不用 axios：axios 基于 XHR，无法逐块读取响应体，
 * 拿不到「打字机」效果所需的增量文本。这里用 fetch + ReadableStream 手动解析 SSE。
 *
 * @returns 一个 abort 函数，调用可中断请求
 */
export function chatStreamApi(
    parm: AiPlanParam,
    callbacks: StreamCallbacks
): () => void {
    const controller = new AbortController()
    const token = userStore().getToken
    // baseURL 与 axios 实例共用同一个常量，避免两处各写一份导致生产环境打错地址
    const baseURL = BASE_API

    // 是否收到后端明确的收尾信号（result 或 done）。
    // 只有 reader 自然读完是不够的：网络中途断了也是「读完」，
    // 那种情况用户会看到半截内容却以为成功了。
    let serverFinished = false

    // 手动解析 SSE 帧：以 \n\n 分隔事件块，逐个读取 event: / data:
    const parseChunk = (buffer: string): { rest: string } => {
        let sepIndex: number
        // 兼容 \r\n\r\n
        while ((sepIndex = buffer.search(/\r?\n\r?\n/)) >= 0) {
            const rawEvent = buffer.slice(0, sepIndex)
            buffer = buffer.slice(sepIndex).replace(/^\r?\n\r?\n/, '')

            let eventName = 'message'
            const dataLines: string[] = []
            for (const line of rawEvent.split(/\r?\n/)) {
                if (line.startsWith('event:')) {
                    eventName = line.slice(6).trim()
                } else if (line.startsWith('data:')) {
                    dataLines.push(line.slice(5).trim())
                }
            }
            if (dataLines.length === 0) {
                continue
            }
            const dataStr = dataLines.join('\n')
            try {
                const payload = JSON.parse(dataStr)
                if (eventName === 'token') {
                    callbacks.onToken?.(payload.text ?? '')
                } else if (eventName === 'result') {
                    // 收到结构化结果即视为后端已正常产出
                    serverFinished = true
                    callbacks.onResult?.(payload)
                } else if (eventName === 'error') {
                    callbacks.onError?.(payload.message ?? 'AI 生成失败')
                } else if (eventName === 'done') {
                    // 后端明确收尾
                    serverFinished = true
                }
                // eventName === 'ping' 是心跳保活，忽略即可
            } catch (e) {
                // 单帧解析失败不中断整个流
                console.warn('SSE 帧解析失败', dataStr)
            }
        }
        return { rest: buffer }
    }

    ;(async () => {
        let buffer = ''
        try {
            const response = await fetch(`${baseURL}/api/ai/chat/stream`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'token': token || ''
                },
                body: JSON.stringify(parm),
                signal: controller.signal
            })

            if (!response.ok) {
                // 401 才是「登录失效」，需要清 token 跳登录。
                // 403 是「已登录但没权限」（例如新会员还没绑定 AI 角色），
                // 把它也当登录失效会把正常登录的用户踢下线，必须分开处理。
                if (response.status === 401) {
                    handleAuthExpired('登录已失效，请重新登录')
                    return
                }
                if (response.status === 403) {
                    callbacks.onError?.('当前账号没有 AI 教练权限，请联系管理员开通')
                    return
                }
                callbacks.onError?.(`请求失败：HTTP ${response.status}`)
                return
            }
            if (!response.body) {
                callbacks.onError?.('当前浏览器不支持流式响应')
                return
            }

            // 后端权限拦截（@PreAuthorize）走的是项目的 CustomerAccessDeniedHandler，
            // 返回的是 HTTP 200 + JSON{code:700}，而不是 HTTP 403。
            // fetch 不经过 axios 拦截器，必须在这里自己识别，否则会把这包 JSON
            // 当成 SSE 帧解析失败，最后报「连接中断」，用户完全看不懂。
            const contentType = response.headers.get('content-type') || ''
            if (!contentType.includes('text/event-stream')) {
                const text = await response.text()
                try {
                    const j = JSON.parse(text)
                    if (j && j.code === 700) {
                        callbacks.onError?.(j.msg || '当前账号没有 AI 教练权限，请联系管理员开通')
                        return
                    }
                    callbacks.onError?.(j?.msg || 'AI 服务返回异常')
                } catch {
                    callbacks.onError?.(`AI 服务返回异常（HTTP ${response.status}）`)
                }
                return
            }

            const reader = response.body.getReader()
            const decoder = new TextDecoder('utf-8')

            while (true) {
                const { done, value } = await reader.read()
                if (done) {
                    break
                }
                buffer += decoder.decode(value, { stream: true })
                const { rest } = parseChunk(buffer)
                buffer = rest
            }
            // 处理最后残留的一帧
            if (buffer.trim()) {
                parseChunk(buffer + '\n\n')
            }

            if (!serverFinished) {
                // 连接断了但后端没发 result/done：说明是异常中断，不能当成功
                callbacks.onError?.('连接中断，内容可能不完整，请重试')
                return
            }
            callbacks.onDone?.()
        } catch (e: any) {
            if (e?.name === 'AbortError') {
                return
            }
            callbacks.onError?.(e?.message || 'AI 服务连接失败')
        }
    })()

    return () => controller.abort()
}
