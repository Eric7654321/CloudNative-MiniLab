<script setup lang="ts">
import HeaderBar from './components/HeaderBar.vue';
import WorkList from './views/WorkList.vue';
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div >
    <HeaderBar />
    <RouterView />
    <!-- <WorkList /> -->
  </div>
</template>

<style scoped>
</style>
