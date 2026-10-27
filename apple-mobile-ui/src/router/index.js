import { createRouter, createWebHistory } from 'vue-router'
import { showToast } from 'vant'
import { useAuthStore } from '@/store/auth.js'

const BUILD_TARGET = import.meta.env.VITE_BUILD_TARGET || 'full'

// Lite bundle routes (h5-ui equivalent) — always mounted at /h5/*
const liteRoutes = [
  {
    path: '/h5/login',
    name: 'LiteLogin',
    component: () => import('@/modules/lite/login/LoginPage.vue'),
    meta: { requiresAuth: false, liteOnly: true, layout: 'plain' }
  },
  {
    path: '/h5',
    component: () => import('@/layouts-lite/TabLayout.vue'),
    meta: { liteOnly: true },
    children: [
      {
        path: 'planting/record',
        name: 'LitePlantingRecord',
        component: () => import('@/modules/lite/planting/GrowthRecordPage.vue')
      },
      {
        path: 'logistics/sign',
        name: 'LiteLogisticsSign',
        component: () => import('@/modules/lite/logistics/SignPage.vue')
      },
      {
        path: 'profile',
        name: 'LiteProfile',
        component: () => import('@/modules/lite/profile/ProfilePage.vue')
      }
    ]
  },
  {
    path: '/h5/trace/:code',
    name: 'LiteTrace',
    component: () => import('@/modules/lite/trace/TracePage.vue'),
    meta: { requiresAuth: false, liteOnly: true, layout: 'plain' }
  }
]

