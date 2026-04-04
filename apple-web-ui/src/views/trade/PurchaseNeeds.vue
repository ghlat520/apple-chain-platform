<template>
  <div class="purchase-needs-page">
    <van-search
      v-model="query.keyword"
      placeholder="搜索需求编号"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="草稿" name="DRAFT" />
        <van-tab title="已发布" name="PUBLISHED" />
        <van-tab title="已匹配" name="MATCHED" />
        <van-tab title="已关闭" name="CLOSED" />
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
              <span>{{ item.variety || '未知品种' }}</span>
              <van-tag :type="statusTagType(item.status)" size="small" style="margin-left:6px">{{ statusLabel(item.status) }}</van-tag>
            </template>
            <template #label>
              数量: {{ item.quantity ? item.quantity + 'kg' : '-' }} · 最高价: {{ item.priceMax ? '¥' + item.priceMax + '/kg' : '-' }} · 需求日期: {{ item.requireDate || '-' }}
            </template>
            <template #value>
              <span style="font-size:12px;color:#969799">{{ item.needNo || '' }}</span>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无采购需求" />
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
        <span>{{ editId ? '编辑采购需求' : '新增采购需求' }}</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.variety"
            label="苹果品种"
            placeholder="请输入苹果品种"
            :rules="[{ required: true, message: '请填写品种' }]"
          />
          <van-field
            v-model="form.quantity"
            label="需求数量(kg)"
            placeholder="请输入需求数量"
            type="number"
            :rules="[{ required: true, message: '请填写数量' }]"
          />
          <van-field
            v-model="form.priceMax"
            label="最高单价(元/kg)"
            placeholder="请输入最高可接受价格"
            type="number"
          />
          <van-field label="品质等级">
            <template #input>
              <van-radio-group v-model="form.quality" direction="horizontal">
                <van-radio name="A">A级</van-radio>
                <van-radio name="B">B级</van-radio>
                <van-radio name="C">C级</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field
            v-model="form.requireDate"
            label="需求日期"
            placeholder="例：2024-12-01"
          />
          <van-field
            v-model="form.deliveryAddr"
            label="配送地址"
            placeholder="请输入配送地址"
          />
          <van-field
            v-model="form.description"
            label="需求描述"
            type="textarea"
            placeholder="请输入需求描述"
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
        <span>采购需求详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="需求编号" :value="currentItem.needNo || '-'" />
        <van-cell title="苹果品种" :value="currentItem.variety || '-'" />
        <van-cell title="需求数量" :value="currentItem.quantity ? currentItem.quantity + 'kg' : '-'" />
        <van-cell title="最高单价" :value="currentItem.priceMax ? '¥' + currentItem.priceMax + '/kg' : '-'" />
        <van-cell title="品质等级" :value="currentItem.quality ? currentItem.quality + '级' : '-'" />
        <van-cell title="需求日期" :value="currentItem.requireDate || '-'" />
        <van-cell title="配送地址" :value="currentItem.deliveryAddr || '-'" />
        <van-cell title="需求描述" :value="currentItem.description || '-'" />
        <van-cell title="创建时间" :value="currentItem.createTime || '-'" />
      </van-cell-group>
      <div class="drawer-actions">
        <van-button
          v-if="currentItem && currentItem.status === 'DRAFT'"
          block type="success" @click="handlePublish(currentItem)"
          style="margin-bottom:8px"
        >发布需求</van-button>
        <van-button block type="primary" plain @click="openEditDrawer(currentItem)">编辑</van-button>
        <van-button block type="danger" plain @click="handleDelete(currentItem)" style="margin-top:8px">删除</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { tradeApi } from '@/api/trade.js'
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

const query = reactive({ status: '', keyword: '', page: 1, size: 10 })
const form = reactive({ variety: '', quantity: '', priceMax: '', quality: 'A', requireDate: '', deliveryAddr: '', description: '' })

function statusTagType(status) {
  const map = { DRAFT: 'default', PUBLISHED: 'primary', MATCHED: 'success', CLOSED: 'danger' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { DRAFT: '草稿', PUBLISHED: '已发布', MATCHED: '已匹配', CLOSED: '已关闭' }
  return map[status] || status || '-'
}

function resetForm() {
  Object.assign(form, { variety: '', quantity: '', priceMax: '', quality: 'A', requireDate: '', deliveryAddr: '', description: '' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await tradeApi.getPurchaseNeeds({ ...query, page: 1 })
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
    const res = await tradeApi.getPurchaseNeeds({ ...query })
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
    variety: item.variety || '',
    quantity: item.quantity || '',
    priceMax: item.priceMax || '',
    quality: item.quality || 'A',
    requireDate: item.requireDate || '',
    deliveryAddr: item.deliveryAddr || '',
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
      await tradeApi.updatePurchaseNeed(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await tradeApi.createPurchaseNeed(form)
      showToast({ type: 'success', message: '新增成功' })
    }
    showDrawer.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handlePublish(item) {
  try {
    await showConfirmDialog({ title: '确认发布', message: '发布后将公开采购需求，确认？' })
    await tradeApi.publishPurchaseNeed(item.id)
    showToast({ type: 'success', message: '发布成功' })
    showDetail.value = false
    loadList()
  } catch (e) {
    // user cancelled
  }
}

async function handleDelete(item) {
  try {
    await showConfirmDialog({ title: '确认删除', message: '确认删除该采购需求？' })
    await tradeApi.deletePurchaseNeed(item.id)
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
    await tradeApi.exportPurchaseNeeds()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.purchase-needs-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
