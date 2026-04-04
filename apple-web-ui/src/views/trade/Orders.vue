<template>
  <div class="orders-page">
    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="待确认" name="PENDING" />
        <van-tab title="已确认" name="CONFIRMED" />
        <van-tab title="已发货" name="SHIPPED" />
        <van-tab title="已完成" name="COMPLETED" />
        <van-tab title="已取消" name="CANCELLED" />
      </van-tabs>
    </div>

    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openAddDrawer">新增订单</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell is-link @click="openDetailDrawer(item)">
            <template #title>
              <span>{{ item.orderNo || `订单 #${item.id}` }}</span>
            </template>
            <template #label>
              品种: {{ item.variety || '-' }} · 数量: {{ item.quantity || '-' }}kg · ¥{{ item.totalAmount || '-' }}
            </template>
            <template #value>
              <van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag>
            </template>
          </van-cell>
          <van-cell label="" style="padding-top:0">
            <template #title>
              <span style="font-size:12px;color:#969799">买方: {{ item.buyerName || item.buyerId || '-' }} · 果园: {{ item.orchardName || '-' }}</span>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无订单" />
      </van-list>
    </van-pull-refresh>

    <!-- Add Order Drawer -->
    <van-popup
      v-model:show="showAddDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>新增订单</span>
        <van-icon name="cross" @click="showAddDrawer = false" />
      </div>
      <van-form @submit="handleCreate" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.buyerName"
            label="买方姓名"
            placeholder="请输入买方姓名"
            :rules="[{ required: true, message: '请填写买方姓名' }]"
          />
          <van-field
            v-model="form.buyerPhone"
            label="买方电话"
            placeholder="请输入买方联系电话"
          />
          <van-field
            v-model="form.orchardName"
            label="果园名称"
            placeholder="请输入来源果园名称"
          />
          <van-field
            v-model="form.batchCode"
            label="批次编号"
            placeholder="请输入批次编号"
          />
          <van-field
            v-model="form.variety"
            label="苹果品种"
            placeholder="请输入苹果品种"
            :rules="[{ required: true, message: '请填写苹果品种' }]"
          />
          <van-field
            v-model.number="form.quantity"
            label="数量(kg)"
            type="number"
            placeholder="请输入购买数量"
            :rules="[{ required: true, message: '请填写数量' }]"
          />
          <van-field
            v-model.number="form.unitPrice"
            label="单价(元/kg)"
            type="number"
            placeholder="请输入单价"
            :rules="[{ required: true, message: '请填写单价' }]"
          />
          <van-field
            v-model="form.remark"
            label="备注"
            type="textarea"
            rows="2"
            autosize
            placeholder="请输入备注"
          />
        </van-cell-group>
        <div class="drawer-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">确认下单</van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- Detail Drawer -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>订单详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="订单号" :value="currentItem.orderNo || `#${currentItem.id}`" />
        <van-cell title="状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="买方姓名" :value="currentItem.buyerName || '-'" />
        <van-cell title="买方电话" :value="currentItem.buyerPhone || '-'" />
        <van-cell title="果园名称" :value="currentItem.orchardName || '-'" />
        <van-cell title="批次编号" :value="currentItem.batchCode || '-'" />
        <van-cell title="苹果品种" :value="currentItem.variety || '-'" />
        <van-cell title="等级" :value="currentItem.grade || '-'" />
        <van-cell title="数量(kg)" :value="currentItem.quantity ?? '-'" />
        <van-cell title="单价(元/kg)" :value="currentItem.unitPrice ?? '-'" />
        <van-cell title="总金额(元)" :value="currentItem.totalAmount ?? '-'" />
        <van-cell title="支付状态" :value="currentItem.paymentStatus || '-'" />
        <van-cell title="备注" :value="currentItem.remark || '-'" />
        <van-cell title="创建时间" :value="currentItem.createTime || '-'" />
      </van-cell-group>

      <!-- Status Transitions -->
      <div class="status-actions" v-if="currentItem">
        <p class="status-title">状态操作</p>
        <div class="status-btn-group">
          <van-button
            v-for="action in availableActions(currentItem.status)"
            :key="action.status"
            :type="action.type"
            size="small"
            plain
            :loading="transitioning"
            @click="handleStatusChange(currentItem, action.status)"
          >
            {{ action.label }}
          </van-button>
        </div>
        <van-button
          block type="danger" plain size="small"
          style="margin-top:8px"
          @click="handleDelete(currentItem)"
        >
          删除订单
        </van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { tradeApi } from '@/api/trade.js'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddDrawer = ref(false)
