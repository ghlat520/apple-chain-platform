<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">仓单管理</span>
      <van-button type="primary" size="small" icon="plus" @click="showAddSheet = true">新增仓单</van-button>
    </div>

    <van-tabs v-model:active="activeStatus" @change="handleTabChange" style="margin-bottom:12px;">
      <van-tab title="全部" name="" />
      <van-tab title="有效" name="VALID" />
      <van-tab title="已质押" name="PLEDGED" />
      <van-tab title="已转让" name="TRANSFERRED" />
      <van-tab title="已注销" name="CANCELLED" />
    </van-tabs>

    <van-search v-model="query.keyword" placeholder="搜索仓单编号/货主/品种" @search="loadList(true)" shape="round" />

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-swipe-cell v-for="item in list" :key="item.id" style="margin-bottom:8px;">
          <van-cell-group inset>
            <van-cell is-link @click="showDetail(item)">
              <template #title>
                <span style="font-weight:600;">{{ item.receiptNo }}</span>
                <van-tag :type="statusTagType(item.status)" style="margin-left:6px;">
                  {{ statusLabel(item.status) }}
                </van-tag>
              </template>
              <template #label>
                {{ item.warehouseName }} · {{ item.ownerName }}
              </template>
            </van-cell>
            <van-cell title="品种" :value="item.variety" />
            <van-cell title="数量" :value="`${item.quantity} kg`" />
            <van-cell title="总估值" :value="`¥${item.totalValue || '-'}`" />
          </van-cell-group>
          <template #right>
            <van-button
              v-if="item.status === 'VALID'"
              square type="warning" text="质押" style="height:100%;"
              @click="changeReceiptStatus(item.id, 'PLEDGED')"
            />
            <van-button
              v-if="item.status === 'VALID'"
              square type="primary" text="转让" style="height:100%;"
              @click="changeReceiptStatus(item.id, 'TRANSFERRED')"
            />
            <van-button
              v-if="item.status === 'VALID' || item.status === 'PLEDGED'"
              square type="danger" text="注销" style="height:100%;"
              @click="changeReceiptStatus(item.id, 'CANCELLED')"
            />
          </template>
        </van-swipe-cell>
        <van-empty v-if="!loading && list.length === 0" description="暂无仓单数据" />
      </van-list>
    </van-pull-refresh>

    <!-- 新增仓单 -->
    <van-action-sheet v-model:show="showAddSheet" title="新增仓单" style="max-height:85%;">
      <div style="padding:16px;max-height:70vh;overflow-y:auto;">
        <van-form @submit="handleCreate">
          <van-field v-model="addForm.warehouseId" label="仓库ID" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.ownerId" label="货主ID" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.ownerName" label="货主姓名" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.batchCode" label="批次编码" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.variety" label="品种" placeholder="如：红富士" :rules="[{required:true}]" />
          <van-field v-model="addForm.grade" label="等级" placeholder="如：A级" />
          <van-field v-model="addForm.quantity" label="数量(kg)" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.unitPrice" label="单价(元/kg)" type="number" placeholder="请输入" />
          <van-field v-model="addForm.storageDate" label="入库日期" placeholder="YYYY-MM-DD" />
          <van-field v-model="addForm.validUntil" label="有效期至" placeholder="YYYY-MM-DD" />
          <van-field v-model="addForm.traceCode" label="溯源码" placeholder="请输入" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 仓单详情 -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:65%;">
      <div style="padding:16px;" v-if="selectedReceipt">
        <h3 style="margin-bottom:12px;">{{ selectedReceipt.receiptNo }}</h3>
        <van-cell-group>
          <van-cell title="仓库名称" :value="selectedReceipt.warehouseName || '-'" />
          <van-cell title="货主" :value="selectedReceipt.ownerName || '-'" />
          <van-cell title="品种" :value="selectedReceipt.variety || '-'" />
          <van-cell title="等级" :value="selectedReceipt.grade || '-'" />
          <van-cell title="数量" :value="`${selectedReceipt.quantity} kg`" />
          <van-cell title="单价" :value="`¥${selectedReceipt.unitPrice || '-'}/kg`" />
          <van-cell title="总估值" :value="`¥${selectedReceipt.totalValue || '-'}`" />
          <van-cell title="批次编码" :value="selectedReceipt.batchCode || '-'" />
          <van-cell title="入库日期" :value="selectedReceipt.storageDate || '-'" />
          <van-cell title="有效期至" :value="selectedReceipt.validUntil || '-'" />
          <van-cell title="溯源码" :value="selectedReceipt.traceCode || '-'" />
          <van-cell title="状态">
            <template #value>
              <van-tag :type="statusTagType(selectedReceipt.status)">
                {{ statusLabel(selectedReceipt.status) }}
              </van-tag>
            </template>
          </van-cell>
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { warehouseApi } from '@/api/warehouse'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const showDetailPopup = ref(false)
const selectedReceipt = ref(null)
const activeStatus = ref('')

const query = reactive({ keyword: '', status: '', page: 1, size: 10 })
const addForm = reactive({
  warehouseId: '', ownerId: '', ownerName: '', batchCode: '',
  variety: '', grade: '', quantity: '', unitPrice: '',
  storageDate: '', validUntil: '', traceCode: ''
})

const statusMap = {
  VALID: { label: '有效', type: 'success' },
  PLEDGED: { label: '已质押', type: 'warning' },
  TRANSFERRED: { label: '已转让', type: 'primary' },
  CANCELLED: { label: '已注销', type: 'default' }
}

function statusLabel(s) { return statusMap[s]?.label || s }
function statusTagType(s) { return statusMap[s]?.type || 'default' }

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await warehouseApi.receiptList({ ...query })
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

function handleTabChange(val) {
  query.status = val
  loadList(true)
}

function showDetail(receipt) {
  selectedReceipt.value = receipt
  showDetailPopup.value = true
}

async function changeReceiptStatus(id, status) {
  try {
    await showConfirmDialog({ title: '确认', message: `确定执行"${statusLabel(status)}"操作吗？` })
    await warehouseApi.receiptChangeStatus(id, { status })
    showToast('操作成功')
    loadList(true)
  } catch {
    // user cancelled or API error
  }
}

async function handleCreate() {
  await warehouseApi.receiptCreate(addForm)
  showToast('仓单创建成功')
  showAddSheet.value = false
  Object.assign(addForm, {
    warehouseId: '', ownerId: '', ownerName: '', batchCode: '',
    variety: '', grade: '', quantity: '', unitPrice: '',
    storageDate: '', validUntil: '', traceCode: ''
  })
  loadList(true)
}

loadList(true)
</script>
