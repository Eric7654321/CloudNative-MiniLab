<template>
  <div class="task-calendar-container">
    <n-space vertical :size="12">
      <!-- 篩選區域 -->
      <n-card title="篩選條件" class="filter-card" content-style="padding-top: 20px;">
        <n-space>
          <n-date-picker
            v-model:value="filterDateRange"
            type="daterange"
            clearable
            placeholder="篩選任務起迄日期"
          />
          <n-select
            v-model:value="filterEmployee"
            :options="employeeOptions"
            clearable
            placeholder="篩選員工"
            style="width: 200px"
          />
          <n-select
            v-model:value="filterIsFinish"
            :options="finishStatusOptions"
            clearable
            placeholder="篩選完成狀態"
            style="width: 150px"
          />
          <n-button @click="clearFilters" type="warning">清除篩選</n-button>
        </n-space>
      </n-card>

      <!-- 行事曆 -->
      <div class="calendar-content">
        <n-calendar
          v-model:value="calendarDate"
          #default="{ year, month, date }"
          style="height: 700px"
        >
          <div class="calendar-tasks-container">
            <template v-for="task in getTasksForDate(year, month, date)" :key="task.id">
              <n-popover
                trigger="hover"
                placement="bottom-start"
                :style="{ width: '320px', maxWidth: '90vw' }"
                scrollable
              >
                <template #trigger>
                  <div
                    class="task-entry"
                    :class="{ 'overdue-task-indicator': isTaskOverdueAndUnfinished(task) }"
                  >
                    <n-ellipsis :line-clamp="1" :tooltip="false">
                      <!-- tooltip=false to prevent n-ellipsis's own tooltip -->
                      {{ task.description }}
                    </n-ellipsis>
                  </div>
                </template>
                <!-- Popover Content: Task Details -->
                <n-thing>
                  <template #header>
                    <n-ellipsis style="max-width: 280px">
                      {{ task.description }}
                    </n-ellipsis>
                  </template>
                  <template #header-extra>
                    <n-tag :type="task.isFinish === 1 ? 'success' : 'error'" size="small">
                      {{ task.isFinish === 1 ? '已完成' : '未完成' }}
                    </n-tag>
                  </template>
                  <template #description>
                    <n-space vertical size="small" style="margin-top: 8px">
                      <div><strong>員工:</strong> {{ task.empName }}</div>
                      <div><strong>開始:</strong> {{ formatDateTime(task.startTime) }}</div>
                      <div><strong>結束:</strong> {{ formatDateTime(task.endTime) }}</div>
                    </n-space>
                  </template>
                </n-thing>
                <n-alert
                  v-if="isTaskOverdueAndUnfinished(task)"
                  title="任務提醒"
                  type="warning"
                  :bordered="false"
                  class="overdue-alert-popover"
                >
                  此任務已逾期且未完成！
                </n-alert>
              </n-popover>
            </template>
          </div>
        </n-calendar>
      </div>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  NCalendar,
  NCard,
  NSpace,
  NTag,
  NThing,
  NAlert,
  NEllipsis,
  NDatePicker,
  NSelect,
  NButton,
  NPopover,
} from 'naive-ui'
import {
  parseISO,
  format,
  isWithinInterval,
  startOfDay,
  endOfDay,
  isBefore,
  getTime,
} from 'date-fns'
import { useUserData } from '@/stores/UserData'
import axios from 'axios'

// --- Task Interface ---
interface TaskDataForm {
  id: number
  emp: number // Employee ID
  empName: string // Employee Name
  machine: string // JSON string of machine IDs
  machineName: string // JSON string of machine names
  startTime: string
  endTime: string
  tag: string // Comma-separated string of skills
  description: string
  group: string
  updaterId: number
  isFinish: number // 0 for not finished, 1 for finished
  updateTime: string
}
// --- 模擬的 Task 資料 (與之前相同) ---
const now = new Date()
const today = startOfDay(now)
const yesterday = startOfDay(new Date(new Date().setDate(new Date().getDate() - 1))) // Corrected yesterday
const tomorrow = startOfDay(new Date(new Date().setDate(new Date().getDate() + 1))) // Corrected tomorrow

