import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from 'vue-router';

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/m/planting/record' },

  {
    path: '/m/login',
    name: 'login',
    component: () => import('@/views/login/LoginPage.vue'),
    meta: { public: true, layout: 'plain' },
  },

  {
    path: '/m',
    component: () => import('@/layouts/TabLayout.vue'),
    children: [
      {
        path: 'planting/record',
        name: 'planting-record',
        component: () => import('@/views/planting/GrowthRecordPage.vue'),
      },
      {
        path: 'logistics/sign',
        name: 'logistics-sign',
        component: () => import('@/views/logistics/SignPage.vue'),
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/profile/ProfilePage.vue'),
      },
    ],
  },

  {
    path: '/m/trace/:code',
    name: 'trace-detail',
    component: () => import('@/views/trace/TracePage.vue'),
    meta: { public: true, layout: 'plain' },
  },

  {
    path: '/:pathMatch(.*)*',
    redirect: '/m/planting/record',
  },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach((to) => {
  const isPublic = to.meta.public === true;
  if (isPublic) return true;
  const token = localStorage.getItem('h5_token');
  if (!token) {
    return {
      path: '/m/login',
      query: { redirect: to.fullPath },
    };
  }
  return true;
});
