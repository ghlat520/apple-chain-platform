<template>
  <div class="page-container">
    <div class="page-header">
      <h2>大屏配置</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增大屏</el-button>
    </div>
    <div class="filter-bar">
      <el-select v-model="query.category" placeholder="分类" clearable style="width:140px" @change="loadData">
        <el-option label="物流" value="LOGISTICS" />
        <el-option label="交易" value="TRADE" />
        <el-option label="种植" value="PLANTING" />
        <el-option label="总览" value="OVERVIEW" />
        <el-option label="政府" value="GOV" />
        <el-option label="运营" value="OPS" />
        <el-option label="产业" value="INDUSTRY" />
        <el-option label="自定义" value="CUSTOM" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="dashboardCode" label="编码" width="160" />
      <el-table-column prop="dashboardName" label="名称" width="200" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="theme" label="主题" width="100" />
      <el-table-column label="可见性" width="100">
        <template #default="{ row }">
          <el-tag :type="row.visibility === 'PUBLIC' ? 'success' : 'info'" size="small">{{ row.visibility }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布" width="80">
        <template #default="{ row }">
          <el-tag :type="row.published ? 'success' : 'info'" size="small">{{ row.published ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="refreshSeconds" label="刷新(s)" width="100" class-name="nums-tabular" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewWidgets(row)">组件</el-button>
            <el-button v-if="!row.published" link type="success" size="small" @click="togglePublish(row)">发布</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- Widgets Drawer -->
    <el-drawer v-model="widgetsVisible" title="组件列表" size="600px">
      <el-table :data="widgetsData" stripe border v-loading="widgetsLoading">
        <el-table-column prop="widgetName" label="组件名" width="160" />
        <el-table-column prop="widgetType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.widgetType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataSource" label="数据源" width="180" />
        <el-table-column prop="positionJson" label="位置" />
      </el-table>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑大屏' : '新增大屏'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="编码" prop="dashboardCode">
          <el-input v-model="form.dashboardCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="名称" prop="dashboardName">
          <el-input v-model="form.dashboardName" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="物流" value="LOGISTICS" />
            <el-option label="交易" value="TRADE" />
            <el-option label="种植" value="PLANTING" />
            <el-option label="总览" value="OVERVIEW" />
            <el-option label="政府" value="GOV" />
            <el-option label="运营" value="OPS" />
            <el-option label="产业" value="INDUSTRY" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="主题">
          <el-select v-model="form.theme" style="width:100%">
            <el-option label="dark" value="dark" />
            <el-option label="light" value="light" />
            <el-option label="blue" value="blue" />
          </el-select>
        </el-form-item>
        <el-form-item label="可见性">
          <el-select v-model="form.visibility" style="width:100%">
            <el-option label="公开" value="PUBLIC" />
            <el-option label="私有" value="PRIVATE" />
            <el-option label="按角色" value="ROLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="刷新间隔">
          <el-input-number v-model="form.refreshSeconds" :min="5" :max="300" /> 秒
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
import { ref, reactive, onMounted } from 'vue'
import { screenApi } from '@/api/bigdata.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, category: '' })

const widgetsVisible = ref(false)
const widgetsLoading = ref(false)
const widgetsData = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  dashboardCode: '',
  dashboardName: '',
  category: 'OPS',
  theme: 'dark',
  visibility: 'PUBLIC',
  refreshSeconds: 30
})
const formRules = {
  dashboardCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  dashboardName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await screenApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function viewWidgets(row) {
  widgetsVisible.value = true
  widgetsLoading.value = true
  try {
    const res = await screenApi.widgets(row.id)
    widgetsData.value = res || []
  } catch (e) {
    widgetsData.value = []
  } finally {
    widgetsLoading.value = false
  }
}

async function togglePublish(row) {
  try {
    await screenApi.publish(row.id)
    ElMessage.success('已发布')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    dashboardCode: '',
    dashboardName: '',
    category: 'OPS',
    theme: 'dark',
    visibility: 'PUBLIC',
    refreshSeconds: 30
  })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    dashboardCode: row.dashboardCode,
    dashboardName: row.dashboardName,
    category: row.category || 'OPS',
    theme: row.theme || 'dark',
    visibility: row.visibility || 'PUBLIC',
    refreshSeconds: row.refreshSeconds || 30
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await screenApi.update(form.id, form)
    } else {
      await screenApi.create(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除大屏 ${row.dashboardName}?`, '提示', { type: 'warning' })
  try {
    await screenApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