const rawTasks = ref<TaskDataForm[]>([
  {
    id: 1,
    emp: 101,
    empName: '張三',
    machine: '["1","2"]',
    machineName: '["機台A","機台B"]',
    startTime: today.toISOString(),
    endTime: new Date(new Date(today).setHours(today.getHours() + 4)).toISOString(),
    tag: '電性',
    description: '電路板維修與測試，檢查所有連接點並更換損壞元件。',
    group: 'G01',
    updaterId: 1,
    isFinish: 0,
    updateTime: new Date().toISOString(),
  },
  {
    id: 2,
    emp: 102,
    empName: '李四',
    machine: '["3"]',
    machineName: '["機台C"]',
    startTime: new Date(new Date(yesterday).setHours(yesterday.getHours() - 5)).toISOString(),
    endTime: new Date(new Date(yesterday).setHours(yesterday.getHours() - 1)).toISOString(),
    tag: '機械',
    description: '機械手臂校準作業，非常緊急，需要立即處理，否則產線會停擺。',
    group: 'G02',
    updaterId: 2,
    isFinish: 0,
    updateTime: new Date().toISOString(),
  },
  {
    id: 3,
    emp: 101,
    empName: '張三',
    machine: '["1"]',
    machineName: '["機台A"]',
    startTime: new Date(new Date(today).setHours(today.getHours() + 2)).toISOString(),
    endTime: new Date(new Date(tomorrow).setHours(tomorrow.getHours() + 6)).toISOString(), // 跨日任務
    tag: '軟體',
    description: '系統更新與部署，包含資料庫遷移和前端界面升級。',
    group: 'G01',
    updaterId: 1,
    isFinish: 0,
    updateTime: new Date().toISOString(),
  },
  {
    id: 4,
    emp: 103,
    empName: '王五',
    machine: '["4"]',
    machineName: '["機台D"]',
    startTime: new Date(new Date(yesterday).setHours(yesterday.getHours() - 8)).toISOString(),
    endTime: new Date().toISOString(),
    tag: '清潔',
    description: '廠區環境清潔，特別是無塵室區域。',
    group: 'G03',
    updaterId: 3,
    isFinish: 1,
    updateTime: new Date().toISOString(),
  },
  {
    id: 5,
    emp: 102,
    empName: '李四',
    machine: '["2"]',
    machineName: '["機台B"]',
    startTime: today.toISOString(), // 與任務1同一天開始
    endTime: new Date(new Date(today).setHours(today.getHours() + 2)).toISOString(),
    tag: '電性',
    description: '機台B的日常保養檢查。',
    group: 'G01',
    updaterId: 1,
    isFinish: 0,
    updateTime: new Date().toISOString(),
  },
])

// --- 行事曆狀態 ---
const calendarDate = ref(getTime(new Date()))

// --- 篩選狀態 ---
const filterDateRange = ref(null)
const filterEmployee = ref(null)
const filterIsFinish = ref(null)

const employeeOptions = computed(() => {
  const names = new Set(rawTasks.value.map((task) => task.empName))
  return Array.from(names).map((name) => ({ label: name, value: name }))
})

const finishStatusOptions = [
  { label: '未完成', value: 0 },
  { label: '已完成', value: 1 },
]

const clearFilters = () => {
  filterDateRange.value = null
  filterEmployee.value = null
  filterIsFinish.value = null
}

