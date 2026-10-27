import { createRouter, createWebHistory } from 'vue-router'
import { showToast } from 'vant'
import { useAuthStore } from '@/store/auth.js'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/scan/:code',
    name: 'PublicScan',
    component: () => import('@/views/trace/PublicScan.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/styleguide',
    name: 'Styleguide',
    component: () => import('@/views/Styleguide.vue'),
    meta: { requiresAuth: false }  // 视觉 QA 页面，无需登录
  },
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'farm/orchards',
        name: 'Orchards',
        component: () => import('@/views/farm/Orchards.vue')
      },
      {
        path: 'cultivation/batches',
        name: 'Batches',
        component: () => import('@/views/cultivation/Batches.vue')
      },
      {
        path: 'cultivation/operations',
        name: 'Operations',
        component: () => import('@/views/cultivation/Operations.vue')
      },
      {
        path: 'cultivation/growth-records',
        name: 'GrowthRecords',
        component: () => import('@/views/cultivation/GrowthRecords.vue')
      },
      {
        // M6 — maturity sample input
        path: 'planting/maturity-record',
        name: 'MaturityRecord',
        component: () => import('@/views/planting/MaturityRecord.vue')
      },
      {
        // M6 — harvest recommendation window viewer
        path: 'planting/harvest-recommend',
        name: 'HarvestRecommend',
        component: () => import('@/views/planting/HarvestRecommend.vue')
      },
      {
        // Harvest batch management
        path: 'planting/harvest-batch',
        name: 'HarvestBatch',
        component: () => import('@/views/planting/HarvestBatch.vue')
      },
      {
        // AI 作业计划
        path: 'planting/task-plan',
        name: 'TaskPlan',
        component: () => import('@/views/planting/TaskPlan.vue')
      },
      {
        // 种植分析
        path: 'planting/analysis',
        name: 'PlantingAnalysis',
        component: () => import('@/views/planting/Analysis.vue')
      },
      {
        path: 'trace/records',
        name: 'TraceRecords',
        component: () => import('@/views/trace/Records.vue')
      },
      {
        path: 'trace/query',
        name: 'TraceQuery',
        component: () => import('@/views/trace/Query.vue')
      },
      {
        path: 'trace/code-generate',
        name: 'TraceCodeGenerate',
        component: () => import('@/views/trace/CodeGenerate.vue')
      },
      {
        path: 'trace/anomaly',
        name: 'AnomalyTrace',
        component: () => import('@/views/trace/Anomaly.vue')
      },
      {
        path: 'trace/blockchain',
        name: 'Blockchain',
        component: () => import('@/views/trace/Blockchain.vue')
      },
      {
        // 采收批次管理 (P0)
        path: 'trace/harvest-batch',
        name: 'TraceHarvestBatch',
        component: () => import('@/views/trace/HarvestBatch.vue'),
        meta: { perm: 'harvest:read' }
      },
      {
        path: 'farm/farmers',
        name: 'Farmers',
        component: () => import('@/views/farm/Farmers.vue')
      },
      {
        path: 'trade/supply',
        name: 'Supply',
        component: () => import('@/views/trade/Supply.vue')
      },
      {
        path: 'trade/orders',
        name: 'Orders',
        component: () => import('@/views/trade/Orders.vue')
      },
      {
        path: 'trade/purchase-needs',
        name: 'PurchaseNeeds',
        component: () => import('@/views/trade/PurchaseNeeds.vue')
      },
      {
        path: 'trade/inspection',
        name: 'Inspection',
        component: () => import('@/views/trade/Inspection.vue')
      },
      {
        path: 'trade/stats',
        name: 'TradeStats',
        component: () => import('@/views/trade/TradeStats.vue')
      },
      {
        path: 'trade/matching',
        name: 'Matching',
        component: () => import('@/views/trade/Matching.vue')
      },
      // Input module
      {
        path: 'input/products',
        name: 'AgriProducts',
        component: () => import('@/views/input/Products.vue')
      },
      {
        path: 'input/suppliers',
        name: 'AgriSuppliers',
        component: () => import('@/views/input/Suppliers.vue')
      },
      {
        path: 'input/purchases',
        name: 'AgriPurchases',
        component: () => import('@/views/input/Purchases.vue')
      },
      {
        path: 'input/inventory',
        name: 'AgriInventory',
        component: () => import('@/views/input/Inventory.vue')
      },
      {
        path: 'input/usage',
        name: 'AgriUsage',
        component: () => import('@/views/input/Usage.vue')
      },
      // Warehouse module
      {
        path: 'warehouse/list',
        name: 'Warehouses',
        component: () => import('@/views/warehouse/Warehouses.vue')
      },
      {
        path: 'warehouse/records',
        name: 'WarehouseRecords',
        component: () => import('@/views/warehouse/Records.vue')
      },
      {
        path: 'warehouse/receipts',
        name: 'WarehouseReceipts',
        component: () => import('@/views/warehouse/Receipts.vue')
      },
      // Cold-chain module
      {
        path: 'coldchain/vehicles',
        name: 'Vehicles',
        component: () => import('@/views/coldchain/Vehicles.vue')
      },
      {
        path: 'coldchain/tasks',
        name: 'TransportTasks',
        component: () => import('@/views/coldchain/Tasks.vue')
      },
      {
        path: 'coldchain/temperatures',
        name: 'Temperatures',
        component: () => import('@/views/coldchain/Temperatures.vue')
      },
      {
        path: 'coldchain/deliveries',
        name: 'Deliveries',
        component: () => import('@/views/coldchain/Deliveries.vue')
      },
      // Finance module
      {
        path: 'finance/credits',
        name: 'Credits',
        component: () => import('@/views/finance/Credits.vue')
      },
      {
        path: 'finance/loans',
        name: 'Loans',
        component: () => import('@/views/finance/Loans.vue')
      },
      {
        path: 'finance/pledges',
        name: 'Pledges',
        component: () => import('@/views/finance/Pledges.vue')
      },
      {
        path: 'finance/risks',
        name: 'Risks',
        component: () => import('@/views/finance/Risks.vue')
      },
      // Admin / RBAC management (M1)
      {
        path: 'admin/roles',
        name: 'AdminRoles',
        component: () => import('@/views/admin/Roles.vue'),
        meta: { perm: 'role:read' }
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { perm: 'user:read' }
      },
      // === 新增页面路由（P2 合并） ===
      // 模块入口
      { path: 'modules', name: 'ModuleGrid', component: () => import('@/views/ModuleGrid.vue'), meta: { requiresAuth: true } },
      // 果园详情
      { path: 'farm/orchard/:id', name: 'OrchardDetail', component: () => import('@/views/farm/OrchardDetail.vue'), meta: { requiresAuth: true } },
      // 果园地图
      { path: 'farm/orchard-map', name: 'OrchardMap', component: () => import('@/views/farm/OrchardMap.vue'), meta: { requiresAuth: true } },
      // 溯源详情
      { path: 'trace/detail/:batchCode', name: 'TraceDetail', component: () => import('@/views/trace/Detail.vue'), meta: { requiresAuth: true } },
      // 农资溯源链
      { path: 'input/trace-chain/:code', name: 'TraceChain', component: () => import('@/views/input/TraceChain.vue'), meta: { requiresAuth: true } },
      // 预冷管理
      { path: 'coldchain/precooling', name: 'Precooling', component: () => import('@/views/coldchain/Precooling.vue'), meta: { requiresAuth: true } },
      // 冷链统计
      { path: 'coldchain/statistics', name: 'ColdchainStats', component: () => import('@/views/coldchain/Statistics.vue'), meta: { requiresAuth: true } },
      // 仓储统计
      { path: 'warehouse/statistics', name: 'WarehouseStats', component: () => import('@/views/warehouse/Statistics.vue'), meta: { requiresAuth: true } },
      // 金融统计
      { path: 'finance/statistics', name: 'FinanceStats', component: () => import('@/views/finance/Statistics.vue'), meta: { requiresAuth: true } },
      // === M8 大数据管理 (bigdata) ===
      { path: 'bigdata/data-asset', name: 'BigDataAsset', component: () => import('@/views/bigdata/DataAsset.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/data-source', name: 'BigDataSource', component: () => import('@/views/bigdata/DataSource.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/data-quality', name: 'BigDataQuality', component: () => import('@/views/bigdata/DataQuality.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/collect-job', name: 'BigCollectJob', component: () => import('@/views/bigdata/CollectJob.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/lineage', name: 'BigLineage', component: () => import('@/views/bigdata/Lineage.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/metric-center', name: 'BigMetricCenter', component: () => import('@/views/bigdata/MetricCenter.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/report', name: 'BigReport', component: () => import('@/views/bigdata/Report.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/audit-log', name: 'BigAuditLog', component: () => import('@/views/bigdata/AuditLog.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/open-api', name: 'BigOpenApi', component: () => import('@/views/bigdata/OpenApi.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/screen-config', name: 'BigScreenConfig', component: () => import('@/views/bigdata/ScreenConfig.vue'), meta: { perm: 'bigdata:read' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  // Public pages: bypass both auth and permission checks.
  if (to.meta.requiresAuth === false) {
    if (to.path === '/login' && token) {
      next('/')
    } else {
      next()
    }
    return
  }
  // Authenticated routes: require token first.
  if (!token) {
    next('/login')
    return
  }
  // RBAC permission gate (spec § 1.6): meta.perm = "trade:write"
  // or meta.perm = ["trade:write","trade:approve"] (AND semantics).
  const required = to.meta?.perm
  if (required) {
    const auth = useAuthStore()
    const codes = Array.isArray(required) ? required : [required]
    if (!auth.hasAllPerms(codes)) {
      showToast({ type: 'fail', message: '权限不足' })
      next(false)
      return
    }
  }
  next()
})

export default router
