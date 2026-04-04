<template>
  <div class="deliveries-page">
    <van-search v-model="query.keyword" placeholder="搜索配送编码/收货人" @search="handleSearch" @clear="handleSearch" />
    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="待配送" name="PENDING" />
        <van-tab title="配送中" name="DELIVERING" />
        <van-tab title="已签收" name="SIGNED" />
        <van-tab title="已拒收" name="REJECTED" />
      </van-tabs>
    </div>
    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openForm()">新增配送</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell :title="item.deliveryCode" :label="`收货人: ${item.receiverName} · ${item.receiverAddr || '-'}`" is-link @click="openDetail(item)">
            <template #value>
              <van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无配送记录" />
      </van-list>
    </van-pull-refresh>

    <!-- Detail Popup -->
    <van-popup v-model:show="showDetail" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>配送详情</span><van-icon name="cross" @click="showDetail = false" /></div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="配送编码" :value="currentItem.deliveryCode" />
        <van-cell title="运输任务ID" :value="currentItem.taskId ?? '-'" />
        <van-cell title="收货人" :value="currentItem.receiverName" />
        <van-cell title="电话" :value="currentItem.receiverPhone || '-'" />
        <van-cell title="地址" :value="currentItem.receiverAddr || '-'" />
        <van-cell title="配送时间" :value="formatTime(currentItem.deliveryTime)" />
        <van-cell title="签收时间" :value="formatTime(currentItem.signTime)" />
        <van-cell title="质检状态"><template #value><van-tag :type="qcTagType(currentItem.qualityCheck)">{{ qcLabel(currentItem.qualityCheck) }}</van-tag></template></van-cell>
        <van-cell v-if="currentItem.qualityRemark" title="质检备注" :value="currentItem.qualityRemark" />
        <van-cell title="状态"><template #value><van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag></template></van-cell>
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="success" plain v-if="currentItem?.status !== 'SIGNED'" @click="handleSign">签收确认</van-button>
        <van-button block type="primary" plain style="margin-top:8px" @click="openForm(currentItem)">编辑</van-button>
        <van-button block type="danger" plain style="margin-top:8px" @click="handleDelete(currentItem)">删除</van-button>
      </div>
    </van-popup>

    <!-- Form Popup -->
    <van-popup v-model:show="showForm" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>{{ formData.id ? '编辑配送' : '新增配送' }}</span><van-icon name="cross" @click="showForm = false" /></div>
      <van-form @submit="handleSubmit">
        <van-cell-group inset>
          <van-field v-model="formData.taskId" label="运输任务ID" type="number" />
          <van-field v-model="formData.receiverName" label="收货人" required :rules="[{required:true,message:'请输入收货人'}]" />
          <van-field v-model="formData.receiverPhone" label="电话" type="tel" />
          <van-field v-model="formData.receiverAddr" label="地址" />
          <van-field v-model="formData.remark" label="备注" type="textarea" rows="2" />
        </van-cell-group>
        <div style="padding:16px"><van-button block type="primary" native-type="submit">保存</van-button></div>
      </van-form>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { coldchainApi } from '@/api/coldchain.js'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showDetail = ref(false)
const showForm = ref(false)
const currentItem = ref(null)
const query = reactive({ keyword: '', status: '', page: 1, size: 10 })
const formData = reactive({ id: null, taskId: null, receiverName: '', receiverPhone: '', receiverAddr: '', remark: '' })

function statusTagType(s) { return { PENDING: 'warning', DELIVERING: 'primary', SIGNED: 'success', REJECTED: 'danger' }[s] || 'default' }
function statusLabel(s) { return { PENDING: '待配送', DELIVERING: '配送中', SIGNED: '已签收', REJECTED: '已拒收' }[s] || s || '-' }
function qcTagType(s) { return { PENDING: 'warning', PASSED: 'success', REJECTED: 'danger' }[s] || 'default' }
function qcLabel(s) { return { PENDING: '待质检', PASSED: '合格', REJECTED: '不合格' }[s] || s || '-' }
function formatTime(t) { return t ? t.replace('T', ' ').substring(0, 16) : '-' }

async function loadList() {
  loading.value = true
  try {
    const res = await coldchainApi.getDeliveries({ ...query, page: 1 })
    list.value = res?.records || []
    finished.value = list.value.length >= (res?.total || 0)
    query.page = 1
  } finally { loading.value = false; refreshing.value = false }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const res = await coldchainApi.getDeliveries({ ...query })
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.size) finished.value = true
  } finally { loading.value = false }
}

function handleSearch() { query.page = 1; finished.value = false; list.value = []; loadList() }
function openDetail(item) { currentItem.value = item; showDetail.value = true }

function openForm(item) {
  showDetail.value = false
  if (item) { Object.assign(formData, item) } else { Object.keys(formData).forEach(k => formData[k] = k === 'id' || k === 'taskId' ? null : '') }
  showForm.value = true
}

async function handleSubmit() {
  try {
    if (formData.id) { await coldchainApi.updateDelivery(formData.id, { ...formData }) }
    else { await coldchainApi.createDelivery({ ...formData }) }
    showToast('保存成功'); showForm.value = false; handleSearch()
  } catch (e) { showToast('保存失败') }
}

async function handleSign() {
  try {
    await showConfirmDialog({ title: '签收确认', message: '确定签收该配送？' })
    await coldchainApi.signDelivery(currentItem.value.id, { qualityCheck: 'PASSED' })
    showToast('签收成功'); showDetail.value = false; handleSearch()
  } catch (e) { if (e !== 'cancel') showToast('操作失败') }
}

async function handleDelete(item) {
  try {
    await showConfirmDialog({ title: '确认删除', message: `确定删除配送 ${item.deliveryCode}？` })
    await coldchainApi.deleteDelivery(item.id)
    showToast('删除成功'); showDetail.value = false; handleSearch()
  } catch (e) { if (e !== 'cancel') showToast('删除失败') }
}

function handleExport() { coldchainApi.exportDeliveries() }

onMounted(loadList)
</script>

<style scoped>
.deliveries-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 8px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-actions { padding: 16px; }
</style>
