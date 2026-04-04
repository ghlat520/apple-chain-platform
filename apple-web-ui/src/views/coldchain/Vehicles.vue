<template>
  <div class="vehicles-page">
    <van-search v-model="query.keyword" placeholder="搜索车牌号/驾驶员" @search="handleSearch" @clear="handleSearch" />
    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="空闲" name="IDLE" />
        <van-tab title="运输中" name="IN_TRANSIT" />
        <van-tab title="维修中" name="MAINTENANCE" />
        <van-tab title="已退役" name="RETIRED" />
      </van-tabs>
    </div>
    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openForm()">新增车辆</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell :title="item.plateNumber" :label="`${item.brand || '-'} · ${vehicleTypeLabel(item.vehicleType)} · ${item.driverName || '-'}`" is-link @click="openDetail(item)">
            <template #value>
              <van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无车辆" />
      </van-list>
    </van-pull-refresh>

    <!-- Detail Popup -->
    <van-popup v-model:show="showDetail" position="bottom" round :style="{ maxHeight: '80vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>车辆详情</span><van-icon name="cross" @click="showDetail = false" /></div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="车辆编码" :value="currentItem.vehicleCode" />
        <van-cell title="车牌号" :value="currentItem.plateNumber" />
        <van-cell title="类型" :value="vehicleTypeLabel(currentItem.vehicleType)" />
        <van-cell title="品牌" :value="currentItem.brand || '-'" />
        <van-cell title="载重(吨)" :value="currentItem.capacity ?? '-'" />
        <van-cell title="容积(m3)" :value="currentItem.volume ?? '-'" />
        <van-cell title="控温范围" :value="currentItem.temperatureMin != null ? `${currentItem.temperatureMin}~${currentItem.temperatureMax}℃` : '-'" />
        <van-cell title="驾驶员" :value="currentItem.driverName || '-'" />
        <van-cell title="电话" :value="currentItem.driverPhone || '-'" />
        <van-cell title="状态"><template #value><van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag></template></van-cell>
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="primary" plain @click="openForm(currentItem)">编辑</van-button>
        <van-button block type="danger" plain style="margin-top:8px" @click="handleDelete(currentItem)">删除</van-button>
      </div>
    </van-popup>

    <!-- Form Popup -->
    <van-popup v-model:show="showForm" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>{{ formData.id ? '编辑车辆' : '新增车辆' }}</span><van-icon name="cross" @click="showForm = false" /></div>
      <van-form @submit="handleSubmit">
        <van-cell-group inset>
          <van-field v-model="formData.plateNumber" label="车牌号" required :rules="[{required:true,message:'请输入车牌号'}]" />
          <van-field v-model="formData.vehicleType" label="车辆类型" required is-link readonly @click="showTypePicker = true" :rules="[{required:true,message:'请选择车辆类型'}]" :formatter="vehicleTypeLabel" />
          <van-field v-model="formData.brand" label="品牌型号" />
          <van-field v-model="formData.capacity" label="载重(吨)" type="number" />
          <van-field v-model="formData.volume" label="容积(m3)" type="number" />
          <van-field v-model="formData.temperatureMin" label="最低控温(℃)" type="number" />
          <van-field v-model="formData.temperatureMax" label="最高控温(℃)" type="number" />
          <van-field v-model="formData.driverName" label="驾驶员" />
          <van-field v-model="formData.driverPhone" label="驾驶员电话" type="tel" />
          <van-field v-model="formData.remark" label="备注" type="textarea" rows="2" />
        </van-cell-group>
        <div style="padding:16px"><van-button block type="primary" native-type="submit">保存</van-button></div>
      </van-form>
    </van-popup>

    <van-popup v-model:show="showTypePicker" position="bottom" round>
      <van-picker :columns="typeOptions" @confirm="onTypePick" @cancel="showTypePicker = false" />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { coldchainApi } from '@/api/coldchain.js'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showDetail = ref(false)
const showForm = ref(false)
const showTypePicker = ref(false)
const currentItem = ref(null)
const query = reactive({ keyword: '', status: '', page: 1, size: 10 })
const formData = reactive({ id: null, plateNumber: '', vehicleType: '', brand: '', capacity: null, volume: null, temperatureMin: null, temperatureMax: null, driverName: '', driverPhone: '', remark: '' })

const typeOptions = [{ text: '冷藏车', value: 'REFRIGERATED' }, { text: '保温车', value: 'INSULATED' }, { text: '普通车', value: 'NORMAL' }]

function vehicleTypeLabel(t) { return { REFRIGERATED: '冷藏车', INSULATED: '保温车', NORMAL: '普通车' }[t] || t || '-' }
function statusTagType(s) { return { IDLE: 'success', IN_TRANSIT: 'primary', MAINTENANCE: 'warning', RETIRED: 'default' }[s] || 'default' }
function statusLabel(s) { return { IDLE: '空闲', IN_TRANSIT: '运输中', MAINTENANCE: '维修中', RETIRED: '已退役' }[s] || s || '-' }

async function loadList() {
  loading.value = true
  try {
    const res = await coldchainApi.getVehicles({ ...query, page: 1 })
    list.value = res?.records || []
    finished.value = list.value.length >= (res?.total || 0)
    query.page = 1
  } finally { loading.value = false; refreshing.value = false }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const res = await coldchainApi.getVehicles({ ...query })
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

function onTypePick({ selectedValues }) { formData.vehicleType = selectedValues[0]; showTypePicker.value = false }

async function handleSubmit() {
  try {
    if (formData.id) { await coldchainApi.updateVehicle(formData.id, { ...formData }) }
    else { await coldchainApi.createVehicle({ ...formData }) }
    showToast('保存成功'); showForm.value = false; handleSearch()
  } catch (e) { showToast('保存失败') }
}

async function handleDelete(item) {
  try {
    await showConfirmDialog({ title: '确认删除', message: `确定删除车辆 ${item.plateNumber}？` })
    await coldchainApi.deleteVehicle(item.id)
    showToast('删除成功'); showDetail.value = false; handleSearch()
  } catch (e) { if (e !== 'cancel') showToast('删除失败') }
}

function handleExport() { coldchainApi.exportVehicles() }

import { onMounted } from 'vue'
onMounted(loadList)
</script>

<style scoped>
.vehicles-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 8px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-actions { padding: 16px; }
</style>
