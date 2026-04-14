import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authApi, type LoginResponse, type CurrentUser } from '@apple/shared-core';

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('h5_token'));
  const profile = ref<CurrentUser | null>(
    JSON.parse(localStorage.getItem('h5_user') || 'null') as CurrentUser | null
  );

  const isLoggedIn = computed(() => !!token.value);
  const displayName = computed(
    () => profile.value?.realName || profile.value?.username || '未登录'
  );

  async function login(username: string, password: string): Promise<LoginResponse> {
    const res = await authApi.login({ username, password });
    token.value = res.token;
    localStorage.setItem('h5_token', res.token);
    // Fetch full profile
    try {
      const me = await authApi.getCurrentUser();
      profile.value = me;
      localStorage.setItem('h5_user', JSON.stringify(me));
    } catch {
      // Non-fatal; login already succeeded
    }
    return res;
  }

  async function logout(): Promise<void> {
    try {
      await authApi.logout();
    } catch {
      // ignore; still clear locally
    }
    token.value = null;
    profile.value = null;
    localStorage.removeItem('h5_token');
    localStorage.removeItem('h5_user');
  }

  return { token, profile, isLoggedIn, displayName, login, logout };
});