// Full bundle routes — only included when building for full target
const fullRoutes = BUILD_TARGET === 'full' ? [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/modules/full/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/scan/:code',
    name: 'PublicScan',
    component: () => import('@/modules/full/trace/PublicScan.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/styleguide',
    name: 'Styleguide',
    component: () => import('@/modules/full/Styleguide.vue'),
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
        component: () => import('@/modules/full/Dashboard.vue')
      },
      {
        path: 'farm/orchards',
        name: 'Orchards',
        component: () => import('@/modules/full/farm/Orchards.vue')
      },
      {
        path: 'cultivation/batches',
        name: 'Batches',
        component: () => import('@/modules/full/cultivation/Batches.vue')
      },
      {
        path: 'cultivation/operations',
        name: 'Operations',
        component: () => import('@/modules/full/cultivation/Operations.vue')
      },
      {
        path: 'cultivation/growth-records',
        name: 'GrowthRecords',
        component: () => import('@/modules/full/cultivation/GrowthRecords.vue')
      },
      {
        // M6 — maturity sample input
        path: 'planting/maturity-record',
        name: 'MaturityRecord',
        component: () => import('@/modules/full/planting/MaturityRecord.vue')
      },
      {
        // M6 — harvest recommendation window viewer
        path: 'planting/harvest-recommend',
        name: 'HarvestRecommend',
        component: () => import('@/modules/full/planting/HarvestRecommend.vue')
      },
      {
        // Harvest batch management
        path: 'planting/harvest-batch',
        name: 'HarvestBatch',
        component: () => import('@/modules/full/planting/HarvestBatch.vue')
      },
      {
        // AI 作业计划
        path: 'planting/task-plan',
        name: 'TaskPlan',
        component: () => import('@/modules/full/planting/TaskPlan.vue')
      },
      {
        // 种植分析
        path: 'planting/analysis',
        name: 'PlantingAnalysis',
        component: () => import('@/modules/full/planting/Analysis.vue')
      },
      {
        path: 'trace/records',
        name: 'TraceRecords',
        component: () => import('@/modules/full/trace/Records.vue')
      },
      {
        path: 'trace/query',
        name: 'TraceQuery',
        component: () => import('@/modules/full/trace/Query.vue')
      },
      {
        path: 'trace/code-generate',
        name: 'TraceCodeGenerate',
        component: () => import('@/modules/full/trace/CodeGenerate.vue')
      },
      {
        path: 'trace/anomaly',
        name: 'AnomalyTrace',
        component: () => import('@/modules/full/trace/Anomaly.vue')
      },
      {
        path: 'trace/blockchain',
        name: 'Blockchain',
        component: () => import('@/modules/full/trace/Blockchain.vue')
      },
      {
        // 采收批次管理 (P0)
        path: 'trace/harvest-batch',
        name: 'TraceHarvestBatch',
        component: () => import('@/modules/full/trace/HarvestBatch.vue'),
        meta: { perm: 'harvest:read' }
      },
      {
        path: 'farm/farmers',
        name: 'Farmers',
        component: () => import('@/modules/full/farm/Farmers.vue')
      },
      {
        path: 'trade/supply',
        name: 'Supply',
        component: () => import('@/modules/full/trade/Supply.vue')
      },
      {
        path: 'trade/orders',
        name: 'Orders',
        component: () => import('@/modules/full/trade/Orders.vue')
      },
      {
        path: 'trade/purchase-needs',
        name: 'PurchaseNeeds',
        component: () => import('@/modules/full/trade/PurchaseNeeds.vue')
      },
      {
        path: 'trade/inspection',
        name: 'Inspection',
        component: () => import('@/modules/full/trade/Inspection.vue')
      },
      {
        path: 'trade/stats',
        name: 'TradeStats',
        component: () => import('@/modules/full/trade/TradeStats.vue')
      },
      {
        path: 'trade/matching',
        name: 'Matching',
        component: () => import('@/modules/full/trade/Matching.vue')
      },
      // Input module
      {
        path: 'input/products',
        name: 'AgriProducts',
        component: () => import('@/modules/full/input/Products.vue')
      },
      {
        path: 'input/suppliers',
        name: 'AgriSuppliers',
        component: () => import('@/modules/full/input/Suppliers.vue')
      },
      {
        path: 'input/purchases',
        name: 'AgriPurchases',
        component: () => import('@/modules/full/input/Purchases.vue')
      },
      {
        path: 'input/inventory',
        name: 'AgriInventory',
        component: () => import('@/modules/full/input/Inventory.vue')
      },
      {
        path: 'input/usage',
        name: 'AgriUsage',
        component: () => import('@/modules/full/input/Usage.vue')
      },
      // Warehouse module
      {
        path: 'warehouse/list',
        name: 'Warehouses',
        component: () => import('@/modules/full/warehouse/Warehouses.vue')
      },
      {
        path: 'warehouse/records',
        name: 'WarehouseRecords',
        component: () => import('@/modules/full/warehouse/Records.vue')
      },
      {
        path: 'warehouse/receipts',
        name: 'WarehouseReceipts',
        component: () => import('@/modules/full/warehouse/Receipts.vue')
      },
      // Cold-chain module
      {
        path: 'coldchain/vehicles',
        name: 'Vehicles',
        component: () => import('@/modules/full/coldchain/Vehicles.vue')
      },
      {
        path: 'coldchain/tasks',
        name: 'TransportTasks',
        component: () => import('@/modules/full/coldchain/Tasks.vue')
      },
      {
        path: 'coldchain/temperatures',
        name: 'Temperatures',
        component: () => import('@/modules/full/coldchain/Temperatures.vue')
      },
      {
        path: 'coldchain/deliveries',
        name: 'Deliveries',
        component: () => import('@/modules/full/coldchain/Deliveries.vue')
      },
      // Finance module
      {
        path: 'finance/credits',
        name: 'Credits',
        component: () => import('@/modules/full/finance/Credits.vue')
      },
      {
        path: 'finance/loans',
        name: 'Loans',
        component: () => import('@/modules/full/finance/Loans.vue')
      },
      {
        path: 'finance/pledges',
        name: 'Pledges',
        component: () => import('@/modules/full/finance/Pledges.vue')
      },
      {
        path: 'finance/risks',
        name: 'Risks',
        component: () => import('@/modules/full/finance/Risks.vue')
      },
      // Admin / RBAC management (M1)
      {
        path: 'admin/roles',
        name: 'AdminRoles',
        component: () => import('@/modules/full/admin/Roles.vue'),
        meta: { perm: 'role:read' }
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/modules/full/admin/Users.vue'),
        meta: { perm: 'user:read' }
      },
      // === 新增页面路由（P2 合并） ===
      // 模块入口
      { path: 'modules', name: 'ModuleGrid', component: () => import('@/modules/full/ModuleGrid.vue'), meta: { requiresAuth: true } },
      // 果园详情
      { path: 'farm/orchard/:id', name: 'OrchardDetail', component: () => import('@/modules/full/farm/OrchardDetail.vue'), meta: { requiresAuth: true } },
      // 果园地图
      { path: 'farm/orchard-map', name: 'OrchardMap', component: () => import('@/modules/full/farm/OrchardMap.vue'), meta: { requiresAuth: true } },
      // 溯源详情
      { path: 'trace/detail/:batchCode', name: 'TraceDetail', component: () => import('@/modules/full/trace/Detail.vue'), meta: { requiresAuth: true } },
      // 农资溯源链
      { path: 'input/trace-chain/:code', name: 'TraceChain', component: () => import('@/modules/full/input/TraceChain.vue'), meta: { requiresAuth: true } },
      // 预冷管理
      { path: 'coldchain/precooling', name: 'Precooling', component: () => import('@/modules/full/coldchain/Precooling.vue'), meta: { requiresAuth: true } },
      // 冷链统计
      { path: 'coldchain/statistics', name: 'ColdchainStats', component: () => import('@/modules/full/coldchain/Statistics.vue'), meta: { requiresAuth: true } },
      // 仓储统计
      { path: 'warehouse/statistics', name: 'WarehouseStats', component: () => import('@/modules/full/warehouse/Statistics.vue'), meta: { requiresAuth: true } },
      // 金融统计
      { path: 'finance/statistics', name: 'FinanceStats', component: () => import('@/modules/full/finance/Statistics.vue'), meta: { requiresAuth: true } },
      // === M8 大数据管理 (bigdata) ===
      { path: 'bigdata/data-asset', name: 'BigDataAsset', component: () => import('@/modules/full/bigdata/DataAsset.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/data-source', name: 'BigDataSource', component: () => import('@/modules/full/bigdata/DataSource.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/data-quality', name: 'BigDataQuality', component: () => import('@/modules/full/bigdata/DataQuality.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/collect-job', name: 'BigCollectJob', component: () => import('@/modules/full/bigdata/CollectJob.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/lineage', name: 'BigLineage', component: () => import('@/modules/full/bigdata/Lineage.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/metric-center', name: 'BigMetricCenter', component: () => import('@/modules/full/bigdata/MetricCenter.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/report', name: 'BigReport', component: () => import('@/modules/full/bigdata/Report.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/audit-log', name: 'BigAuditLog', component: () => import('@/modules/full/bigdata/AuditLog.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/open-api', name: 'BigOpenApi', component: () => import('@/modules/full/bigdata/OpenApi.vue'), meta: { perm: 'bigdata:read' } },
      { path: 'bigdata/screen-config', name: 'BigScreenConfig', component: () => import('@/modules/full/bigdata/ScreenConfig.vue'), meta: { perm: 'bigdata:read' } }
    ]
  }
] : []

const routes = [...fullRoutes, ...liteRoutes]

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
