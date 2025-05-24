<script setup lang="ts">
import type { Ref } from 'vue'
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import axios, { type AxiosResponse } from 'axios'
import { useCookies } from 'vue3-cookies'
import AlertText from '../components/AlertText.vue'
import { useUserData } from '@/stores/UserData'

let submiting = false

const router = useRouter()
const { cookies } = useCookies()

const account: Ref<string> = ref('')
const password: Ref<string> = ref('')

const account_id: string = 'account'
const password_id: string = 'password'

const isUsernameFocused = ref(false)
const isPasswordFocused = ref(false)

const LoginWarn: Ref<InstanceType<typeof AlertText> | null> = ref(null)
const PasswdWarn: Ref<InstanceType<typeof AlertText> | null> = ref(null)

const submit = async () => {
  if (submiting) {
    alert('submitting...')
    return
  }
  submiting = true
  let is_empty: boolean = false

  if (account.value === '') {
    // alert("Please enter your account");
    LoginWarn.value?.set_message('Account is required')
    LoginWarn.value?.set_display(true)
    submiting = false
    is_empty ||= true
  } else {
    LoginWarn.value?.set_display(false)
  }
  if (password.value === '') {
    PasswdWarn.value?.set_message('Password is required')
    PasswdWarn.value?.set_display(true)
    submiting = false
    is_empty ||= true
  } else {
    PasswdWarn.value?.set_display(false)
  }

  if (is_empty) return

  console.log('submit')
  console.log(`account: ${account.value}, passwd: ${password.value}`)

  let result: AxiosResponse<any, any>
  try {
    result = await axios.post(
      '/api/login',
      {
        username: account.value,
        password: password.value,
      },
      {
        timeout: 1000,
      },
    )

    console.log(result.data)

    if (result.data.code === 1) {
      cookies.set('token', result.data.data.jwt, '7d')
      useUserData().loadDataFromCookie()

      router.push({ path: '/loginRedirect' })
    } else if (result.data.code === 0) {
      PasswdWarn.value?.set_message(result.data.msg)
      PasswdWarn.value?.set_display(true)
    }
  } catch (error) {
    alert('Network error')
  }

  submiting = false
}
</script>

<template>
  <div class="login-container">
    <h2>登入系統 (Login to the System)</h2>
    <form @submit.prevent="submit" class="login-form">
      <div class="input-group" :class="{ 'has-content': account || isUsernameFocused }">
        <label for="username">使用者名稱 (Username)</label>
        <input
          type="text"
          id="username"
          v-model="account"
          @focus="isUsernameFocused = true"
          @blur="isUsernameFocused = false"
          required
        />
        <AlertText ref="LoginWarn" />
      </div>

      <div class="input-group" :class="{ 'has-content': password || isPasswordFocused }">
        <label for="password">密碼 (Password)</label>
        <input
          type="password"
          id="password"
          v-model="password"
          @focus="isPasswordFocused = true"
          @blur="isPasswordFocused = false"
          required
        />
        <AlertText ref="PasswdWarn" />
      </div>
      <button type="submit" class="login-button">登入 (Login)</button>
    </form>
  </div>
</template>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 100px auto;
  padding: 30px;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  font-family: 'Arial', sans-serif;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 25px;
}

.login-form {
  display: flex;
  flex-direction: column;
}

.input-group {
  position: relative;
  margin-bottom: 25px;
}

.input-group input {
  width: 100%;
  padding: 12px 10px; /* 增加上下 padding 給 label 空間 */
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
  background-color: #fff;
  box-sizing: border-box; /* 確保 padding 和 border 不會增加元素的總寬高 */
  outline: none; /* 移除 focus 時的預設 outline */
}

.input-group input:focus {
  border-color: #007bff; /* Focus 時的邊框顏色 */
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25); /* Focus 時的光暈效果 */
}

.input-group label {
  position: absolute;
  left: 10px;
  top: 50%; /* 初始垂直居中 */
  transform: translateY(-50%);
  color: #999;
  background-color: transparent; /* 初始透明背景 */
  padding: 0 5px;
  transition: all 0.2s ease-out; /* 動畫效果 */
  pointer-events: none; /* 讓點擊可以穿透 label 到 input */
  font-size: 16px;
}

/* 當 input focus 或有內容時，label 的樣式 */
.input-group.has-content label,
.input-group input:focus + label {
  top: 0;
  transform: translateY(-50%) scale(0.85); /* 向上移動並縮小 */
  left: 8px;
  color: #007bff; /* Active 時的 label 顏色 */
  background-color: #f9f9f9; /* 與容器背景色相同，製造"切割"效果 */
  font-weight: bold;
}

/* 針對 autofill 的背景色處理 */
.input-group input:-webkit-autofill,
.input-group input:-webkit-autofill:hover,
.input-group input:-webkit-autofill:focus,
.input-group input:-webkit-autofill:active {
  -webkit-box-shadow: 0 0 0 30px white inset !important; /* 強制背景為白色 */
  transition: background-color 5000s ease-in-out 0s; /* 延遲瀏覽器自動填充樣式的應用 */
}
/* 如果 autofill 後 label 沒有浮動，可以加這條 (通常 has-content 會處理) */
.input-group input:-webkit-autofill + label {
  top: 0;
  transform: translateY(-50%) scale(0.85);
  left: 8px;
  color: #007bff;
  background-color: #f9f9f9;
  font-weight: bold;
}

.login-button {
  padding: 12px 15px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.login-button:hover {
  background-color: #0056b3;
}
</style>
