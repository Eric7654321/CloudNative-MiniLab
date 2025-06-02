<template>
  <n-flex align="center" vertical>
    <n-h1>回報表單</n-h1>

    <n-form
      ref="FormRef"
      :model="FormInput"
      :rules="rules"
      label-placement="left"
      label-width="auto"
      require-mark-placement="right-hanging"
    >
      <n-form-item path="groupId" label="群組ID">
        <n-input v-model:value="FormInput.groupId" :disabled="true" type="text" />
      </n-form-item>
      <n-form-item path="taskid" label="任務ID">
        <n-input-number v-model:value="FormInput.taskid" :min="1" placeholder="任務ID" />
      </n-form-item>
      <n-form-item path="type" label="回報種類">
        <n-select :options="options" v-model:value="FormInput.type" placeholder="選擇種類" />
      </n-form-item>
      <n-form-item path="message" label="備註">
        <n-input
          v-model:value="FormInput.message"
          type="textarea"
          @keydown.enter.prevent
          clearable
          placeholder="輸入備註"
        />
      </n-form-item>
      <n-button type="primary" @click="handle_submit">
        <n-spin :show="loading" size="small"> 提交 </n-spin>
      </n-button>
    </n-form>
  </n-flex>
</template>

<script lang="ts" setup>
import { useUserData } from '@/stores/UserData'
import type { FormInst, FormRules, SelectOption, SelectGroupOption } from 'naive-ui'
import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NInputNumber,
  NSpin,
  NH1,
  NFlex,
  useMessage,
} from 'naive-ui'
import axios from 'axios'
import { ref } from 'vue'
import { isForOfStatement } from 'typescript'

const message = useMessage()
const options: Array<SelectOption | SelectGroupOption> = [
  {
    label: '任務完成',
    value: '任務完成',
  },
  {
    label: '機器故障',
    value: '機器故障',
  },
  {
    label: '其他',
    value: '其他',
  },
]

// const message = useMessage()
const FormRef = ref<FormInst | null>(null)
const loading = ref(false)
const FormInput = ref({
  taskid: null,
  type: null,
  groupId: useUserData().group,
  id: useUserData().id,
  message: '',
  time: '',
})

const validateMessageRequire = () => {
  if (FormInput.value.type === '其他') {
    return FormInput.value.message !== null && FormInput.value.message !== ''
  } else return true
}

const rules: FormRules = {
  taskid: {
    required: true,
    type: 'integer',
    message: '請輸入任務ID(整數)',
    trigger: ['blur'],
  },
  type: {
    required: true,
    message: '請選擇回報種類',
    trigger: ['input', 'blur'],
  },
  message: {
    validator: validateMessageRequire,
    message: '請輸入註解',
    trigger: 'blur',
  },
}

const handle_submit = async (e: MouseEvent) => {
  e.preventDefault()
  loading.value = true
  try {
    await FormRef.value?.validate(async (errors, { warnings }) => {
      if (!errors) {
        const time = Date.now()
        await axios.post('/api/task/msg/send', {
          id: FormInput.value.id,
          taskId: FormInput.value.taskid,
          description: `[${FormInput.value.type}]\n${FormInput.value.message}`,
          group: FormInput.value.groupId,
          status: 0,
        })

        FormInput.value = {
          taskid: null,
          type: null,
          groupId: useUserData().group,
          id: useUserData().id,
          message: '',
          time: '',
        }
        message.success('完成')
        loading.value = false
      } else {
        message.error('請完成表單')
      }
    })
  } catch (error) {
    // console.error('Error during processing', error)
    // message.error('驗證過程發生錯誤')
    loading.value = false
  }
}
</script>

<style lang="css" scoped>
.n-flex {
  position: flex;
  width: 100vw;
  height: calc(100vh - 60px);
  display: flex;
  overflow: hidden;
}
</style>
