<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">业务模块</span>
    </div>

    <div class="module-grid">
      <div
        v-for="mod in modules"
        :key="mod.path"
        class="module-card"
        @click="navigateTo(mod)"
      >
        <div class="module-icon" :style="{ background: mod.color }">
          <van-icon :name="mod.icon" size="28" color="#fff" />
        </div>
        <div class="module-name">{{ mod.name }}</div>
        <div class="module-desc">{{ mod.desc }}</div>
      </div>
    </div>

    <div class="section-header" style="margin-top:16px;">
      <span class="section-title">数据闭环链路</span>
    </div>
    <div class="chain-flow">
      <div v-for="(step, idx) in chainSteps" :key="idx" class="chain-step">
        <span class="chain-dot" :style="{ background: step.color }"></span>
        <span class="chain-label">{{ step.label }}</span>
        <van-icon v-if="idx < chainSteps.length - 1" name="arrow" class="chain-arrow" />
      </div>
    </div>

    <van-action-sheet
      v-model:show="showPicker"
      :actions="pickerActions"
      :title="currentMod?.name || ''"
      cancel-text="取消"
      @select="onPickerSelect"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const modules = [
  {
    name: '果园管理',
    desc: '种植基地、果农',
    icon: 'flower-o',
    color: '#07c160',
    path: '/orchards',
    subModules: [
      { name: '果园列表', path: '/orchards' },
      { name: '果园地图', path: '/orchards-map' },
      { name: '果农管理', path: '/farmers' }
    ]
  },
  {
    name: '农资管理',
    desc: '供应商、采购、库存',
    icon: 'point-gift-o',
    color: '#ff976a',
    path: '/input/suppliers',
    subModules: [
      { name: '供应商', path: '/input/suppliers' },
      { name: '农资产品', path: '/input/products' },
      { name: '采购管理', path: '/input/purchases' },
      { name: '库存管理', path: '/input/inventory' },
      { name: '使用记录', path: '/input/usage' },
      { name: '溯源链路', path: '/input/trace' }
    ]
  },
  {
    name: '采收管理',
    desc: '采收批次、质检',
    icon: 'todo-list-o',
    color: '#4caf50',
    path: '/harvest'
  },
  {
    name: '溯源管理',
    desc: '批次溯源、链路查询',
    icon: 'search',
    color: '#1989fa',
    path: '/trace'
  },
  {
    name: '收购交易',
    desc: '订单、结算',
    icon: 'bill-o',
    color: '#ee0a24',
    path: '/trades'
  },
  {
    name: '仓储管理',
    desc: '仓库、仓单、出入库',
    icon: 'shop-o',
    color: '#7232dd',
    path: '/warehouse',
    subModules: [
      { name: '仓库列表', path: '/warehouse' },
      { name: '仓单管理', path: '/warehouse/receipts' },
      { name: '出入库记录', path: '/warehouse/records' },
      { name: '仓储统计', path: '/warehouse/stats' }
    ]
  },
  {
    name: '冷链物流',
    desc: '车辆、运输、预冷',
    icon: 'logistics',
    color: '#00b578',
    path: '/coldchain/vehicles',
    subModules: [
      { name: '车辆管理', path: '/coldchain/vehicles' },
      { name: '运输管理', path: '/coldchain/transports' },
      { name: '预冷管理', path: '/coldchain/precooling' },
      { name: '冷链统计', path: '/coldchain/stats' }
    ]
  },
  {
    name: '金融服务',
    desc: '贷款、质押、风控',
    icon: 'gold-coin-o',
    color: '#faab0c',
    path: '/finance/loans',
    subModules: [
      { name: '贷款管理', path: '/finance/loans' },
      { name: '仓单质押', path: '/finance/pledges' },
      { name: '信用评级', path: '/finance/ratings' },
      { name: '风控管理', path: '/finance/risks' },
      { name: '金融统计', path: '/finance/stats' }
    ]
  },
  {
    name: '数据中心',
    desc: '总览、趋势、待办',
    icon: 'chart-trending-o',
    color: '#4fc08d',
    path: '/dashboard'
  }
]

const chainSteps = [
  { label: '果园', color: '#07c160' },
  { label: '种植', color: '#2db84d' },
  { label: '采收', color: '#4caf50' },
  { label: '溯源', color: '#1989fa' },
  { label: '仓储', color: '#7232dd' },
  { label: '冷链', color: '#00b578' },
  { label: '交易', color: '#ee0a24' },
  { label: '金融', color: '#faab0c' }
]

const showPicker = ref(false)
const pickerActions = ref([])
const currentMod = ref(null)

function navigateTo(mod) {
  if (mod.subModules && mod.subModules.length > 0) {
    currentMod.value = mod
    pickerActions.value = mod.subModules.map(s => ({ name: s.name, value: s.path }))
    showPicker.value = true
  } else {
    router.push(mod.path)
  }
}

function onPickerSelect(action) {
  router.push(action.value)
}
</script>

<style scoped>
.module-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 12px;
}
.module-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: transform 0.15s;
}
.module-card:active {
  transform: scale(0.96);
}
.module-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}
.module-name {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 2px;
}
.module-desc {
  font-size: 11px;
  color: #999;
}
.chain-flow {
  display: flex;
  align-items: center;
  overflow-x: auto;
  padding: 12px 16px;
  background: #fff;
  border-radius: 12px;
  margin: 0 12px 12px;
}
.chain-step {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.chain-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
}
.chain-label {
  font-size: 13px;
  color: #333;
  white-space: nowrap;
}
.chain-arrow {
  margin: 0 8px;
  color: #ccc;
}
</style>
