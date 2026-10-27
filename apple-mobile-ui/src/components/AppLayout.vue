<template>
  <div class="app-layout">
    <van-nav-bar
      :title="currentTitle"
      left-arrow
      @click-left="handleBack"
      fixed
      placeholder
    >
      <template #right>
        <van-icon name="user-o" size="18" @click="showUserMenu = true" />
      </template>
    </van-nav-bar>

    <div class="page-content">
      <router-view />
    </div>

    <van-tabbar v-model="activeTab" fixed safe-area-inset-bottom @change="handleTabChange">
      <van-tabbar-item icon="home-o" name="/">首页</van-tabbar-item>
      <van-tabbar-item icon="flower-o" name="/cultivation/batches">种植</van-tabbar-item>
      <van-tabbar-item icon="label-o" name="/input/products">农资</van-tabbar-item>
      <van-tabbar-item icon="search" name="/trace/records">溯源</van-tabbar-item>
      <van-tabbar-item icon="shop-o" name="/trade/orders">交易</van-tabbar-item>
    </van-tabbar>

    <van-popup v-model:show="showUserMenu" position="right" :style="{ width: '240px', height: '100%' }">
      <div class="user-menu">
        <div class="user-info">
          <van-icon name="user-circle-o" size="48" color="#1989fa" />
          <p class="user-name">{{ authStore.user?.username || '用户' }}</p>
        </div>
        <van-collapse v-model="expandedModules" class="menu-collapse" accordion>
          <van-collapse-item title="种植管理" name="farm" icon="flower-o">
            <van-cell title="果园管理" icon="wap-home-o" is-link @click="navigate('/farm/orchards')" />
            <van-cell title="种植批次" icon="flower-o" is-link @click="navigate('/cultivation/batches')" />
            <van-cell title="种植作业" icon="todo-list-o" is-link @click="navigate('/cultivation/operations')" />
            <van-cell title="生长记录" icon="notes-o" is-link @click="navigate('/cultivation/growth-records')" />
            <van-cell title="农户管理" icon="contact" is-link @click="navigate('/farm/farmers')" />
            <van-cell title="果园地图" icon="map-marked" is-link @click="navigate('/farm/orchard-map')" />
            <van-cell title="采收批次" icon="todo-list-o" is-link @click="navigate('/planting/harvest-batch')" />
            <van-cell title="AI作业计划" icon="clock-o" is-link @click="navigate('/planting/task-plan')" />
            <van-cell title="种植分析" icon="chart-trending-o" is-link @click="navigate('/planting/analysis')" />
            <van-cell title="模块入口" icon="apps-o" is-link @click="navigate('/modules')" />
          </van-collapse-item>
          <van-collapse-item title="农资管理" name="input" icon="label-o">
            <van-cell title="农资产品" icon="label-o" is-link @click="navigate('/input/products')" />
            <van-cell title="农资供应商" icon="friends-o" is-link @click="navigate('/input/suppliers')" />
            <van-cell title="农资采购" icon="cart-o" is-link @click="navigate('/input/purchases')" />
            <van-cell title="农资库存" icon="balance-list-o" is-link @click="navigate('/input/inventory')" />
            <van-cell title="使用记录" icon="todo-list-o" is-link @click="navigate('/input/usage')" />
          </van-collapse-item>
          <van-collapse-item title="溯源管理" name="trace" icon="scan">
            <van-cell title="溯源记录" icon="records" is-link @click="navigate('/trace/records')" />
            <van-cell title="溯源查询" icon="search" is-link @click="navigate('/trace/query')" />
            <van-cell title="异常追溯" icon="warning-o" is-link @click="navigate('/trace/anomaly')" />
            <van-cell title="区块链存证" icon="link-o" is-link @click="navigate('/trace/blockchain')" />
          </van-collapse-item>
          <van-collapse-item title="交易管理" name="trade" icon="shopping-cart-o">
            <van-cell title="供货信息" icon="goods-collect-o" is-link @click="navigate('/trade/supply')" />
            <van-cell title="交易订单" icon="orders-o" is-link @click="navigate('/trade/orders')" />
            <van-cell title="采购需求" icon="shopping-cart-o" is-link @click="navigate('/trade/purchase-needs')" />
            <van-cell title="质量检验" icon="checked" is-link @click="navigate('/trade/inspection')" />
            <van-cell title="交易统计" icon="chart-trending-o" is-link @click="navigate('/trade/stats')" />
            <van-cell title="供需撮合" icon="exchange" is-link @click="navigate('/trade/matching')" />
          </van-collapse-item>
          <van-collapse-item title="仓储管理" name="warehouse" icon="shop-o">
            <van-cell title="仓库管理" icon="shop-o" is-link @click="navigate('/warehouse/list')" />
            <van-cell title="出入库记录" icon="logistics" is-link @click="navigate('/warehouse/records')" />
            <van-cell title="智能仓单" icon="certificate" is-link @click="navigate('/warehouse/receipts')" />
            <van-cell title="仓储统计" icon="chart-trending-o" is-link @click="navigate('/warehouse/statistics')" />
          </van-collapse-item>
          <van-collapse-item title="冷链物流" name="coldchain" icon="logistics">
            <van-cell title="车辆管理" icon="logistics" is-link @click="navigate('/coldchain/vehicles')" />
            <van-cell title="运输任务" icon="guide-o" is-link @click="navigate('/coldchain/tasks')" />
            <van-cell title="温度监控" icon="fire-o" is-link @click="navigate('/coldchain/temperatures')" />
            <van-cell title="配送管理" icon="send-gift-o" is-link @click="navigate('/coldchain/deliveries')" />
            <van-cell title="预冷管理" icon="snow-o" is-link @click="navigate('/coldchain/precooling')" />
            <van-cell title="冷链统计" icon="chart-trending-o" is-link @click="navigate('/coldchain/statistics')" />
          </van-collapse-item>
          <van-collapse-item title="供应链金融" name="finance" icon="gold-coin-o">
            <van-cell title="信用评级" icon="medal-o" is-link @click="navigate('/finance/credits')" />
            <van-cell title="贷款管理" icon="gold-coin-o" is-link @click="navigate('/finance/loans')" />
            <van-cell title="仓单质押" icon="certificate" is-link @click="navigate('/finance/pledges')" />
            <van-cell title="风控管理" icon="shield-o" is-link @click="navigate('/finance/risks')" />
            <van-cell title="金融统计" icon="chart-trending-o" is-link @click="navigate('/finance/statistics')" />
          </van-collapse-item>
          <van-collapse-item title="大数据管理" name="bigdata" icon="chart-trending-o">
            <van-cell title="数据资产" icon="cluster-o" is-link @click="navigate('/bigdata/data-asset')" />
            <van-cell title="数据源管理" icon="database-o" is-link @click="navigate('/bigdata/data-source')" />
            <van-cell title="数据质量" icon="checked" is-link @click="navigate('/bigdata/data-quality')" />
            <van-cell title="采集任务" icon="todo-list-o" is-link @click="navigate('/bigdata/collect-job')" />
            <van-cell title="数据血缘" icon="link-o" is-link @click="navigate('/bigdata/lineage')" />
            <van-cell title="指标中心" icon="chart-trending-o" is-link @click="navigate('/bigdata/metric-center')" />
            <van-cell title="分析报告" icon="description" is-link @click="navigate('/bigdata/report')" />
            <van-cell title="审计日志" icon="records" is-link @click="navigate('/bigdata/audit-log')" />
            <van-cell title="开放API" icon="exchange" is-link @click="navigate('/bigdata/open-api')" />
            <van-cell title="大屏配置" icon="tv-o" is-link @click="navigate('/bigdata/screen-config')" />
          </van-collapse-item>
          <van-collapse-item title="系统管理" name="admin" icon="setting-o">
            <van-cell title="角色权限" icon="shield-o" is-link @click="navigate('/admin/roles')" />
            <van-cell title="用户管理" icon="friends-o" is-link @click="navigate('/admin/users')" />
          </van-collapse-item>
        </van-collapse>
        <div class="logout-btn">
          <van-button block type="danger" plain @click="handleLogout">退出登录</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { showConfirmDialog } from 'vant'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const showUserMenu = ref(false)
