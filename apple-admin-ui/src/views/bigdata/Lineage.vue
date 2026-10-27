<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据血缘</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增关系</el-button>
    </div>
    <div class="filter-bar">
      <el-select v-model="query.relationType" placeholder="关系类型" clearable style="width:180px" @change="loadData">
        <el-option label="DERIVES_FROM" value="DERIVES_FROM" />
        <el-option label="TRANSFORMS" value="TRANSFORMS" />
        <el-option label="AGGREGATES" value="AGGREGATES" />
        <el-option label="REFERENCES" value="REFERENCES" />
        <el-option label="JOIN" value="JOIN" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="upstreamName" label="上游实体" width="220">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.upstreamType }}</el-tag>
          {{ row.upstreamName }}
          <div style="color:#909399;font-size:12px">{{ row.upstreamId }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="downstreamName" label="下游实体" width="220">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.downstreamType }}</el-tag>
          {{ row.downstreamName }}
          <div style="color:#909399;font-size:12px">{{ row.downstreamId }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="relationType" label="关系类型" width="160">
        <template #default="{ row }">
          <el-tag :type="relationTypeColor(row.relationType)" size="small">{{ row.relationType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total > 0" style="margin-top:16px;justify-content:flex-end" background layout="total, prev, pager, next" :total="total" :page-size="query.size" :current-page="query.page" @current-change="(p) => { query.page = p; loadData() }" />

    <el-dialog v-model="dialogVisible" title="新增血缘关系" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="上游ID" prop="upstreamId">
          <el-input v-model="form.upstreamId" />
        </el-form-item>
        <el-form-item label="上游名称" prop="upstreamName">
          <el-input v-model="form.upstreamName" />
        </el-form-item>
        <el-form-item label="下游ID" prop="downstreamId">
          <el-input v-model="form.downstreamId" />
        </el-form-item>
        <el-form-item label="下游名称" prop="downstreamName">
          <el-input v-model="form.downstreamName" />
        </el-form-item>
        <el-form-item label="关系类型" prop="relationType">
          <el-select v-model="form.relationType" style="width:100%">
            <el-option label="DERIVES_FROM" value="DERIVES_FROM" />
            <el-option label="TRANSFORMS" value="TRANSFORMS" />
            <el-option label="AGGREGATES" value="AGGREGATES" />
            <el-option label="REFERENCES" value="REFERENCES" />
            <el-option label="JOIN" value="JOIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
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
import { lineageApi } from '@/api/bigdata.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, relationType: '' })

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ upstreamId: '', upstreamName: '', downstreamId: '', downstreamName: '', upstreamType: 'TABLE', downstreamType: 'TABLE', relationType: 'DERIVES_FROM', description: '' })
const formRules = {
  upstreamId: [{ required: true, message: '请输入上游ID', trigger: 'blur' }],
  upstreamName: [{ required: true, message: '请输入上游名称', trigger: 'blur' }],
  downstreamId: [{ required: true, message: '请输入下游ID', trigger: 'blur' }],
  downstreamName: [{ required: true, message: '请输入下游名称', trigger: 'blur' }],
  relationType: [{ required: true, message: '请选择关系类型', trigger: 'change' }]
}

function relationTypeColor(type) {
  return { DERIVES_FROM: 'primary', TRANSFORMS: 'success', AGGREGATES: 'warning', REFERENCES: 'danger', JOIN: 'info' }[type] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    // Use list API (downstream/upstream return 500)
    const res = await lineageApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { upstreamId: '', upstreamName: '', downstreamId: '', downstreamName: '', upstreamType: 'TABLE', downstreamType: 'TABLE', relationType: 'DERIVES_FROM', description: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await lineageApi.create(form)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {} finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除此血缘关系?', '提示', { type: 'warning' })
  try {
    await lineageApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

onMounted(loadData)
</script>
