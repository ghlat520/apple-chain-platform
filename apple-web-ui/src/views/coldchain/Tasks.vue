<template>
  <div class="tasks-page">
    <van-search v-model="query.keyword" placeholder="搜索任务编码/货物" @search="handleSearch" @clear="handleSearch" />
    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="待发车" name="PENDING" />
        <van-tab title="运输中" name="IN_TRANSIT" />
        <van-tab title="已送达" name="DELIVERED" />
        <van-tab title="已取消" name="CANCELLED" />
      </van-tabs>
    </div>
    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openForm()">新增任务</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell :title="item.taskCode" :label="`${item.origin} → ${item.destination}`" is-link @click="openDetail(item)">
            <template #value>
              <van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无运输任务" />
      </van-list>
    </van-pull-refresh>

    <!-- Detail Popup -->
    <van-popup v-model:show="showDetail" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>任务详情</span><van-icon name="cross" @click="showDetail = false" /></div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="任务编码" :value="currentItem.taskCode" />
        <van-cell title="出发地" :value="currentItem.origin" />
        <van-cell title="目的地" :value="currentItem.destination" />
        <van-cell title="货物" :value="currentItem.cargoDesc || '-'" />
        <van-cell title="重量(kg)" :value="currentItem.cargoWeight ?? '-'" />
        <van-cell title="要求温度" :value="currentItem.requiredTemp != null ? `${currentItem.requiredTemp}℃` : '-'" />
        <van-cell title="计划发车" :value="formatTime(currentItem.planDepart)" />
        <van-cell title="实际发车" :value="formatTime(currentItem.actualDepart)" />
        <van-cell title="计划到达" :value="formatTime(currentItem.planArrive)" />
        <van-cell title="实际到达" :value="formatTime(currentItem.actualArrive)" />
        <van-cell title="距离(km)" :value="currentItem.distance ?? '-'" />
        <van-cell title="费用(元)" :value="currentItem.cost ?? '-'" />
        <van-cell title="状态"><template #value><van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag></template></van-cell>
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="success" plain v-if="currentItem?.status === 'PENDING'" @click="handleDepart">发车</van-button>
        <van-button block type="primary" plain v-if="currentItem?.status === 'IN_TRANSIT'" @click="handleDeliver">确认送达</van-button>
        <van-button block type="primary" plain style="margin-top:8px" @click="openForm(currentItem)">编辑</van-button>
        <van-button block type="danger" plain style="margin-top:8px" @click="handleDelete(currentItem)">删除</van-button>
      </div>
    </van-popup>

    <!-- Form Popup -->
    <van-popup v-model:show="showForm" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>{{ formData.id ? '编辑任务' : '新增任务' }}</span><van-icon name="cross" @click="showForm = false" /></div>
      <van-form @submit="handleSubmit">
        <van-cell-group inset>
          <van-field v-model="formData.origin" label="出发地" required :rules="[{required:true,message:'请输入出发地'}]" />
          <van-field v-model="formData.destination" label="目的地" required :rules="[{required:true,message:'请输入目的地'}]" />
          <van-field v-model="formData.cargoDesc" label="货物描述" />
          <van-field v-model="formData.cargoWeight" label="重量(kg)" type="number" />
          <van-field v-model="formData.requiredTemp" label="要求温度(℃)" type="number" />
          <van-field v-model="formData.distance" label="距离(km)" type="number" />
          <van-field v-model="formData.cost" label="费用(元)" type="number" />
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
const formData = reactive({ id: null, origin: '', destination: '', cargoDesc: '', cargoWeight: null, requiredTemp: null, distance: null, cost: null, remark: '' })

function statusTagType(s) { return { PENDING: 'warning', IN_TRANSIT: 'primary', DELIVERED: 'success', CANCELLED: 'default' }[s] || 'default' }
function statusLabel(s) { return { PENDING: '待发车', IN_TRANSIT: '运输中', DELIVERED: '已送达', CANCELLED: '已取消' }[s] || s || '-' }
function formatTime(t) { return t ? t.replace('T', ' ').substring(0, 16) : '-' }

async function loadList() {
  loading.value = true
  try {
    const res = await coldchainApi.getTasks({ ...query, page: 1 })
    list.value = res?.records || []
    finished.value = list.value.length >= (res?.total || 0)
    query.page = 1
  } finally { loading.value = false; refreshing.value = false }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const res = await coldchainApi.getTasks({ ...query })
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.size) finished.value = true
  } finally { loading.value = false }
}

function handleSearch() { query.page = 1; finished.value = false; list.value = []; loadList() }
function openDetail(item) { currentItem.value = item; showDetail.value = true }

function openForm(item) {
  showDetail.value = false
  if (item) { Object.assign(formData, item) } else { Object.keys(formData).forEach(k => formData[k] = k === 'id' ? null : '') }
  showForm.value = true
}

async function handleSubmit() {
  try {
    if (formData.id) { await coldchainApi.updateTask(formData.id, { ...formData }) }
    else { await coldchainApi.createTask({ ...formData }) }
    showToast('保存成功'); showForm.value = false; handleSearch()
  } catch (e) { showToast('保存失败') }
}

async function handleDepart() {
  try {
    await showConfirmDialog({ title: '确认发车', message: '确定标记该任务为发车？' })
    await coldchainApi.departTask(currentItem.value.id)
    showToast('发车成功'); showDetail.value = false; handleSearch()
  } catch (e) { if (e !== 'cancel') showToast('操作失败') }
}

async function handleDeliver() {
  try {
    await showConfirmDialog({ title: '确认送达', message: '确定标记该任务为已送达？' })
    await coldchainApi.deliverTask(currentItem.value.id)
    showToast('确认送达'); showDetail.value = false; handleSearch()
  } catch (e) { if (e !== 'cancel') showToast('操作失败') }
}

async function handleDelete(item) {
  try {
    await showConfirmDialog({ title: '确认删除', message: `确定删除任务 ${item.taskCode}？` })
    await coldchainApi.deleteTask(item.id)
    showToast('删除成功'); showDetail.value = false; handleSearch()
  } catch (e) { if (e !== 'cancel') showToast('删除失败') }
}

function handleExport() { coldchainApi.exportTasks() }

onMounted(loadList)
</script>

<style scoped>
.tasks-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 8px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-actions { padding: 16px; }
</style>
