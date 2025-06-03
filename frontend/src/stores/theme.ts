import { defineStore } from 'pinia'
import { darkTheme, lightTheme } from 'naive-ui'
import type { BuiltInGlobalTheme } from 'naive-ui/es/themes/interface'

interface Theme {
  theme: BuiltInGlobalTheme
}

export const useTheme = defineStore('Theme', {
  state: () => ({
    theme: lightTheme,
  }),
  actions: {
    toggleTheme() {
      if (this.theme.name === 'dark') {
        this.theme = lightTheme
      } else {
        this.theme = darkTheme
      }
    },
  },
})
