import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!user.value)
  const role = computed(() => user.value?.role || '')
  const userId = computed(() => user.value?.userId || null)
  const realName = computed(() => user.value?.realName || '')

  function setUser(userData) {
    user.value = userData
    localStorage.setItem('user', JSON.stringify(userData))
  }

  function setToken(token) {
    localStorage.setItem('token', token)
  }

  function logout() {
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { user, isLoggedIn, role, userId, realName, setUser, setToken, logout }
})
