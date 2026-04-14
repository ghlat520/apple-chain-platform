<template>
  <div class="page-container">
    <div class="page-header">
      <h2>撮合管理</h2>
    </div>

    <!-- Compute Match Section -->
    <el-card style="margin-bottom:20px">
      <template #header>
        <span>计算撮合</span>
      </template>
      <el-form :model="computeForm" inline label-width="80px">
        <el-form-item label="供货ID">
          <el-input v-model="computeForm.supplyId" placeholder="请输入供货ID" style="width:200px" />
        </el-form-item>
        <el-form-item label="TOP K">
          <el-input-number v-model="computeForm.topK" :min="1" :max="50" style="width:120px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCompute" :loading="computing" icon="Connection">
            计算撮合
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="computeResults.length > 0" style="margin-top:16px">
        <div style="margin-bottom:12px;font-weight:600;color:var(--el-text-color-primary)">
          撮合结果（共 {{ computeResults.length }} 条）
        </div>
        <el-row :gutter="16">
          <el-col
            v-for="(item, idx) in computeResults"
            :key="idx"
            :span="8"
            style="margin-bottom:12px"
          >
            <el-card shadow="hover" class="match-card">
              <div class="match-card__score">
                <span class="nums-tabular">得分：{{ item.matchScore ?? '-' }}</span>
              </div>
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="需求ID">{{ item.demandId || '-' }}</el-descriptions-item>
                <el-descriptions-item label="供货ID">{{ item.supplyId || '-' }}</el-descriptions-item>
                <el-descriptions-item label="撮合时间">{{ item.matchTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="matchStatusType(item.status)" size="small">{{ matchStatusLabel(item.status) }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>
      </div>
      <el-empty v-else-if="computeSearched" description="暂无撮合结果" />
    </el-card>

    <!-- Top Match Section -->
    <el-card>
      <template #header>
        <span>TOP 匹配查询</span>
      </template>
      <el-form inline label-width="80px">
        <el-form-item label="供货ID">
          <el-input v-model="topSupplyId" placeholder="请输入供货ID" style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleTop" :loading="topLoading" icon="Rank">
            查看TOP匹配
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="topResults.length > 0" style="margin-top:16px">
        <div style="margin-bottom:12px;font-weight:600;color:var(--el-text-color-primary)">
          TOP匹配结果（共 {{ topResults.length }} 条）
        </div>
        <el-row :gutter="16">
          <el-col
            v-for="(item, idx) in topResults"
            :key="idx"
            :span="8"
            style="margin-bottom:12px"
          >
            <el-card shadow="hover" class="match-card">
              <div class="match-card__score">
                <el-tag type="danger" size="small" style="margin-right:8px">TOP {{ idx + 1 }}</el-tag>
                <span class="nums-tabular">得分：{{ item.matchScore ?? '-' }}</span>
              </div>
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="需求ID">{{ item.demandId || '-' }}</el-descriptions-item>
                <el-descriptions-item label="供货ID">{{ item.supplyId || '-' }}</el-descriptions-item>
                <el-descriptions-item label="撮合时间">{{ item.matchTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="matchStatusType(item.status)" size="small">{{ matchStatusLabel(item.status) }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>
      </div>
      <el-empty v-else-if="topSearched" description="暂无TOP匹配结果" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { matchApi } from '@/api/trade.js'
import { ElMessage } from 'element-plus'

const computing = ref(false)
const computeSearched = ref(false)
const computeResults = ref([])
const computeForm = reactive({ supplyId: '', topK: 10 })

const topLoading = ref(false)
const topSearched = ref(false)
const topResults = ref([])
const topSupplyId = ref('')

const STATUS_MAP = {
  0: { label: '候选', type: 'info' },
  1: { label: '已联系', type: 'primary' },
  2: { label: '议价中', type: 'warning' },
  3: { label: '已成交', type: 'success' },
  4: { label: '已拒绝', type: 'danger' },
}

function matchStatusType(status) {
  return STATUS_MAP[status]?.type || 'info'
}

function matchStatusLabel(status) {
  return STATUS_MAP[status]?.label || status
}

async function handleCompute() {
  if (!computeForm.supplyId) {
    ElMessage.warning('请输入供货ID')
    return
  }
  computing.value = true
  computeSearched.value = false
  try {
    const res = await matchApi.compute({ supplyId: computeForm.supplyId, topK: computeForm.topK })
    computeResults.value = Array.isArray(res) ? res : (res?.records || res?.list || [])
    computeSearched.value = true
  } catch (e) {
    computeResults.value = []
    computeSearched.value = true
  } finally {
    computing.value = false
  }
}

async function handleTop() {
  if (!topSupplyId.value) {
    ElMessage.warning('请输入供货ID')
    return
  }
  topLoading.value = true
  topSearched.value = false
  try {
    const res = await matchApi.top(topSupplyId.value)
    topResults.value = Array.isArray(res) ? res : (res?.records || res?.list || [])
    topSearched.value = true
  } catch (e) {
    topResults.value = []
    topSearched.value = true
  } finally {
    topLoading.value = false
  }
}
</script>

<style scoped>
.match-card {
  border-top: 3px solid var(--el-color-danger);
}
.match-card__score {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
}
</style>
