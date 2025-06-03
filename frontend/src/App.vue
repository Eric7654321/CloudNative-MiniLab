<script setup lang="ts">
import HeaderBar from './components/HeaderBar.vue'
import { RouterLink, RouterView } from 'vue-router'
import {
  NMessageProvider,
  NConfigProvider,
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  darkTheme,
  lightTheme,
} from 'naive-ui'
import { ref } from 'vue'

import { useTheme } from '@/stores/theme'

const theme = useTheme()

const toggleTheme = () => {
  theme.toggleTheme()
}
</script>

<template>
  <n-config-provider :theme="theme.theme">
    <n-message-provider>
      <n-dialog-provider>
        <n-layout>
          <n-layout-header>
            <HeaderBar v-if="$route.path !== '/login'" @ToggleTheme="toggleTheme" />
          </n-layout-header>
          <div class="content"></div>
          <n-layout-content>
            <RouterView />
          </n-layout-content>
        </n-layout>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<style scoped>
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
