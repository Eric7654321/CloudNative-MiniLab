<template>
  <div class="container">
    <aside class="message-list">
      <h2>群組：{{ userdata.group }}</h2>
      <ul>
        <li
          v-for="message in messages"
          :key="message.id"
          :class="{ selected: message.id === selectedMessageId }"
          @click="selectedMessageId = message.id"
        >
          <span>{{ message.id }}</span><br>
          <span>任務 ID: {{ message.taskId }}</span><br>
          <span>更新時間: {{ message.updateTime }}</span>
        </li>
      </ul>
    </aside>

    <main class="content">
      <h1 v-if="!selectedMessage">點擊右側訊息欄</h1>
      <div v-else>
        <h2>訊息內容</h2>
        <p>{{ selectedMessage.description }}</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUserData } from '@/stores/UserData'
import axios from 'axios'

interface Message {
  id: number
  taskId: number
  description: string
  group: string
  updateTime: string
}

const messages = ref<Message[]>([])
const userdata = useUserData()
let intervalId: number | undefined

const selectedMessageId = ref<number | null>(null)
const selectedMessage = computed(() =>
  messages.value.find(m => m.id === selectedMessageId.value)
)

const get_messages = async () => {
  const res = await axios.get(`/api//task/msg/get/${userdata.group}`)
  messages.value = res?.data.data
}

onMounted(() => {
  get_messages()
  intervalId = window.setInterval(get_messages, 10000)
})

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
</script>

<style scoped>
.container {
  display: flex;
  padding-top: 60px;
  height: 100vh;
  overflow: hidden;
  width: 100vw;
  justify-content: center;
  margin: 0;
}

.message-list {
  width: auto;
  background-color: white;
  padding: 20px;
  border-right: 1px solid #e5e7eb;
  margin-left: 0;
  height: 100vh-60px;
  overflow-y: auto;
  position: sticky;
  top: 0;
}

.message-list h2 {
  margin-bottom: 10px;
  color: black;
}

.message-list ul {
  list-style: none;
  padding: 0;
}

.message-list li {
  padding: 8px 0;
  border-bottom: 1px solid #ddd;
  color: black;
  cursor: pointer;
}

.message-list li.selected {
  background: #ffdddd;
  color: red;
}

.content {
  flex: 1;
  padding: 32px;
  /* overflow-y: auto; */
  height: 100vh;
  background: wheat;
  color: black;
  font-size: large;
}
</style>
