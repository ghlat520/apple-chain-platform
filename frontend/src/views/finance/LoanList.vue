<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">贷款管理</span>
      <van-button type="primary" size="small" icon="plus" @click="openCreateSheet">新增贷款</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索贷款编号/借款人" @search="loadList(true)" shape="round" />

    <van-dropdown-menu active-color="#07c160">
      <van-dropdown-item v-model="query.loanType" :options="loanTypeOptions" @change="loadList(true)" />
      <van-dropdown-item v-model="query.status" :options="statusOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-top:8px;margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.loanCode }}</span>
              <van-tag :type="statusColor(item.status)" style="margin-left:6px;">{{ statusLabel(item.status) }}</van-tag>
            </template>
            <template #label>
              {{ item.borrowerName }} · {{ loanTypeLabel(item.loanType) }}
            </template>
            <template #value>
              <span style="font-weight:700;color:#1a1a2e;">{{ formatMoney(item.amount) }}</span>
            </template>
          </van-cell>
          <van-cell>
            <template #title>
              <span style="font-size:13px;color:#666;">利率: {{ item.interestRate }}% · {{ item.termMonths }}个月</span>
            </template>
            <template #value>
              <div style="display:flex;gap:4px;">
                <van-button v-if="item.status === 'PENDING'" size="mini" type="success" @click.stop="handleAction(item, 'approve')">审批</van-button>
                <van-button v-if="item.status === 'PENDING'" size="mini" type="danger" @click.stop="handleAction(item, 'reject')">拒绝</van-button>
                <van-button v-if="item.status === 'APPROVED'" size="mini" type="primary" @click.stop="handleAction(item, 'disburse')">放款</van-button>
                <van-button v-if="item.status === 'DISBURSED'" size="mini" type="success" @click.stop="handleAction(item, 'repay')">还款</van-button>
                <van-button v-if="item.status === 'REPAID'" size="mini" type="default" @click.stop="handleAction(item, 'settle')">结清</van-button>
              </div>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无贷款数据" />
      </van-list>
    </van-pull-refresh>

    <!-- New loan form -->
    <van-action-sheet v-model:show="showCreateSheet" title="新增贷款">
      <div style="padding:16px;">
        <van-form @submit="handleCreate">
          <van-field v-model="createForm.borrowerId" label="借款人ID" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.borrowerName" label="借款人姓名" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.loanType" is-link readonly label="贷款类型" placeholder="请选择" @click="showLoanTypePicker = true" :rules="[{ required: true }]" />
          <van-field v-model="createForm.amount" label="贷款金额" type="number" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.interestRate" label="年利率(%)" type="number" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.termMonths" label="期限(月)" type="digit" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.applyDate" is-link readonly label="开始日期" placeholder="请选择" @click="showStartDatePicker = true" />
          <van-field v-model="createForm.dueDate" is-link readonly label="结束日期" placeholder="请选择" @click="showEndDatePicker = true" />
          <van-field v-model="createForm.remark" label="备注" type="textarea" rows="2" placeholder="选填" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交申请</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- Loan detail popup -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:70%;">
      <div style="padding:16px;" v-if="selectedItem">
        <h3 style="margin-bottom:12px;">{{ selectedItem.loanCode }}</h3>
        <van-cell-group>
          <van-cell title="借款人" :value="selectedItem.borrowerName" />
          <van-cell title="贷款类型" :value="loanTypeLabel(selectedItem.loanType)" />
          <van-cell title="贷款金额" :value="formatMoney(selectedItem.amount)" />
          <van-cell title="年利率" :value="`${selectedItem.interestRate}%`" />
          <van-cell title="期限" :value="`${selectedItem.termMonths}个月`" />
          <van-cell title="开始日期" :value="selectedItem.applyDate || '-'" />
          <van-cell title="结束日期" :value="selectedItem.dueDate || '-'" />
          <van-cell title="状态">
            <template #value>
              <van-tag :type="statusColor(selectedItem.status)">{{ statusLabel(selectedItem.status) }}</van-tag>
            </template>
          </van-cell>
          <van-cell title="备注" :value="selectedItem.remark || '-'" />
        </van-cell-group>
        <div style="margin-top:16px;display:flex;gap:8px;flex-wrap:wrap;">
          <van-button v-if="selectedItem.status === 'PENDING'" type="success" size="small" @click="handleAction(selectedItem, 'approve')">审批通过</van-button>
          <van-button v-if="selectedItem.status === 'PENDING'" type="danger" size="small" @click="handleAction(selectedItem, 'reject')">拒绝</van-button>
          <van-button v-if="selectedItem.status === 'APPROVED'" type="primary" size="small" @click="handleAction(selectedItem, 'disburse')">放款</van-button>
          <van-button v-if="selectedItem.status === 'DISBURSED'" type="success" size="small" @click="handleAction(selectedItem, 'repay')">还款</van-button>
          <van-button v-if="selectedItem.status === 'REPAID'" type="default" size="small" @click="handleAction(selectedItem, 'settle')">结清</van-button>
          <van-button v-if="selectedItem.status === 'PENDING' || selectedItem.status === 'REJECTED'" type="danger" size="small" plain @click="handleDelete(selectedItem)">删除</van-button>
        </div>
      </div>
    </van-popup>

    <!-- Pickers -->
    <van-popup v-model:show="showLoanTypePicker" round position="bottom">
      <van-picker :columns="loanTypePickerColumns" @confirm="onLoanTypeConfirm" @cancel="showLoanTypePicker = false" />
    </van-popup>
    <van-popup v-model:show="showStartDatePicker" round position="bottom">
      <van-date-picker v-model="startDatePickerValue" @confirm="onStartDateConfirm" @cancel="showStartDatePicker = false" />
    </van-popup>
    <van-popup v-model:show="showEndDatePicker" round position="bottom">
      <van-date-picker v-model="endDatePickerValue" @confirm="onEndDateConfirm" @cancel="showEndDatePicker = false" />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { financeApi } from '@/api/finance'
