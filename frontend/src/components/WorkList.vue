<template>
  <div class="container">
    <aside class="task-list">
      <h2>我的工作</h2>
      <ul>
        <li v-for="task in tasks" :key="task.id">
          <span :class="{ done: task.isFinish }">{{ task.id }}</span><br>
          <span>完成: {{ task.isFinish ? '是' : '否' }}</span><br>
          <span>開始時間: {{ task.startTime }}</span><br>
          <span>結束時間: {{ task.endTime }}</span><br>
          <span>更新時間: {{ task.updateTime }}</span><br>
          <span>所需技能: {{ task.tag }}</span><br>
          <span>任務描述: {{ task.description }}</span><br>
        </li>
      </ul>
    </aside>

    <main class="content">
      <img src="/main_system.png" alt="Main System" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useCookies } from 'vue3-cookies'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useUserData } from '@/stores/UserData'

interface Task {
  id: number // 任務 ID
  emp: number // 員工 ID
  empName: string // 員工名稱（後端填入）
  machine: string[] // JSON 字串陣列形式，例如：["1","2"]
  machineName: string[] // JSON 字串陣列形式（後端填入）
  startTime: string // 任務開始時間 (ISO 字串)
  endTime: string // 任務結束時間 (ISO 字串)
  tag: string // 所需技能（如：電性）
  description: string // 任務描述
  group: string // 所屬群組 ID
  updaterId: number // 更新者 ID
  isFinish: number // 是否已完成（1=完成，0=未完成）
  updateTime: string // 更新時間 (ISO 字串)
}

const tasks = ref<Task[]>([])
const userdata = useUserData()
let intervalId: number | undefined
interface Task {
  id: number // 任務 ID
  emp: number // 員工 ID
  empName: string // 員工名稱（後端填入）
  machine: string[] // JSON 字串陣列形式，例如：["1","2"]
  machineName: string[] // JSON 字串陣列形式（後端填入）
  startTime: string // 任務開始時間 (ISO 字串)
  endTime: string // 任務結束時間 (ISO 字串)
  tag: string // 所需技能（如：電性）
  description: string // 任務描述
  group: string // 所屬群組 ID
  updaterId: number // 更新者 ID
  isFinish: number // 是否已完成（1=完成，0=未完成）
  updateTime: string // 更新時間 (ISO 字串)
}

const tasks = ref<Task[]>([])
const userdata = useUserData()
let intervalId: number | undefined

const { cookies } = useCookies()
if (cookies.get('token') === null) {
  useRouter().push('/')
}
const { cookies } = useCookies()
if (cookies.get('token') === null) {
  useRouter().push('/')
}

const fetchTodayTasks = async () => {
  try {
    const response = await axios.get(`/api/task/search/${userdata.group}`)
    // console.log(response)
    tasks.value = response.data.data // 因為你的 Result.success() 包了 data
    tasks.value = tasks.value.filter((emp: any) => emp.emp === userdata.id)
    console.log('今日任務', tasks.value)
  } catch (error) {
    console.error('取得任務失敗', error)
  }
}

onMounted(() => {
  fetchTodayTasks()
  intervalId = window.setInterval(fetchTodayTasks, 10000) // 10,000 ms = 10 seconds
})
onMounted(() => {
  fetchTodayTasks()
  intervalId = window.setInterval(fetchTodayTasks, 10000) // 10,000 ms = 10 seconds
})

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
</script>

<style scoped>
.container {
  display: flex;
  padding-top: 60px;
  /* Header 的高度 */
  height: 100vh;
  overflow: hidden;
  width: 100vw;
  justify-content: flex-start;
  /* 添加這行來確保向左對齊 */
  margin: 0;
  margin: 0;
}

.task-list {
  width: auto;
  background-color: white;
  padding: 20px;
  border-right: 1px solid #e5e7eb;
  overflow-y: auto;
  margin-left: 0;
  /* 確保沒有左邊距 */
}

.task-list h2 {
  margin-bottom: 10px;
  color: black;
}

.task-list ul {
  list-style: none;
  padding: 0;
}

.task-list li {
  padding: 8px 0;
  border-bottom: 1px solid #ddd;
  color: black;
}

.task-list .done {
  text-decoration: line-through;
  color: blue;
}

.content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: black;
}
.content img {
  border-radius: 8px;
  border: 4px solid gray;
}
</style>
