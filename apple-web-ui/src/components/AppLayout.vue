<template>
  <div class="app-layout">
    <van-nav-bar
      :title="currentTitle"
      left-arrow
      @click-left="handleBack"
      fixed
      placeholder
    >
      <template #right>
        <van-icon name="user-o" size="18" @click="showUserMenu = true" />
      </template>
    </van-nav-bar>

    <div class="page-content">
      <router-view />
    </div>

    <van-tabbar v-model="activeTab" fixed safe-area-inset-bottom @change="handleTabChange">
      <van-tabbar-item icon="home-o" name="/">首页</van-tabbar-item>
      <van-tabbar-item icon="farm-o" name="/farm/orchards">果园</van-tabbar-item>
      <van-tabbar-item icon="leaf" name="/cultivation/batches">种植</van-tabbar-item>
      <van-tabbar-item icon="search" name="/trace/query">溯源</van-tabbar-item>
      <van-tabbar-item icon="shop-o" name="/trade/orders">交易</van-tabbar-item>
    </van-tabbar>

    <van-popup v-model:show="showUserMenu" position="right" :style="{ width: '240px', height: '100%' }">
      <div class="user-menu">
        <div class="user-info">
          <van-icon name="user-circle-o" size="48" color="#1989fa" />
          <p class="user-name">{{ authStore.user?.username || '用户' }}</p>
        </div>
        <van-cell-group inset>
          <van-cell title="果园管理" icon="farm-o" is-link @click="navigate('/farm/orchards')" />
          <van-cell title="种植批次" icon="leaf" is-link @click="navigate('/cultivation/batches')" />
          <van-cell title="种植作业" icon="todo-list-o" is-link @click="navigate('/cultivation/operations')" />
          <van-cell title="溯源记录" icon="records" is-link @click="navigate('/trace/records')" />
          <van-cell title="溯源查询" icon="search" is-link @click="navigate('/trace/query')" />
          <van-cell title="供货信息" icon="goods-collect-o" is-link @click="navigate('/trade/supply')" />
          <van-cell title="交易订单" icon="orders-o" is-link @click="navigate('/trade/orders')" />
        </van-cell-group>
        <div class="logout-btn">
          <van-button block type="danger" plain @click="handleLogout">退出登录</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { showConfirmDialog } from 'vant'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const showUserMenu = ref(false)
const activeTab = ref('/')

const titleMap = {
  '/': '苹果产业链云服务平台',
  '/farm/orchards': '果园管理',
  '/cultivation/batches': '种植批次',
  '/cultivation/operations': '种植作业',
  '/trace/records': '溯源记录',
  '/trace/query': '溯源查询',
  '/trade/supply': '供货信息',
  '/trade/orders': '交易订单'
}

const currentTitle = computed(() => titleMap[route.path] || '苹果产业链云服务平台')

watch(
  () => route.path,
  (path) => {
    const tabRoutes = ['/', '/farm/orchards', '/cultivation/batches', '/trace/query', '/trade/orders']
    activeTab.value = tabRoutes.includes(path) ? path : activeTab.value
  },
  { immediate: true }
)

function handleBack() {
  if (route.path !== '/') {
    router.back()
  }
}

function handleTabChange(name) {
  router.push(name)
}

function navigate(path) {
  showUserMenu.value = false
  router.push(path)
}

async function handleLogout() {
  showUserMenu.value = false
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确认退出登录？'
    })
    await authStore.logout()
  } catch (e) {
    // user cancelled
  }
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: #f7f8fa;
}

.page-content {
  padding-bottom: 60px;
}

.user-menu {
  padding: 20px 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  border-bottom: 1px solid #ebedf0;
  margin-bottom: 12px;
}

.user-name {
  margin-top: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #323233;
}

.logout-btn {
  padding: 24px 16px;
}
</style>
