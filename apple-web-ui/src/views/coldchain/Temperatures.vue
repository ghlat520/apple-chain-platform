<template>
  <div class="temps-page">
    <van-search v-model="query.taskId" placeholder="输入运输任务ID" @search="handleSearch" @clear="handleSearch" type="number" />
    <div class="filter-bar">
      <van-tabs v-model:active="alarmFilter" @change="handleAlarmChange" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="正常" name="0" />
        <van-tab title="报警" name="1" />
      </van-tabs>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell :title="`${item.temperature}℃ · ${item.humidity != null ? item.humidity + '%' : '-'}`" :label="`任务ID: ${item.taskId} · ${item.location || '-'} · ${formatTime(item.recordTime)}`">
            <template #value>
              <van-tag :type="item.isAlarm ? 'danger' : 'success'">{{ item.isAlarm ? '报警' : '正常' }}</van-tag>
            </template>
          </van-cell>
          <van-cell v-if="item.alarmMsg" :title="item.alarmMsg" title-style="color:#ee0a24;font-size:12px" />
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无温度记录" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { coldchainApi } from '@/api/coldchain.js'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const alarmFilter = ref('')
const query = reactive({ taskId: '', page: 1, size: 10 })

function formatTime(t) { return t ? t.replace('T', ' ').substring(0, 16) : '-' }

function handleAlarmChange() { handleSearch() }

async function loadList() {
  loading.value = true
  try {
    const params = { page: 1, size: query.size }
    if (query.taskId) params.taskId = query.taskId
    if (alarmFilter.value !== '') params.isAlarm = Number(alarmFilter.value)
    const res = await coldchainApi.getTemperatures(params)
    list.value = res?.records || []
    finished.value = list.value.length >= (res?.total || 0)
    query.page = 1
  } finally { loading.value = false; refreshing.value = false }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const params = { page: query.page, size: query.size }
    if (query.taskId) params.taskId = query.taskId
    if (alarmFilter.value !== '') params.isAlarm = Number(alarmFilter.value)
    const res = await coldchainApi.getTemperatures(params)
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.size) finished.value = true
  } finally { loading.value = false }
}

function handleSearch() { query.page = 1; finished.value = false; list.value = []; loadList() }

onMounted(loadList)
</script>

<style scoped>
.temps-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 8px; }
</style>
