// ===== AI 健身教练模块类型定义 =====

// 表单选项
export type AiOption = {
    value: string
    label: string
}

export type AiOptions = {
    goals: AiOption[]
    levels: AiOption[]
    equipments: AiOption[]
    injuries: AiOption[]
}

// 用户画像（后端返回的原始结构，数组字段以逗号分隔存储）
export type AiProfileRaw = {
    profileId?: number
    memberId?: number
    goal?: string
    level?: string
    daysPerWeek?: number
    sessionMinutes?: number
    equipment?: string
    injuries?: string
    injuryNote?: string
}

// 保存画像的入参（数组形式）
export type AiProfileParam = {
    goal: string
    level: string
    daysPerWeek: number
    sessionMinutes: number
    equipment: string[]
    injuries: string[]
    injuryNote?: string
}

// 计划里的单个动作
export type AiExercise = {
    name: string
    sets?: number
    reps?: string
    restSec?: number
}

// 计划里的单个训练日
export type AiDayPlan = {
    day: number
    bodyPart?: string
    exercises: AiExercise[]
}

// 结构化训练计划
export type AiPlanResult = {
    title?: string
    days: AiDayPlan[]
    notes?: string
}

// 推荐课程（只展示，报名需用户自己去课程页）
export type AiRecommendCourse = {
    courseId: number
    courseName: string
    teacherName?: string
    image?: string
    courseHour?: number
    coursePrice?: number
}

// 生成结果
export type AiPlanVO = {
    sessionId: string
    planId: number
    plan: AiPlanResult
    warnings: string[]
    costMs: number
    recommendCourses: AiRecommendCourse[]
}

// 对话入参
export type AiPlanParam = {
    sessionId?: string
    message: string
}

// 历史计划列表项
export type AiPlanListItem = {
    planId: number
    sessionId?: string
    title?: string
    summary?: string
    model?: string
    tokenUsage?: number
    costMs?: number
    warnings?: string
    createTime?: string
}

// 训练记录
export type AiTrainingLog = {
    logId: number
    memberId: number
    planId?: number
    trainDate: string
    bodyPart?: string
    exerciseSummary?: string
    durationMin?: number
    createTime?: string
}

export type AiTrainingLogParam = {
    planId?: number
    trainDate?: string
    bodyPart: string
    exerciseSummary?: string
    durationMin?: number
}

// 对话记录
export type AiChatMessage = {
    messageId?: number
    sessionId: string
    role: string
    content: string
    createTime?: string
}

// 训练部位中文名
export const BODY_PART_LABELS: Record<string, string> = {
    CHEST: '胸',
    BACK: '背',
    LEG: '腿',
    SHOULDER: '肩',
    ARM: '手臂',
    CORE: '核心',
    CARDIO: '有氧'
}
