<template>
  <div class="task-plan-page">
    <van-nav-bar title="AI 作业计划" left-arrow @click-left="$router.back()" />

    <!-- ========== 查询筛选 ========== -->
    <van-cell-group inset title="查询条件" style="margin-top: var(--space-3)">
      <van-field
        v-model.number="queryOrchardId"
        label="果园ID"
        type="number"
        placeholder="请输入果园ID"
      />
      <van-cell title="开始日期" is-link :value="queryFrom" @click="showFromPicker = true" />
      <van-cell title="结束日期" is-link :value="queryTo" @click="showToPicker = true" />
      <van-cell>
        <template #value>
          <div class="filter-actions">
            <van-button size="small" type="primary" :loading="loading" @click="loadList">
              查询
            </van-button>
            <van-button size="small" type="success" plain @click="openGenerateDrawer">
              生成计划
            </van-button>
          </div>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 日期选择 Popup -->
    <van-popup v-model:show="showFromPicker" position="bottom" round>
      <van-date-picker
        v-model="fromPickerValue"
        title="选择开始日期"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onPickFrom"
        @cancel="showFromPicker = false"
      />
    </van-popup>
    <van-popup v-model:show="showToPicker" position="bottom" round>
      <van-date-picker
        v-model="toPickerValue"
        title="选择结束日期"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onPickTo"
        @cancel="showToPicker = false"
      />
    </van-popup>

    <!-- ========== 列表 ========== -->
    <div v-if="loading" class="loading-wrapper">
      <van-loading size="24px" vertical>加载中...</van-loading>
    </div>

    <van-empty v-else-if="!taskList.length && searched" description="暂无作业计划" />

    <van-empty v-else-if="!searched" description="请输入果园ID并查询" />

    <van-cell-group
      v-for="(plan, index) in taskList"
      :key="plan.id"
      :title="`#${index + 1} · ${plan.taskName || '未命名'}`"
      inset
      class="plan-card"
    >
      <van-cell title="作业类型" :value="plan.operationType || '-'" />
      <van-cell title="计划日期" :value="plan.planDate || '-'" />
      <van-cell title="优先级">
        <template #value>
          <van-tag :type="priorityTagType(plan.priority)" plain>
            {{ priorityLabel(plan.priority) }}
          </van-tag>
        </template>
      </van-cell>
      <van-cell title="状态">
        <template #value>
          <van-tag :type="statusTagType(plan.status)">
            {{ statusLabel(plan.status) }}
          </van-tag>
        </template>
      </van-cell>
      <van-cell title="生成方式" :value="plan.generatedBy === 'AUTO' ? 'AI 自动' : '手动'" />
      <van-cell v-if="plan.materialSuggestion" title="物料建议" :label="plan.materialSuggestion" />
      <van-cell v-if="plan.remark" title="备注" :label="plan.remark" />
      <van-cell title="创建时间" :value="plan.createTime || '-'" />

      <!-- 操作按钮 -->
      <van-cell v-if="plan.status === 'PENDING' || plan.status === 'IN_PROGRESS'" title="操作">
        <template #value>
          <div class="cell-actions">
            <van-button
              size="mini"
              type="success"
              plain
              :loading="actionLoading[plan.id] === 'done'"
              @click="handleDone(plan)"
            >
              标记完成
            </van-button>
            <van-button
              size="mini"
              type="warning"
              plain
              :loading="actionLoading[plan.id] === 'skip'"
              @click="openSkipDialog(plan)"
            >
              跳过
            </van-button>
          </div>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- ========== 生成计划 Drawer ========== -->
    <van-popup
      v-model:show="showGenerate"
      position="bottom"
      round
      :style="{ maxHeight: '70%' }"
    >
      <div class="drawer-content">
        <div class="drawer-header">
          <span class="drawer-title">AI 生成作业计划</span>
          <van-icon name="cross" size="20" @click="showGenerate = false" />
        </div>

        <van-form @submit="handleGenerate">
          <van-field
            v-model.number="genOrchardId"
            label="果园ID"
            type="number"
            placeholder="请输入果园ID"
            :rules="[{ required: true, message: '请填写果园ID' }]"
          />

          <van-field
            v-model.number="genMonths"
            label="生成月数"
            type="number"
            placeholder="1-12"
            :rules="[
              { required: true, message: '请填写月数' },
              { validator: (v) => v >= 1 && v <= 12, message: '月数范围 1-12' }
            ]"
          />

          <div class="drawer-actions">
            <van-button block round type="primary" native-type="submit" :loading="generating">
              开始生成
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <!-- ========== 跳过原因 Dialog ========== -->
    <van-dialog
      v-model:show="showSkipDialog"
      title="跳过计划"
      show-cancel-button
      :before-close="handleSkip"
    >
      <van-field
        v-model="skipReason"
        rows="3"
        autosize
        type="textarea"
        maxlength="200"
        placeholder="请输入跳过原因"
        show-word-limit
        class="skip-field"
      />
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { plantingApi } from '@/api/planting.js'

const route = useRoute()

// ---------- 日期工具 ----------
const today = new Date()
const currentYear = today.getFullYear()
const minDate = new Date(currentYear - 1, 0, 1)
const maxDate = new Date(currentYear + 1, 11, 31)

