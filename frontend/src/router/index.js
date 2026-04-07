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
      {
        path: 'trades',
        name: 'Trades',
        component: () => import('@/views/TradeList.vue'),
        meta: { title: '收购交易' }
      },
      {
        path: 'farmers',
        name: 'Farmers',
        component: () => import('@/views/FarmerList.vue'),
        meta: { title: '果农管理' }
      },
      {
        path: 'warehouse',
        name: 'Warehouse',
        component: () => import('@/views/WarehouseList.vue'),
        meta: { title: '仓储管理' }
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
