<template>
  <div class="page-container" v-if="record">
    <div class="harvest-header">
      <van-tag type="success" size="large">批次编号: {{ record.batchCode }}</van-tag>
      <van-tag :type="record.status === 'COMPLETED' ? 'success' : 'primary'" style="margin-left:8px;">
        {{ record.status === 'COMPLETED' ? '已完成' : '进行中' }}
      </van-tag>
    </div>

    <van-cell-group inset title="采收信息">
      <van-cell title="果园" :value="record.orchardName" />
      <van-cell title="果农" :value="record.farmerName" />
      <van-cell title="苹果品种" :value="record.variety" />
      <van-cell title="采收日期" :value="record.harvestDate" />
      <van-cell title="采收数量" :value="`${record.quantity} ${record.unit}`" />
      <van-cell title="等级" :value="`${record.grade}级`" />
      <van-cell title="用工人数" :value="`${record.workers} 人`" />
    </van-cell-group>

    <van-cell-group inset title="质检信息">
      <van-cell title="糖度" :value="`${record.sugarDegree} 度`" />
      <van-cell title="果径" :value="record.fruitDiameter" />
      <van-cell title="农残检测" :value="record.pesticideCheck === 'PASS' ? '合格' : '待检'" />
      <van-cell title="检测机构" :value="record.pesticideCheckOrg || '-'" />
    </van-cell-group>

    <van-cell-group inset title="备注">
      <van-cell :value="record.remark || '-'" />
    </van-cell-group>

    <!-- 演示链路导航 -->
    <div style="padding:16px;">
      <van-button
        block
        type="primary"
        color="#07c160"
        icon="guide-o"
        @click="router.push('/trace')"
      >
        下一步：查看溯源码
      </van-button>
    </div>
  </div>
  <van-loading v-else type="spinner" vertical style="margin-top:120px;">加载中...</van-loading>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { harvestApi } from '@/api/harvest'

const route = useRoute()
const router = useRouter()
const record = ref(null)

onMounted(async () => {
  const res = await harvestApi.getById(route.params.id)
  record.value = res.data
})
</script>

<style scoped>
.harvest-header {
  padding: 16px 0;
  text-align: center;
}
</style>
