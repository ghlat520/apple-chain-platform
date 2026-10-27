<template>
  <div class="page-container">
    <div class="page-header">
      <h2>分析报告</h2>
      <el-button type="primary" @click="openGenerate" icon="Document">生成报告</el-button>
    </div>
    <div class="filter-bar">
      <el-button type="primary" @click="loadData" icon="Search">刷新</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="reportNo" label="报告编号" width="180" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="reportType" label="类型" width="120">
        <template #default="{ row }">
          <el-tag size="small">{{ row.reportType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="period" label="周期" width="120" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="generatedTime" label="生成时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status !== 'PUBLISHED'" link type="success" size="small" @click="handlePublish(row)">发布</el-button>
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

    <!-- Detail Drawer -->
    <el-drawer v-model="drawerVisible" title="报告详情" size="600px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="报告编号">{{ currentRow.reportNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ currentRow.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ currentRow.reportType }}</el-descriptions-item>
          <el-descriptions-item label="周期">{{ currentRow.period }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(currentRow.status)" size="small">{{ currentRow.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="生成时间">{{ currentRow.generatedTime }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="currentRow.summary" style="margin-top:16px">
          <h4>摘要</h4>
          <div style="white-space:pre-wrap;color:#606266">{{ currentRow.summary }}</div>
        </div>
        <div v-if="currentRow.plantingSection" style="margin-top:16px">
          <h4>种植板块</h4>
          <div style="white-space:pre-wrap;color:#606266">{{ currentRow.plantingSection }}</div>
        </div>
        <div v-if="currentRow.tradeSection" style="margin-top:16px">
          <h4>交易板块</h4>
          <div style="white-space:pre-wrap;color:#606266">{{ currentRow.tradeSection }}</div>
        </div>
        <div v-if="currentRow.warehouseSection" style="margin-top:16px">
          <h4>仓储板块</h4>
          <div style="white-space:pre-wrap;color:#606266">{{ currentRow.warehouseSection }}</div>
        </div>
        <div v-if="currentRow.financeSection" style="margin-top:16px">
          <h4>金融板块</h4>
          <div style="white-space:pre-wrap;color:#606266">{{ currentRow.financeSection }}</div>
        </div>
      </template>
    </el-drawer>

    <!-- Generate Dialog -->
    <el-dialog v-model="generateVisible" title="生成报告" width="400px">
      <el-form :model="generateForm" label-width="80px">
        <el-form-item label="类型">
          <el-select v-model="generateForm.type" style="width:100%">
            <el-option label="周报" value="WEEKLY" />
            <el-option label="月报" value="MONTHLY" />
            <el-option label="季报" value="SEASONAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期">
          <el-input v-model="generateForm.period" placeholder="如: 2026-04 或 2026-W15" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerate" :loading="generating">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { reportApi } from '@/api/bigdata.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const generating = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20 })

const drawerVisible = ref(false)
const currentRow = ref(null)
const generateVisible = ref(false)
const generateForm = reactive({ type: 'WEEKLY', period: '' })

function statusType(s) {
  return { DRAFT: 'info', PUBLISHED: 'success' }[s] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await reportApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function viewDetail(row) {
  try {
    const data = await reportApi.get(row.id)
    currentRow.value = data || row
  } catch (e) {
    currentRow.value = row
  }
  drawerVisible.value = true
}

function openGenerate() {
  Object.assign(generateForm, { type: 'WEEKLY', period: '' })
  generateVisible.value = true
}

async function handleGenerate() {
  if (!generateForm.period) {
    ElMessage.warning('请输入报告周期')
    return
  }
  generating.value = true
  try {
    await reportApi.generate({ type: generateForm.type, period: generateForm.period })
    ElMessage.success('报告生成中...')
    generateVisible.value = false
    setTimeout(loadData, 2000)
  } catch (e) {
    // error shown by interceptor
  } finally {
    generating.value = false
  }
}

async function handlePublish(row) {
  await ElMessageBox.confirm(`确认发布报告 ${row.title}?`, '提示', { type: 'warning' })
  try {
    await reportApi.publish(row.id)
    ElMessage.success('发布成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
