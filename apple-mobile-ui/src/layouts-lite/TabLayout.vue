<template>
  <div class="tab-layout">
    <main class="tab-layout__main">
      <router-view />
    </main>

    <van-tabbar
      v-model="active"
      route
      safe-area-inset-bottom
      :border="true"
    >
      <van-tabbar-item to="/m/planting/record" icon="flower-o">
        种植
      </van-tabbar-item>
      <van-tabbar-item to="/m/logistics/sign" icon="logistics">
        物流
      </van-tabbar-item>
      <van-tabbar-item to="/m/profile" icon="user-o">
        我的
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Tabbar as VanTabbar, TabbarItem as VanTabbarItem } from 'vant';

const active = ref<string>('planting');
</script>

<style scoped>
.tab-layout {
  min-height: 100vh;
  padding-bottom: calc(50px + env(safe-area-inset-bottom));
  background: var(--color-surface-base);
}
.tab-layout__main {
  min-height: calc(100vh - 50px);
}
</style>
