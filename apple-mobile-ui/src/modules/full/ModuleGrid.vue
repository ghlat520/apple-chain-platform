<template>
  <div class="module-grid-page">
    <!-- Header -->
    <div class="page-header">
      <h1 class="page-title">苹果产业链管理平台</h1>
      <p class="page-subtitle">全链路数字化管理与溯源</p>
    </div>

    <!-- Module Grid -->
    <div class="module-grid">
      <div
        v-for="mod in modules"
        :key="mod.name"
        class="module-card"
        tabindex="0"
        role="link"
        :aria-label="`进入${mod.label}`"
        @click="router.push(mod.path)"
        @keydown.enter="router.push(mod.path)"
      >
        <div class="module-icon-wrap" :style="{ background: mod.bgColor }">
          <span class="module-emoji">{{ mod.icon }}</span>
        </div>
        <span class="module-label">{{ mod.label }}</span>
      </div>
    </div>

    <!-- Chain Flow Strip -->
    <div class="chain-flow">
      <h3 class="chain-flow-title">产业链流程</h3>
      <div class="chain-flow-steps">
        <template v-for="(step, idx) in chainSteps" :key="step.label">
          <div class="chain-flow-node">
            <span class="chain-emoji" :style="{ color: step.color }">{{ step.icon }}</span>
            <span class="chain-flow-label">{{ step.label }}</span>
          </div>
          <span v-if="idx < chainSteps.length - 1" class="chain-flow-arrow">→</span>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const modules = [
  {
    label: '种植管理',
    name: 'farm',
    icon: '🌳',
    path: '/farm/orchards',
    bgColor: 'var(--color-leaf-green-light)',
  },
  {
    label: '种植作业',
    name: 'cultivation',
    icon: '🌱',
    path: '/cultivation/batches',
    bgColor: 'var(--color-leaf-green-light)',
  },
  {
    label: '农资管理',
    name: 'input',
    icon: '📦',
    path: '/input/products',
    bgColor: 'var(--color-brand-light)',
  },
  {
    label: '溯源管理',
    name: 'trace',
    icon: '🔍',
    path: '/trace/records',
    bgColor: 'var(--color-brand-light)',
  },
  {
    label: '交易管理',
    name: 'trade',
    icon: '📋',
    path: '/trade/orders',
    bgColor: 'var(--color-warningBg)',
  },
  {
    label: '仓储管理',
    name: 'warehouse',
    icon: '🏭',
    path: '/warehouse/warehouses',
    bgColor: 'var(--color-infoBg)',
  },
  {
    label: '冷链物流',
    name: 'coldchain',
    icon: '🚛',
    path: '/coldchain/tasks',
    bgColor: 'var(--color-infoBg)',
  },
  {
    label: '供应链金融',
    name: 'finance',
    icon: '💰',
    path: '/finance/loans',
    bgColor: 'var(--color-warningBg)',
  },
  {
    label: '大数据管理',
    name: 'bigdata',
    icon: '📊',
    path: '/bigdata/data-asset',
    bgColor: 'var(--color-errorBg)',
  },
  {
    label: 'IoT 设备',
    name: 'iot',
    icon: '📡',
    path: '/trace/records',
    bgColor: 'var(--color-successBg)',
  },
  {
    label: '系统管理',
    name: 'admin',
    icon: '⚙️',
    path: '/admin/users',
    bgColor: 'var(--color-surface-raised)',
  },
]

const chainSteps = [
  { label: '种植', icon: '🌳', color: 'var(--color-leaf-green)' },
  { label: '采收', icon: '🍎', color: 'var(--color-leaf-green)' },
  { label: '溯源', icon: '🔍', color: 'var(--color-brand-primary)' },
  { label: '质检', icon: '✅', color: 'var(--color-warning)' },
  { label: '仓储', icon: '🏭', color: 'var(--color-info)' },
  { label: '冷链', icon: '🚛', color: 'var(--color-info)' },
  { label: '交易', icon: '📋', color: 'var(--color-brand-primary)' },
  { label: '金融', icon: '💰', color: 'var(--color-warning)' },
]
</script>

<style scoped>
.module-grid-page {
  padding: var(--space-5) var(--space-4);
  padding-bottom: var(--space-8);
}

.page-header {
  text-align: center;
  padding: var(--space-6) 0 var(--space-5);
}

.page-title {
  font-size: var(--space-6);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
  line-height: 1.35;
}

.page-subtitle {
  font-size: var(--space-3);
  color: var(--color-text-tertiary);
  margin: var(--space-1) 0 0;
}

/* Module Grid: 4 columns */
.module-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-3);
}

.module-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-2);
  background: var(--color-surface-raised);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-1);
  cursor: pointer;
  transition: transform var(--motion-base) var(--ease-out),
              box-shadow var(--motion-base) var(--ease-out);
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.module-card:hover {
  transform: translateY(calc(var(--space-1) * -1));
  box-shadow: var(--shadow-2);
}

.module-card:active {
  transform: scale(0.97);
  transition-duration: var(--motion-fast);
}

.module-card:focus-visible {
  outline: var(--space-1) solid var(--color-brand-primary);
  outline-offset: var(--space-1);
}

.module-icon-wrap {
  width: calc(var(--space-8) + var(--space-4));
  height: calc(var(--space-8) + var(--space-4));
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.module-emoji {
  font-size: var(--font-size-display-l);
  line-height: 1;
}

.module-label {
  font-size: var(--space-3);
  font-weight: 500;
  color: var(--color-text-primary);
  text-align: center;
  white-space: nowrap;
}

/* Chain Flow Strip */
.chain-flow {
  margin-top: var(--space-6);
  background: var(--color-surface-raised);
  border-radius: var(--radius-md);
  padding: var(--space-4) var(--space-3);
  box-shadow: var(--shadow-1);
}

.chain-flow-title {
  font-size: var(--space-3);
  font-weight: 600;
  color: var(--color-text-secondary);
  margin: 0 0 var(--space-3);
  text-align: center;
}

.chain-flow-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  flex-wrap: wrap;
}

.chain-flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
}

.chain-emoji {
  font-size: var(--font-size-h1);
  line-height: 1;
}

.chain-flow-label {
  font-size: var(--space-2);
  color: var(--color-text-tertiary);
}

.chain-flow-arrow {
  margin: 0 var(--space-1);
  margin-bottom: var(--space-5);
  color: var(--color-text-tertiary);
  font-size: var(--font-size-caption);
}

@media (prefers-reduced-motion: reduce) {
  .module-card {
    transition: none;
  }
}
</style>
