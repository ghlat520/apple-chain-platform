<template>
  <div class="growth-records-page">
    <van-search
      v-model="query.orchardId"
      placeholder="按果园ID筛选"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.recordType" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="施肥" name="FERTILIZE" />
        <van-tab title="喷药" name="SPRAY" />
        <van-tab title="灌溉" name="IRRIGATE" />
        <van-tab title="修剪" name="PRUNE" />
        <van-tab title="病虫害防治" name="PEST_CONTROL" />
      </van-tabs>
    </div>

    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openAddDrawer">新增</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell is-link @click="openDetailDrawer(item)">
            <template #title>
              <van-tag :type="typeTagType(item.recordType)" size="small">{{ typeLabel(item.recordType) }}</van-tag>
              <span style="margin-left:6px">果园#{{ item.orchardId || '-' }}</span>
            </template>
            <template #label>
              操作人: {{ item.operator || '-' }} · 日期: {{ item.operateDate || '-' }} · {{ item.weather || '' }}
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无生长记录" />
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
        <span>{{ editId ? '编辑生长记录' : '新增生长记录' }}</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.orchardId"
            label="果园ID"
            placeholder="请输入果园ID"
            type="digit"
            :rules="[{ required: true, message: '请填写果园ID' }]"
          />
          <van-field label="记录类型" :rules="[{ required: true, message: '请选择类型' }]">
            <template #input>
              <van-radio-group v-model="form.recordType" direction="horizontal">
                <van-radio name="FERTILIZE">施肥</van-radio>
                <van-radio name="SPRAY">喷药</van-radio>
                <van-radio name="IRRIGATE">灌溉</van-radio>
                <van-radio name="PRUNE">修剪</van-radio>
                <van-radio name="PEST_CONTROL">防治</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field
            v-model="form.operateDate"
            label="操作日期"
            placeholder="例：2024-10-01"
            :rules="[{ required: true, message: '请填写日期' }]"
          />
          <van-field
            v-model="form.operator"
            label="操作人"
            placeholder="请输入操作人"
          />
          <van-field
            v-model="form.weather"
            label="天气"
            placeholder="请输入天气情况"
          />
          <van-field
            v-model="form.notes"
            label="备注"
            type="textarea"
            placeholder="请输入备注"
            rows="2"
          />
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
        <span>生长记录详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="记录类型">
          <template #value>
            <van-tag :type="typeTagType(currentItem.recordType)">{{ typeLabel(currentItem.recordType) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="果园ID" :value="String(currentItem.orchardId || '-')" />
        <van-cell title="操作日期" :value="currentItem.operateDate || '-'" />
        <van-cell title="操作人" :value="currentItem.operator || '-'" />
        <van-cell title="天气" :value="currentItem.weather || '-'" />
        <van-cell title="备注" :value="currentItem.notes || '-'" />
        <van-cell title="创建时间" :value="currentItem.createTime || '-'" />
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="primary" plain @click="openEditDrawer(currentItem)">编辑</van-button>
        <van-button block type="danger" plain @click="handleDelete(currentItem)" style="margin-top:8px">删除</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { cultivationApi } from '@/api/cultivation.js'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showDrawer = ref(false)
const showDetail = ref(false)
const submitting = ref(false)
const editId = ref(null)
const currentItem = ref(null)

const query = reactive({ recordType: '', orchardId: '', page: 1, size: 10 })
const form = reactive({ orchardId: '', recordType: 'FERTILIZE', operateDate: '', operator: '', weather: '', notes: '' })

function typeTagType(type) {
  const map = { FERTILIZE: 'primary', SPRAY: 'warning', IRRIGATE: 'success', PRUNE: 'default', PEST_CONTROL: 'danger' }
  return map[type] || 'default'
}

function typeLabel(type) {
  const map = { FERTILIZE: '施肥', SPRAY: '喷药', IRRIGATE: '灌溉', PRUNE: '修剪', PEST_CONTROL: '病虫害防治' }
  return map[type] || type || '-'
}

function resetForm() {
  Object.assign(form, { orchardId: '', recordType: 'FERTILIZE', operateDate: '', operator: '', weather: '', notes: '' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await cultivationApi.getGrowthRecords({ ...query, page: 1 })
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
    const res = await cultivationApi.getGrowthRecords({ ...query })
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.size) {
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
    orchardId: item.orchardId || '',
    recordType: item.recordType || 'FERTILIZE',
    operateDate: item.operateDate || '',
    operator: item.operator || '',
    weather: item.weather || '',
    notes: item.notes || ''
  })
  editId.value = item.id
  showDetail.value = false
  showDrawer.value = true
}

function openDetailDrawer(item) {
  currentItem.value = item
  showDetail.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (editId.value) {
      await cultivationApi.updateGrowthRecord(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await cultivationApi.createGrowthRecord(form)
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
    await showConfirmDialog({ title: '确认删除', message: '确认删除该生长记录？' })
    await cultivationApi.deleteGrowthRecord(item.id)
    showToast({ type: 'success', message: '删除成功' })
    showDetail.value = false
    loadList()
  } catch (e) {
    // user cancelled
  }
}

onMounted(loadList)
</script>

<style scoped>
.growth-records-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
