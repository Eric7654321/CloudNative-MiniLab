<script setup lang="ts">
import HeaderBar from './components/HeaderBar.vue'
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div>
    <HeaderBar v-if="$route.path !== '/login'" />
    <RouterView />
  </div>
</template>

<style scoped></style>
