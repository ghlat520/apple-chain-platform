<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">仓单质押</span>
      <van-button type="primary" size="small" icon="plus" @click="openCreateSheet">新增质押</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索仓单编号/货主" @search="loadList(true)" shape="round" />

    <van-dropdown-menu active-color="#07c160">
      <van-dropdown-item v-model="query.status" :options="statusOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-top:8px;margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.receiptNo }}</span>
              <van-tag :type="statusColor(item.status)" style="margin-left:6px;">{{ statusLabel(item.status) }}</van-tag>
            </template>
            <template #label>
              {{ item.warehouseName }} · {{ item.farmerName }}
            </template>
            <template #value>
              <span style="font-weight:700;color:#1a1a2e;">{{ formatMoney(item.pledgeAmount) }}</span>
            </template>
          </van-cell>
          <van-cell>
            <template #title>
              <span style="font-size:13px;color:#666;">
                {{ item.variety }} · {{ item.grade }} · {{ item.quantity }}吨
              </span>
            </template>
            <template #value>
              <div style="display:flex;gap:4px;">
                <van-button v-if="item.status === 'PLEDGED'" size="mini" type="success" @click.stop="handleAction(item, 'release')">解押</van-button>
                <van-button v-if="item.status === 'PLEDGED'" size="mini" type="danger" @click.stop="handleAction(item, 'default')">违约</van-button>
              </div>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无质押数据" />
      </van-list>
    </van-pull-refresh>

    <!-- New pledge form -->
    <van-action-sheet v-model:show="showCreateSheet" title="新增质押">
      <div style="padding:16px;">
        <van-form @submit="handleCreate">
          <van-field v-model="createForm.receiptId" label="仓单ID" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.receiptNo" label="仓单编号" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.warehouseName" label="仓库名称" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.farmerName" label="货主姓名" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.variety" label="品种" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.grade" label="等级" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.quantity" label="数量(吨)" type="number" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.pledgeAmount" label="质押金额" type="number" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.remark" label="备注" type="textarea" rows="2" placeholder="选填" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- Detail popup -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:60%;">
      <div style="padding:16px;" v-if="selectedItem">
        <h3 style="margin-bottom:12px;">{{ selectedItem.receiptNo }}</h3>
        <van-cell-group>
          <van-cell title="仓库名称" :value="selectedItem.warehouseName" />
          <van-cell title="货主" :value="selectedItem.farmerName" />
          <van-cell title="品种" :value="selectedItem.variety" />
          <van-cell title="等级" :value="selectedItem.grade" />
          <van-cell title="数量" :value="`${selectedItem.quantity} 吨`" />
          <van-cell title="质押金额" :value="formatMoney(selectedItem.pledgeAmount)" />
          <van-cell title="状态">
            <template #value>
              <van-tag :type="statusColor(selectedItem.status)">{{ statusLabel(selectedItem.status) }}</van-tag>
            </template>
          </van-cell>
          <van-cell title="备注" :value="selectedItem.remark || '-'" />
        </van-cell-group>
        <div style="margin-top:16px;display:flex;gap:8px;">
          <van-button v-if="selectedItem.status === 'PLEDGED'" type="success" size="small" @click="handleAction(selectedItem, 'release')">解押</van-button>
          <van-button v-if="selectedItem.status === 'PLEDGED'" type="danger" size="small" @click="handleAction(selectedItem, 'default')">违约</van-button>
          <van-button v-if="selectedItem.status === 'PLEDGED'" type="danger" size="small" plain @click="handleDelete(selectedItem)">删除</van-button>
        </div>
      </div>
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
const selectedItem = ref(null)
const query = reactive({ keyword: '', status: '', page: 1, size: 10 })
const createForm = reactive({
  receiptId: '', receiptNo: '', warehouseName: '', farmerName: '',
  variety: '', grade: '', quantity: '', pledgeAmount: '', remark: ''
})

const STATUS_MAP = { PLEDGED: '质押中', RELEASED: '已解押', DEFAULTED: '已违约' }
const STATUS_COLOR_MAP = { PLEDGED: 'warning', RELEASED: 'success', DEFAULTED: 'danger' }
const statusOptions = [
  { text: '全部状态', value: '' },
  ...Object.entries(STATUS_MAP).map(([value, text]) => ({ text, value }))
]

function statusLabel(s) { return STATUS_MAP[s] || s }
function statusColor(s) { return STATUS_COLOR_MAP[s] || 'default' }
function formatMoney(v) { return v != null ? `¥${Number(v).toLocaleString()}` : '-' }

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await financeApi.pledgeList({ ...query })
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

function openCreateSheet() {
  Object.assign(createForm, {
    receiptId: '', receiptNo: '', warehouseName: '', farmerName: '',
    variety: '', grade: '', quantity: '', pledgeAmount: '', remark: ''
  })
  showCreateSheet.value = true
}

async function handleCreate() {
  await financeApi.pledgeCreate({
    ...createForm,
    quantity: Number(createForm.quantity),
    pledgeAmount: Number(createForm.pledgeAmount)
  })
  showToast('质押创建成功')
  showCreateSheet.value = false
  loadList(true)
}

const ACTION_MAP = {
  release: { api: 'pledgeRelease', label: '已解押' },
  default: { api: 'pledgeDefault', label: '已标记违约' }
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
  await showConfirmDialog({ title: '删除确认', message: '确定删除该质押记录？' })
  await financeApi.pledgeDelete(item.id)
  showToast('已删除')
  showDetailPopup.value = false
  loadList(true)
}

loadList(true)
</script>
