<template>
  <div class="page-container" data-density="compact">
    <van-nav-bar title="审计日志" left-arrow @click-left="$router.back()" />

    <van-search v-model="keyword" placeholder="搜索用户名/模块" @search="onSearch" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="filterAction" :options="actionOptions" @change="onSearch" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="list.length ? '没有更多了' : ''" @load="onLoad">
        <van-cell-group v-for="item in list" :key="item.id" inset style="margin-bottom: var(--space-2, 8px)">
          <van-cell :title="item.username" :label="`${item.module} · ${item.action}`">
            <template #value>
              <span style="font-size:var(--typography-caption-size,13px);color:var(--color-text-tertiary, #8A9099)">
                {{ item.createdAt }}
              </span>
            </template>
          </van-cell>
          <van-cell title="操作目标" :label="`${item.targetType || '-'} / ${item.targetId || '-'}`" />
          <van-cell title="请求信息" :label="`${item.requestMethod || ''} ${item.requestUri || ''}`">
            <template #value>
              <div style="display:flex;flex-direction:column;align-items:flex-end;gap:2px">
                <van-tag :type="item.responseStatus === 200 ? 'success' : 'danger'" size="medium">
                  {{ item.responseStatus || '-' }}
                </van-tag>
                <span v-if="item.durationMs != null" class="nums-tabular" style="font-size:var(--typography-caption-size,13px);color:var(--color-text-tertiary, #8A9099)">
                  {{ item.durationMs }}ms
                </span>
              </div>
            </template>
          </van-cell>
          <van-cell v-if="item.requestIp" title="IP" :value="item.requestIp" />
          <van-cell v-if="item.errorMessage" title="错误" :label="item.errorMessage" label-class="error-text" />
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无日志" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { auditApi } from '@/api/bigdata.js'

const keyword = ref('')
const filterAction = ref('')
const actionOptions = [
  { text: '全部操作', value: '' },
  { text: '新增', value: 'CREATE' },
  { text: '修改', value: 'UPDATE' },
  { text: '删除', value: 'DELETE' },
  { text: '查询', value: 'READ' },
  { text: '登录', value: 'LOGIN' },
]

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

async function loadData() {
  try {
    const params = { page: page.value, size: 20, action: filterAction.value || undefined }
    if (keyword.value) params.username = keyword.value
    const res = await auditApi.list(params)
    const records = res.records || res
    if (page.value === 1) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.total ?? records.length)
  } catch (e) { finished.value = true }
}

function onLoad() { loadData().finally(() => { loading.value = false; refreshing.value = false }) }
function onRefresh() { list.value = []; page.value = 1; finished.value = false; }
function onSearch() { onRefresh(); onLoad() }
</script>

<style scoped>
.page-container { min-height: 100vh; background: var(--color-surface-base, #FAFAF8); }
.error-text { color: var(--color-semantic-error, #D32F2F); }
</style>
