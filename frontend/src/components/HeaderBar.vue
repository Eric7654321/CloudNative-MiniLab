<template>
  <n-grid x-gap="12" :cols="2" class="header-bar-grid" :class="headerThemeClass">
    <n-grid-item class="header-user-info">
      <n-p>您好! {{ userdata.Role }} {{ userdata.name }}</n-p>
    </n-grid-item>
    <n-grid-item class="header-menu-items">
      <n-button text style="font-size: 20px" @click="requestToggleTheme">
        <n-icon>
          <DarkModeOutlined v-if="themeStore.theme.name === 'dark'" />
          <LightModeOutlined v-if="themeStore.theme.name === 'light'" />
        </n-icon>
      </n-button>
      <n-menu mode="horizontal" v-model:options="options" @update:value="handleUpdateValue" />
    </n-grid-item>
  </n-grid>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue' // Import computed
import { useCookies } from 'vue3-cookies'
import { useRouter } from 'vue-router'
import { useUserData } from '@/stores/UserData'
import { NButton, NGrid, NGridItem, NP, NMenu, NIcon, type MenuOption } from 'naive-ui'
import { LightModeOutlined, DarkModeOutlined } from '@vicons/material'
import { useTheme } from '@/stores/theme'

const router = useRouter()
const userdata = useUserData()
const { cookies } = useCookies()
const themeStore = useTheme()

// Computed property to determine the header's theme class
const headerThemeClass = computed(() => {
  return themeStore.theme.name === 'dark' ? 'header-dark' : 'header-light'
})

const options = ref<MenuOption[]>([
  {
    label: '首頁',
    key: '/',
  },
  {
    label: '行事曆',
    key: '/calendar',
  },
  {
    label: '訊息',
    key: '/message',
  },
  {
    label: '回報',
    key: '/report',
  },
  {
    label: '排程',
    key: '/taskassign',
  },
  {
    label: '登出',
    key: 'logout',
  },
])

const handleUpdateValue = (key: string, item: MenuOption) => {
  if (key === 'logout') {
    goLogout()
  } else {
    router.push(key)
  }
}

const goLogout = () => {
  cookies.remove('token')
  userdata.reset()
  router.push('/')
}

const requestToggleTheme = () => {
  themeStore.toggleTheme()
}

const emit = defineEmits()
</script>

<style scoped>
.header-bar-grid {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 60px;
  padding: 0 24px;
  box-sizing: border-box;
  align-items: center;
  z-index: 1000;
  transition: background-color 0.3s; /* Add transition for smooth background color change */
  /* Default/Light theme background and border */
  /* background-color: #ffffff; */ /* Naive UI light theme card color by default is white */
  /* border-bottom: 1px solid #efefef; */ /* A light border */
}

/* Styles for light theme header */
.header-light {
  background-color: #ffffff; /* Or your preferred light theme header background */
  border-bottom: 1px solid #efefef;
}

/* Styles for dark theme header */
.header-dark {
  background-color: #2c2c2c; /* Or your preferred dark theme header background, e.g., Naive UI's dark card color */
  border-bottom: 1px solid #3a3a3a; /* A slightly lighter border for dark theme */
}

/* Ensure text color in header is readable against different backgrounds */
/* You might need to adjust this based on your specific theme colors */
.header-light .n-p,
.header-light .n-menu .n-menu-item-content__title {
  color: #333333; /* Darker text for light background */
}
.header-dark .n-p,
.header-dark .n-menu .n-menu-item-content__title {
  color: #ffffffcc; /* Lighter text for dark background (Naive UI uses opacity) */
}
.header-dark .n-button .n-icon,
.header-dark .n-menu .n-icon {
  color: #ffffffcc;
}

.header-user-info {
  /* The n-grid-item itself will align its content (n-p) based on the parent's align-items */
}

.header-user-info .n-p {
  margin: 0;
  font-size: 16px;
}

.header-menu-items {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.header-menu-items > .n-button {
  margin-right: 20px;
}
</style>
