<script setup lang="ts">
import { ref } from 'vue';

const is_display = ref(false)
const message = ref("default message")

const set_message = (msg: string) => {
  message.value = msg
}

const set_display = (arg: boolean = false) => {
  is_display.value = arg
}


defineExpose({
  set_display,
  set_message
})
</script>

<template>
  <div v-if="is_display" class="alert">
    {{ message }}
  </div>
</template>

<style scoped>
.alert {
  color: red;
}
</style>
