<template>
  <div class="page-container">
    <div class="page-header">
      <h2>采购需求</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增需求</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索需求编号" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.variety" placeholder="品种" clearable style="width:140px" @change="loadData">
        <el-option label="红富士" value="红富士" />
        <el-option label="嘎啦" value="嘎啦" />
        <el-option label="黄金维纳斯" value="黄金维纳斯" />
        <el-option label="秦冠" value="秦冠" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px" @change="loadData">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="已匹配" value="MATCHED" />
        <el-option label="已关闭" value="CLOSED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="needNo" label="需求编号" width="180" />
      <el-table-column prop="buyerId" label="买家ID" width="120" class-name="nums-tabular" />
      <el-table-column prop="variety" label="品种" width="120" />
      <el-table-column prop="quantity" label="需求数量(kg)" width="140" class-name="nums-tabular" />
      <el-table-column prop="priceMax" label="最高价(元/kg)" width="140" class-name="nums-tabular" />
      <el-table-column prop="requireDate" label="需求日期" width="120" />
      <el-table-column prop="quality" label="质量等级" width="100" />
      <el-table-column prop="deliveryAddr" label="交货地址" width="160" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button
              v-if="row.status === 'DRAFT'"
              link type="success" size="small"
              @click="handlePublish(row)"
            >发布</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑采购需求' : '新增采购需求'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="买家ID" prop="buyerId">
          <el-input-number v-model="form.buyerId" :min="1" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="品种" prop="variety">
          <el-select v-model="form.variety" style="width:100%">
            <el-option label="红富士" value="红富士" />
            <el-option label="嘎啦" value="嘎啦" />
            <el-option label="黄金维纳斯" value="黄金维纳斯" />
            <el-option label="秦冠" value="秦冠" />
          </el-select>
        </el-form-item>
        <el-form-item label="需求数量(kg)" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="最高价(元/kg)" prop="priceMax">
          <el-input-number v-model="form.priceMax" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="需求日期">
          <el-date-picker v-model="form.requireDate" type="date" value-format="YYYY-MM-DD" placeholder="选择需求日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="质量等级">
          <el-select v-model="form.quality" style="width:100%" clearable>
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="交货地址">
          <el-input v-model="form.deliveryAddr" placeholder="请输入交货地址" />
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
import { needApi } from '@/api/trade.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', variety: '', status: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, buyerId: null, variety: '', quantity: 0, priceMax: 0, requireDate: '', quality: '', deliveryAddr: '', description: '' })
const formRules = {
  buyerId: [{ required: true, message: '请输入买家ID', trigger: 'blur' }],
  variety: [{ required: true, message: '请选择品种', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入需求数量', trigger: 'blur' }],
  priceMax: [{ required: true, message: '请输入最高价', trigger: 'blur' }],
}

function statusType(status) {
  const map = { DRAFT: 'info', PUBLISHED: 'success', MATCHED: 'warning', CLOSED: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { DRAFT: '草稿', PUBLISHED: '已发布', MATCHED: '已匹配', CLOSED: '已关闭' }
  return map[status] || status || '-'
}

async function loadData() {
  loading.value = true
  try {
    const res = await needApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, buyerId: null, variety: '', quantity: 0, priceMax: 0, requireDate: '', quality: '', deliveryAddr: '', description: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    buyerId: row.buyerId,
    variety: row.variety,
    quantity: row.quantity,
    priceMax: row.priceMax,
    requireDate: row.requireDate || '',
    quality: row.quality || '',
    deliveryAddr: row.deliveryAddr || '',
    description: row.description || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await needApi.update(form.id, form)
    } else {
      await needApi.create(form)
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

async function handlePublish(row) {
  await ElMessageBox.confirm(`确认发布采购需求 ${row.needNo}?`, '提示', { type: 'warning' })
  try {
    await needApi.publish(row.id)
    ElMessage.success('发布成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除采购需求 ${row.needNo}?`, '提示', { type: 'warning' })
  try {
    await needApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
