<template>
  <div class="harvest-recommend-page">
    <van-nav-bar title="最佳采收推荐" left-arrow @click-left="$router.back()" />

    <van-cell-group inset title="查询" style="margin-top: 12px">
      <van-field
        v-model.number="orchardId"
        label="果园ID"
        type="number"
        placeholder="请输入果园ID"
      >
        <template #button>
          <van-button size="small" type="primary" :loading="loading" @click="loadRecommend">
            查询
          </van-button>
        </template>
      </van-field>
    </van-cell-group>

    <van-empty v-if="!recommend && !loading" description="请输入果园ID查询" />

    <van-cell-group v-if="recommend" inset title="推荐结果" style="margin-top: 16px">
      <van-cell title="状态" center>
        <template #value>
          <van-tag :type="statusTag(recommend.status)" size="large">
            {{ statusLabel(recommend.status) }}
          </van-tag>
        </template>
      </van-cell>
      <van-cell title="成熟度评分" :value="String(recommend.score)" />
      <van-cell title="置信度" :value="`${(Number(recommend.confidence) * 100).toFixed(0)}%`" />
      <van-cell title="推荐采收开始日" :value="recommend.windowStart" />
      <van-cell title="推荐采收截止日" :value="recommend.windowEnd" />
      <van-cell
        v-if="recommend.daysUntilOptimal > 0"
        title="距离最佳采收"
        :value="`约 ${recommend.daysUntilOptimal} 天`"
      />
      <van-cell title="说明" :label="recommend.message" />
    </van-cell-group>

    <van-cell-group v-if="records.length" inset title="历史采样" style="margin-top: 16px">
      <van-cell
        v-for="row in records"
        :key="row.id"
        :title="`${row.sampleDate} · ${varietyLabel(row.variety)}`"
        :label="`糖度 ${row.brix} · 硬度 ${row.firmness} · 积温 ${row.accumulateTemp}`"
      >
        <template #value>
          <van-tag :type="statusTag(row.recommendation)">
            {{ row.maturityScore }}
          </van-tag>
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showFailToast } from 'vant'
import { maturityApi } from '@/api/maturity.js'

const route = useRoute()
const orchardId = ref(route.query.orchardId ? Number(route.query.orchardId) : '')
const loading = ref(false)
const recommend = ref(null)
const records = ref([])

const VARIETY_LABELS = {
  red_fuji: '红富士',
  gala: '嘎拉',
  golden_delicious: '黄元帅'
}

function varietyLabel(v) { return VARIETY_LABELS[v] || v || '-' }

function statusTag(s) {
  return { OPTIMAL: 'success', UNRIPE: 'warning', OVERRIPE: 'danger' }[s] || 'default'
}

function statusLabel(s) {
  return { OPTIMAL: '最佳采收期', UNRIPE: '尚未成熟', OVERRIPE: '已过熟，请立即采收' }[s] || s || '-'
}

async function loadRecommend() {
  if (!orchardId.value) {
    showFailToast('请输入果园ID')
    return
  }
  loading.value = true
  try {
    const [rec, list] = await Promise.all([
      maturityApi.recommend(orchardId.value),
      maturityApi.listOrchardRecords(orchardId.value, 10)
    ])
    recommend.value = rec
    records.value = list || []
  } catch (e) {
    recommend.value = null
    records.value = []
    showFailToast(e?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (orchardId.value) {
    loadRecommend()
  }
})
</script>

<style scoped>
.harvest-recommend-page { padding-bottom: 32px; background: #f7f8fa; min-height: 100vh; }
</style>
