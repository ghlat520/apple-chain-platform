<template>
  <div class="records-page">
    <van-search
      v-model="query.keyword"
      placeholder="搜索溯源码"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="种植中" name="PLANTED" />
        <van-tab title="已采收" name="HARVESTED" />
        <van-tab title="入库中" name="IN_STORAGE" />
        <van-tab title="运输中" name="IN_TRANSIT" />
        <van-tab title="已售出" name="SOLD" />
      </van-tabs>
    </div>

    <div class="action-bar">
      <van-button type="primary" size="small" icon="search" @click="router.push('/trace/query')">溯源查询</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell
            :title="item.traceCode || '无溯源码'"
            :label="`产品类型: ${item.productType || '-'} · 批次: ${item.batchNo || '-'}`"
            is-link
            @click="openDetailDrawer(item)"
          >
            <template #value>
              <van-tag :type="statusTagType(item.currentStatus)">{{ statusLabel(item.currentStatus) }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无溯源记录" />
      </van-list>
    </van-pull-refresh>

    <!-- Detail Popup -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>溯源链详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="溯源码" :value="currentItem.traceCode || '-'" />
        <van-cell title="产品类型" :value="currentItem.productType || '-'" />
        <van-cell title="批次编号" :value="currentItem.batchNo || '-'" />
        <van-cell title="果园ID" :value="currentItem.orchardId ?? '-'" />
        <van-cell title="当前状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.currentStatus)">{{ statusLabel(currentItem.currentStatus) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="备注" :value="currentItem.remark || '-'" />
        <van-cell title="创建时间" :value="currentItem.createTime || '-'" />
      </van-cell-group>
      <div v-if="currentItem" style="text-align:center;padding:16px">
        <img
          v-if="qrDataUrl"
          :src="qrDataUrl"
          alt="溯源二维码"
          style="width:200px;height:200px;border:1px solid #ebedf0;border-radius:8px"
        />
        <van-loading v-else size="40px" style="margin:60px auto" />
        <p style="margin-top:8px;font-size:12px;color:#969799">扫码查看溯源信息</p>
      </div>
      <div class="drawer-actions">
        <van-button
          block type="primary" plain
          @click="goToQuery(currentItem)"
        >
          查看完整溯源链
        </van-button>
        <van-button
          block type="success" plain
          @click="downloadQrCode(currentItem)"
          style="margin-top:8px"
        >
          下载二维码
        </van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { traceApi } from '@/api/trace.js'
import { showToast } from 'vant'

function downloadQrCode(item) {
  const token = localStorage.getItem('token')
  fetch(`/api/trace/qrcode/${item.traceCode}?size=400`, {
    headers: { Authorization: `Bearer ${token}` }
  })
    .then(r => r.blob())
    .then(blob => {
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `溯源码_${item.traceCode}.png`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(a.href)
    })
    .catch(() => showToast('下载失败'))
}

const router = useRouter()
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showDetail = ref(false)
const currentItem = ref(null)
const qrDataUrl = ref(null)

const query = reactive({ keyword: '', status: '', page: 1, pageSize: 10 })

function statusTagType(status) {
  const map = { PLANTED: 'primary', HARVESTED: 'success', IN_STORAGE: 'warning', IN_TRANSIT: 'warning', SOLD: 'default' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { PLANTED: '种植中', HARVESTED: '已采收', IN_STORAGE: '入库中', IN_TRANSIT: '运输中', SOLD: '已售出' }
  return map[status] || status || '-'
}

async function loadList() {
  loading.value = true
  try {
    const res = await traceApi.getChains({ ...query, page: 1 })
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
    const res = await traceApi.getChains({ ...query })
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

function openDetailDrawer(item) {
  currentItem.value = item
  qrDataUrl.value = null
  showDetail.value = true
  loadQrCode(item.traceCode)
}

async function loadQrCode(traceCode) {
  try {
    const token = localStorage.getItem('token')
    const res = await fetch(`/api/trace/qrcode/${traceCode}?size=200`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (!res.ok) return
    const blob = await res.blob()
    qrDataUrl.value = URL.createObjectURL(blob)
  } catch (e) {
    // QR loading failed silently
  }
}

function goToQuery(item) {
  showDetail.value = false
  router.push(`/trace/query?code=${item.traceCode}`)
}

async function handleExport() {
  showToast('正在导出...')
  try {
    await traceApi.exportChains()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.records-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 8px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-actions { padding: 16px; }
</style>