const showDetail = ref(false)
const submitting = ref(false)
const transitioning = ref(false)
const currentItem = ref(null)

const query = reactive({ status: '', keyword: '', page: 1, pageSize: 10 })
const form = reactive({ buyerName: '', buyerPhone: '', orchardName: '', batchCode: '', variety: '', quantity: '', unitPrice: '', remark: '' })

const STATUS_TRANSITIONS = {
  PENDING: [
    { status: 'CONFIRMED', label: '确认订单', type: 'primary' },
    { status: 'CANCELLED', label: '取消订单', type: 'danger' }
  ],
  CONFIRMED: [
    { status: 'SHIPPED', label: '标记发货', type: 'warning' },
    { status: 'CANCELLED', label: '取消订单', type: 'danger' }
  ],
  SHIPPED: [
    { status: 'COMPLETED', label: '确认收货', type: 'success' }
  ],
  COMPLETED: [],
  CANCELLED: []
}

function availableActions(status) {
  return STATUS_TRANSITIONS[status] || []
}

function statusTagType(status) {
  const map = { PENDING: 'warning', CONFIRMED: 'primary', SHIPPED: 'warning', COMPLETED: 'success', CANCELLED: 'default' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { PENDING: '待确认', CONFIRMED: '已确认', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status || '-'
}

function resetForm() {
  Object.assign(form, { buyerName: '', buyerPhone: '', orchardName: '', batchCode: '', variety: '', quantity: '', unitPrice: '', remark: '' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await tradeApi.getOrders({ ...query, page: 1 })
    const records = res?.records || res || []
    list.value = records
    finished.value = records.length >= (res?.total || records.length)
    query.page = 1
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const res = await tradeApi.getOrders({ ...query })
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.pageSize) {
      finished.value = true
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  finished.value = false
  list.value = []
  loadList()
}

function openAddDrawer() {
  resetForm()
  showAddDrawer.value = true
}

function openDetailDrawer(item) {
  currentItem.value = item
  showDetail.value = true
}

async function handleCreate() {
  submitting.value = true
  try {
    await tradeApi.createOrder(form)
    showToast({ type: 'success', message: '订单已创建' })
    showAddDrawer.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(item, newStatus) {
  const labelMap = { CONFIRMED: '确认', SHIPPED: '发货', COMPLETED: '完成', CANCELLED: '取消' }
  try {
    await showConfirmDialog({
      title: '状态变更',
      message: `确认将订单 #${item.id} 状态变更为「${statusLabel(newStatus)}」？`
    })
    transitioning.value = true
    await tradeApi.updateOrderStatus(item.id, newStatus)
    showToast({ type: 'success', message: `已${labelMap[newStatus] || '更新'}` })
    showDetail.value = false
    loadList()
  } catch (e) {
    // user cancelled
  } finally {
    transitioning.value = false
  }
}

async function handleDelete(item) {
  try {
    await showConfirmDialog({ title: '确认删除', message: `确认删除订单 #${item.id}？` })
    await tradeApi.deleteOrder(item.id)
    showToast({ type: 'success', message: '删除成功' })
    showDetail.value = false
    loadList()
  } catch (e) {
    // user cancelled
  }
}

async function handleExport() {
  showToast('正在导出...')
  try {
    await tradeApi.exportOrders()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.orders-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
.status-actions { padding: 16px; border-top: 1px solid #ebedf0; margin-top: 8px; }
.status-title { font-size: 13px; color: #646566; margin-bottom: 10px; }
.status-btn-group { display: flex; flex-wrap: wrap; gap: 8px; }
</style>
