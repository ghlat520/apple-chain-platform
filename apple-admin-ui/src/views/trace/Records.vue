<template>
  <div class="page-container">
    <div class="page-header">
      <h2>链路记录</h2>
      <el-button type="primary" @click="openAddNode" icon="Plus">新增节点</el-button>
    </div>
    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索溯源码/批次号"
        clearable
        style="width:220px"
        @clear="loadData"
        @keyup.enter="loadData"
      />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:140px" @change="loadData">
        <el-option label="已种植" value="PLANTED" />
        <el-option label="已采收" value="HARVESTED" />
        <el-option label="入库中" value="IN_STORAGE" />
        <el-option label="运输中" value="IN_TRANSIT" />
        <el-option label="已销售" value="SOLD" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="traceCode" label="溯源码" width="200" />
      <el-table-column prop="productType" label="产品类型" width="120" />
      <el-table-column prop="batchNo" label="批次号" width="180" />
      <el-table-column label="当前状态" width="120">
        <template #default="{ row }">{{ chainStatusLabel(row.currentStatus) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewChain(row)">查看链路</el-button>
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

    <!-- Full Chain Drawer -->
    <el-drawer v-model="drawerVisible" :title="`溯源链路 — ${currentTraceCode}`" size="600px">
      <div v-loading="chainLoading">
        <div v-if="chainNodes.length === 0 && !chainLoading" style="text-align:center;padding:40px;color:var(--el-text-color-secondary)">
          暂无链路数据
        </div>
        <el-timeline v-else>
          <el-timeline-item
            v-for="(node, idx) in chainNodes"
            :key="idx"
            :timestamp="node.nodeTime"
            placement="top"
            :type="nodeTimelineType(node.nodeType)"
          >
            <el-card shadow="never" style="padding:8px 0">
              <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px">
                <el-tag size="small" :type="nodeTimelineType(node.nodeType)">
                  {{ nodeTypeLabel(node.nodeType) }}
                </el-tag>
                <span style="font-weight:600">{{ node.operatorName }}</span>
              </div>
              <div style="color:var(--el-text-color-secondary);font-size:13px">
                <span>{{ node.location }}</span>
                <span v-if="node.summary" style="margin-left:12px">{{ node.summary }}</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>

    <!-- Add Node Dialog -->
    <el-dialog v-model="nodeDialogVisible" title="新增溯源节点" width="480px">
      <el-form ref="nodeFormRef" :model="nodeForm" :rules="nodeRules" label-width="100px">
        <el-form-item label="溯源码" prop="traceCode">
          <el-input v-model="nodeForm.traceCode" placeholder="请输入溯源码" />
        </el-form-item>
        <el-form-item label="节点类型" prop="nodeType">
          <el-select v-model="nodeForm.nodeType" style="width:100%">
            <el-option label="种植" value="PLANT" />
            <el-option label="生长" value="GROW" />
            <el-option label="采收" value="HARVEST" />
            <el-option label="仓储" value="STORAGE" />
            <el-option label="物流" value="LOGISTICS" />
            <el-option label="交易" value="TRADE" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人" prop="operatorName">
          <el-input v-model="nodeForm.operatorName" />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="nodeForm.location" />
        </el-form-item>
        <el-form-item label="操作人ID" prop="operatorId">
          <el-input-number v-model="nodeForm.operatorId" :min="1" style="width:100%" placeholder="请输入操作人ID" />
        </el-form-item>
        <el-form-item label="节点时间" prop="nodeTime">
          <el-date-picker v-model="nodeForm.nodeTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择节点时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="nodeForm.summary" type="textarea" :rows="2" placeholder="节点摘要描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddNode" :loading="nodeSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { traceApi } from '@/api/trace.js'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: '' })

// Chain drawer
const drawerVisible = ref(false)
const chainLoading = ref(false)
const chainNodes = ref([])
const currentTraceCode = ref('')

// Add node dialog
const nodeDialogVisible = ref(false)
const nodeSubmitting = ref(false)
const nodeFormRef = ref()
const nodeForm = reactive({ traceCode: '', nodeType: '', operatorName: '', location: '', operatorId: null, nodeTime: '', summary: '' })
const nodeRules = {
  traceCode: [{ required: true, message: '请输入溯源码', trigger: 'blur' }],
  nodeType: [{ required: true, message: '请选择节点类型', trigger: 'change' }],
  operatorName: [{ required: true, message: '请输入操作人', trigger: 'blur' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
}

function nodeTimelineType(type) {
  const map = {
    PLANT: 'success',
    GROW: 'primary',
    HARVEST: 'success',
    STORAGE: 'warning',
    LOGISTICS: 'info',
    TRADE: 'primary',
  }
  return map[type] || 'info'
}

function nodeTypeLabel(type) {
  const map = { PLANT: '种植', GROW: '生长', HARVEST: '采收', STORAGE: '仓储', LOGISTICS: '物流', TRADE: '交易' }
  return map[type] || type || '-'
}

function chainStatusLabel(status) {
  const map = { PLANTED: '已种植', HARVESTED: '已采收', IN_STORAGE: '入库中', IN_TRANSIT: '运输中', SOLD: '已销售' }
  return map[status] || status || '-'
}

async function loadData() {
  loading.value = true
  try {
    const res = await traceApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function viewChain(row) {
  currentTraceCode.value = row.traceCode
  chainNodes.value = []
  drawerVisible.value = true
  chainLoading.value = true
  try {
    const res = await traceApi.scan(row.traceCode)
    chainNodes.value = res?.timeline || res?.nodes || []
  } catch (e) {
    chainNodes.value = []
  } finally {
    chainLoading.value = false
  }
}

function openAddNode() {
  Object.assign(nodeForm, { traceCode: '', nodeType: '', operatorName: '', location: '', operatorId: null, nodeTime: '', summary: '' })
  nodeDialogVisible.value = true
}

async function handleAddNode() {
  const valid = await nodeFormRef.value.validate().catch(() => false)
  if (!valid) return
  nodeSubmitting.value = true
  try {
    await traceApi.addNode(nodeForm)
    ElMessage.success('节点添加成功')
    nodeDialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    nodeSubmitting.value = false
  }
}

onMounted(loadData)
</script>
