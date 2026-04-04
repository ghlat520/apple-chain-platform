<template>
  <div class="usage-page">
    <van-search
      v-model="query.keyword"
      placeholder="搜索批次号或产品名称"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.method" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="撒施" name="撒施" />
        <van-tab title="喷洒" name="喷洒" />
        <van-tab title="滴灌" name="滴灌" />
        <van-tab title="穴施" name="穴施" />
      </van-tabs>
    </div>

    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openAddDrawer">新增</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell is-link @click="openDetailDrawer(item)">
            <template #title>
              <span>{{ item.productName || '未知产品' }}</span>
              <van-tag type="primary" size="small" style="margin-left:6px">{{ item.method || '-' }}</van-tag>
            </template>
            <template #label>
              批次: {{ item.batchCode || '-' }} · 果园: {{ item.orchardName || '-' }}<br/>
              用量: {{ item.quantity || '-' }} {{ item.unit || '' }} · 日期: {{ item.usageDate || '-' }}
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无使用记录" />
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
        <span>{{ editId ? '编辑使用记录' : '新增使用记录' }}</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.productName"
            label="产品名称"
            placeholder="请输入产品名称"
            :rules="[{ required: true, message: '请填写产品名称' }]"
          />
          <van-field
            v-model="form.batchCode"
            label="批次号"
            placeholder="请输入批次号"
          />
          <van-field
            v-model="form.orchardName"
            label="果园名称"
            placeholder="请输入果园名称"
            :rules="[{ required: true, message: '请填写果园名称' }]"
          />
          <van-field
            v-model.number="form.quantity"
            label="用量"
            type="number"
            placeholder="请输入用量"
            :rules="[{ required: true, message: '请填写用量' }]"
          />
          <van-field
            v-model="form.unit"
            label="单位"
            placeholder="如：kg、L、g"
          />
          <van-field
            v-model="form.usageDate"
            label="使用日期"
            placeholder="例：2024-10-01"
          />
          <van-field
            v-model="form.operator"
            label="操作人"
            placeholder="请输入操作人"
          />
          <van-field label="施用方式">
            <template #input>
              <van-radio-group v-model="form.method" direction="horizontal">
                <van-radio name="撒施">撒施</van-radio>
                <van-radio name="喷洒">喷洒</van-radio>
                <van-radio name="滴灌">滴灌</van-radio>
                <van-radio name="穴施">穴施</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field
            v-model="form.traceCode"
            label="溯源码"
            placeholder="请输入溯源码"
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
        <span>使用记录详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="产品名称" :value="currentItem.productName || '-'" />
        <van-cell title="批次号" :value="currentItem.batchCode || '-'" />
        <van-cell title="果园名称" :value="currentItem.orchardName || '-'" />
        <van-cell title="用量" :value="(currentItem.quantity ?? '-') + ' ' + (currentItem.unit || '')" />
        <van-cell title="使用日期" :value="currentItem.usageDate || '-'" />
        <van-cell title="操作人" :value="currentItem.operator || '-'" />
        <van-cell title="施用方式">
          <template #value>
            <van-tag type="primary">{{ currentItem.method || '-' }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="溯源码" v-if="currentItem.traceCode" is-link @click="goToTrace(currentItem.traceCode)">
          <template #value>
            <van-tag type="success" size="medium">{{ currentItem.traceCode }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="溯源码" v-else value="-" />
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
import { useRouter } from 'vue-router'
import { inputApi } from '@/api/input.js'
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

const query = reactive({ method: '', keyword: '', page: 1, pageSize: 10 })
const form = reactive({ productName: '', batchCode: '', orchardName: '', quantity: '', unit: '', usageDate: '', operator: '', method: '撒施', traceCode: '' })

function resetForm() {
  Object.assign(form, { productName: '', batchCode: '', orchardName: '', quantity: '', unit: '', usageDate: '', operator: '', method: '撒施', traceCode: '' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await inputApi.getUsageList({ ...query, page: 1 })
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
    const res = await inputApi.getUsageList({ ...query })
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
    productName: item.productName || '',
    batchCode: item.batchCode || '',
    orchardName: item.orchardName || '',
    quantity: item.quantity ?? '',
    unit: item.unit || '',
    usageDate: item.usageDate || '',
    operator: item.operator || '',
    method: item.method || '撒施',
    traceCode: item.traceCode || ''
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
      await inputApi.updateUsage(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await inputApi.createUsage(form)
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
    await showConfirmDialog({ title: '确认删除', message: '确认删除该使用记录？' })
    await inputApi.deleteUsage(item.id)
    showToast({ type: 'success', message: '删除成功' })
    showDetail.value = false
    loadList()
  } catch (e) {
    // user cancelled
  }
}

function goToTrace(traceCode) {
  showDetail.value = false
  router.push(`/trace/query?code=${traceCode}`)
}

async function handleExport() {
  showToast('正在导出...')
  try {
    await inputApi.exportUsage()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.usage-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
