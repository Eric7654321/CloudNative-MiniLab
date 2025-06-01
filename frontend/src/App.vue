<script setup lang="ts">
import HeaderBar from './components/HeaderBar.vue'
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div>
    <HeaderBar v-if="$route.path !== '/login'" />
    <RouterView />
  </div>
      <HeaderBar v-if="$route.path !== '/login'" />
      <div class="content"></div>
      <RouterView />
</template>

<style scoped></style>
<style>
#app {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 如果需要垂直居中 */
  height: 100%; /* 仅用于演示 */
  width: 100%;
}
.n-config-provider {
  width: 100%;
}
</style>

<style scoped>
.content {
  margin-top: 60px;
}
</style>
