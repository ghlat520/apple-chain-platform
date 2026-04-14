import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth.js'
import router from '@/router/index.js'

const PERM_KEY = 'perms'
const ROLE_KEY = 'roles'

function readJsonArray(key) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : []
  } catch (_) {
    return []
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const roles = ref(readJsonArray(ROLE_KEY))
  const permissions = ref(readJsonArray(PERM_KEY))

  const isLoggedIn = computed(() => !!token.value)

  const hasPerm = (code) => {
    if (!code) return true
    return permissions.value.includes(code)
  }
  const hasAnyPerm = (codes) => !codes || codes.length === 0 || codes.some(hasPerm)
  const hasAllPerms = (codes) => !codes || codes.length === 0 || codes.every(hasPerm)
  const hasRole = (code) => roles.value.includes(code)

  function _persistRbac(rolesArr, permsArr) {
    roles.value = Array.isArray(rolesArr) ? rolesArr : []
    permissions.value = Array.isArray(permsArr) ? permsArr : []
    localStorage.setItem(ROLE_KEY, JSON.stringify(roles.value))
    localStorage.setItem(PERM_KEY, JSON.stringify(permissions.value))
  }

  async function login(username, password) {
    const res = await authApi.login({ username, password })
    token.value = res.token
    user.value = res
    localStorage.setItem('token', res.token)
    localStorage.setItem('user', JSON.stringify(res))
    _persistRbac(res.roles, res.permissions)
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch (e) {
      // ignore
    }
    token.value = ''
    user.value = null
    _persistRbac([], [])
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }

  async function fetchProfile() {
    const data = await authApi.getMe()
    user.value = data
    localStorage.setItem('user', JSON.stringify(data))
    _persistRbac(data.roles, data.permissions)
    return data
  }

  return {
    token, user, roles, permissions,
    isLoggedIn,
    hasPerm, hasAnyPerm, hasAllPerms, hasRole,
    login, logout, fetchProfile
  }
})
