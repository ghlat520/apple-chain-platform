<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">收购交易</span>
      <van-button type="primary" size="small" icon="plus" @click="showAddSheet = true">发起交易</van-button>
    </div>

    <van-tabs v-model:active="activeStatus" @change="handleTabChange" style="margin-bottom:12px;">
      <van-tab title="全部" name="" />
      <van-tab title="待审核" name="PENDING" />
      <van-tab title="进行中" name="IN_PROGRESS" />
      <van-tab title="已完成" name="COMPLETED" />
      <van-tab title="已取消" name="CANCELLED" />
    </van-tabs>

    <van-search v-model="query.keyword" placeholder="搜索交易编号/果农名称" @search="loadList(true)" shape="round" />

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-swipe-cell v-for="item in list" :key="item.id" style="margin-bottom:8px;">
          <van-cell-group inset>
            <van-cell :title="`交易单 #${item.tradeNo}`" :label="`果农: ${item.farmerName} · ${item.createdAt}`">
              <template #right-icon>
                <van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag>
              </template>
            </van-cell>
            <van-cell title="品种" :value="item.variety" />
            <van-cell title="数量" :value="`${item.quantity} kg`" />
            <van-cell title="单价" :value="`¥${item.unitPrice}/kg`" />
            <van-cell title="总金额" :value="`¥${item.totalAmount}`" />
          </van-cell-group>
          <template #right>
            <van-button
              v-if="item.status === 'PENDING'"
              square
              type="primary"
              text="确认"
              style="height:100%;"
              @click="updateStatus(item.id, 'IN_PROGRESS')"
            />
          </template>
        </van-swipe-cell>
        <van-empty v-if="!loading && list.length === 0" description="暂无交易记录" />
      </van-list>
    </van-pull-refresh>

    <van-action-sheet v-model:show="showAddSheet" title="发起收购交易">
      <div style="padding:16px;">
        <van-form @submit="handleCreate">
          <van-field v-model="addForm.farmerId" label="果农ID" placeholder="请输入果农ID" :rules="[{required:true}]" />
          <van-field v-model="addForm.variety" label="苹果品种" placeholder="如：红富士" :rules="[{required:true}]" />
          <van-field v-model="addForm.quantity" label="数量(kg)" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.unitPrice" label="单价(元/kg)" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.expectedDate" label="预计交货日" placeholder="YYYY-MM-DD" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">发起交易</van-button>
        </van-form>
      </div>
    </van-action-sheet>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { tradeApi } from '@/api/trade'
import { showToast } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const activeStatus = ref('')
const query = reactive({ keyword: '', status: '', page: 1, size: 10 })
const addForm = reactive({ farmerId: '', variety: '', quantity: '', unitPrice: '', expectedDate: '' })

const statusMap = {
  PENDING: { label: '待审核', type: 'warning' },
  IN_PROGRESS: { label: '进行中', type: 'primary' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'default' }
}

function statusLabel(s) { return statusMap[s]?.label || s }
function statusTagType(s) { return statusMap[s]?.type || 'default' }

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await tradeApi.list({ ...query })
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

function handleTabChange(val) { query.status = val; loadList(true) }

async function updateStatus(id, status) {
  await tradeApi.updateStatus(id, status)
  showToast('状态已更新')
  loadList(true)
}

async function handleCreate() {
  await tradeApi.create(addForm)
  showToast('交易发起成功')
  showAddSheet.value = false
  Object.assign(addForm, { farmerId: '', variety: '', quantity: '', unitPrice: '', expectedDate: '' })
  loadList(true)
}

loadList(true)
</script>
