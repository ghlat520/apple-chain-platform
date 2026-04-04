<template>
  <div class="page-container" v-if="orchard">
    <van-card :title="orchard.name" :desc="orchard.location">
      <template #tags>
        <van-tag :type="orchard.status === 'ACTIVE' ? 'success' : 'default'" style="margin-right:4px;">
          {{ orchard.status === 'ACTIVE' ? '正常' : '休耕' }}
        </van-tag>
        <van-tag type="primary">{{ orchard.variety }}</van-tag>
      </template>
      <template #footer>
        <van-button size="small" @click="showEdit = true">编辑信息</van-button>
      </template>
    </van-card>

    <van-cell-group inset style="margin-top:12px;" title="基础信息">
      <van-cell title="果园面积" :value="`${orchard.area} 亩`" />
      <van-cell title="树龄" :value="`${orchard.treeAge || '-'} 年`" />
      <van-cell title="负责果农" :value="orchard.farmerName || '-'" />
      <van-cell title="注册日期" :value="orchard.createdAt" />
      <van-cell title="最近检测" :value="orchard.lastInspectionDate || '未检测'" />
    </van-cell-group>

    <div class="section-header" style="margin-top:16px;">
      <span class="section-title">溯源批次记录</span>
    </div>
    <van-cell-group inset>
      <van-cell
        v-for="record in traceBatches"
        :key="record.batchCode"
        :title="record.batchCode"
        :label="`${record.harvestDate} · ${record.quantity}kg`"
        is-link
        @click="router.push(`/trace/${record.batchCode}`)"
      >
        <template #right-icon>
          <van-tag type="success">{{ record.status }}</van-tag>
        </template>
      </van-cell>
      <van-empty v-if="traceBatches.length === 0" description="暂无溯源记录" image-size="60" />
    </van-cell-group>
  </div>
  <van-loading v-else type="spinner" vertical style="margin-top:120px;">加载中...</van-loading>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orchardApi } from '@/api/orchard'
import { traceApi } from '@/api/trace'

const route = useRoute()
const router = useRouter()
const orchard = ref(null)
const traceBatches = ref([])
const showEdit = ref(false)

onMounted(async () => {
  const [orchardRes, traceRes] = await Promise.all([
    orchardApi.getById(route.params.id),
    traceApi.list({ orchardId: route.params.id })
  ])
  orchard.value = orchardRes.data
  traceBatches.value = traceRes.data?.records || []
})
</script>
