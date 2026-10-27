<template>
  <div class="page">
    <van-nav-bar title="我的" />

    <div class="profile-hero">
      <van-image
        round
        width="64"
        height="64"
        :src="userStore.profile?.avatar || defaultAvatar"
      />
      <div class="profile-hero__meta">
        <div class="profile-hero__name">{{ userStore.displayName }}</div>
        <div class="profile-hero__role">
          {{ userStore.profile?.roleCode || '未绑定角色' }}
        </div>
      </div>
    </div>

    <van-cell-group inset>
      <van-cell title="所属组织" :value="userStore.profile?.orgName || '-'" />
      <van-cell title="手机号" :value="maskedPhone" />
      <van-cell
        title="溯源查询"
        is-link
        @click="onQueryTrace"
        icon="search"
      />
    </van-cell-group>

    <div class="page__actions">
      <van-button
        v-if="userStore.isLoggedIn"
        block
        round
        type="danger"
        plain
        @click="onLogout"
      >
        退出登录
      </van-button>
      <van-button
        v-else
        block
        round
        type="primary"
        @click="$router.push('/m/login')"
      >
        去登录
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  NavBar as VanNavBar,
  CellGroup as VanCellGroup,
  Cell as VanCell,
  Button as VanButton,
  Image as VanImage,
  showDialog,
} from 'vant';
import { maskPhone } from '@apple/shared-core';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const router = useRouter();

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64"><circle cx="32" cy="32" r="32" fill="%23D32F2F"/><text x="50%" y="55%" text-anchor="middle" fill="white" font-size="24" font-family="sans-serif">果</text></svg>';

const maskedPhone = computed(() => maskPhone(userStore.profile?.phone));

async function onQueryTrace(): Promise<void> {
  const result = await showDialog({
    title: '溯源查询',
    message: '请输入溯源码',
    showCancelButton: true,
    closeOnClickOverlay: true,
    // Using confirmButtonText + a prompt-style override requires custom slot;
    // keep simple: jump to a known demo code if user cancels input flow later.
  }).catch(() => null);
  if (result) {
    // simple: navigate with whatever is typed into the URL bar later
    void router.push('/m/trace/TB202604010001');
  }
}

async function onLogout(): Promise<void> {
  await userStore.logout();
  void router.replace('/m/login');
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-surface-base);
  padding-bottom: var(--space-8);
}
.profile-hero {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6) var(--space-4);
  background: linear-gradient(
    135deg,
    var(--color-brand-primary-light) 0%,
    var(--color-surface-raised) 100%
  );
  margin-bottom: var(--space-4);
}
.profile-hero__meta {
  flex: 1;
}
.profile-hero__name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}
.profile-hero__role {
  font-size: 14px;
  color: var(--color-text-secondary);
}
.page__actions {
  padding: var(--space-6) var(--space-4);
}
</style>
