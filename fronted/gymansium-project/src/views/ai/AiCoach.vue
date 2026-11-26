<template>
  <el-main>
    <el-row :gutter="16">
      <!-- 左：对话区 -->
      <el-col :span="15">
        <el-card shadow="never" class="chat-card">
          <template #header>
            <div class="card-header">
              <span class="title">AI 教练</span>
              <span class="header-right">
                <el-tag size="small" :type="isStreaming ? 'warning' : 'success'">
                  {{ isStreaming ? '生成中…' : '就绪' }}
                </el-tag>
                <!-- 新建对话：换话题时清空上下文，否则 sessionId 永久复用、
                     历史对话无限累积，模型会被无关的旧内容干扰 -->
                <el-button
                  size="small"
                  :icon="Plus"
                  :disabled="isStreaming"
                  @click="newSession"
                >新建对话</el-button>
              </span>
            </div>
          </template>

          <el-alert
            v-if="!profileReady"
            type="warning"
            :closable="false"
            show-icon
            title="请先完善健身画像"
            style="margin-bottom: 12px"
          >
            <template #default>
              AI 需要先了解你的目标、器械条件和伤病情况。
              <el-button type="primary" link @click="goProfile">去填写画像</el-button>
            </template>
          </el-alert>

          <!-- 消息列表 -->
          <div class="msg-list" ref="msgListRef">
            <div v-if="messages.length === 0" class="empty-tip">
              <p>告诉我你的训练需求，例如：</p>
              <el-tag
                v-for="(sample, i) in samples"
                :key="i"
                class="sample"
                @click="useSample(sample)"
              >{{ sample }}</el-tag>
            </div>

            <div
              v-for="(msg, idx) in messages"
              :key="idx"
              :class="['msg-item', msg.role === 'user' ? 'is-user' : 'is-ai']"
            >
              <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
              <div class="bubble">
                <!-- 流式过程中的原始文本 -->
                <template v-if="msg.streaming">
                  <pre class="streaming-text">{{ msg.content }}<span class="caret">|</span></pre>
                </template>
                <template v-else>{{ msg.content }}</template>
              </div>
            </div>
          </div>

          <!-- 输入区 -->
          <div class="input-area">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="2"
              resize="none"
              :disabled="isStreaming || !profileReady"
              placeholder="描述你的训练需求，Enter 发送 / Shift+Enter 换行"
              @keydown.enter.exact.prevent="sendBtn"
            />
            <div class="input-actions">
              <el-checkbox v-model="useStream" :disabled="isStreaming">流式输出</el-checkbox>
              <el-button
                v-if="isStreaming"
                type="danger"
                plain
                @click="stopBtn"
              >停止</el-button>
              <el-button
                type="primary"
                :loading="isStreaming && !useStream"
                :disabled="!profileReady || !inputText.trim()"
                @click="sendBtn"
              >发送</el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右：计划结果 -->
      <el-col :span="9">
        <el-card shadow="never" class="plan-card">
          <template #header>
            <div class="card-header">
              <span class="title">训练计划</span>
              <el-tag v-if="costMs > 0" size="small" type="info">耗时 {{ (costMs / 1000).toFixed(1) }}s</el-tag>
            </div>
          </template>

          <el-empty v-if="!currentPlan" description="生成好的计划会显示在这里" :image-size="80" />

          <div v-else class="plan-body">
            <h3 class="plan-title">{{ currentPlan.title }}</h3>

            <!-- 规则引擎告警：被 AI 或规则引擎调整过的地方，必须让用户看得见 -->
            <el-alert
              v-if="warnings.length > 0"
              type="warning"
              :closable="false"
              show-icon
              title="已按你的伤病情况调整"
              style="margin-bottom: 12px"
            >
              <ul class="warning-list">
                <li v-for="(w, i) in warnings" :key="i">{{ w }}</li>
              </ul>
            </el-alert>

            <el-collapse v-model="openedDays">
              <el-collapse-item
                v-for="day in currentPlan.days"
                :key="day.day"
                :name="day.day"
              >
                <template #title>
                  <span class="day-title">
                    第 {{ day.day }} 天
                    <el-tag size="small" effect="plain">{{ bodyPartLabel(day.bodyPart) }}</el-tag>
                  </span>
                </template>

                <el-table :data="day.exercises" size="small" border>
                  <el-table-column prop="name" label="动作" show-overflow-tooltip />
                  <el-table-column prop="sets" label="组数" width="60" align="center" />
                  <el-table-column prop="reps" label="次数" width="90" align="center" />
                  <el-table-column label="休息" width="70" align="center">
                    <template #default="{ row }">
                      {{ row.restSec ? row.restSec + 's' : '-' }}
                    </template>
                  </el-table-column>
                </el-table>

                <!-- 打卡：写一条训练记录，成为 AI 的记忆 -->
                <el-button
                  class="checkin-btn"
                  size="small"
                  type="success"
                  plain
                  :loading="checkingIn === day.day"
                  @click="checkIn(day)"
                >完成今天训练（打卡）</el-button>
              </el-collapse-item>
            </el-collapse>

            <p v-if="currentPlan.notes" class="plan-notes">
              <strong>注意事项：</strong>{{ currentPlan.notes }}
            </p>

            <!-- 课程推荐：只推荐，报名需本人操作 -->
            <div v-if="recommendCourses.length > 0" class="recommend">
              <div class="recommend-title">
                相关课程推荐
                <span class="recommend-tip">仅作推荐，报名请到「我的课程」自行操作</span>
              </div>
              <div
                v-for="c in recommendCourses"
                :key="c.courseId"
                class="course-item"
              >
                <span class="course-name">{{ c.courseName }}</span>
                <span class="course-meta">{{ c.teacherName }} · {{ c.courseHour }}课时 · ¥{{ c.coursePrice }}</span>
                <el-button type="primary" link @click="goCourse">去看看</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </el-main>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getProfileApi,
  chatApi,
  chatStreamApi,
  addTrainingLogApi
} from '@/api/ai'
import { BODY_PART_LABELS } from '@/api/ai/AiModel'
import type {
  AiPlanResult,
  AiRecommendCourse,
  AiDayPlan
} from '@/api/ai/AiModel'

