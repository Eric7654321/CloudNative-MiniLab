<template>
  <header class="header-bar">
    <div>
      <div>{{ userdata.Role }} {{ userdata.name }}</div>
      <button @click="goMessage">訊息</button>
      <button @click="goReport">回報</button>
      <button @click="goCalender">行事曆</button>
      <button @click="goLogout">登出</button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useCookies } from 'vue3-cookies'
import { useRouter } from 'vue-router'
import { useUserData } from '@/stores/UserData'

const { cookies } = useCookies()
const router = useRouter()
const userdata = useUserData()

const goMessage = () => {
  console.log('前往訊息')
  // 例如：router.push('/home')
}

const goReport = () => {
  console.log('前往回報')
}

const goCalender = () => {
  console.log('行事曆')
}

const goLogout = () => {
  cookies.remove('token')
  userdata.reset()
  router.push('/')
}
</script>

<style scoped>
.header-bar {
  position: fixed;
  /* ✅ 固定在畫面某個位置 */
  top: 0;
  /* ✅ 靠上方 */
  left: 0;
  width: 100%;
  /* ✅ 滿版 */
  height: 60px;
  display: flex;
  justify-content: flex-end;
  /* 右側對齊 */
  align-items: center;
  background-color: #1e293b;
  color: white;
  z-index: 1000;
  /* ✅ 確保在最上層 */
}

.header-bar button {
  color: white;
  background-color: #3b82f6;
  /* 藍色按鈕 */
  border: none;
  padding: 8px 16px;
  border-radius: 5px;
  cursor: pointer;
}

.header-bar button:hover {
  background-color: #2563eb;
  /* 深一點藍色 */
}
</style>
