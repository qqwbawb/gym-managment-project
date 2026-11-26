<template>
  <el-main>
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">我的健身画像</span>
          <span class="tip">画像由你填写，AI 只读取、不会修改，保证训练计划基于真实情况</span>
        </div>
      </template>

      <el-alert
        v-if="!configured && !loading"
        title="还没有填写健身画像"
        type="warning"
        :closable="false"
        show-icon
        description="填写画像后，AI 才能根据你的目标、器械条件和伤病情况生成训练计划。"
        style="margin-bottom: 18px"
      />

      <el-form
        v-loading="loading"
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="120px"
        style="max-width: 720px"
      >
        <el-form-item label="训练目标" prop="goal">
          <el-select v-model="form.goal" placeholder="请选择训练目标" style="width: 100%">
            <el-option
              v-for="opt in options.goals"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="训练水平" prop="level">
          <el-radio-group v-model="form.level">
            <el-radio-button
              v-for="opt in options.levels"
              :key="opt.value"
              :label="opt.value"
            >{{ opt.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="每周训练" prop="daysPerWeek">
          <el-input-number v-model="form.daysPerWeek" :min="1" :max="7" />
          <span class="unit">次 / 周</span>
        </el-form-item>

        <el-form-item label="单次时长" prop="sessionMinutes">
          <el-input-number v-model="form.sessionMinutes" :min="10" :max="240" :step="5" />
          <span class="unit">分钟</span>
        </el-form-item>

        <el-form-item label="可用器械" prop="equipment">
          <el-checkbox-group v-model="form.equipment">
            <el-checkbox
              v-for="opt in options.equipments"
              :key="opt.value"
              :label="opt.value"
            >{{ opt.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="伤病 / 禁忌">
          <el-checkbox-group v-model="form.injuries">
            <el-checkbox
              v-for="opt in options.injuries"
              :key="opt.value"
              :label="opt.value"
            >{{ opt.label }}</el-checkbox>
          </el-checkbox-group>
          <div class="hint">没有伤病可以不选。AI 会自动避开该部位的高风险动作。</div>
        </el-form-item>

        <el-form-item label="伤病说明">
          <el-input
            v-model="form.injuryNote"
            type="textarea"
            :rows="2"
            maxlength="255"
            show-word-limit
            placeholder="例如：右膝半月板旧伤，深蹲和跑跳时会疼（选填，补充越具体，计划越贴合）"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveBtn">保存画像</el-button>
          <el-button @click="loadProfile">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 训练记录：AI 记忆的来源 -->
    <el-card shadow="never" style="margin-top: 18px">
      <template #header>
        <div class="card-header">
          <span class="title">我的训练记录</span>
          <span class="tip">AI 会参考最近 7 天的记录，避免同一部位连续训练</span>
        </div>
      </template>

      <el-table :data="trainingLogs" border stripe size="small" empty-text="还没有训练记录，去「AI 教练」页面完成一次训练后打卡吧">
        <el-table-column prop="trainDate" label="日期" width="120">
          <template #default="{ row }">{{ formatDate(row.trainDate) }}</template>
        </el-table-column>
        <el-table-column label="部位" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ bodyPartLabel(row.bodyPart) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exerciseSummary" label="动作" show-overflow-tooltip />
        <el-table-column prop="durationMin" label="时长(分)" width="100" />
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click="removeLog(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </el-main>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getProfileApi,
  saveProfileApi,
  getOptionsApi,
  listTrainingLogApi,
  deleteTrainingLogApi
} from '@/api/ai'
import { BODY_PART_LABELS } from '@/api/ai/AiModel'
import type { AiOptions, AiProfileParam, AiTrainingLog } from '@/api/ai/AiModel'

const loading = ref(false)
const saving = ref(false)
const configured = ref(false)
const formRef = ref<FormInstance>()
const trainingLogs = ref<AiTrainingLog[]>([])

// 默认空表单，避免未加载时访问 undefined 字段
const form = reactive<AiProfileParam>({
  goal: '',
  level: 'BEGINNER',
  daysPerWeek: 3,
  sessionMinutes: 60,
  equipment: [],
  injuries: [],
  injuryNote: ''
})

const options = reactive<AiOptions>({
  goals: [],
  levels: [],
  equipments: [],
  injuries: []
})

const rules: FormRules = {
  goal: [{ required: true, message: '请选择训练目标', trigger: 'change' }],
  level: [{ required: true, message: '请选择训练水平', trigger: 'change' }],
  daysPerWeek: [{ required: true, message: '请填写每周训练次数', trigger: 'blur' }],
  sessionMinutes: [{ required: true, message: '请填写单次训练时长', trigger: 'blur' }],
  equipment: [
    {
      validator: (_rule, value: string[], callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少选择一种可用器械（自重也算）'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 加载表单选项
const loadOptions = async () => {
  try {
    const res: any = await getOptionsApi()
    if (res && res.code === 200) {
      Object.assign(options, res.data)
    }
  } catch (e) {
    // http 拦截器已提示
  }
}

// 加载我的画像
const loadProfile = async () => {
  loading.value = true
  try {
    const res: any = await getProfileApi()
    if (res && res.code === 200) {
      const data = res.data || {}
      configured.value = !!data.configured
      const p = data.profile
      if (p) {
        form.goal = p.goal || ''
        form.level = p.level || 'BEGINNER'
        form.daysPerWeek = p.daysPerWeek ?? 3
        form.sessionMinutes = p.sessionMinutes ?? 60
        // 后端以逗号分隔存储，前端转成数组
        form.equipment = splitCsv(p.equipment)
        form.injuries = splitCsv(p.injuries)
        form.injuryNote = p.injuryNote || ''
      } else {
        resetForm()
      }
    }
  } catch (e) {
    // http 拦截器已提示
  } finally {
    loading.value = false
  }
}

const splitCsv = (s?: string): string[] => {
  if (!s) return []
  return s.split(',').map((x) => x.trim()).filter((x) => x.length > 0)
}

const resetForm = () => {
  form.goal = ''
  form.level = 'BEGINNER'
  form.daysPerWeek = 3
  form.sessionMinutes = 60
  form.equipment = []
  form.injuries = []
  form.injuryNote = ''
}

// 保存
const saveBtn = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const res: any = await saveProfileApi({ ...form })
      if (res && res.code === 200) {
        ElMessage.success('保存成功')
        configured.value = true
        await loadProfile()
      }
    } catch (e) {
      // http 拦截器已提示
    } finally {
      saving.value = false
    }
  })
}

// 加载训练记录
const loadTrainingLogs = async () => {
  try {
    const res: any = await listTrainingLogApi(20)
    if (res && res.code === 200) {
      trainingLogs.value = res.data || []
    }
  } catch (e) {
    // http 拦截器已提示
  }
}

const removeLog = (row: AiTrainingLog) => {
  ElMessageBox.confirm('确定删除这条训练记录吗？删除后 AI 将不再参考它。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const res: any = await deleteTrainingLogApi(row.logId)
      if (res && res.code === 200) {
        ElMessage.success('删除成功')
        await loadTrainingLogs()
      }
    })
    .catch(() => {})
}

const bodyPartLabel = (bp?: string) => {
  if (!bp) return '-'
  return BODY_PART_LABELS[bp] || bp
}

// 后端返回的 trainDate 可能是时间戳或 ISO 字符串，统一格式化
const formatDate = (v: any): string => {
  if (!v) return '-'
  const d = new Date(v)
  if (isNaN(d.getTime())) return String(v)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

onMounted(async () => {
  await loadOptions()
  await loadProfile()
  await loadTrainingLogs()
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.card-header .title {
  font-size: 16px;
  font-weight: 600;
}
.card-header .tip {
  font-size: 12px;
  color: #909399;
}
.unit {
  margin-left: 8px;
  color: #606266;
}
.hint {
  width: 100%;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
</style>
