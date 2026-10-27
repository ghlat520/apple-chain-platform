<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">采收管理</span>
    </div>

    <van-tabs v-model:active="activeStatus" @change="handleTabChange" style="margin-bottom:12px;">
      <van-tab title="全部" name="" />
      <van-tab title="已完成" name="COMPLETED" />
      <van-tab title="进行中" name="IN_PROGRESS" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell
            :title="`采收批次 ${item.batchCode}`"
            :label="`${item.orchardName} · ${item.farmerName} · ${item.harvestDate}`"
            is-link
            @click="router.push(`/harvest/${item.id}`)"
          >
            <template #right-icon>
              <van-tag :type="item.status === 'COMPLETED' ? 'success' : 'primary'">
                {{ item.status === 'COMPLETED' ? '已完成' : '进行中' }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell title="品种" :value="item.variety" />
          <van-cell title="数量" :value="`${item.quantity} ${item.unit}`" />
          <van-cell title="等级" :value="`${item.grade}级`" />
          <van-cell title="糖度" :value="`${item.sugarDegree}度`" />
          <van-cell title="农残检测" :value="item.pesticideCheck === 'PASS' ? '合格' : '待检'" />
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无采收记录" />
      </van-list>
    </van-pull-refresh>

    <!-- 演示链路导航 -->
    <div style="padding:16px;">
      <van-button
        block
        type="primary"
        color="#07c160"
        icon="guide-o"
        @click="router.push('/trace')"
      >
        查看溯源码
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { harvestApi } from '@/api/harvest'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const activeStatus = ref('')
const query = reactive({ keyword: '', status: '', page: 1, size: 10 })

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await harvestApi.list({ ...query })
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

loadList(true)
</script>
