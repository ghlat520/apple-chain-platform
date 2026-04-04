<template>
  <div class="batches-page">
    <van-search
      v-model="query.keyword"
      placeholder="搜索批次号/品种"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="定植" name="PLANTING" />
        <van-tab title="生长中" name="GROWING" />
        <van-tab title="待采收" name="READY_FOR_HARVEST" />
        <van-tab title="已采收" name="HARVESTED" />
        <van-tab title="已关闭" name="CLOSED" />
      </van-tabs>
    </div>

    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openAddDrawer">新增批次</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell
            :title="item.batchCode"
            :label="`品种: ${item.appleVariety || '-'} · ${item.orchardName || '果园ID:' + item.orchardId}${item.traceCode ? ' · 溯源:' + item.traceCode : ''}`"
            is-link
            @click="openDetailDrawer(item)"
          >
            <template #value>
              <van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无数据" />
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
        <span>{{ editId ? '编辑批次' : '新增批次' }}</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.batchCode"
            label="批次编号"
            placeholder="请输入批次编号"
            :rules="[{ required: true, message: '请填写批次编号' }]"
          />
          <van-field
            v-model="form.orchardId"
            label="果园ID"
            placeholder="请输入果园ID"
            :rules="[{ required: true, message: '请填写果园ID' }]"
          />
          <van-field
            v-model="form.appleVariety"
            label="苹果品种"
            placeholder="请输入苹果品种"
            :rules="[{ required: true, message: '请填写苹果品种' }]"
          />
          <van-field
            v-model.number="form.plantYear"
            label="种植年份"
            type="number"
            placeholder="请输入种植年份"
          />
          <van-field
            v-model.number="form.expectedYield"
            label="预计产量(kg)"
            type="number"
            placeholder="请输入预计产量"
          />
          <van-field
            v-model.number="form.actualYield"
            label="实际产量(kg)"
            type="number"
            placeholder="请输入实际产量"
          />
          <van-field
            v-model="form.harvestDate"
            label="采收日期"
            placeholder="例：2024-10-01"
          />
          <van-field label="状态">
            <template #input>
              <van-radio-group v-model="form.status" direction="horizontal">
                <van-radio name="PLANTING">定植</van-radio>
                <van-radio name="GROWING">生长中</van-radio>
                <van-radio name="READY_FOR_HARVEST">待采收</van-radio>
                <van-radio name="HARVESTED">已采收</van-radio>
                <van-radio name="CLOSED">已关闭</van-radio>
              </van-radio-group>
            </template>
          </van-field>
        </van-cell-group>
        <div class="drawer-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">
            {{ editId ? '保存修改' : '确认新增' }}
          </van-button>
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
        <span>批次详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="批次编号" :value="currentItem.batchCode" />
        <van-cell title="溯源码" v-if="currentItem.traceCode" is-link @click="goToTrace(currentItem.traceCode)">
          <template #value>
            <van-tag type="success" size="medium">{{ currentItem.traceCode }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="溯源码" v-else value="未生成（采收后自动关联）" />
        <van-cell title="果园" :value="currentItem.orchardName || `ID: ${currentItem.orchardId}`" />
        <van-cell title="苹果品种" :value="currentItem.appleVariety || '-'" />
        <van-cell title="种植年份" :value="currentItem.plantYear ?? '-'" />
        <van-cell title="预计产量(kg)" :value="currentItem.expectedYield ?? '-'" />
        <van-cell title="实际产量(kg)" :value="currentItem.actualYield ?? '-'" />
        <van-cell title="采收日期" :value="currentItem.harvestDate || '-'" />
        <van-cell title="状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="创建时间" :value="currentItem.createTime || '-'" />
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="primary" plain @click="openEditDrawer(currentItem)">编辑</van-button>
        <van-button block type="default" plain @click="goToOperations(currentItem)" style="margin-top:8px">查看作业记录</van-button>
        <van-button block type="danger" plain @click="handleDelete(currentItem)" style="margin-top:8px">删除</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { cultivationApi } from '@/api/cultivation.js'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showDrawer = ref(false)
const showDetail = ref(false)
const submitting = ref(false)
const editId = ref(null)
const currentItem = ref(null)

const query = reactive({ keyword: '', status: '', orchardId: '', page: 1, pageSize: 10 })
const form = reactive({
  batchCode: '', orchardId: '', appleVariety: '', plantYear: '',
  expectedYield: '', actualYield: '', harvestDate: '', status: 'PLANTING'
})

function statusTagType(status) {
  const map = { PLANTING: 'primary', GROWING: 'success', READY_FOR_HARVEST: 'warning', HARVESTED: 'success', CLOSED: 'default' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { PLANTING: '定植', GROWING: '生长中', READY_FOR_HARVEST: '待采收', HARVESTED: '已采收', CLOSED: '已关闭' }
  return map[status] || status || '-'
}

function resetForm() {
  Object.assign(form, { batchCode: '', orchardId: '', appleVariety: '', plantYear: '', expectedYield: '', actualYield: '', harvestDate: '', status: 'PLANTING' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await cultivationApi.getBatches({ ...query, page: 1 })
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
    const res = await cultivationApi.getBatches({ ...query })
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
    batchCode: item.batchCode || '',
    orchardId: item.orchardId || '',
    appleVariety: item.appleVariety || '',
    plantYear: item.plantYear || '',
    expectedYield: item.expectedYield || '',
    actualYield: item.actualYield || '',
    harvestDate: item.harvestDate || '',
    status: item.status || 'PLANTING'
  })
  editId.value = item.id
  showDetail.value = false
  showDrawer.value = true
}

function openDetailDrawer(item) {
  currentItem.value = item
  showDetail.value = true
}

function goToTrace(traceCode) {
  showDetail.value = false
  router.push(`/trace/query?code=${traceCode}`)
}

function goToOperations(item) {
  showDetail.value = false
  router.push(`/cultivation/operations?batchId=${item.id}`)
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (editId.value) {
      await cultivationApi.updateBatch(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await cultivationApi.createBatch(form)
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
    await showConfirmDialog({ title: '确认删除', message: `确认删除批次「${item.batchCode}」？` })
    await cultivationApi.deleteBatch(item.id)
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
    await cultivationApi.exportBatches()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.batches-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 8px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
