import 'lib-flexible';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import { showToast } from 'vant';
import 'vant/lib/index.css';
import './design/vant-theme.css';

import { configureApi } from '@apple/shared-core';
import App from './App.vue';
import { router } from './router';

// Wire shared-core's HTTP client to Vant toasts + Vue router navigation.
configureApi({
  baseURL: '/api',
  timeout: 15000,
  getToken: () => localStorage.getItem('h5_token'),
  onUnauthorized: () => {
    localStorage.removeItem('h5_token');
    localStorage.removeItem('h5_user');
    // Use replace so back button cannot return to the broken page
    if (router.currentRoute.value.path !== '/m/login') {
      void router.replace({
        path: '/m/login',
        query: { redirect: router.currentRoute.value.fullPath },
      });
    }
  },
  onError: (message) => {
    showToast({ type: 'fail', message, duration: 2500 });
  },
});

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.mount('#app');
