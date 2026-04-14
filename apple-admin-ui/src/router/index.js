import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue')
      },
      {
        path: 'bigdata/data-asset',
        name: 'DataAsset',
        component: () => import('@/views/bigdata/DataAsset.vue'),
        meta: { perm: 'bigdata:read' }
      },
      {
        path: 'bigdata/data-source',
        name: 'DataSource',
        component: () => import('@/views/bigdata/DataSource.vue'),
        meta: { perm: 'bigdata:read' }
      },
      {
        path: 'bigdata/data-quality',
        name: 'DataQuality',
        component: () => import('@/views/bigdata/DataQuality.vue'),
        meta: { perm: 'bigdata:read' }
      },
      {
        path: 'bigdata/collect-job',
        name: 'CollectJob',
        component: () => import('@/views/bigdata/CollectJob.vue'),
        meta: { perm: 'bigdata:read' }
      },
      {
        path: 'bigdata/lineage',
        name: 'Lineage',
        component: () => import('@/views/bigdata/Lineage.vue'),
        meta: { perm: 'bigdata:read' }
      },
      {
        path: 'bigdata/metric-center',
        name: 'MetricCenter',
        component: () => import('@/views/bigdata/MetricCenter.vue'),
        meta: { perm: 'bigdata:read' }
      },
      {
        path: 'bigdata/report',
        name: 'Report',
        component: () => import('@/views/bigdata/Report.vue'),
        meta: { perm: 'bigdata:read' }
      },
      {
        path: 'bigdata/audit-log',
        name: 'AuditLog',
        component: () => import('@/views/bigdata/AuditLog.vue'),
        meta: { perm: 'bigdata:read' }
      },
      {
        path: 'bigdata/open-api',
        name: 'OpenApi',
        component: () => import('@/views/bigdata/OpenApi.vue'),
        meta: { perm: 'bigdata:read' }
      },
      {
        path: 'bigdata/screen-config',
        name: 'ScreenConfig',
        component: () => import('@/views/bigdata/ScreenConfig.vue'),
        meta: { perm: 'bigdata:read' }
      },
      // ── 种植生产管理 ──────────────────────────────────────
      { path: 'planting/orchards', name: 'Orchards', component: () => import('@/views/planting/Orchards.vue'), meta: { perm: 'orchard:read' } },
      { path: 'planting/farmers', name: 'Farmers', component: () => import('@/views/planting/Farmers.vue'), meta: { perm: 'orchard:read' } },
      { path: 'planting/harvest', name: 'HarvestBatch', component: () => import('@/views/planting/HarvestBatch.vue'), meta: { perm: 'orchard:read' } },
      { path: 'planting/cultivation', name: 'Cultivation', component: () => import('@/views/planting/Cultivation.vue'), meta: { perm: 'orchard:read' } },
      { path: 'planting/maturity', name: 'Maturity', component: () => import('@/views/planting/Maturity.vue'), meta: { perm: 'orchard:read' } },
      { path: 'planting/task-plan', name: 'TaskPlan', component: () => import('@/views/planting/TaskPlan.vue'), meta: { perm: 'orchard:read' } },
      { path: 'planting/growth-record', name: 'GrowthRecord', component: () => import('@/views/planting/GrowthRecord.vue'), meta: { perm: 'orchard:read' } },
      { path: 'planting/analysis', name: 'PlantingAnalysis', component: () => import('@/views/planting/Analysis.vue'), meta: { perm: 'orchard:read' } },
      // ── 农资管理 ──────────────────────────────────────────
      { path: 'input/suppliers', name: 'AgriSuppliers', component: () => import('@/views/input/Suppliers.vue'), meta: { perm: 'input:read' } },
      { path: 'input/products', name: 'AgriProducts', component: () => import('@/views/input/Products.vue'), meta: { perm: 'input:read' } },
      { path: 'input/purchases', name: 'Purchases', component: () => import('@/views/input/Purchases.vue'), meta: { perm: 'input:read' } },
      { path: 'input/inventory', name: 'Inventory', component: () => import('@/views/input/Inventory.vue'), meta: { perm: 'input:read' } },
      { path: 'input/usage', name: 'Usage', component: () => import('@/views/input/Usage.vue'), meta: { perm: 'input:read' } },
      { path: 'input/trace', name: 'InputTrace', component: () => import('@/views/input/TraceChain.vue'), meta: { perm: 'input:read' } },
      // ── 全流程溯源 ──────────────────────────────────────────
      { path: 'trace/batches', name: 'TraceBatches', component: () => import('@/views/trace/Batches.vue'), meta: { perm: 'trace:read' } },
      { path: 'trace/codes', name: 'TraceCodes', component: () => import('@/views/trace/Codes.vue'), meta: { perm: 'trace:read' } },
      { path: 'trace/records', name: 'TraceRecords', component: () => import('@/views/trace/Records.vue'), meta: { perm: 'trace:read' } },
      { path: 'trace/anomaly', name: 'Anomaly', component: () => import('@/views/trace/Anomaly.vue'), meta: { perm: 'trace:read' } },
      // ── 收购交易撮合 ──────────────────────────────────────
      { path: 'trade/supply', name: 'Supply', component: () => import('@/views/trade/Supply.vue'), meta: { perm: 'trade:read' } },
      { path: 'trade/need', name: 'PurchaseNeed', component: () => import('@/views/trade/PurchaseNeed.vue'), meta: { perm: 'trade:read' } },
      { path: 'trade/match', name: 'TradeMatch', component: () => import('@/views/trade/Match.vue'), meta: { perm: 'trade:read' } },
      { path: 'trade/orders', name: 'TradeOrders', component: () => import('@/views/trade/Orders.vue'), meta: { perm: 'trade:read' } },
      { path: 'trade/inspection', name: 'Inspection', component: () => import('@/views/trade/Inspection.vue'), meta: { perm: 'trade:read' } },
      { path: 'trade/statistics', name: 'TradeStatistics', component: () => import('@/views/trade/Statistics.vue'), meta: { perm: 'trade:read' } },
      // ── 仓储管理 ──────────────────────────────────────────
      { path: 'warehouse/warehouses', name: 'Warehouses', component: () => import('@/views/warehouse/Warehouses.vue'), meta: { perm: 'warehouse:read' } },
      { path: 'warehouse/receipts', name: 'Receipts', component: () => import('@/views/warehouse/Receipts.vue'), meta: { perm: 'warehouse:read' } },
      { path: 'warehouse/records', name: 'WarehouseRecords', component: () => import('@/views/warehouse/Records.vue'), meta: { perm: 'warehouse:read' } },
      { path: 'warehouse/statistics', name: 'WarehouseStats', component: () => import('@/views/warehouse/Statistics.vue'), meta: { perm: 'warehouse:read' } },
      // ── 冷链物流 ──────────────────────────────────────────
      { path: 'coldchain/vehicles', name: 'Vehicles', component: () => import('@/views/coldchain/Vehicles.vue'), meta: { perm: 'logistics:read' } },
      { path: 'coldchain/tasks', name: 'TransportTasks', component: () => import('@/views/coldchain/Tasks.vue'), meta: { perm: 'logistics:read' } },
      { path: 'coldchain/deliveries', name: 'Deliveries', component: () => import('@/views/coldchain/Deliveries.vue'), meta: { perm: 'logistics:read' } },
      { path: 'coldchain/precool', name: 'Precool', component: () => import('@/views/coldchain/Precool.vue'), meta: { perm: 'logistics:read' } },
      { path: 'coldchain/temperatures', name: 'Temperatures', component: () => import('@/views/coldchain/Temperatures.vue'), meta: { perm: 'logistics:read' } },
      { path: 'coldchain/logistics', name: 'Logistics', component: () => import('@/views/coldchain/Logistics.vue'), meta: { perm: 'logistics:read' } },
      { path: 'coldchain/statistics', name: 'ColdchainStats', component: () => import('@/views/coldchain/Statistics.vue'), meta: { perm: 'logistics:read' } },
      // ── 供应链金融 ──────────────────────────────────────────
      { path: 'finance/loans', name: 'Loans', component: () => import('@/views/finance/Loans.vue'), meta: { perm: 'finance:read' } },
      { path: 'finance/pledges', name: 'Pledges', component: () => import('@/views/finance/Pledges.vue'), meta: { perm: 'finance:read' } },
      { path: 'finance/credits', name: 'Credits', component: () => import('@/views/finance/Credits.vue'), meta: { perm: 'finance:read' } },
      { path: 'finance/risks', name: 'Risks', component: () => import('@/views/finance/Risks.vue'), meta: { perm: 'finance:read' } },
      { path: 'finance/statistics', name: 'FinanceStats', component: () => import('@/views/finance/Statistics.vue'), meta: { perm: 'finance:read' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth === false) {
    if (to.path === '/login' && token) {
      next('/')
    } else {
      next()
    }
    return
  }
  if (!token) {
    next('/login')
    return
  }
  const required = to.meta?.perm
  if (required) {
    const auth = useAuthStore()
    const codes = Array.isArray(required) ? required : [required]
    if (!auth.hasAllPerms(codes)) {
      next(false)
      return
    }
  }
  next()
})

export default router
