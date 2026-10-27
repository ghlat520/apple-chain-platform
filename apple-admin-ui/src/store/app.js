import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const currentModule = ref('bigdata')

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setModule(mod) {
    currentModule.value = mod
  }

  return { sidebarCollapsed, currentModule, toggleSidebar, setModule }
})
