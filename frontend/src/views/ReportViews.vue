<template>
  <n-flex align="center" vertical>
    <n-h1>回報表單</n-h1>

    <n-form ref="FormRef" :model="FormInput" :rules="rules" label-placement="left" label-width="auto"
      require-mark-placement="right-hanging">
      <n-form-item path="groupId" label="群組ID">
        <n-input v-model:value="FormInput.groupId" :disabled="true" type="text" />
      </n-form-item>
      <n-form-item path="taskid" label="任務ID">
        <!-- 修改開始 -->
        <n-select v-model:value="FormInput.taskid" :options="taskOptions" placeholder="選擇任務ID" clearable filterable />
        <!-- 修改結束 -->
      </n-form-item>
      <n-form-item path="type" label="回報種類">
        <n-select :options="reportTypeOptions" v-model:value="FormInput.type" placeholder="選擇種類" />
      </n-form-item>
      <n-form-item path="message" label="備註">
        <n-input v-model:value="FormInput.message" type="textarea" @keydown.enter.prevent clearable
          placeholder="輸入備註" />
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
  // NInputNumber, // 不再需要 NInputNumber，除非其他地方用到
  NSpin,
  NH1,
  NFlex,
  useMessage,
} from 'naive-ui'
import axios from 'axios'
import { ref, onMounted } from 'vue' // 引入 onMounted (如果需要異步獲取任務列表)
// import { isForOfStatement } from 'typescript' // 這行看起來是未使用的，可以移除

// --- Task Interface ---
interface Task {
  id: number;
  emp: number | null; // Employee ID
  empName: string;   // Employee Name
  machine: string;   // JSON string of machine IDs
  machineName: string; // JSON string of machine names
  startTime: string | null;
  endTime: string | null;
  tag: string; // Comma-separated string of skills
  description: string;
  group: string;
  updaterId: number | null;
  isFinish: number; // 0 for not finished, 1 for finished
  updateTime: string | null;
}


const message = useMessage()
const userdata = useUserData()

// 回報種類選項
const reportTypeOptions: Array<SelectOption | SelectGroupOption> = [
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

// 新增：任務ID選項 (這裡使用靜態數據作為範例)
// 在實際應用中，你可能會從 API 獲取這些數據
const taskOptions = ref<Array<SelectOption | SelectGroupOption>>([])

// 模擬從API獲取任務列表
const fetchTaskOptions = async () => {
  // 假設的 API 請求
  const response = await axios.get(`/api/task/search/${userdata.group}`);
  const task: Task[] = response?.data.data

  // taskOptions.value = response.data.map(task => ({ label: `任務 ${task.name} (ID: ${task.id})`, value: task.id }));

  // 靜態範例數據
  taskOptions.value = task.filter(t => (userdata.role === 0 ? (t.emp === userdata.id) : 1) && t.isFinish === 0).map(t => ({
    label: `${t.empName} ${t.id.toString()} (${t.description})`,
    value: t.id,
  }))
    ;
}

// 在組件掛載時獲取任務選項
onMounted(() => {
  fetchTaskOptions();
})


const FormRef = ref<FormInst | null>(null)
const loading = ref(false)
const FormInput = ref({
  taskid: null as number | null, // 保持 null 或 number 類型
  type: null,
  groupId: useUserData().group,
  id: useUserData().id,
  message: '',
  time: '',
})

const validateMessageRequire = () => {
  if (FormInput.value.type === '其他') {
    return FormInput.value.message !== null && FormInput.value.message.trim() !== ''
  }
  return true
}

const rules: FormRules = {
  taskid: {
    required: true,
    type: 'number', // 如果 value 是數字，type 保持 'number' 或 'integer'
    message: '請選擇任務ID', // 更新錯誤訊息
    trigger: ['blur', 'change'], // 'change' 對 select 比較合適
  },
  type: {
    required: true,
    message: '請選擇回報種類',
    trigger: ['input', 'blur', 'change'],
  },
  message: {
    validator: validateMessageRequire,
    message: '當回報種類為"其他"時，請輸入註解', // 稍微調整訊息使其更清晰
    trigger: ['blur', 'input'],
  },
}

const handle_submit = async (e: MouseEvent) => {
  e.preventDefault()

  FormRef.value?.validate(async (errors) => {
    if (!errors) {
      loading.value = true; // 先觸發 validate，成功後再設 loading
      try {
        // const time = Date.now() // time 變數未使用，可以移除或賦值給 FormInput.time
        await axios.post('/api/task/msg/send', {
          id: FormInput.value.id,
          taskId: FormInput.value.taskid, // 這裡傳送的是選中的 taskid
          description: `[${FormInput.value.type}]\n${FormInput.value.message}`,
          group: FormInput.value.groupId,
          status: 0, // 根據你的業務邏輯，這裡的 status 可能需要調整
        })

        if(FormInput.value.type === '任務完成') {
          await axios.put('/api/schedule/task/update', {
            id: FormInput.value.taskid,
            emp: userdata.id,
            isFinish: 1,
          })
        }

        FormInput.value = {
          taskid: null,
          type: null,
          groupId: useUserData().group,
          id: useUserData().id,
          message: '',
          time: '',
        }
        fetchTaskOptions();
        // 手動清除驗證狀態，避免重置後仍顯示之前的錯誤
        FormRef.value?.restoreValidation()
        message.success('提交成功')
      } catch (apiError) {
        console.error('API submission error:', apiError)
        message.error('提交失敗，請稍後再試')
      } finally {
        loading.value = false
      }
    } else {
      console.log('Form validation errors:', errors)
      message.error('請檢查表單並完成必填項')
      loading.value = false; // 驗證失敗也應停止 loading
    }
  })
}
</script>

<style lang="css" scoped>
.n-flex {
  /* position: flex; */
  /* 'flex' 不是 position 的有效值，通常是 relative, absolute, fixed, static, sticky */
  width: 100vw;
  /* 這樣會導致水平滾動條，因為 n-form 本身可能有 padding/margin */
  /* height: calc(100vh - 60px); */
  /* 這樣會導致垂直滾動條，因為 n-h1 等元素佔據空間 */
  /* display: flex; */
  /* n-flex 已經是 flex 容器了 */
  /* overflow: hidden; */
  /* 可能會隱藏滾動條，但不解決根本佈局問題 */

  /* 建議的佈局調整 */
  max-width: 600px;
  /* 給表單一個最大寬度，使其在寬螢幕上不會太展開 */
  margin: 20px auto;
  /* 上下邊距20px，左右自動居中 */
  padding: 20px;
  /* 內部留白 */
  box-sizing: border-box;
  /* 確保 padding 不會增加總寬度 */
}

/* 如果希望表單內容在垂直方向上填滿可用空間，同時允許滾動 */
/* .n-flex {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  min-height: calc(100vh - 60px); // 假設 60px 是 header 高度
  padding: 20px;
  box-sizing: border-box;
}
.n-form {
  width: 100%;
  max-width: 500px; // 控制表單的最大寬度
} */
</style>
