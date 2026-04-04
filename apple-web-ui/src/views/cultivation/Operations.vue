<template>
  <div class="operations-page">
    <!-- Batch Filter -->
    <div class="top-filter">
      <van-field
        v-model="query.batchId"
        label="批次ID"
        placeholder="输入批次ID过滤"
        clearable
        @blur="handleSearch"
      />
      <van-field label="作业类型">
        <template #input>
          <van-dropdown-menu>
            <van-dropdown-item v-model="query.operationType" :options="operationTypeOptions" @change="handleSearch" />
          </van-dropdown-menu>
        </template>
      </van-field>
    </div>

    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openAddDrawer">新增作业</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell
            :title="item.operationType || '未知作业'"
            :label="`操作人: ${item.operator || '-'} · 日期: ${item.operationDate || '-'}`"
            is-link
            @click="openDetailDrawer(item)"
          >
            <template #value>
              <van-tag type="primary">批次{{ item.batchId }}</van-tag>
            </template>
          </van-cell>
          <van-cell
            v-if="item.remark"
            :label="item.remark"
            style="color:#969799;font-size:12px"
          />
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无作业记录" />
      </van-list>
    </van-pull-refresh>

    <!-- Add/Edit Drawer -->
    <van-popup
      v-model:show="showDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>{{ editId ? '编辑作业' : '新增作业' }}</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.batchId"
            label="批次ID"
            placeholder="请输入批次ID"
            :rules="[{ required: true, message: '请填写批次ID' }]"
          />
          <van-field
            v-model="form.operationType"
            label="作业类型"
            placeholder="如：施肥/灌溉/修剪/打药"
            :rules="[{ required: true, message: '请填写作业类型' }]"
          />
          <van-field
            v-model="form.operationDate"
            label="作业日期"
            placeholder="例：2024-06-15"
            :rules="[{ required: true, message: '请填写作业日期' }]"
          />
          <van-field
            v-model="form.operator"
            label="操作人"
            placeholder="请输入操作人姓名"
            :rules="[{ required: true, message: '请填写操作人' }]"
          />
          <van-field
            v-model="form.materials"
            label="使用材料"
            placeholder="请输入使用的材料/农药等"
            type="textarea"
            rows="2"
            autosize
          />
          <van-field
            v-model="form.remark"
            label="备注"
            type="textarea"
            placeholder="请输入备注信息"
            rows="2"
            autosize
          />
        </van-cell-group>
        <div class="drawer-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">
            {{ editId ? '保存修改' : '确认新增' }}
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- Detail Popup -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ maxHeight: '85vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>作业详情</span>
        <van-icon name="cross" @click="closeDetail" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="批次ID" :value="currentItem.batchId ?? '-'" />
        <van-cell title="作业类型" :value="currentItem.operationType || '-'" />
        <van-cell title="作业日期" :value="currentItem.operationDate || '-'" />
        <van-cell title="操作人" :value="currentItem.operator || '-'" />
        <van-cell title="使用材料" :value="currentItem.materials || '-'" />
        <van-cell title="备注" :value="currentItem.remark || '-'" />
        <van-cell title="创建时间" :value="currentItem.createTime || '-'" />
      </van-cell-group>
      <van-divider content-position="left">关联农资使用</van-divider>
      <van-cell-group inset v-if="usageRecords.length > 0">
        <van-cell
          v-for="(record, index) in usageRecords"
          :key="index"
          :title="record.productName || '-'"
          :label="`用量: ${record.quantity ?? '-'} ${record.unit || ''} · 日期: ${record.usageDate || '-'}`"
        >
          <template #value>
            <van-tag type="success" v-if="record.method">{{ record.method }}</van-tag>
          </template>
        </van-cell>
      </van-cell-group>
      <div v-else class="empty-usage-hint">暂无关联农资记录</div>
      <div class="drawer-actions">
        <van-button block type="primary" plain @click="openEditDrawer(currentItem)">编辑</van-button>
        <van-button block type="danger" plain @click="handleDelete(currentItem)" style="margin-top:8px">删除</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { cultivationApi } from '@/api/cultivation.js'
import { inputApi } from '@/api/input.js'
import { showToast, showConfirmDialog } from 'vant'

const route = useRoute()
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showDrawer = ref(false)
const showDetail = ref(false)
const submitting = ref(false)
const editId = ref(null)
const currentItem = ref(null)
const usageRecords = ref([])

const operationTypeOptions = [
  { text: '全部', value: '' },
  { text: '施肥', value: '施肥' },
  { text: '灌溉', value: '灌溉' },
  { text: '修剪', value: '修剪' },
  { text: '打药', value: '打药' },
  { text: '采收', value: '采收' }
]

const query = reactive({ batchId: route.query.batchId || '', operationType: '', page: 1, pageSize: 10 })
const form = reactive({ batchId: '', operationType: '', operationDate: '', operator: '', materials: '', remark: '' })

function resetForm() {
  Object.assign(form, { batchId: query.batchId || '', operationType: '', operationDate: '', operator: '', materials: '', remark: '' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await cultivationApi.getOperations({ ...query, page: 1 })
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
    const res = await cultivationApi.getOperations({ ...query })
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
  editId.value = null
  showDrawer.value = true
}

function openEditDrawer(item) {
  Object.assign(form, {
    batchId: item.batchId || '',
    operationType: item.operationType || '',
    operationDate: item.operationDate || '',
    operator: item.operator || '',
    materials: item.materials || '',
    remark: item.remark || ''
  })
  editId.value = item.id
  showDetail.value = false
  usageRecords.value = []
  showDrawer.value = true
}

function closeDetail() {
  showDetail.value = false
  usageRecords.value = []
}

async function openDetailDrawer(item) {
  currentItem.value = item
  usageRecords.value = []
  showDetail.value = true
  if (item.batchId) {
    try {
      const res = await inputApi.getUsageByBatch(item.batchId)
      usageRecords.value = res?.records || res || []
    } catch (e) {
      usageRecords.value = []
    }
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (editId.value) {
      await cultivationApi.updateOperation(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await cultivationApi.createOperation(form)
      showToast({ type: 'success', message: '新增成功' })
    }
    showDrawer.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(item) {
  try {
    await showConfirmDialog({ title: '确认删除', message: '确认删除该作业记录？' })
    await cultivationApi.deleteOperation(item.id)
    showToast({ type: 'success', message: '删除成功' })
    showDetail.value = false
    usageRecords.value = []
    loadList()
  } catch (e) {
    // user cancelled
  }
}

onMounted(loadList)
</script>

<style scoped>
.operations-page { padding-bottom: 20px; }
.top-filter { background: #fff; padding: 0 0 4px; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
.empty-usage-hint { text-align: center; color: #969799; font-size: 13px; padding: 12px 0; }
</style>
