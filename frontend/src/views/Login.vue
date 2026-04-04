<template>
  <div class="login-page">
    <div class="login-header">
      <div class="logo-area">
        <div class="logo-icon">&#127822;</div>
        <h1 class="app-name">苹果产业链云服务平台</h1>
        <p class="app-subtitle">数字化全链路管理</p>
      </div>
    </div>

    <div class="login-form">
      <van-form @submit="handleLogin">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            name="username"
            label="账号"
            placeholder="请输入账号"
            left-icon="user-o"
            :rules="[{ required: true, message: '请输入账号' }]"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            left-icon="lock"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>

        <div class="login-btn-area">
          <van-button
            round
            block
            type="primary"
            native-type="submit"
            :loading="loading"
            color="#07c160"
          >
            登录
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { showToast } from 'vant'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

async function handleLogin() {
  loading.value = true
  try {
    await authStore.login(form.value.username, form.value.password)
    router.push('/dashboard')
  } catch (err) {
    showToast('账号或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #e8f5e9 0%, #f5f6fa 40%);
  display: flex;
  flex-direction: column;
}

.login-header {
  padding: 60px 24px 40px;
  text-align: center;
}

.logo-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.app-name {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.app-subtitle {
  font-size: 14px;
  color: #666;
}

.login-form {
  padding: 0 16px;
}

.login-btn-area {
  margin: 24px 16px 0;
}
</style>
