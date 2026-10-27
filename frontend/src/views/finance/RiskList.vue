<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">风控管理</span>
      <van-button type="primary" size="small" icon="plus" @click="openCreateSheet">新增记录</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索贷款编号/借款人" @search="loadList(true)" shape="round" />

    <van-dropdown-menu active-color="#07c160">
      <van-dropdown-item v-model="query.riskType" :options="riskTypeOptions" @change="loadList(true)" />
      <van-dropdown-item v-model="query.riskLevel" :options="riskLevelOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-top:8px;margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.loanNo }}</span>
              <van-tag :type="levelColor(item.riskLevel)" style="margin-left:6px;">{{ riskLevelLabel(item.riskLevel) }}</van-tag>
            </template>
            <template #label>
              {{ item.borrowerName }} · {{ riskTypeLabel(item.riskType) }}
            </template>
            <template #value>
              <van-tag :type="statusColor(item.status)">{{ statusLabel(item.status) }}</van-tag>
            </template>
          </van-cell>
          <van-cell>
            <template #title>
              <span style="font-size:13px;color:#666;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;display:block;max-width:240px;">
                {{ item.description }}
              </span>
            </template>
            <template #value>
              <div style="display:flex;gap:4px;" v-if="item.status === 'PENDING'">
                <van-button size="mini" type="success" @click.stop="handleAction(item, 'resolve')">处理</van-button>
                <van-button size="mini" type="warning" @click.stop="handleAction(item, 'escalate')">升级</van-button>
              </div>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无风控记录" />
      </van-list>
    </van-pull-refresh>

    <!-- New risk record form -->
    <van-action-sheet v-model:show="showCreateSheet" title="新增风控记录">
      <div style="padding:16px;">
        <van-form @submit="handleCreate">
          <van-field v-model="createForm.loanId" label="贷款ID" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.loanNo" label="贷款编号" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.borrowerName" label="借款人" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.riskType" is-link readonly label="风险类型" placeholder="请选择" @click="showRiskTypePicker = true" :rules="[{ required: true }]" />
          <van-field v-model="createForm.riskLevel" is-link readonly label="风险等级" placeholder="请选择" @click="showRiskLevelPicker = true" :rules="[{ required: true }]" />
          <van-field v-model="createForm.description" label="描述" type="textarea" rows="2" placeholder="请输入风险描述" :rules="[{ required: true }]" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- Handle popup -->
    <van-action-sheet v-model:show="showHandleSheet" :title="handleTitle">
      <div style="padding:16px;">
        <van-form @submit="handleResolve">
          <van-field v-model="handleForm.handlerName" label="处理人" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="handleForm.handleResult" label="处理结果" type="textarea" rows="3" placeholder="请输入处理结果" :rules="[{ required: true }]" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">确认</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- Detail popup -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:65%;">
      <div style="padding:16px;" v-if="selectedItem">
        <h3 style="margin-bottom:12px;">{{ selectedItem.loanNo }}</h3>
        <van-cell-group>
          <van-cell title="借款人" :value="selectedItem.borrowerName" />
          <van-cell title="风险类型" :value="riskTypeLabel(selectedItem.riskType)" />
          <van-cell title="风险等级">
            <template #value>
              <van-tag :type="levelColor(selectedItem.riskLevel)">{{ riskLevelLabel(selectedItem.riskLevel) }}</van-tag>
            </template>
          </van-cell>
          <van-cell title="状态">
            <template #value>
              <van-tag :type="statusColor(selectedItem.status)">{{ statusLabel(selectedItem.status) }}</van-tag>
            </template>
          </van-cell>
          <van-cell title="描述" :value="selectedItem.description" />
          <van-cell title="处理人" :value="selectedItem.handlerName || '-'" />
          <van-cell title="处理结果" :value="selectedItem.handleResult || '-'" />
        </van-cell-group>
        <div style="margin-top:16px;display:flex;gap:8px;" v-if="selectedItem.status === 'PENDING'">
          <van-button type="success" size="small" @click="openHandleSheet(selectedItem, 'resolve')">处理</van-button>
          <van-button type="warning" size="small" @click="openHandleSheet(selectedItem, 'escalate')">升级</van-button>
        </div>
      </div>
    </van-popup>

    <!-- Pickers -->
    <van-popup v-model:show="showRiskTypePicker" round position="bottom">
      <van-picker :columns="riskTypePickerColumns" @confirm="onRiskTypeConfirm" @cancel="showRiskTypePicker = false" />
    </van-popup>
    <van-popup v-model:show="showRiskLevelPicker" round position="bottom">
      <van-picker :columns="riskLevelPickerColumns" @confirm="onRiskLevelConfirm" @cancel="showRiskLevelPicker = false" />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { financeApi } from '@/api/finance'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showCreateSheet = ref(false)
