<template>
  <div class="scan-page">
    <div class="scan-header">
      <h2>苹果产业链溯源查询</h2>
      <p>扫码验证产品全程可追溯</p>
    </div>

    <van-loading v-if="loading" size="48px" style="text-align:center;margin:60px 0" />

    <div v-else-if="traceData" class="scan-result">
      <van-cell-group inset title="产品信息">
        <van-cell title="溯源码" :value="traceData.traceCode" />
        <van-cell title="产品类型" :value="traceData.productType || '-'" />
        <van-cell title="批次编号" :value="traceData.batchNo || '-'" />
        <van-cell title="当前状态">
          <template #value>
            <van-tag :type="statusTagType(traceData.currentStatus)" size="large">
              {{ statusLabel(traceData.currentStatus) }}
            </van-tag>
          </template>
        </van-cell>
        <van-cell title="上链状态">
          <template #value>
            <van-tag :type="traceData.chainStatus === 1 ? 'success' : 'warning'">
              {{ traceData.chainStatus === 1 ? '已上链' : '待上链' }}
            </van-tag>
          </template>
        </van-cell>
      </van-cell-group>

      <van-cell-group inset title="溯源时间轴" style="margin-top:12px" v-if="traceData.timeline && traceData.timeline.length">
        <van-steps direction="vertical" :active="traceData.timeline.length - 1" active-color="#07c160">
          <van-step v-for="node in traceData.timeline" :key="node.id">
            <h4>{{ nodeTypeLabel(node.nodeType) }}</h4>
            <p style="color:#323233">{{ node.summary }}</p>
            <p style="color:#969799;font-size:12px">
              {{ node.operatorName || '-' }} · {{ node.location || '-' }}
            </p>
            <p style="color:#c8c9cc;font-size:12px">{{ formatTime(node.nodeTime) }}</p>
          </van-step>
        </van-steps>
      </van-cell-group>

      <div style="text-align:center;padding:24px;color:#969799;font-size:12px">
        苹果产业链云服务平台 提供溯源验证
      </div>
    </div>

    <div v-else-if="error" class="scan-error">
      <van-empty image="error" :description="error" />
      <van-button type="primary" size="small" @click="$router.push('/login')" style="margin-top:16px">
        返回登录
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const loading = ref(true)
const traceData = ref(null)
const error = ref(null)

function statusTagType(status) {
  const map = { PLANTED: 'primary', HARVESTED: 'success', IN_STORAGE: 'warning', IN_TRANSIT: 'warning', SOLD: 'default' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { PLANTED: '种植中', HARVESTED: '已采收', IN_STORAGE: '入库中', IN_TRANSIT: '运输中', SOLD: '已售出' }
  return map[status] || status || '-'
}

function nodeTypeLabel(type) {
  const map = { PLANT: '种植', GROW: '生长', HARVEST: '采收', INSPECT: '质检', STORAGE: '入库', TRANSPORT: '运输', LOGISTICS: '物流', SELL: '销售', TRADE: '交易' }
  return map[type] || type || '-'
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(async () => {
  const code = route.params.code
  if (!code) {
    error.value = '未提供溯源码'
    loading.value = false
    return
  }
  try {
    const res = await axios.get(`/api/trace/scan/${code}`)
    if (res.data.code === 200) {
      traceData.value = res.data.data
    } else {
      error.value = res.data.message || '查询失败'
    }
  } catch (e) {
    error.value = '未找到溯源信息，请核实溯源码'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.scan-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e8f5e9 0%, #f7f8fa 30%);
}
.scan-header {
  text-align: center;
  padding: 32px 16px 16px;
}
.scan-header h2 {
  font-size: 20px;
  color: #323233;
  margin: 0;
}
.scan-header p {
  font-size: 14px;
  color: #969799;
  margin-top: 4px;
}
.scan-result { padding-bottom: 20px; }
.scan-error { text-align: center; padding: 40px 16px; }
</style>