import { showToast, showConfirmDialog } from 'vant'

// --- Data ---
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showCreateSheet = ref(false)
const showDetailPopup = ref(false)
const showLoanTypePicker = ref(false)
const showStartDatePicker = ref(false)
const showEndDatePicker = ref(false)
const selectedItem = ref(null)
const startDatePickerValue = ref(['2026', '01', '01'])
const endDatePickerValue = ref(['2026', '12', '31'])

const query = reactive({ keyword: '', loanType: '', status: '', page: 1, size: 10 })
const createForm = reactive({
  borrowerId: '', borrowerName: '', loanType: '',
  amount: '', interestRate: '', termMonths: '',
  applyDate: '', dueDate: '', remark: ''
})

// --- Enums ---
const LOAN_TYPE_MAP = {
  PLEDGE: '仓单质押', RECEIVABLE: '应收账款', CREDIT: '信用贷款',
  PLANT: '种植贷', WAREHOUSE: '仓储贷', TRADE: '贸易融资', EXPORT: '出口融资'
}
const STATUS_MAP = {
  PENDING: '待审批', APPROVED: '已审批', REJECTED: '已拒绝',
  DISBURSED: '已放款', REPAID: '已还款', OVERDUE: '逾期', SETTLED: '已结清'
}
const STATUS_COLOR_MAP = {
  PENDING: 'warning', APPROVED: 'primary', REJECTED: 'danger',
  DISBURSED: 'success', REPAID: 'success', OVERDUE: 'danger', SETTLED: 'default'
}

const loanTypeOptions = [
  { text: '全部类型', value: '' },
  ...Object.entries(LOAN_TYPE_MAP).map(([value, text]) => ({ text, value }))
]
const statusOptions = [
  { text: '全部状态', value: '' },
  ...Object.entries(STATUS_MAP).map(([value, text]) => ({ text, value }))
]
const loanTypePickerColumns = Object.entries(LOAN_TYPE_MAP).map(([value, text]) => ({ text, value }))

// --- Helpers ---
function statusLabel(s) { return STATUS_MAP[s] || s }
function statusColor(s) { return STATUS_COLOR_MAP[s] || 'default' }
function loanTypeLabel(t) { return LOAN_TYPE_MAP[t] || t }
function formatMoney(v) { return v != null ? `¥${Number(v).toLocaleString()}` : '-' }

// --- Load ---
async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await financeApi.loanList({ ...query })
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

// --- Detail ---
function showDetail(item) {
  selectedItem.value = item
  showDetailPopup.value = true
}

// --- Create ---
function openCreateSheet() {
  Object.assign(createForm, {
    borrowerId: '', borrowerName: '', loanType: '',
    amount: '', interestRate: '', termMonths: '',
    applyDate: '', dueDate: '', remark: ''
  })
  showCreateSheet.value = true
}

function onLoanTypeConfirm({ selectedValues }) {
  createForm.loanType = selectedValues[0]
  showLoanTypePicker.value = false
}
function onStartDateConfirm({ selectedValues }) {
  createForm.applyDate = selectedValues.join('-')
  showStartDatePicker.value = false
}
function onEndDateConfirm({ selectedValues }) {
  createForm.dueDate = selectedValues.join('-')
  showEndDatePicker.value = false
}

async function handleCreate() {
  await financeApi.loanCreate({
    ...createForm,
    amount: Number(createForm.amount),
    interestRate: Number(createForm.interestRate),
    termMonths: Number(createForm.termMonths)
  })
  showToast('贷款申请已提交')
  showCreateSheet.value = false
  loadList(true)
}

// --- Actions ---
const ACTION_MAP = {
  approve: { api: 'loanApprove', label: '审批通过' },
  reject: { api: 'loanReject', label: '已拒绝' },
  disburse: { api: 'loanDisburse', label: '已放款' },
  repay: { api: 'loanRepay', label: '已还款' },
  settle: { api: 'loanSettle', label: '已结清' }
}

async function handleAction(item, action) {
  const cfg = ACTION_MAP[action]
  if (!cfg) return
  await showConfirmDialog({ title: '操作确认', message: `确定执行「${cfg.label}」操作？` })
  await financeApi[cfg.api](item.id)
  showToast(cfg.label)
  showDetailPopup.value = false
  loadList(true)
}

async function handleDelete(item) {
  await showConfirmDialog({ title: '删除确认', message: '确定删除该贷款记录？此操作不可恢复。' })
  await financeApi.loanDelete(item.id)
  showToast('已删除')
  showDetailPopup.value = false
  loadList(true)
}

loadList(true)
</script>
