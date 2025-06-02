<template>
  <n-space vertical style="height: 100vh; width: 100vw" :size="12">
    <n-card :bordered="false"> 群組：{{ userdata.group }} </n-card>

    <n-layout has-sider style="flex-grow: 1; height: calc(100vh - 60px)">
      <n-layout-sider
        bordered
        collapse-mode="width"
        :collapsed-width="88"
        :width="440"
        show-trigger="arrow-circle"
        :inverted="invertedSide"
        content-style="padding: 12px"
      >
        <n-menu
          v-model:value="selectedMessageKey"
          :inverted="invertedSide"
          :collapsed-width="64"
          :collapsed-icon-size="22"
          :options="messageMenuOptions"
          @update:value="onMenuUpdate"
        />
      </n-layout-sider>
      <n-layout content-style="padding: 24px;">
        <n-card
          :title="selectedMessage ? selectedMessage.description : '沒有選擇訊息'"
          :bordered="true"
        >
          <div v-if="selectedMessage">
            <n-descriptions label-placement="left" bordered :columns="1" size="small">
              <n-descriptions-item label="訊息 ID">
                {{ selectedMessage.id }}
              </n-descriptions-item>
              <n-descriptions-item label="任務 ID">
                {{ selectedMessage.taskId }}
              </n-descriptions-item>
              <n-descriptions-item label="更新時間">
                {{ selectedMessage.updateTime }}
              </n-descriptions-item>
              <n-descriptions-item label="訊息內容">
                <div class="content" style="white-space: pre-wrap">
                  {{ selectedMessage.description }}
                </div>
              </n-descriptions-item>
            </n-descriptions>
          </div>
          <div v-else>
            <n-empty description="請從左側菜單選擇一個訊息以查看詳情。" size="large"> </n-empty>
          </div>
        </n-card>
      </n-layout>
    </n-layout>
  </n-space>
</template>

<script setup lang="ts">
import { ref, computed, h, type Component } from 'vue' // Removed defineComponent
import type { MenuOption } from 'naive-ui'
import {
  NSpace,
  NLayout,
  NLayoutSider,
  NMenu,
  NDescriptions,
  NDescriptionsItem,
  NEmpty,
  NIcon,
  NCard,
} from 'naive-ui'
import { useUserData } from '@/stores/UserData'
import { BookOutline as BookIcon } from '@vicons/ionicons5'

// Optional: If you want icons in your menu, import them.
// import { BookOutline as BookIcon } from '@vicons/ionicons5';
import { onMounted, onUnmounted } from 'vue'
import axios from 'axios'

interface Message {
  description: string
  id: number
  group: string
  status: number
  taskId: null | number
  updateTime: string
}

const userdata = useUserData()

const invertedSide = ref(false) // Controls the dark/light theme of the sider

const messages = ref<Message[]>([])

const selectedMessageKey = ref<number | null>(null)

// Automatically select the first message if available and none is selected
if (messages.value.length > 0 && selectedMessageKey.value === null) {
  selectedMessageKey.value = messages.value[0].id
}

function renderIcon(icon: Component) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const messageMenuOptions = computed<MenuOption[]>(() => {
  // Computed property to generate menu options from the allMessages array
  return messages.value.map((msg) => ({
    label: `任務ID：${msg.taskId}\n更新時間：${msg.updateTime}`, // Use message's title for menu item label
    key: msg.id,
    icon: renderIcon(BookIcon), // Example: if icons were desired for each menu item
  }))
})

// Computed property to get the full selected message object
const selectedMessage = computed<Message | null>(() => {
  if (selectedMessageKey.value === null) {
    return null
  }
  return messages.value.find((msg) => msg.id === selectedMessageKey.value) || null
})

// Handler for menu updates (when a new item is selected)
// v-model:value on n-menu already updates selectedMessageKey.
// This function can be used for additional logic upon selection if needed.
const onMenuUpdate = (key: string, item: MenuOption) => {
  // console.log(`Menu item selected: key='${key}', label='${String(item.label)}'`);
  // Additional logic can be placed here, e.g., fetching more data for the selected item.
}

const get_messages = async () => {
  const res = await axios.get(`/api//task/msg/get/${userdata.group}`)
  console.log(res?.data.data)
  messages.value = res?.data.data
}

let intervalId: number = 0

onMounted(() => {
  get_messages()
  intervalId = window.setInterval(get_messages, 10000)
  console.log(messages.value)
})

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
</script>

<style scoped>
/* Add any component-specific styles here */
.content {
  /* Styles for the description content if needed */
  /* For example, ensuring long words break */
  word-break: break-word;
}
.n-card {
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.12),
    0 1px 2px rgba(0, 0, 0, 0.24);
}
</style>