const activeTab = ref('/')
const expandedModules = ref('')

const titleMap = {
  '/': '苹果产业链云服务平台',
  '/farm/orchards': '果园管理',
  '/cultivation/batches': '种植批次',
  '/cultivation/operations': '种植作业',
  '/cultivation/growth-records': '生长记录',
  '/farm/farmers': '农户管理',
  '/trace/records': '溯源记录',
  '/trace/query': '溯源查询',
  '/trace/anomaly': '异常追溯',
  '/trace/blockchain': '区块链存证',
  '/trade/supply': '供货信息',
  '/trade/orders': '交易订单',
  '/trade/purchase-needs': '采购需求',
  '/trade/inspection': '质量检验',
  '/trade/stats': '交易统计',
  '/trade/matching': '供需撮合',
  '/input/products': '农资产品',
  '/input/suppliers': '农资供应商',
  '/input/purchases': '农资采购',
  '/input/inventory': '农资库存',
  '/input/usage': '农资使用记录',
  '/warehouse/list': '仓库管理',
  '/warehouse/records': '出入库记录',
  '/warehouse/receipts': '智能仓单',
  '/coldchain/vehicles': '车辆管理',
  '/coldchain/tasks': '运输任务',
  '/coldchain/temperatures': '温度监控',
  '/coldchain/deliveries': '配送管理',
  '/finance/credits': '信用评级',
  '/finance/loans': '贷款管理',
  '/finance/pledges': '仓单质押',
  '/finance/risks': '风控管理',
  '/admin/roles': '角色权限',
  '/admin/users': '用户管理',
  '/modules': '功能模块',
  '/farm/orchard-map': '果园地图',
  '/farm/orchard/': '果园详情',
  '/trace/detail/': '溯源详情',
  '/input/trace-chain/': '农资溯源链',
  '/coldchain/precooling': '预冷管理',
  '/coldchain/statistics': '冷链统计',
  '/warehouse/statistics': '仓储统计',
  '/finance/statistics': '金融统计',
  '/planting/harvest-batch': '采收批次',
  '/planting/task-plan': 'AI作业计划',
  '/planting/analysis': '种植分析',
  '/bigdata/data-asset': '数据资产',
  '/bigdata/data-source': '数据源管理',
  '/bigdata/data-quality': '数据质量',
  '/bigdata/collect-job': '采集任务',
  '/bigdata/lineage': '数据血缘',
  '/bigdata/metric-center': '指标中心',
  '/bigdata/report': '分析报告',
  '/bigdata/audit-log': '审计日志',
  '/bigdata/open-api': '开放API',
  '/bigdata/screen-config': '大屏配置'
}

