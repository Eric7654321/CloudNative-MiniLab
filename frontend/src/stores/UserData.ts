import { defineStore } from 'pinia'
import { useCookies } from 'vue3-cookies'
import { jwtDecode } from 'jwt-decode'
import axios from 'axios'

interface UserData {
  id: number
  name: string
  username: string
  group: string
  role: number
  usable: boolean
  tags: string[]
  exp: number
}

export const useUserData = defineStore('UserData', {
  state: () => ({
    token: '',
    // isAuth: true,
    id: 0,
    username: '',
    name: '',
    group: '',
    role: 0,
    exp: 0,
    tryCookieBeforeAuth: true,
  }),
  getters: {
    isAuth: (stat): boolean => {
      if (stat.tryCookieBeforeAuth === true) {
        stat.tryCookieBeforeAuth = false
        useUserData().loadDataFromCookie()
      }
      if (
        stat.token === null ||
        stat.token === '' ||
        Number(new Date().getTime()) - stat.exp <= 0
      ) {
        return false
      } else {
        return true
      }
    },
    Role: (stat): string => {
      switch (stat.role) {
        case 1:
          return '管理員'
        case 0:
          return '員工'
        default:
          return ''
      }
    },
  },
  actions: {
    loadDataFromCookie() {
      const { cookies } = useCookies()
      this.token = cookies.get('token')
      if (this.token !== '' && this.token !== null) {
        const data = jwtDecode<UserData>(this.token)
        this.id = data.id
        this.username = data.username
        this.name = data.name
        this.group = data.group
        this.role = data.role
        this.exp = data.exp
        this.tryCookieBeforeAuth = false
        axios.defaults.headers.common['token'] = `${this.token}`
      } else {
        this.tryCookieBeforeAuth = true
      }
    },
    reset() {
      this.token = ''
      this.id = 0
      this.username = ''
      this.name = ''
      this.group = ''
      this.role = 0
      this.exp = 0
      this.tryCookieBeforeAuth = true
      axios.defaults.headers.common['token'] = null
    },
    auth() {},
  },
})