const router = useRouter()

// 页面状态
const profileReady = ref(false)
const inputText = ref('')
const isStreaming = ref(false)
const useStream = ref(true)
const costMs = ref(0)
const warnings = ref<string[]>([])
const currentPlan = ref<AiPlanResult | null>(null)
const recommendCourses = ref<AiRecommendCourse[]>([])
const openedDays = ref<number[]>([])
const checkingIn = ref<number | null>(null)
const sessionId = ref<string>('')
// 当前这份计划的主键，打卡时作为来源带上
const currentPlanId = ref<number>(0)
const msgListRef = ref<HTMLElement>()

// 当前正在流式输出的消息下标
const streamingIndex = ref(-1)
// 中断函数
let abortStream: (() => void) | null = null

type ChatItem = {
  role: 'user' | 'assistant'
  content: string
  streaming?: boolean
}

const messages = reactive<ChatItem[]>([])

const samples = [
  '帮我排一个下周的减脂训练计划',
  '我只有哑铃，能在家练的动作多排一些',
  '把第 2 天换成练腿',
  '我膝盖有点疼，计划里能避开深蹲吗'
]

const bodyPartLabel = (bp?: string) => {
  if (!bp) return '未指定'
  return BODY_PART_LABELS[bp] || bp
}

const goProfile = () => router.push('/aiProfile')
const goCourse = () => router.push('/mycourse')