const showDetailPopup = ref(false)
const showHandleSheet = ref(false)
const showRiskTypePicker = ref(false)
const showRiskLevelPicker = ref(false)
const selectedItem = ref(null)
const handleTitle = ref('处理')
const handleActionType = ref('resolve')
const query = reactive({ keyword: '', riskType: '', riskLevel: '', page: 1, size: 10 })
const createForm = reactive({
  loanId: '', loanNo: '', borrowerName: '',
  riskType: '', riskLevel: '', description: ''
})
const handleForm = reactive({ handlerName: '', handleResult: '' })

const RISK_TYPE_MAP = { OVERDUE: '逾期', DEFAULT: '违约', FRAUD: '欺诈', OTHER: '其他' }
const RISK_LEVEL_MAP = { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', CRITICAL: '严重' }
const RISK_LEVEL_COLOR_MAP = { LOW: 'success', MEDIUM: 'primary', HIGH: 'warning', CRITICAL: 'danger' }
const STATUS_MAP = { PENDING: '待处理', RESOLVED: '已处理', ESCALATED: '已升级' }
const STATUS_COLOR_MAP = { PENDING: 'warning', RESOLVED: 'success', ESCALATED: 'danger' }

const riskTypeOptions = [
  { text: '全部类型', value: '' },
  ...Object.entries(RISK_TYPE_MAP).map(([value, text]) => ({ text, value }))
]
const riskLevelOptions = [
  { text: '全部等级', value: '' },
  ...Object.entries(RISK_LEVEL_MAP).map(([value, text]) => ({ text, value }))
]
const riskTypePickerColumns = Object.entries(RISK_TYPE_MAP).map(([value, text]) => ({ text, value }))
const riskLevelPickerColumns = Object.entries(RISK_LEVEL_MAP).map(([value, text]) => ({ text, value }))

function riskTypeLabel(t) { return RISK_TYPE_MAP[t] || t }
function riskLevelLabel(l) { return RISK_LEVEL_MAP[l] || l }
function levelColor(l) { return RISK_LEVEL_COLOR_MAP[l] || 'default' }
function statusLabel(s) { return STATUS_MAP[s] || s }
function statusColor(s) { return STATUS_COLOR_MAP[s] || 'default' }

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await financeApi.riskList({ ...query })
    const records = res.data?.records || []
    if (reset) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.data?.total || 0)
    query.page++
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function showDetail(item) {
  selectedItem.value = item
  showDetailPopup.value = true
}

function onRiskTypeConfirm({ selectedValues }) {
  createForm.riskType = selectedValues[0]
  showRiskTypePicker.value = false
}
function onRiskLevelConfirm({ selectedValues }) {
  createForm.riskLevel = selectedValues[0]
  showRiskLevelPicker.value = false
}

function openCreateSheet() {
  Object.assign(createForm, {
    loanId: '', loanNo: '', borrowerName: '',
    riskType: '', riskLevel: '', description: ''
  })
  showCreateSheet.value = true
}

async function handleCreate() {
  await financeApi.riskCreate({ ...createForm })
  showToast('风控记录创建成功')
  showCreateSheet.value = false
  loadList(true)
}

function openHandleSheet(item, action) {
  selectedItem.value = item
  handleActionType.value = action
  handleTitle.value = action === 'resolve' ? '处理风险' : '升级风险'
  Object.assign(handleForm, { handlerName: '', handleResult: '' })
  showDetailPopup.value = false
  showHandleSheet.value = true
}

async function handleAction(item, action) {
  openHandleSheet(item, action)
}

async function handleResolve() {
  const api = handleActionType.value === 'resolve' ? 'riskResolve' : 'riskEscalate'
  await financeApi[api](selectedItem.value.id, { ...handleForm })
  showToast(handleActionType.value === 'resolve' ? '已处理' : '已升级')
  showHandleSheet.value = false
  loadList(true)
}

loadList(true)
</script>
