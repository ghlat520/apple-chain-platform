<template>
  <div class="login-page">
    <header class="login-page__header">
      <h1 class="login-page__title">苹果产业链</h1>
      <p class="login-page__subtitle">种植 · 冷链 · 溯源 · 一码通</p>
    </header>

    <van-form class="login-page__form" @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="username"
          label="手机号"
          placeholder="请输入登录手机号"
          :rules="[{ required: true, message: '请输入手机号' }]"
          clearable
          autocomplete="tel"
          maxlength="11"
          type="tel"
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          type="password"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
          clearable
          autocomplete="current-password"
        />
      </van-cell-group>

      <div class="login-page__actions">
        <van-button
          block
          round
          type="primary"
          native-type="submit"
          :loading="submitting"
        >
          登录
        </van-button>
      </div>

      <p class="login-page__hint">
        当前后端仅支持 <b>账号密码</b> 登录。短信验证码登录待后端接口就绪后接入。
      </p>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  Form as VanForm,
  Field as VanField,
  CellGroup as VanCellGroup,
  Button as VanButton,
  showToast,
} from 'vant';
import { isValidMobile } from '@apple/shared-core';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const form = reactive({
  username: '',
  password: '',
});
const submitting = ref(false);

async function onSubmit(): Promise<void> {
  if (!isValidMobile(form.username) && form.username.length < 4) {
    showToast('请输入有效的手机号或账号');
    return;
  }
  submitting.value = true;
  try {
    await userStore.login(form.username.trim(), form.password);
    showToast({ type: 'success', message: '登录成功' });
    const redirect = (route.query.redirect as string) || '/m/planting/record';
    await router.replace(redirect);
  } catch {
    // configureApi.onError already toasted
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: var(--space-8) var(--space-4) var(--space-6);
  background: linear-gradient(
    180deg,
    var(--color-brand-primary-light) 0%,
    var(--color-surface-base) 40%
  );
}
.login-page__header {
  padding: var(--space-6) var(--space-2) var(--space-8);
  text-align: center;
}
.login-page__title {
  margin: 0 0 var(--space-2);
  font-size: 28px;
  font-weight: 600;
  color: var(--color-brand-primary);
  letter-spacing: 2px;
}
.login-page__subtitle {
  margin: 0;
  font-size: 16px;
  color: var(--color-text-secondary);
}
.login-page__form {
  margin-top: var(--space-4);
}
.login-page__actions {
  margin: var(--space-6) var(--space-4) 0;
}
.login-page__hint {
  margin: var(--space-5) var(--space-4) 0;
  font-size: 13px;
  color: var(--color-text-tertiary);
  line-height: 1.6;
}
:deep(.van-field__label) {
  width: 72px;
  color: var(--color-text-secondary);
}
</style>
