import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据中心' }
      },
      {
        path: 'modules',
        name: 'ModuleGrid',
        component: () => import('@/views/ModuleGrid.vue'),
        meta: { title: '业务模块' }
      },
      // ===== M1 种植 =====
      {
        path: 'orchards',
        name: 'Orchards',
        component: () => import('@/views/OrchardList.vue'),
        meta: { title: '果园管理' }
      },
      {
        path: 'orchards/:id',
        name: 'OrchardDetail',
        component: () => import('@/views/OrchardDetail.vue'),
        meta: { title: '果园详情' }
      },
      {
        path: 'orchards-map',
        name: 'OrchardMap',
        component: () => import('@/views/OrchardMap.vue'),
        meta: { title: '果园地图' }
      },
      {
        path: 'farmers',
        name: 'Farmers',
        component: () => import('@/views/FarmerList.vue'),
        meta: { title: '果农管理' }
      },
      // ===== 采收 =====
      {
        path: 'harvest',
        name: 'Harvest',
        component: () => import('@/views/HarvestList.vue'),
        meta: { title: '采收管理' }
      },
      {
        path: 'harvest/:id',
        name: 'HarvestDetail',
        component: () => import('@/views/HarvestDetail.vue'),
        meta: { title: '采收详情' }
      },
      // ===== M2 农资 =====
      {
        path: 'input/suppliers',
        name: 'InputSuppliers',
        component: () => import('@/views/input/SupplierList.vue'),
        meta: { title: '供应商管理' }
      },
      {
        path: 'input/products',
        name: 'InputProducts',
        component: () => import('@/views/input/ProductList.vue'),
        meta: { title: '农资产品管理' }
      },
      {
        path: 'input/purchases',
        name: 'InputPurchases',
        component: () => import('@/views/input/PurchaseList.vue'),
        meta: { title: '采购管理' }
      },
      {
        path: 'input/inventory',
        name: 'InputInventory',
        component: () => import('@/views/input/InventoryList.vue'),
        meta: { title: '库存管理' }
      },
      {
        path: 'input/usage',
        name: 'InputUsage',
        component: () => import('@/views/input/UsageList.vue'),
        meta: { title: '使用记录' }
      },
      {
        path: 'input/trace',
        name: 'InputTrace',
        component: () => import('@/views/input/TraceChain.vue'),
        meta: { title: '溯源链路查询' }
      },
      // ===== M3 溯源 =====
      {
        path: 'trace',
        name: 'Trace',
        component: () => import('@/views/TraceList.vue'),
        meta: { title: '溯源管理' }
      },
      {
        path: 'trace/:batchCode',
        name: 'TraceDetail',
        component: () => import('@/views/TraceDetail.vue'),
        meta: { title: '溯源详情' }
      },
      // ===== M4 交易 =====
      {
        path: 'trades',
        name: 'Trades',
        component: () => import('@/views/TradeList.vue'),
        meta: { title: '收购交易' }
      },
      // ===== M5 仓储 =====
      {
        path: 'warehouse',
        name: 'Warehouse',
        component: () => import('@/views/WarehouseList.vue'),
        meta: { title: '仓储管理' }
      },
      {
        path: 'warehouse/receipts',
        name: 'WarehouseReceipts',
        component: () => import('@/views/warehouse/ReceiptList.vue'),
        meta: { title: '仓单管理' }
      },
      {
        path: 'warehouse/records',
        name: 'WarehouseRecords',
        component: () => import('@/views/warehouse/RecordList.vue'),
        meta: { title: '出入库记录' }
      },
      {
        path: 'warehouse/stats',
        name: 'WarehouseStats',
        component: () => import('@/views/warehouse/WarehouseStats.vue'),
        meta: { title: '仓储统计' }
      },
      // ===== M6 冷链 =====
      {
        path: 'coldchain/vehicles',
        name: 'ColdchainVehicles',
        component: () => import('@/views/coldchain/VehicleList.vue'),
        meta: { title: '车辆管理' }
      },
      {
        path: 'coldchain/transports',
        name: 'ColdchainTransports',
        component: () => import('@/views/coldchain/TransportList.vue'),
        meta: { title: '运输管理' }
      },
      {
        path: 'coldchain/precooling',
        name: 'ColdchainPrecooling',
        component: () => import('@/views/coldchain/PrecoolingList.vue'),
        meta: { title: '预冷管理' }
      },
      {
        path: 'coldchain/stats',
        name: 'ColdchainStats',
        component: () => import('@/views/coldchain/ColdchainStats.vue'),
        meta: { title: '冷链统计' }
      },
      // ===== M7 金融 =====
      {
        path: 'finance/loans',
        name: 'FinanceLoans',
        component: () => import('@/views/finance/LoanList.vue'),
        meta: { title: '贷款管理' }
      },
      {
        path: 'finance/pledges',
        name: 'FinancePledges',
        component: () => import('@/views/finance/PledgeList.vue'),
        meta: { title: '仓单质押' }
      },
      {
        path: 'finance/ratings',
        name: 'FinanceRatings',
        component: () => import('@/views/finance/CreditRating.vue'),
        meta: { title: '信用评级' }
      },
      {
        path: 'finance/risks',
        name: 'FinanceRisks',
        component: () => import('@/views/finance/RiskList.vue'),
        meta: { title: '风控管理' }
      },
      {
        path: 'finance/stats',
        name: 'FinanceStats',
        component: () => import('@/views/finance/FinanceStats.vue'),
        meta: { title: '金融统计' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.public) return next()
  const auth = useAuthStore()
  if (!auth.token) return next('/login')
  next()
})

export default router
