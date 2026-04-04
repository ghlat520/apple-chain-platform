<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">果园管理</span>
      <van-button type="primary" size="small" icon="plus" @click="showAddSheet = true">新增</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索果园名称/位置" @search="loadList" shape="round" />

    <van-tabs v-model:active="activeStatus" @change="handleStatusChange" style="margin-bottom:12px;">
      <van-tab title="全部" name="" />
      <van-tab title="正常" name="ACTIVE" />
      <van-tab title="休耕" name="INACTIVE" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell
            :title="item.name"
            :label="`${item.location} · ${item.area}亩 · ${item.variety}`"
            is-link
            @click="router.push(`/orchards/${item.id}`)"
          >
            <template #right-icon>
              <span :class="`status-tag status-${item.status === 'ACTIVE' ? 'active' : 'inactive'}`">
                {{ item.status === 'ACTIVE' ? '正常' : '休耕' }}
              </span>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无果园数据" />
      </van-list>
    </van-pull-refresh>

    <van-floating-bubble
      icon="down"
      @click="exportData"
      axis="xy"
      style="--van-floating-bubble-background:#07c160"
    />

    <van-action-sheet v-model:show="showAddSheet" title="新增果园">
      <div style="padding:16px;">
        <van-form @submit="handleCreate">
          <van-field v-model="addForm.name" label="果园名称" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.location" label="位置" placeholder="请输入地址" :rules="[{required:true}]" />
          <van-field v-model="addForm.area" label="面积(亩)" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.variety" label="苹果品种" placeholder="如：红富士" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { orchardApi } from '@/api/orchard'
import { showToast } from 'vant'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const activeStatus = ref('')

const query = reactive({ keyword: '', status: '', page: 1, size: 10 })
const addForm = reactive({ name: '', location: '', area: '', variety: '' })

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await orchardApi.list({ ...query })
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

function loadMore() { loadList() }
function onRefresh() { loadList(true) }
function handleStatusChange(val) { query.status = val; loadList(true) }

async function handleCreate() {
  await orchardApi.create(addForm)
  showToast('创建成功')
  showAddSheet.value = false
  Object.assign(addForm, { name: '', location: '', area: '', variety: '' })
  loadList(true)
}

async function exportData() {
  const res = await orchardApi.export()
  const url = URL.createObjectURL(new Blob([res], { type: 'text/csv;charset=utf-8;' }))
  const a = document.createElement('a')
  a.href = url
  a.download = `果园列表_${new Date().toLocaleDateString()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

loadList(true)
</script>
