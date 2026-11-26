<template>
  <el-main>
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">我的历史训练计划</span>
          <el-button :icon="Refresh" size="small" @click="loadList">刷新</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        empty-text="还没有生成过训练计划，去「AI 教练」试试吧"
      >
        <el-table-column prop="title" label="计划标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="summary" label="摘要" min-width="240" show-overflow-tooltip />
        <el-table-column label="模型" width="140">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.model || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="90" align="center">
          <template #default="{ row }">
            {{ row.costMs ? (row.costMs / 1000).toFixed(1) + 's' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="生成时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 14px; justify-content: flex-end"
        @size-change="onSizeChange"
        @current-change="onCurrentChange"
        :current-page="current"
        :page-sizes="[10, 20, 40]"
        :page-size="size"
        layout="total, sizes, prev, pager, next"
        :total="total"
        background
      />
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" :title="detail?.title || '计划详情'" size="45%">
      <div v-if="detail" class="detail-body">
        <el-descriptions :column="2" size="small" border style="margin-bottom: 14px">
          <el-descriptions-item label="模型">{{ detail.model || '-' }}</el-descriptions-item>
          <el-descriptions-item label="耗时">
            {{ detail.costMs ? (detail.costMs / 1000).toFixed(1) + 's' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="Token 估算">{{ detail.tokenUsage ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="生成时间">{{ formatDateTime(detail.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="detailWarnings.length > 0"
          type="warning"
          :closable="false"
          show-icon
          title="生成时的调整提示"
          style="margin-bottom: 12px"
        >
          <ul class="warning-list">
            <li v-for="(w, i) in detailWarnings" :key="i">{{ w }}</li>
          </ul>
        </el-alert>

        <template v-if="detail.plan">
          <el-collapse v-model="openedDays">
            <el-collapse-item
              v-for="day in detail.plan.days"
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
            </el-collapse-item>
          </el-collapse>

          <p v-if="detail.plan.notes" class="notes">
            <strong>注意事项：</strong>{{ detail.plan.notes }}
          </p>
        </template>
        <el-empty v-else description="计划内容为空" :image-size="60" />
      </div>
    </el-drawer>
  </el-main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { pagePlansApi, getPlanDetailApi } from '@/api/ai'
import { BODY_PART_LABELS } from '@/api/ai/AiModel'
import type { AiPlanListItem } from '@/api/ai/AiModel'

const loading = ref(false)
const tableData = ref<AiPlanListItem[]>([])
const current = ref(1)
const size = ref(10)
const total = ref(0)

const drawerVisible = ref(false)
const detail = ref<any>(null)
const openedDays = ref<number[]>([])

const detailWarnings = computed<string[]>(() => {
  const w = detail.value?.warnings
  return Array.isArray(w) ? w : []
})

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await pagePlansApi({ current: current.value, size: size.value })
    if (res && res.code === 200) {
      const page = res.data || {}
      tableData.value = page.records || []
      total.value = page.total || 0
    }
  } catch (e) {
    // http 拦截器已提示
  } finally {
    loading.value = false
  }
}

const onSizeChange = (val: number) => {
  size.value = val
  current.value = 1
  loadList()
}

const onCurrentChange = (val: number) => {
  current.value = val
  loadList()
}

const viewDetail = async (row: AiPlanListItem) => {
  try {
    const res: any = await getPlanDetailApi(row.planId)
    if (res && res.code === 200) {
      detail.value = res.data
      const days = res.data?.plan?.days || []
      openedDays.value = days.length > 0 ? [days[0].day] : []
      drawerVisible.value = true
    }
  } catch (e) {
    // http 拦截器已提示
  }
}

const bodyPartLabel = (bp?: string) => {
  if (!bp) return '未指定'
  return BODY_PART_LABELS[bp] || bp
}

const formatDateTime = (v: any): string => {
  if (!v) return '-'
  const d = new Date(v)
  if (isNaN(d.getTime())) return String(v)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-header .title {
  font-size: 16px;
  font-weight: 600;
}
.day-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}
.warning-list {
  margin: 0;
  padding-left: 18px;
  font-size: 12px;
  line-height: 1.7;
}
.notes {
  font-size: 12px;
  color: #606266;
  line-height: 1.7;
  margin-top: 12px;
}
</style>