const currentTitle = computed(() => {
  // 精确匹配优先
  if (titleMap[route.path]) return titleMap[route.path]
  // 前缀匹配（用于动态参数路由如 /farm/orchard/123、/trace/detail/BATCH001）
  const matched = Object.keys(titleMap)
    .filter(k => k.endsWith('/'))
    .sort((a, b) => b.length - a.length)
    .find(k => route.path.startsWith(k))
  return matched ? titleMap[matched] : '苹果产业链云服务平台'
})

watch(
  () => route.path,
  (path) => {
    const tabRoutes = ['/', '/cultivation/batches', '/input/products', '/trace/records', '/trade/orders']
    activeTab.value = tabRoutes.includes(path) ? path : activeTab.value
  },
  { immediate: true }
)

function handleBack() {
  if (route.path !== '/') {
    router.back()
  }
}

function handleTabChange(name) {
  router.push(name)
}

const moduleRouteMap = {
  farm: ['/farm/', '/cultivation/', '/planting/', '/modules'],
  input: ['/input/'],
  trace: ['/trace/'],
  trade: ['/trade/'],
  warehouse: ['/warehouse/'],
  coldchain: ['/coldchain/'],
  finance: ['/finance/'],
  bigdata: ['/bigdata/'],
  admin: ['/admin/']
}

function getModuleByPath(path) {
  const matched = Object.entries(moduleRouteMap).find(([, prefixes]) =>
    prefixes.some(p => path.startsWith(p))
  )
  return matched ? matched[0] : ''
}

watch(
  () => route.path,
  (path) => {
    expandedModules.value = getModuleByPath(path)
  },
  { immediate: true }
)

function navigate(path) {
  showUserMenu.value = false
  router.push(path)
}

async function handleLogout() {
  showUserMenu.value = false
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确认退出登录？'
    })
    await authStore.logout()
  } catch (e) {
    // user cancelled
  }
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: #f7f8fa;
}

.page-content {
  padding-bottom: 60px;
}

.user-menu {
  padding: 20px 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  border-bottom: 1px solid #ebedf0;
  margin-bottom: 12px;
}

.user-name {
  margin-top: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #323233;
}

.menu-collapse {
  margin: 0 8px;
}

.menu-collapse :deep(.van-collapse-item__content) {
  padding: 0;
}

.menu-collapse :deep(.van-collapse-item__title) {
  font-weight: 500;
}

.menu-collapse :deep(.van-cell) {
  padding-left: 32px;
}

.logout-btn {
  padding: 24px 16px;
}
</style>
