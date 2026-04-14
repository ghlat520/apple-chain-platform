<template>
  <div class="h5-app">
    <router-view v-slot="{ Component, route }">
      <component :is="Component" :key="route.fullPath" />
    </router-view>
  </div>
</template>

<script setup lang="ts">
// Root shell — route renders its own layout (tabbar / plain / scan).
</script>

<style scoped>
.h5-app {
  min-height: 100vh;
  background: var(--color-surface-base);
}
</style>
