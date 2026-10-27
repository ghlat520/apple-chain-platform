<template>
  <div class="page-container">
    <div class="page-header">
      <h2>交易订单</h2>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索订单号" clearable style="width:220px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="订单状态" clearable style="width:130px" @change="loadData">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已确认" value="CONFIRMED" />
        <el-option label="已发货" value="DELIVERED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="buyerId" label="买家ID" width="110" class-name="nums-tabular" />
      <el-table-column prop="farmerId" label="农户ID" width="110" class-name="nums-tabular" />
      <el-table-column prop="variety" label="品种" width="110" />
      <el-table-column prop="quantity" label="数量(kg)" width="110" class-name="nums-tabular" />
      <el-table-column prop="unitPrice" label="单价(元/kg)" width="120" class-name="nums-tabular" />
      <el-table-column prop="totalAmount" label="总金额(元)" width="130" class-name="nums-tabular" />
      <el-table-column prop="tradeDate" label="交易日期" width="120" />
      <el-table-column prop="deliveryDate" label="交货日期" width="120" />
      <el-table-column prop="orderStatus" label="订单状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.orderStatus)" size="small">{{ orderStatusLabel(row.orderStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paymentStatus" label="支付状态" width="100">
        <template #default="{ row }">
          <el-tag :type="paymentStatusType(row.paymentStatus)" size="small">{{ paymentStatusLabel(row.paymentStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button
              v-if="row.orderStatus === 'DRAFT'"
              link type="primary" size="small"
              @click="handleAction('confirm', row)"
            >确认</el-button>
            <el-button
              v-if="row.orderStatus === 'CONFIRMED'"
              link type="warning" size="small"
              @click="handleAction('deliver', row)"
            >发货</el-button>
            <el-button
              v-if="row.orderStatus === 'DELIVERED'"
              link type="success" size="small"
              @click="handleAction('complete', row)"
            >完成</el-button>
            <el-button
              v-if="row.orderStatus !== 'COMPLETED' && row.orderStatus !== 'CANCELLED'"
              link type="danger" size="small"
              @click="handleAction('cancel', row)"
            >取消</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="total > 0"
      style="margin-top:16px;justify-content:flex-end"
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="(p) => { query.page = p; loadData() }"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { orderApi } from '@/api/trade.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: '' })

function statusType(status) {
  const map = {
    DRAFT: 'info',
    CONFIRMED: 'primary',
    DELIVERED: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger',
  }
  return map[status] || 'info'
}

function orderStatusLabel(status) {
  const map = { DRAFT: '草稿', CONFIRMED: '已确认', DELIVERED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status || '-'
}

function paymentStatusType(status) {
  const map = { PENDING: 'warning', PAID: 'success', REFUNDED: 'danger' }
  return map[status] || 'info'
}

function paymentStatusLabel(status) {
  const map = { PENDING: '待支付', PAID: '已支付', REFUNDED: '已退款' }
  return map[status] || status || '-'
}

const actionLabelMap = {
  confirm: '确认',
  deliver: '发货',
  complete: '完成',
  cancel: '取消',
}

async function loadData() {
  loading.value = true
  try {
    const res = await orderApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleAction(action, row) {
  const label = actionLabelMap[action]
  await ElMessageBox.confirm(`确认对订单 ${row.orderNo} 执行「${label}」操作?`, '提示', { type: 'warning' })
  try {
    await orderApi[action](row.id)
    ElMessage.success(`${label}成功`)
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
