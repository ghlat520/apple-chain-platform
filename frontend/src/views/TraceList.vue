<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">溯源管理</span>
      <van-button type="primary" size="small" icon="plus" @click="showAddSheet = true">新增批次</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索批次编号/果园名称" @search="loadList(true)" shape="round" />

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-bottom:8px;" v-for="item in list" :key="item.batchCode">
          <van-cell
            :title="item.batchCode"
            :label="`果园: ${item.orchardName} · 数量: ${item.quantity}kg`"
            is-link
            @click="router.push(`/trace/${item.batchCode}`)"
          >
            <template #right-icon>
              <van-tag type="success" style="align-self:center;">已上链</van-tag>
            </template>
          </van-cell>
          <van-cell title="采收日期" :value="item.harvestDate" />
          <van-cell title="品种" :value="item.variety" />
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无溯源记录" />
      </van-list>
    </van-pull-refresh>

    <van-action-sheet v-model:show="showAddSheet" title="新增溯源批次">
      <div style="padding:16px;">
        <van-form @submit="handleCreate">
          <van-field v-model="addForm.orchardId" label="果园ID" placeholder="请输入果园ID" :rules="[{required:true}]" />
          <van-field v-model="addForm.variety" label="苹果品种" placeholder="如：红富士" :rules="[{required:true}]" />
          <van-field v-model="addForm.quantity" label="数量(kg)" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.harvestDate" label="采收日期" placeholder="YYYY-MM-DD" :rules="[{required:true}]" />
          <van-field v-model="addForm.notes" label="备注" type="textarea" placeholder="选填" rows="2" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { traceApi } from '@/api/trace'
import { showToast } from 'vant'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const query = reactive({ keyword: '', page: 1, size: 10 })
const addForm = reactive({ orchardId: '', variety: '', quantity: '', harvestDate: '', notes: '' })

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await traceApi.list({ ...query })
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

async function handleCreate() {
  await traceApi.create(addForm)
  showToast('批次创建成功')
  showAddSheet.value = false
  Object.assign(addForm, { orchardId: '', variety: '', quantity: '', harvestDate: '', notes: '' })
  loadList(true)
}

loadList(true)
</script>
