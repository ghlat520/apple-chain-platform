<template>
  <div class="supply-page">
    <van-search
      v-model="query.variety"
      placeholder="搜索苹果品种"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="草稿" name="DRAFT" />
        <van-tab title="已发布" name="PUBLISHED" />
        <van-tab title="已配对" name="MATCHED" />
        <van-tab title="已关闭" name="CLOSED" />
      </van-tabs>
    </div>

    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openAddDrawer">发布信息</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell is-link @click="openDetailDrawer(item)">
            <template #title>
              <span>{{ item.variety || '未知品种' }}</span>
              <van-tag type="primary" size="small" style="margin-left:6px">供货</van-tag>
            </template>
            <template #label>
              数量: {{ item.quantity || '-' }} kg · 期望价: ¥{{ item.priceExpected || '-' }}/kg
            </template>
            <template #value>
              <van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag>
            </template>
          </van-cell>
          <van-cell v-if="item.description" :label="item.description" />
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无供货信息" />
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
        <span>{{ editId ? '编辑供货信息' : '发布供货信息' }}</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.variety"
            label="苹果品种"
            placeholder="请输入苹果品种"
            :rules="[{ required: true, message: '请填写苹果品种' }]"
          />
          <van-field
            v-model.number="form.quantity"
            label="数量(kg)"
            type="number"
            placeholder="请输入数量"
            :rules="[{ required: true, message: '请填写数量' }]"
          />
          <van-field
            v-model.number="form.priceExpected"
            label="期望价(元/kg)"
            type="number"
            placeholder="请输入期望价格"
            :rules="[{ required: true, message: '请填写期望价格' }]"
          />
          <van-field
            v-model="form.quality"
            label="质量等级"
            placeholder="A / B / C"
          />
          <van-field
            v-model="form.location"
            label="产地位置"
            placeholder="请输入产地"
          />
          <van-field
            v-model="form.harvestDate"
            label="采收日期"
            placeholder="例：2024-10-01"
          />
          <van-field
            v-model="form.description"
            label="描述"
            type="textarea"
            rows="3"
            autosize
            placeholder="请输入供货描述"
          />
          <van-field label="状态">
            <template #input>
              <van-radio-group v-model="form.status" direction="horizontal">
                <van-radio name="DRAFT">草稿</van-radio>
                <van-radio name="PUBLISHED">已发布</van-radio>
              </van-radio-group>
            </template>
          </van-field>
        </van-cell-group>
        <div class="drawer-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">
            {{ editId ? '保存修改' : '确认发布' }}
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
        <span>供货详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="供货编号" :value="currentItem.supplyNo || '-'" />
        <van-cell title="种植批次" :value="currentItem.batchCode || '-'" />
        <van-cell title="溯源码" v-if="currentItem.traceCode" is-link @click="goToTrace(currentItem.traceCode)">
          <template #value>
            <van-tag type="success" size="medium">{{ currentItem.traceCode }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="苹果品种" :value="currentItem.variety || '-'" />
        <van-cell title="数量(kg)" :value="currentItem.quantity ?? '-'" />
        <van-cell title="期望价(元/kg)" :value="currentItem.priceExpected ?? '-'" />
        <van-cell title="质量等级" :value="currentItem.quality || '-'" />
        <van-cell title="产地位置" :value="currentItem.location || '-'" />
        <van-cell title="采收日期" :value="currentItem.harvestDate || '-'" />
        <van-cell title="描述" :value="currentItem.description || '-'" />
        <van-cell title="发布时间" :value="currentItem.createTime || '-'" />
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
import { tradeApi } from '@/api/trade.js'
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

const query = reactive({ status: '', variety: '', page: 1, pageSize: 10 })
const form = reactive({ status: 'DRAFT', variety: '', quantity: '', priceExpected: '', quality: '', location: '', harvestDate: '', description: '' })

function statusTagType(status) {
  const map = { DRAFT: 'default', PUBLISHED: 'primary', MATCHED: 'success', CLOSED: 'warning' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { DRAFT: '草稿', PUBLISHED: '已发布', MATCHED: '已配对', CLOSED: '已关闭' }
  return map[status] || status || '-'
}

function resetForm() {
  Object.assign(form, { status: 'DRAFT', variety: '', quantity: '', priceExpected: '', quality: '', location: '', harvestDate: '', description: '' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await tradeApi.getSupplies({ ...query, page: 1 })
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
    const res = await tradeApi.getSupplies({ ...query })
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
    status: item.status || 'DRAFT',
    variety: item.variety || '',
    quantity: item.quantity || '',
    priceExpected: item.priceExpected || '',
    quality: item.quality || '',
    location: item.location || '',
    harvestDate: item.harvestDate || '',
    description: item.description || ''
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
      await tradeApi.updateSupply(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await tradeApi.createSupply(form)
      showToast({ type: 'success', message: '发布成功' })
    }
    showDrawer.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(item) {
  try {
    await showConfirmDialog({ title: '确认删除', message: '确认删除该供货信息？' })
    await tradeApi.deleteSupply(item.id)
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
    await tradeApi.exportSupplies()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.supply-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.filter-row { background: #fff; padding: 8px 16px; display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.filter-label { font-size: 13px; color: #646566; flex-shrink: 0; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
