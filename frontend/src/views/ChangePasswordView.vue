<script setup lang="ts">
import type { Ref } from 'vue'
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import axios, { type AxiosResponse } from 'axios'
import { useCookies } from 'vue3-cookies'
import AlertText from '../components/AlertText.vue'

let submiting = false

const router = useRouter()
const { cookies } = useCookies()

const old_password: Ref<string> = ref('')
const new_passowrd: Ref<string> = ref('')
const new_passowrd_check: Ref<string> = ref('')

const account_id: string = 'account'
const password_id: string = 'password'

const LoginWarn: Ref<InstanceType<typeof AlertText> | null> = ref(null)
const PasswdWarn: Ref<InstanceType<typeof AlertText> | null> = ref(null)

const submit = async () => {}
</script>
<template>
  <header>
    <h1>Login to the System</h1>
  </header>
  <div class="login">
    <div style="">
      <div class="input">
        <!-- <label for="account">Account</label> -->
        <input
          v-model.trim="old_password"
          @keyup.enter="submit"
          :id="account_id"
          type="password"
          placeholder="Password"
        />
        <AlertText ref="LoginWarn" />
      </div>
      <div class="input">
        <!-- <label for="password">Password</label> -->
        <input
          v-model.trim="new_passowrd"
          @keyup.enter="submit"
          :id="password_id"
          type="password"
          placeholder="New Password"
        />
        <AlertText ref="PasswdWarn" />
      </div>
      <div class="input">
        <!-- <label for="password">Password</label> -->
        <input
          v-model.trim="new_passowrd_check"
          @keyup.enter="submit"
          :id="password_id"
          type="password"
          placeholder="Check Password"
        />
        <AlertText ref="PasswdWarn" />
      </div>
      <div class="input">
        <input type="submit" value="Login" @click="submit" />
      </div>
    </div>
  </div>
</template>

<style scoped>
input {
  width: 15em;
  height: 3em;
  font-size: 1em;
}

#password,
#account {
  width: 100%;
  padding: 10px 5px;
  font-size: 16px;
  background: transparent;
  border: none;
  border-bottom: 1px solid #aaa;
  color: white;

  /* animation ; */
  background-image: linear-gradient(to right, #42a5f5 50%, #42a5f5 50%);
  background-position: left bottom;
  background-size: 0% 2px;
  background-repeat: no-repeat;
  /* transition: border-bottom 0.4s ease; */
  outline: none;
}

input#password:focus,
input#account:focus {
  border-bottom: 2px solid #aaa;
  outline: none;
}

input[type='submit'] {
  background-color: transparent;
  color: #0185ff;
  border: 2px solid #0185ff;
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
  border-radius: 6px;
  transition:
    background-color 0.3s,
    color 0.3s,
    border-color 0.3s;
}

input[type='submit']:hover {
  background-color: #00a3ff;
  border-color: #00a3ff;
  color: #181818;
}

.input {
  margin: 10px;
}

h1 {
  text-align: center;
  color: white;
}

.login {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
}
</style>
