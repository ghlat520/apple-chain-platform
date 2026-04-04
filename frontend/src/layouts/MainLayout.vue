<template>
  <div class="layout-container">
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

    <div class="layout-content">
      <router-view />
    </div>

    <van-tabbar v-model="activeTab" route fixed>
      <van-tabbar-item to="/dashboard" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/orchards" icon="apps-o">果园</van-tabbar-item>
      <van-tabbar-item to="/trace" icon="search">溯源</van-tabbar-item>
      <van-tabbar-item to="/trades" icon="bill-o">交易</van-tabbar-item>
      <van-tabbar-item to="/farmers" icon="friends-o">果农</van-tabbar-item>
    </van-tabbar>

    <van-action-sheet
      v-model:show="showUserMenu"
      :actions="userActions"
      cancel-text="取消"
      @select="handleUserAction"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { showConfirmDialog } from 'vant'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('')
const showUserMenu = ref(false)

const currentTitle = computed(() => route.meta.title || '苹果产业链平台')

const userActions = [
  { name: '个人信息', value: 'profile' },
  { name: '退出登录', value: 'logout', color: '#ee0a24' }
]

function handleBack() {
  if (window.history.length > 1) router.go(-1)
}

async function handleUserAction(action) {
  if (action.value === 'logout') {
    await showConfirmDialog({ title: '退出确认', message: '确定要退出登录吗？' })
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background: #f5f6fa;
}
.layout-content {
  padding-bottom: 50px;
}
</style>
