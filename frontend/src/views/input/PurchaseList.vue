<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">采购管理</span>
      <van-button type="primary" size="small" icon="plus" @click="openAddSheet">新增采购</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索采购单号/产品/供应商" @search="loadList(true)" shape="round" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="query.status" :options="statusOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.purchaseNo }}</span>
              <van-tag :type="statusTagType(item.status)" style="margin-left:6px;">
                {{ statusLabel(item.status) }}
              </van-tag>
            </template>
            <template #label>
              {{ item.productName }} · {{ item.supplierName }}
            </template>
          </van-cell>
          <van-cell title="数量" :value="`${item.quantity} ${item.unit || ''}`" />
          <van-cell title="总金额" :value="item.totalAmount != null ? `¥${item.totalAmount}` : '-'" />
          <van-cell title="采购日期" :value="item.purchaseDate || '-'" />

          <!-- 操作按钮 -->
          <div style="padding:8px 16px; display:flex; gap:8px;" v-if="item.status === 'PENDING' || item.status === 'APPROVED'">
            <van-button v-if="item.status === 'PENDING'" type="success" size="mini" @click.stop="handleApprove(item)">审批</van-button>
            <van-button v-if="item.status === 'APPROVED'" type="primary" size="mini" @click.stop="handleReceive(item)">收货</van-button>
            <van-button v-if="item.status === 'PENDING'" type="default" size="mini" @click.stop="handleCancel(item)">取消</van-button>
          </div>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无采购数据" />
      </van-list>
    </van-pull-refresh>

    <!-- 新增采购弹窗 -->
    <van-action-sheet v-model:show="showAddSheet" title="新增采购">
      <div style="padding:16px;">
        <van-form @submit="handleSave">
          <van-field v-model="addForm.productId" label="产品ID" type="digit" placeholder="请输入产品ID" :rules="[{required:true}]" />
          <van-field v-model="addForm.supplierId" label="供应商ID" type="digit" placeholder="请输入供应商ID" :rules="[{required:true}]" />
          <van-field v-model="addForm.quantity" label="数量" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.unit" label="单位" placeholder="如 kg/L/袋/瓶" :rules="[{required:true}]" />
          <van-field v-model="addForm.unitPrice" label="单价(元)" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.purchaseDate" is-link readonly label="采购日期" placeholder="请选择"
            :rules="[{required:true}]" @click="showDatePicker = true" />
          <van-field v-model="addForm.remark" label="备注" type="textarea" placeholder="请输入" rows="2" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 日期选择器 -->
    <van-popup v-model:show="showDatePicker" round position="bottom">
      <van-date-picker v-model="datePickerValue" title="选择采购日期"
        :min-date="new Date(2020, 0, 1)" :max-date="new Date()"
        @confirm="onDateConfirm" @cancel="showDatePicker = false" />
    </van-popup>

    <!-- 详情弹窗 -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:65%;">
      <div style="padding:16px;" v-if="selectedItem">
        <h3 style="margin-bottom:12px;">{{ selectedItem.purchaseNo }}</h3>
        <van-cell-group>
          <van-cell title="状态" :value="statusLabel(selectedItem.status)" />
          <van-cell title="产品" :value="selectedItem.productName || '-'" />
          <van-cell title="供应商" :value="selectedItem.supplierName || '-'" />
          <van-cell title="数量" :value="`${selectedItem.quantity} ${selectedItem.unit || ''}`" />
          <van-cell title="单价" :value="selectedItem.unitPrice != null ? `¥${selectedItem.unitPrice}` : '-'" />
          <van-cell title="总金额" :value="selectedItem.totalAmount != null ? `¥${selectedItem.totalAmount}` : '-'" />
          <van-cell title="采购日期" :value="selectedItem.purchaseDate || '-'" />
          <van-cell title="备注" :value="selectedItem.remark || '-'" />
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { inputApi } from '@/api/input'
import { showToast, showDialog } from 'vant'

const statusMap = {
  PENDING: { label: '待审批', type: 'warning' },
  APPROVED: { label: '已审批', type: 'success' },
  RECEIVED: { label: '已收货', type: 'primary' },
  CANCELLED: { label: '已取消', type: 'default' }
}

const statusOptions = [
  { text: '全部状态', value: '' },
  { text: '待审批', value: 'PENDING' },
  { text: '已审批', value: 'APPROVED' },
  { text: '已收货', value: 'RECEIVED' },
  { text: '已取消', value: 'CANCELLED' }
]

function statusLabel(status) {
  return statusMap[status]?.label || status
}

function statusTagType(status) {
  return statusMap[status]?.type || 'default'
}

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const showDetailPopup = ref(false)
const showDatePicker = ref(false)
const selectedItem = ref(null)

const now = new Date()
const datePickerValue = ref([String(now.getFullYear()), String(now.getMonth() + 1).padStart(2, '0'), String(now.getDate()).padStart(2, '0')])

const query = reactive({ keyword: '', page: 1, size: 10, status: '' })
const addForm = reactive({
  productId: '', supplierId: '', quantity: '', unit: '',
  unitPrice: '', purchaseDate: '', remark: ''
})

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await inputApi.purchaseList({ ...query })
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

function openAddSheet() {
  Object.assign(addForm, {
    productId: '', supplierId: '', quantity: '', unit: '',
    unitPrice: '', purchaseDate: '', remark: ''
  })
  showAddSheet.value = true
}

function onDateConfirm({ selectedValues }) {
  addForm.purchaseDate = selectedValues.join('-')
  showDatePicker.value = false
}

async function handleSave() {
  const payload = {
    ...addForm,
    productId: Number(addForm.productId),
    supplierId: Number(addForm.supplierId),
    quantity: Number(addForm.quantity),
    unitPrice: Number(addForm.unitPrice)
  }
  await inputApi.purchaseCreate(payload)
  showToast('采购单创建成功')
  showAddSheet.value = false
  loadList(true)
}

async function handleApprove(item) {
  await showDialog({ title: '确认审批', message: `确认审批采购单 ${item.purchaseNo}？` })
  await inputApi.purchaseApprove(item.id)
  showToast('审批成功')
  loadList(true)
}

async function handleReceive(item) {
  await showDialog({ title: '确认收货', message: `确认已收到采购单 ${item.purchaseNo} 的货物？收货后将自动更新库存。` })
  await inputApi.purchaseReceive(item.id)
  showToast('收货成功')
  loadList(true)
}

async function handleCancel(item) {
  await showDialog({ title: '确认取消', message: `确认取消采购单 ${item.purchaseNo}？` })
  await inputApi.purchaseCancel(item.id)
  showToast('已取消')
  loadList(true)
}

loadList(true)
</script>
