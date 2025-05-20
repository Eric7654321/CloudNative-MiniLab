<script setup lang="ts">
import HeaderBar from './components/HeaderBar.vue';
import WorkList from './views/WorkList.vue';
</script>

<template>
  <div >
    <HeaderBar />
    <WorkList :employeeId="1" />
  </div>
</template>

<style scoped>
</style>