const useSample = (s: string) => {
  inputText.value = s
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  const el = msgListRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

// 加载画像状态：没填画像不让对话，避免模型输出无依据
const loadProfileState = async () => {
  try {
    const res: any = await getProfileApi()
    if (res && res.code === 200) {
      profileReady.value = !!res.data?.configured
    }
  } catch (e) {
    // http 拦截器已提示
  }
}

// 应用生成结果
const applyPlan = (payload: any) => {
  if (!payload) return
  currentPlan.value = payload.plan || null
  warnings.value = payload.warnings || []
  costMs.value = payload.costMs || 0
  recommendCourses.value = payload.recommendCourses || []
  sessionId.value = payload.sessionId || sessionId.value
  currentPlanId.value = payload.planId || 0

  // 默认展开第一天，避免用户看到全收起的面板
  const days = currentPlan.value?.days || []
  openedDays.value = days.length > 0 ? [days[0].day] : []

  // 用摘要替换掉流式原始文本（原始是 JSON，不该展示给用户）
  const idx = streamingIndex.value
  if (idx >= 0 && messages[idx]) {
    messages[idx].content = buildSummary(currentPlan.value, warnings.value)
    messages[idx].streaming = false
  }
  scrollToBottom()
}

const buildSummary = (plan: AiPlanResult | null, warns: string[]) => {
  if (!plan) return '已生成计划'
  const totalExercises = plan.days.reduce((n, d) => n + (d.exercises?.length || 0), 0)
  let s = `已生成《${plan.title || '训练计划'}》：每周 ${plan.days.length} 练，共 ${totalExercises} 个动作。`
  if (warns.length > 0) {
    s += `（已按你的伤病情况调整 ${warns.length} 处）`
  }
  return s
}

// 发送
const sendBtn = async () => {
  const text = inputText.value.trim()
  if (!text || isStreaming.value) return
  if (!profileReady.value) {
    ElMessage.warning('请先填写健身画像')
    return
  }

  messages.push({ role: 'user', content: text })
  inputText.value = ''
  isStreaming.value = true

  // 占位 AI 消息
  messages.push({ role: 'assistant', content: '', streaming: true })
  streamingIndex.value = messages.length - 1
  await scrollToBottom()

  if (useStream.value) {
    sendStream(text)
  } else {
    await sendSync(text)
  }
}

// 流式
const sendStream = (text: string) => {
  abortStream = chatStreamApi(
    { sessionId: sessionId.value || undefined, message: text },
    {
      onToken: (chunk) => {
        const idx = streamingIndex.value
        if (idx >= 0 && messages[idx]) {
          messages[idx].content += chunk
          scrollToBottom()
        }
      },
      onResult: (payload) => {
        applyPlan(payload)
        isStreaming.value = false
        abortStream = null
      },
      onError: (msg) => {
        finishWithError(msg)
      },
      onDone: () => {
        // 若没有收到 result（例如出错），收尾
        if (isStreaming.value) {
          isStreaming.value = false
        }
        abortStream = null
      }
    }
  )
}

// 同步
const sendSync = async (text: string) => {
  try {
    const res: any = await chatApi({ sessionId: sessionId.value || undefined, message: text })
    if (res && res.code === 200) {
      const idx = streamingIndex.value
      if (idx >= 0) {
        messages[idx].streaming = false
      }
      applyPlan(res.data)
    } else {
      finishWithError(res?.msg || '生成失败')
    }
  } catch (e: any) {
    finishWithError(typeof e === 'string' ? e : '生成失败')
  } finally {
    isStreaming.value = false
  }
}

const finishWithError = (msg: string) => {
  const idx = streamingIndex.value
  if (idx >= 0 && messages[idx]) {
    messages[idx].content = msg
    messages[idx].streaming = false
  }
  isStreaming.value = false
  abortStream = null
  ElMessage.error(msg)
}

const stopBtn = () => {
  if (abortStream) {
    abortStream()
    abortStream = null
  }
  isStreaming.value = false
  const idx = streamingIndex.value
  if (idx >= 0 && messages[idx]) {
    messages[idx].content = '已停止生成'
    messages[idx].streaming = false
  }
}

// 打卡：写一条训练记录
const checkIn = async (day: AiDayPlan) => {
  checkingIn.value = day.day
  try {
    const summary = (day.exercises || []).map((e) => e.name).join('/')
    const res: any = await addTrainingLogApi({
      // 带上来源计划 id：训练记录才能反查到是哪份计划练的，
      // 后端也据此做「同一计划同一天只打卡一次」的去重
      planId: currentPlanId.value || undefined,
      bodyPart: day.bodyPart || '',
      exerciseSummary: summary
    })
    if (res && res.code === 200) {
      ElMessage.success('打卡成功，AI 下次会参考这次训练')
    }
  } catch (e) {
    // http 拦截器已提示
  } finally {
    checkingIn.value = null
  }
}

// 新建对话：清空会话，让下次提问重新建会话
const newSession = () => {
  if (isStreaming.value) {
    stopBtn()
  }
  sessionId.value = ''
  currentPlanId.value = 0
  messages.splice(0, messages.length)
  currentPlan.value = null
  warnings.value = []
  recommendCourses.value = []
  costMs.value = 0
  openedDays.value = []
  streamingIndex.value = -1
  ElMessage.success('已开启新对话')
}

onMounted(async () => {
  await loadProfileState()
})

onBeforeUnmount(() => {
  if (abortStream) {
    abortStream()
  }
})
</script>

<style scoped>
.chat-card,
.plan-card {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-header .title {
  font-size: 16px;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 4px;
  min-height: 200px;
}
.empty-tip {
  color: #909399;
  font-size: 13px;
}
.empty-tip .sample {
  margin: 4px 8px 4px 0;
  cursor: pointer;
}
.msg-item {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.msg-item.is-user {
  flex-direction: row-reverse;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.msg-item.is-user .avatar {
  background: #67c23a;
}
.bubble {
  max-width: 78%;
  padding: 8px 12px;
  border-radius: 8px;
  background: #f4f4f5;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg-item.is-user .bubble {
  background: #ecf5ff;
}
.streaming-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 12px;
  color: #909399;
  max-height: 220px;
  overflow-y: auto;
}
.caret {
  animation: blink 1s step-end infinite;
}
@keyframes blink {
  50% { opacity: 0; }
}
.input-area {
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
}
.input-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}
.input-actions .el-checkbox {
  margin-right: auto;
}
.plan-body {
  overflow-y: auto;
  flex: 1;
}
.plan-title {
  margin: 0 0 12px;
  font-size: 15px;
}
.warning-list {
  margin: 0;
  padding-left: 18px;
  font-size: 12px;
  line-height: 1.7;
}
.day-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}
.checkin-btn {
  margin-top: 8px;
}
.plan-notes {
  font-size: 12px;
  color: #606266;
  line-height: 1.7;
  margin-top: 12px;
}
.recommend {
  margin-top: 16px;
  border-top: 1px dashed #ebeef5;
  padding-top: 12px;
}
.recommend-title {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
}
.recommend-tip {
  font-weight: 400;
  font-size: 11px;
  color: #909399;
  margin-left: 6px;
}
.course-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 12px;
  border-bottom: 1px dashed #f2f6fc;
}
.course-name {
  flex: 1;
  font-weight: 500;
}
.course-meta {
  color: #909399;
}
</style>
