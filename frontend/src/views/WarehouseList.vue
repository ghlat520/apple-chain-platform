<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">仓储管理</span>
      <van-button type="primary" size="small" icon="plus" @click="showAddSheet = true">新增仓库</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索仓库名称/位置" @search="loadList(true)" shape="round" />

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.name }}</span>
              <van-tag :type="item.status === 'ACTIVE' ? 'success' : 'default'" style="margin-left:6px;">
                {{ item.status === 'ACTIVE' ? '运营中' : '停用' }}
              </van-tag>
            </template>
            <template #label>
              {{ item.location }} · 容量: {{ item.capacity }}吨 · 已用: {{ item.usedCapacity }}吨
            </template>
          </van-cell>
          <van-cell title="温度范围" :value="`${item.tempMin}°C ~ ${item.tempMax}°C`" />
          <van-cell title="当前温度" :value="`${item.currentTemp || '-'}°C`" />
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无仓库数据" />
      </van-list>
    </van-pull-refresh>

    <van-action-sheet v-model:show="showAddSheet" title="新增仓库">
      <div style="padding:16px;">
        <van-form @submit="handleCreate">
          <van-field v-model="addForm.name" label="仓库名称" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.location" label="位置" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.capacity" label="总容量(吨)" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.tempMin" label="最低温度(°C)" type="number" placeholder="-2" />
          <van-field v-model="addForm.tempMax" label="最高温度(°C)" type="number" placeholder="4" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:65%;">
      <div style="padding:16px;" v-if="selectedWarehouse">
        <h3 style="margin-bottom:12px;">{{ selectedWarehouse.name }}</h3>
        <van-cell-group>
          <van-cell title="位置" :value="selectedWarehouse.location" />
          <van-cell title="总容量" :value="`${selectedWarehouse.capacity} 吨`" />
          <van-cell title="已使用" :value="`${selectedWarehouse.usedCapacity} 吨`" />
          <van-cell title="空余容量" :value="`${selectedWarehouse.capacity - selectedWarehouse.usedCapacity} 吨`" />
          <van-cell title="温度范围" :value="`${selectedWarehouse.tempMin}°C ~ ${selectedWarehouse.tempMax}°C`" />
          <van-cell title="当前温度" :value="`${selectedWarehouse.currentTemp || '-'}°C`" />
          <van-cell title="联系人" :value="selectedWarehouse.contactName || '-'" />
          <van-cell title="联系电话" :value="selectedWarehouse.contactPhone || '-'" />
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { warehouseApi } from '@/api/warehouse'
import { showToast } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const showDetailPopup = ref(false)
const selectedWarehouse = ref(null)
const query = reactive({ keyword: '', page: 1, size: 10 })
const addForm = reactive({ name: '', location: '', capacity: '', tempMin: '', tempMax: '' })

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await warehouseApi.list({ ...query })
    const records = res.data?.records || []
    if (reset) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.data?.total || 0)
    query.page++
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function showDetail(warehouse) {
  selectedWarehouse.value = warehouse
  showDetailPopup.value = true
}

async function handleCreate() {
  await warehouseApi.create(addForm)
  showToast('仓库创建成功')
  showAddSheet.value = false
  Object.assign(addForm, { name: '', location: '', capacity: '', tempMin: '', tempMax: '' })
  loadList(true)
}

loadList(true)
</script>