function splitDate(dateStr) {
  if (!dateStr) return []
  return dateStr.split('-')
}

// ---------- 查询状态 ----------
const queryOrchardId = ref(route.query.orchardId ? Number(route.query.orchardId) : '')
const queryFrom = ref(`${currentYear}-01-01`)
const queryTo = ref(`${currentYear}-12-31`)
const showFromPicker = ref(false)
const showToPicker = ref(false)
const fromPickerValue = ref(splitDate(queryFrom.value))
const toPickerValue = ref(splitDate(queryTo.value))

const loading = ref(false)
const searched = ref(false)
const taskList = ref([])
const actionLoading = reactive({})

function onPickFrom({ selectedValues }) {
  queryFrom.value = selectedValues.join('-')
  showFromPicker.value = false
}

function onPickTo({ selectedValues }) {
  queryTo.value = selectedValues.join('-')
  showToPicker.value = false
}

async function loadList() {
  if (!queryOrchardId.value) {
    showFailToast('请输入果园ID')
    return
  }
  loading.value = true
  try {
    const data = await plantingApi.getTaskPlans({
      orchardId: queryOrchardId.value,
      from: queryFrom.value,
      to: queryTo.value,
    })
    taskList.value = Array.isArray(data) ? data : []
    searched.value = true
  } catch (e) {
    taskList.value = []
    showFailToast(e?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

// ---------- 标签映射 ----------
function priorityTagType(p) {
  if (p === 1) return 'danger'
  if (p === 2) return 'warning'
  return 'default'
}

function priorityLabel(p) {
  if (p === 1) return '高'
  if (p === 2) return '中'
  if (p === 3) return '低'
  return p != null ? String(p) : '-'
}

function statusTagType(s) {
  const map = {
    PENDING: 'primary',
    IN_PROGRESS: 'warning',
    DONE: 'success',
    SKIPPED: 'default',
  }
  return map[s] || 'default'
}

function statusLabel(s) {
  const map = {
    PENDING: '待执行',
    IN_PROGRESS: '进行中',
    DONE: '已完成',
    SKIPPED: '已跳过',
  }
  return map[s] || s || '-'
}

// ---------- 标记完成 ----------
async function handleDone(plan) {
  actionLoading[plan.id] = 'done'
  try {
    await plantingApi.markTaskDone(plan.id, plan.actualOperationId)
    showSuccessToast('已标记完成')
    await loadList()
  } catch (e) {
    showFailToast(e?.message || '操作失败')
  } finally {
    actionLoading[plan.id] = undefined
  }
}

// ---------- 跳过计划 ----------
const showSkipDialog = ref(false)
const skipReason = ref('')
const skipTarget = ref(null)

function openSkipDialog(plan) {
  skipTarget.value = plan
  skipReason.value = ''
  showSkipDialog.value = true
}

async function handleSkip(action) {
  if (action === 'confirm') {
    if (!skipReason.value.trim()) {
      showFailToast('请输入跳过原因')
      return false
    }
    const plan = skipTarget.value
    if (!plan) return false

    actionLoading[plan.id] = 'skip'
    try {
      await plantingApi.skipTask(plan.id, skipReason.value.trim())
      showSuccessToast('已跳过')
      showSkipDialog.value = false
      await loadList()
    } catch (e) {
      showFailToast(e?.message || '操作失败')
    } finally {
      actionLoading[plan.id] = undefined
    }
    return false
  }
  // action === 'cancel'
  return true
}

// ---------- 生成计划 Drawer ----------
const showGenerate = ref(false)
const genOrchardId = ref('')
const genMonths = ref(1)
const generating = ref(false)

function openGenerateDrawer() {
  if (!queryOrchardId.value) {
    showFailToast('请先输入果园ID')
    return
  }
  genOrchardId.value = queryOrchardId.value
  genMonths.value = 1
  showGenerate.value = true
}

async function handleGenerate() {
  generating.value = true
  try {
    await plantingApi.generateTaskPlan(genOrchardId.value, genMonths.value)
    showSuccessToast('计划已生成')
    showGenerate.value = false
    await loadList()
  } catch (e) {
    showFailToast(e?.message || '生成失败')
  } finally {
    generating.value = false
  }
}

// ---------- 初始化 ----------
onMounted(() => {
  if (queryOrchardId.value) {
    loadList()
  }
})
</script>

<style scoped>
.task-plan-page {
  padding-bottom: var(--space-12);
  background: var(--color-surface-base);
  min-height: 100vh;
}

.filter-actions {
  display: flex;
  gap: var(--space-2);
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: var(--space-8) 0;
}

.plan-card {
  margin-top: var(--space-3);
}

.cell-actions {
  display: flex;
  gap: var(--space-2);
}

/* Drawer */
.drawer-content {
  padding: var(--space-4);
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) 0 var(--space-4);
}

.drawer-title {
  font-size: var(--font-size-body);
  font-weight: 500;
  color: var(--color-text-primary);
}

.drawer-actions {
  padding: var(--space-4) 0;
}

/* Skip dialog textarea */
.skip-field {
  padding: var(--space-3) var(--space-4);
}
</style>
