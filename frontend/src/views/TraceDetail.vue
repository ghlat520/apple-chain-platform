<template>
  <div class="page-container" v-if="record">
    <div class="trace-header">
      <van-tag type="success" size="large">批次编号: {{ record.batchCode }}</van-tag>
    </div>

    <van-steps direction="vertical" :active="record.stepsCompleted || 3" style="margin:16px 0;" active-color="#07c160">
      <van-step>
        <h3>果园采收</h3>
        <p>{{ record.harvestDate }} · {{ record.orchardName }}</p>
      </van-step>
      <van-step>
        <h3>质量检测</h3>
        <p>{{ record.inspectionResult || '已通过质检' }}</p>
      </van-step>
      <van-step>
        <h3>仓储入库</h3>
        <p>{{ record.warehouseName || '-' }}</p>
      </van-step>
      <van-step>
        <h3>冷链运输</h3>
        <p>{{ record.logisticsInfo || '待发货' }}</p>
      </van-step>
      <van-step>
        <h3>终端销售</h3>
        <p>{{ record.salesInfo || '待销售' }}</p>
      </van-step>
    </van-steps>

    <van-cell-group inset title="批次详情">
      <van-cell title="苹果品种" :value="record.variety" />
      <van-cell title="采收数量" :value="`${record.quantity} kg`" />
      <van-cell title="果园" :value="record.orchardName" />
      <van-cell title="果农" :value="record.farmerName" />
      <van-cell title="区块链哈希" :value="record.blockchainHash || '待上链'" />
      <van-cell title="上链时间" :value="record.chainedAt || '-'" />
    </van-cell-group>
  </div>
  <van-loading v-else type="spinner" vertical style="margin-top:120px;">加载中...</van-loading>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { traceApi } from '@/api/trace'

const route = useRoute()
const record = ref(null)

onMounted(async () => {
  const res = await traceApi.getByBatchCode(route.params.batchCode)
  record.value = res.data
})
</script>

<style scoped>
.trace-header {
  padding: 16px 0;
  text-align: center;
}
</style>
