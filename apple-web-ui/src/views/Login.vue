<template>
  <div class="login-page">
    <div class="login-header">
      <div class="logo-icon">🍎</div>
      <h1 class="title">苹果产业链云服务平台</h1>
      <p class="subtitle">Apple Supply Chain Platform</p>
    </div>

    <van-form @submit="handleLogin" class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          left-icon="user-o"
          :rules="[{ required: true, message: '请输入用户名' }]"
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          type="password"
          placeholder="请输入密码"
          left-icon="lock"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="login-btn-wrap">
        <van-button
          round
          block
          type="primary"
          native-type="submit"
          :loading="loading"
          loading-text="登录中..."
          size="large"
        >
          登录
        </van-button>
      </div>
    </van-form>

    <p class="footer-text">苹果产业链数字化管理系统 v1.0</p>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAuthStore } from '@/store/auth.js'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

async function handleLogin() {
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    showToast({ type: 'success', message: '登录成功' })
    router.push('/')
  } catch (e) {
    // error shown by request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 50%, #fff8e1 100%);
  padding: 0 16px;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0 40px;
}

.logo-icon {
  font-size: 64px;
  line-height: 1;
  margin-bottom: 12px;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #323233;
  text-align: center;
}

.subtitle {
  font-size: 13px;
  color: #969799;
  margin-top: 6px;
}

.login-form {
  width: 100%;
  max-width: 420px;
}

.login-btn-wrap {
  margin: 24px 16px 0;
}

.footer-text {
  margin-top: 40px;
  font-size: 12px;
  color: #c8c9cc;
}
</style>