// --- 篩選後的任務 ---
const filteredTasks = computed(() => {
  let tasks = rawTasks.value
  if (filterDateRange.value && filterDateRange.value[0] && filterDateRange.value[1]) {
    const filterStart = startOfDay(new Date(filterDateRange.value[0]))
    const filterEnd = endOfDay(new Date(filterDateRange.value[1]))
    tasks = tasks.filter((task) => {
      const taskStart = parseISO(task.startTime)
      const taskEnd = parseISO(task.endTime)
      return (
        isWithinInterval(taskStart, { start: filterStart, end: filterEnd }) ||
        isWithinInterval(taskEnd, { start: filterStart, end: filterEnd }) ||
        (isBefore(taskStart, filterStart) && isBefore(filterEnd, taskEnd))
      )
    })
  }
  if (filterEmployee.value) {
    tasks = tasks.filter((task) => task.empName === filterEmployee.value)
  }
  if (filterIsFinish.value !== null && filterIsFinish.value !== undefined) {
    tasks = tasks.filter((task) => task.isFinish === filterIsFinish.value)
  }
  return tasks
})

// --- Helper 函數 ---
const formatDateTime = (dateTimeString: any) => {
  if (!dateTimeString) return ''
  return format(parseISO(dateTimeString), 'yyyy-MM-dd HH:mm')
}

const getTasksForDate = (year: any, month: any, day: any) => {
  const cellDateStart = startOfDay(new Date(year, month - 1, day))
  const cellDateEnd = endOfDay(new Date(year, month - 1, day))

  return filteredTasks.value
    .filter((task) => {
      const taskStart = parseISO(task.startTime)
      const taskEnd = parseISO(task.endTime)
      return taskStart <= cellDateEnd && taskEnd >= cellDateStart
    })
}

const isTaskOverdueAndUnfinished = (task: any) => {
  if (task.isFinish === 1) return false
  const taskEndTime = parseISO(task.endTime)
  return isBefore(taskEndTime, new Date())
}

const userdata = useUserData()

// 模擬從API獲取任務列表
const fetchTaskOptions = async () => {
  // 假設的 API 請求
  const response = await axios.get(`/api/task/search/${userdata.group}`)
  const task: TaskDataForm[] = response?.data.data

  // taskOptions.value = response.data.map(task => ({ label: `任務 ${task.name} (ID: ${task.id})`, value: task.id }));

  // 靜態範例數據
  rawTasks.value = task
}

onMounted(() => {
  fetchTaskOptions()
})
</script>

<style scoped>
.task-calendar-container {
  height: calc(100vh - 60px);
  width: 100vw;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}
.calendar-content {
  flex: 1;
  height: calc(100vh - 120px);
  width: 100vw;
  padding-left: 20px;
  overflow-y: auto;
}
.filter-card {
  height: 60px;
  flex-direction: row;
  align-items: center;
}
.calendar-tasks-container {
  display: flex;
  flex-direction: column;
  gap: 3px;
  /* 任務條目之間的間距 */
  overflow-y: auto;
  /* 如果任務太多，允許滾動 */
  max-height: 80px;
  /* 限制每個儲存格的高度，根據需要調整 */
  padding: 2px;
}

.task-entry {
  padding: 2px 5px;
  border-radius: 3px;
  background-color: var(--n-item-color-hover);
  /* 使用 Naive UI 主題色 */
  /* color: var(--n-item-text-color-hover); */
  font-size: 12px;
  cursor: pointer;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
  /* 確保 n-ellipsis 正常工作 */
}

.task-entry:hover {
  background-color: var(--n-item-color-active);
}

.overdue-task-indicator {
  background-color: #f0a02033;
  /* 警告色的淡淡背景 */
  border-left: 3px solid var(--n-warning-color);
  /* Naive UI 警告色 */
  padding-left: 3px;
  /* 因左邊框調整內邊距 */
}

.overdue-task-indicator:hover {
  background-color: #f0a02055;
  /* 懸停時加深一點 */
}

.overdue-alert-popover {
  margin-top: 10px;
}

.overdue-alert-popover :deep(.n-alert-body__title) {
  font-size: 1.05em;
}

/* 可選：如果希望 Popover 中的內容更緊湊 */
:deep(.n-popover .n-thing-header__title) {
  font-size: 14px;
  margin-bottom: 4px;
}

:deep(.n-popover .n-thing-main__description) {
  font-size: 13px;
}

:deep(.n-popover .n-tag) {
  transform: scale(0.9);
  /* 略微縮小標籤 */
}
</style>
