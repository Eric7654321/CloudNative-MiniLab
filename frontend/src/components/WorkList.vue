<template>
  <div
    style="
      height: calc(100vh - 60px);
      width: 100vw;
      overflow: hidden;
      display: flex;
      flex-direction: row;
      justify-content: flex-start;
    "
  >
    <n-layout style="height: calc(100vh - 60px); width: 400px">
      <n-layout-content
        bordered
        show-trigger
        collapse-mode="width"
        :collapsed-width="64"
        :width="320"
        :native-scrollbar="false"
        content-style="padding: 20px;"
      >
        <n-h2 style="margin-bottom: 10px">我的工作</n-h2>
        <n-list hoverable clickable v-if="tasks.length > 0">
          <n-list-item v-for="task in tasks" :key="task.id">
            <n-thing>
              <template #header>
                <n-text :type="task.isFinish ? 'success' : 'default'" :delete="!!task.isFinish">
                  任務 ID: {{ task.id }}
                </n-text>
              </template>
              <template #header-extra>
                <n-tag :type="task.isFinish ? 'success' : 'warning'" size="small">
                  {{ task.isFinish ? '已完成' : '未完成' }}
                </n-tag>
              </template>
              <n-descriptions label-placement="left" :column="1" size="small" bordered>
                <n-descriptions-item label="開始時間">
                  {{ task.startTime ? new Date(task.startTime).toLocaleString() : 'N/A' }}
                </n-descriptions-item>
                <n-descriptions-item label="結束時間">
                  {{ task.endTime ? new Date(task.endTime).toLocaleString() : 'N/A' }}
                </n-descriptions-item>
                <n-descriptions-item label="更新時間">
                  {{ task.updateTime ? new Date(task.updateTime).toLocaleString() : 'N/A' }}
                </n-descriptions-item>
                <n-descriptions-item label="所需技能">
                  {{ task.tag || 'N/A' }}
                </n-descriptions-item>
                <n-descriptions-item label="任務描述">
                  <n-ellipsis :line-clamp="3" :tooltip="true">
                    {{ task.description || '無描述' }}
                  </n-ellipsis>
                </n-descriptions-item>
                <!-- 可選擇性顯示機器信息 -->
                <n-descriptions-item
                  v-if="task.machineName && task.machineName.length > 0"
                  label="使用機台"
                >
                  <n-tag
                    v-for="machine in JSON.parse(task.machineName)"
                    :key="machine"
                    type="info"
                    size="small"
                    style="margin-right: 4px"
                  >
                    {{ machine }}
                  </n-tag>
                </n-descriptions-item>
              </n-descriptions>
            </n-thing>
          </n-list-item>
        </n-list>
        <n-empty v-else description="今日無任務" style="margin-top: 20px" />
      </n-layout-content>
    </n-layout>
    <n-layout
      style="
        height: calc(100vh - 60px);
        width: calc(100vw - 400px);
        display: flex;
        justify-content: center;
      "
    >
      <n-image src="/main_system.png" height="calc(100vh-60px);" />
    </n-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useCookies } from 'vue3-cookies'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useUserData } from '@/stores/UserData'
import {
  NLayout,
  NLayoutSider,
  NLayoutContent,
  NH2,
  NList,
  NListItem,
  NThing,
  NDescriptions,
  NDescriptionsItem,
  NText,
  NTag,
  NImage,
  NEllipsis,
  NEmpty,
} from 'naive-ui'

interface Task {
  id: number // 任務 ID
  emp: number // 員工 ID
  empName: string // 員工名稱（後端填入）
  machine: string[] // JSON 字串陣列形式，例如：["1","2"]
  machineName: string // JSON 字串陣列形式（後端填入）
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
const router = useRouter() // 在 setup 頂層調用

if (cookies.get('token') === null) {
  router.push('/')
}

const fetchTodayTasks = async () => {
  try {
    const response = await axios.get(`/api/task/search/${userdata.group}`)
    if (response.data && response.data.data) {
      let fetchedTasks = response.data.data as Task[]
      tasks.value = fetchedTasks.filter((task: Task) => task.emp === userdata.id)
      console.log('今日任務', tasks.value)
    } else {
      tasks.value = []
      console.log('今日任務: API返回數據格式不正確或無數據')
    }
  } catch (error) {
    console.error('取得任務失敗', error)
    tasks.value = [] // 發生錯誤時清空任務列表
  }
}

onMounted(() => {
  fetchTodayTasks()
  intervalId = window.setInterval(fetchTodayTasks, 10000) // 10,000 ms = 10 seconds
})

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
</script>

<style scoped>
/* 大部分樣式已由 Naive UI 元件屬性處理 */
/* 如果需要覆蓋或添加特定樣式，可以在這裡添加 */
/* 例如，確保 NLayout 填充其父容器 (如果父容器不是 body) */
.n-layout {
  height: 100%;
}
</style>
