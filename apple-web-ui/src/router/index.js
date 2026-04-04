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
        path: 'trade/supply',
        name: 'Supply',
        component: () => import('@/views/trade/Supply.vue')
      },
      {
        path: 'trade/orders',
        name: 'Orders',
        component: () => import('@/views/trade/Orders.vue')
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
