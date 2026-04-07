import { createRouter, createWebHistory } from 'vue-router'
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
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
