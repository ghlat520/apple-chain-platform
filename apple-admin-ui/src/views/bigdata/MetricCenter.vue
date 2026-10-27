<template>
  <div class="page-container">
    <div class="page-header">
      <h2>指标中心</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增指标</el-button>
    </div>
    <div class="filter-bar">
      <el-select v-model="query.category" placeholder="分类" clearable style="width:140px" @change="loadData">
        <el-option label="种植" value="PLANTING" />
        <el-option label="交易" value="TRADE" />
        <el-option label="仓储" value="WAREHOUSE" />
        <el-option label="物流" value="LOGISTICS" />
        <el-option label="金融" value="FINANCE" />
        <el-option label="溯源" value="TRACE" />
        <el-option label="种植" value="种植" />
        <el-option label="交易" value="交易" />
        <el-option label="仓储" value="仓储" />
        <el-option label="物流" value="物流" />
        <el-option label="金融" value="金融" />
        <el-option label="溯源" value="溯源" />
        <el-option label="数据治理" value="数据治理" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="metricCode" label="指标编码" width="180" />
      <el-table-column prop="metricName" label="名称" width="200" />
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{ row }">
          <el-tag :type="categoryType(row.category)" size="small">{{ categoryLabel(row.category) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="isCore" label="核心" width="70">
        <template #default="{ row }">
          <el-icon v-if="row.isCore === 1" color="#F57C00" :size="18"><Star /></el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="success" size="small" @click="viewTrend(row)">趋势</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total > 0" style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next" :total="total" :page-size="query.size" :current-page="query.page" @current-change="(p) => { query.page = p; loadData() }" />

    <!-- Trend Dialog -->
    <el-dialog v-model="trendVisible" :title="`指标趋势 - ${trendMetric.metricName}`" width="700px">
      <div style="margin-bottom:12px">
        <el-radio-group v-model="trendDays" @change="loadTrendData">
          <el-radio-button :value="7">近7天</el-radio-button>
          <el-radio-button :value="30">近30天</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="trendChartRef" style="height:350px"></div>
    </el-dialog>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑指标' : '新增指标'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="指标编码" prop="metricCode">
          <el-input v-model="form.metricCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="名称" prop="metricName">
          <el-input v-model="form.metricName" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="种植" value="PLANTING" />
            <el-option label="交易" value="TRADE" />
            <el-option label="仓储" value="WAREHOUSE" />
            <el-option label="物流" value="LOGISTICS" />
            <el-option label="金融" value="FINANCE" />
            <el-option label="溯源" value="TRACE" />
            <el-option label="数据治理" value="数据治理" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" style="width:200px" />
        </el-form-item>
        <el-form-item label="核心指标">
          <el-switch v-model="form.isCore" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { metricApi } from '@/api/bigdata.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, category: '' })

const trendVisible = ref(false)
const trendMetric = ref({})
const trendDays = ref(7)
const trendChartRef = ref()

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, metricCode: '', metricName: '', category: 'PLANTING', unit: '', isCore: 0, remark: '' })
const formRules = {
  metricCode: [{ required: true, message: '请输入指标编码', trigger: 'blur' }],
  metricName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

function categoryLabel(c) {
  const map = { PLANTING: '种植', TRADE: '交易', WAREHOUSE: '仓储', LOGISTICS: '物流', FINANCE: '金融', TRACE: '溯源' }
  return map[c] || c
}

function categoryType(c) {
  const map = { PLANTING: 'success', TRADE: 'primary', WAREHOUSE: 'warning', LOGISTICS: 'danger', FINANCE: 'warning', TRACE: 'info', '种植': 'success', '交易': 'primary', '仓储': 'warning', '物流': 'danger', '金融': 'warning', '溯源': 'info', '数据治理': 'primary' }
  return map[c] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await metricApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

async function viewTrend(row) {
  trendMetric.value = row
  trendVisible.value = true
  await nextTick()
  loadTrendData()
}

async function loadTrendData() {
  try {
    const to = new Date()
    const from = new Date()
    from.setDate(from.getDate() - trendDays.value)
    const formatDate = (d) => d.toISOString().split('T')[0]
    const res = await metricApi.values(trendMetric.value.metricCode, { from: formatDate(from), to: formatDate(to) })
    const data = res?.records || res?.list || res || []
    await nextTick()
    if (!trendChartRef.value) return
    const chart = echarts.init(trendChartRef.value)
    const dates = data.map(d => d.statDate)
    const values = data.map(d => d.metricValue)
    chart.setOption({
      color: ['#D32F2F'],
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: [{ type: 'line', data: values, smooth: true, areaStyle: { opacity: 0.15 } }]
    })
  } catch (e) {}
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, metricCode: '', metricName: '', category: 'PLANTING', unit: '', isCore: 0, remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await metricApi.update(form.id, form)
    } else {
      await metricApi.create(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {} finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除指标 ${row.metricName}?`, '提示', { type: 'warning' })
  try {
    await metricApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

onMounted(loadData)
</script>
