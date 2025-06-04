<template>
  <n-layout style="height: 100vh">
    <n-layout-header
      bordered
      style="padding: 12px 24px; display: flex; align-items: center; justify-content: space-between"
    >
      <n-h2 style="margin: 0">
        <n-icon :component="TaskIcon" style="margin-right: 8px; vertical-align: -0.15em" />
        任務排程
      </n-h2>
      <n-space>
        <n-button
          type="primary"
          @click="handleConfirmBufferedTasks"
          :disabled="bufferedTasks.length === 0"
          :icon="renderIcon(SendIcon)"
        >
          確認並提交緩衝區 ({{ bufferedTasks.length }})
        </n-button>
        <n-button ghost @click="handleShowAddModal" :icon="renderIcon(AddIcon)"
          >新增任務至緩衝區</n-button
        >
      </n-space>
    </n-layout-header>
    <n-layout-content content-style="padding: 24px;">
      <!-- Buffer Table -->
      <n-card
        v-if="bufferedTasks.length > 0"
        title="待確認任務緩衝區"
        :bordered="false"
        style="margin-bottom: 24px"
        content-style="padding:0;"
      >
        <n-data-table
          :columns="bufferColumns"
          :data="bufferedTasks"
          :bordered="false"
          :single-line="false"
          size="small"
          :row-key="(row) => row.id"
        />
      </n-card>

      <n-card :bordered="false" content-style="padding: 0;">
        <n-data-table
          :columns="columns"
          :data="data"
          :bordered="false"
          :single-line="false"
          :pagination="pagination"
          :row-key="(row) => row.id"
          striped
          size="small"
          :max-height="tableMaxHeight"
          :scroll-x="1900"
        />
      </n-card>
    </n-layout-content>
  </n-layout>

  <!-- Add/Edit Task Modal -->
  <n-modal
    v-model:show="showTaskModal"
    preset="card"
    style="width: 600px"
    :title="modalTitle"
    :bordered="false"
    size="huge"
    :segmented="{ content: 'soft', footer: 'soft' }"
    :mask-closable="false"
  >
    <n-form-item label="技能標籤" path="skillTags">
      <!-- Custom path for validation if needed -->
      <n-radio-group v-model:value="selectedSkillTags" @update-value="handleSelectUpdate">
        <n-space item-style="display: flex;">
          <n-radio
            v-for="skill in skillTagOptions"
            :key="skill.value"
            :value="skill.value"
            :label="skill.label"
          />
        </n-space>
      </n-radio-group>
    </n-form-item>
    <n-form-item label="任務描述" path="description">
      <n-input
        v-model:value="currentTask.description"
        type="textarea"
        :autosize="{ minRows: 2, maxRows: 5 }"
        placeholder="請輸入任務描述"
      />
    </n-form-item>
    <n-form
      ref="formRef"
      :model="currentTask"
      :rules="formRules"
      label-placement="left"
      label-width="auto"
    >
      <n-form-item label="任務 ID" path="id" v-if="isEditing">
        <n-input-number v-model:value="currentTask.id" disabled />
      </n-form-item>
      <!-- 1. 員工 ID 刪除 -->
      <!-- 2. 員工名稱改成選項 -->
      <n-form-item label="員工名稱" path="emp">
        <!-- Path is now 'emp' for the ID -->
        <n-select
          v-model:value="currentTask.emp"
          filterable
          placeholder="請選擇員工"
          :options="employeeOptions"
          clearable
          style="width: 100%"
        />
      </n-form-item>
      <!-- 3. 機台ID 刪除 -->
      <!-- 4. 機臺名稱改成選項 -->
      <n-form-item label="機臺名稱" path="machineIds">
        <!-- Custom path for validation if needed -->
        <n-select
          v-model:value="selectedMachineIds"
          multiple
          filterable
          placeholder="請選擇機台 (可多選)"
          :options="machineOptions"
          clearable
          style="width: 100%"
        />
      </n-form-item>
      <n-form-item label="開始時間" path="startTime">
        <n-date-picker
          v-model:formatted-value="currentTask.startTime"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
          clearable
          style="width: 100%"
          placeholder="請選擇開始時間"
        />
      </n-form-item>
      <n-form-item label="結束時間" path="endTime">
        <n-date-picker
          v-model:formatted-value="currentTask.endTime"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
          clearable
          style="width: 100%"
          placeholder="請選擇結束時間"
        />
      </n-form-item>
      <!-- 5. 技能標籤改成勾選包含 ( 電性、物性、化性 ） -->
      <!-- 6. 群組 ID 跟使用者一樣 (已在 getEmptyTask 設定, 不顯示在表單) -->
      <!-- 7. ID使用使用者ID (假設指更新者ID, 已在 getEmptyTask 設定, 不顯示在表單) -->
      <!-- 8. 刪除完成/未完成選項，一律為未完成 (已在 getEmptyTask 設定, 不顯示在表單) -->
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showTaskModal = false">取消</n-button>
        <n-button type="primary" @click="handleSaveTask">{{
          isEditing ? '儲存變更' : '新增至緩衝區'
        }}</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { h, ref, onMounted, onUnmounted, watch, computed } from 'vue'
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NDataTable,
  NTag,
  NTooltip,
  NText,
  NH2,
  NIcon,
  NCard,
  NButton,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NDatePicker,
  NSelect,
  NSpace,
  NInputNumber,
  NPopconfirm,
  NCheckbox,
  NCheckboxGroup, // Added NCheckbox, NCheckboxGroup
  useMessage,
  useDialog,
  type DataTableColumns,
  type PaginationProps,
  type FormInst,
  type FormRules,
  type FormItemRule,
} from 'naive-ui'
import {
  ListOutline as TaskIcon,
  CreateOutline as EditIcon,
  TrashOutline as DeleteIcon,
  AddOutline as AddIcon,
  SendOutline as SendIcon,
} from '@vicons/ionicons5'
import axios from 'axios'
import { useUserData } from '@/stores/UserData'

// --- Task Interface ---
interface Task {
  id: number
  emp: number | null // Employee ID
  empName: string // Employee Name
  machine: string // JSON string of machine IDs
  machineName: string // JSON string of machine names
  startTime: string | null
  endTime: string | null
  tag: string // Comma-separated string of skills
  description: string
  group: string
  updaterId: number | null
  isFinish: number // 0 for not finished, 1 for finished
  updateTime: string | null
}

interface Employee {
  group: string
  id: number
  jwt: string
  name: string
  role: 0 | 1
  tags: string // JSON string
  updateTime: string
  usable: 1 | 0
  username: string
}

interface Machine {
  id: number // 機器 ID
  name: string // 顯示名稱
  machineName: string // 機器內部名稱
  usable: number // 是否啟用
  group: string // 所屬群組
  updateTime: string // 更新時間 (ISO string)
  tags: string // JSON 格式標籤陣列
}

interface FormOptions {
  label: string
  value: number | string
  name?: string
}

const userdata = useUserData()

// --- Options for Selects/Checkboxes ---
const employeeOptions = ref<FormOptions[]>([])

const machineOptions = ref<FormOptions[]>([])

const skillTagOptions = ref([
  { label: '電性', value: '電性' },
  { label: '物性', value: '物性' },
  { label: '化性', value: '化性' },
])

// --- Refs for multiple select and checkbox group ---
const selectedMachineIds = ref<string[]>([])
const selectedSkillTags = ref<string>('')

const renderIcon = (icon: any) => {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const formatDateTime = (dateTimeString: string | null | undefined) => {
  if (!dateTimeString) return 'N/A'
  try {
    const date = new Date(dateTimeString)
    if (isNaN(date.getTime())) return dateTimeString
    return date
      .toLocaleString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false,
      })
      .replace(/\//g, '-')
  } catch (e) {
    console.error('Error formatting date:', dateTimeString, e)
    return dateTimeString
  }
}

const renderJsonArray = (jsonString: string | null | undefined, maxItemsToShow = 2) => {
  if (!jsonString) return h(NText, { depth: 3 }, { default: () => '無' })
  try {
    const arr = JSON.parse(jsonString)
    if (Array.isArray(arr) && arr.length > 0) {
      const displayedItems = arr.slice(0, maxItemsToShow).join(', ')
      const remainingItemsCount = arr.length - maxItemsToShow
      if (remainingItemsCount > 0) {
        return h(NTooltip, null, {
          trigger: () => h('span', `${displayedItems}... (+${remainingItemsCount})`),
          default: () =>
            h('div', { style: 'max-width: 300px; word-break: break-all;' }, arr.join(', ')),
        })
      }
      return displayedItems
    } else if (Array.isArray(arr) && arr.length === 0) {
      return h(NText, { depth: 3 }, { default: () => '[] (空)' })
    }
    return h(NText, { type: 'error' }, { default: () => '格式錯誤' })
  } catch (e) {
    return h(
      NTooltip,
      { placement: 'top' },
      {
        trigger: () => h(NText, { type: 'error' }, { default: () => '解析失敗' }),
        default: () => `原始值: ${jsonString}`,
      },
    )
  }
}

// ----- Task Modal State and Logic (Add/Edit) -----
const showTaskModal = ref(false)
const currentTask = ref<Task>({} as Task)
const formRef = ref<FormInst | null>(null)
const message = useMessage()
const dialog = useDialog()
const isEditing = ref(false)

const modalTitle = computed(() => (isEditing.value ? '編輯任務' : '新增任務至緩衝區'))

const getEmptyTask = (): Task => ({
  id: 0,
  emp: null, // Employee ID
  empName: '', // Will be derived
  machine: '[]', // JSON string of machine IDs, will be derived
  machineName: '[]', // JSON string of machine names, will be derived
  startTime: null,
  endTime: null,
  tag: '', // Comma-separated string, will be derived
  description: '',
  group: userdata.group || '', // 6. 群組 ID 跟使用者一樣
  updaterId: parseInt(userdata.id || '999'), // 7. ID使用使用者ID (updaterId)
  isFinish: 0, // 8. 一律為未完成
  updateTime: null,
})

const formRules: FormRules = {
  emp: [{ required: true, type: 'number', message: '請選擇員工', trigger: ['change', 'blur'] }],
  startTime: [
    { required: true, message: '請選擇開始時間', trigger: ['change', 'blur'], type: 'string' },
  ],
  // Validation for selectedMachineIds and selectedSkillTags can be added if needed, e.g., at least one selected
  machineIds: [
    // This is a placeholder path, actual validation logic would be on selectedMachineIds
    {
      validator: (rule: FormItemRule, value: any) => {
        // value here is not directly used. We check selectedMachineIds
        if (selectedMachineIds.value.length === 0) {
          // return new Error('請至少選擇一個機台'); // Optional: make it required
        }
        return true
      },
      trigger: ['change'],
    },
  ],
  skillTags: [
    // Placeholder path
    {
      validator: (rule: FormItemRule, value: any) => {
        // if (selectedSkillTags.value.length === 0) {
        //   return new Error('請至少選擇一個技能標籤'); // Optional: make it required
        // }
        return true
      },
      trigger: ['change'],
    },
  ],
}

const handleShowAddModal = () => {
  isEditing.value = false
  currentTask.value = getEmptyTask()
  selectedMachineIds.value = [] // Reset selections
  selectedSkillTags.value = '' // Reset selections
  showTaskModal.value = true
  formRef.value?.restoreValidation()
}

const handleEdit = (row: Task) => {
  isEditing.value = true
  currentTask.value = JSON.parse(JSON.stringify(row))
  // Populate selections from currentTask
  try {
    selectedMachineIds.value = JSON.parse(row.machine || '[]')
  } catch (e) {
    selectedMachineIds.value = []
    console.error('Error parsing machine IDs for edit:', e)
  }
  selectedSkillTags.value = row.tag ? row.tag.split(',').filter((t) => t)[0] : ''

  showTaskModal.value = true
  formRef.value?.restoreValidation()
}

// ----- Buffer Logic -----
const bufferedTasks = ref<Task[]>([])
let tempIdCounter = Date.now()

const handleSaveTask = () => {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      const taskToProcess = { ...currentTask.value }

      // 1. Derive empName from selected emp (ID)
      // currentTask.emp holds the employee ID.
      // employeeOptions is like: [{ label: 'Employee Name', value: employee_id }, ...]
      const selectedEmpOption = employeeOptions.value.find((opt) => opt.value === taskToProcess.emp)
      taskToProcess.empName = selectedEmpOption ? selectedEmpOption.label : ''

      // 2. Derive machine (JSON string of IDs) - This is already correct as selectedMachineIds holds IDs
      // currentTask.machine should store the JSON string of selected machine IDs
      taskToProcess.machine = JSON.stringify(selectedMachineIds.value) // selectedMachineIds.value is an array of machine IDs

      // 3. Derive machineName (JSON string of names) from selectedMachineIds
      // machineOptions is like: [{ label: 'Machine Display Name', value: machine_id }, ...]
      taskToProcess.machineName = JSON.stringify(
        selectedMachineIds.value
          .map((id) => {
            // id here is a machine_id
            const machine = machineOptions.value.find((opt) => opt.value === id) // Find by machine_id
            return machine ? machine.label : '' // Use the label (display name)
          })
          .filter((name) => name), // Filter out any empty names if an ID wasn't found (should not happen in normal flow)
      )

      // 4. Derive tag (comma-separated string) from selectedSkillTags
      taskToProcess.tag = selectedSkillTags.value

      // 5. Set updateTime, updaterId, and group (already handled by getEmptyTask for new, but ensure for edits too)
      taskToProcess.updateTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
      taskToProcess.updaterId = parseInt(userdata.id || '999')
      taskToProcess.group = userdata.group || ''
      // taskToProcess.isFinish remains as is for editing, or 0 for new (set by getEmptyTask)

      if (isEditing.value) {
        // Try to update in the main data array (simulating backend update)
        try {
          const index = data.value.findIndex((task) => task.id === taskToProcess.id)
          if (index !== -1) {
            data.value[index] = { ...taskToProcess } // Ensure reactivity with a new object
            message.success('任務更新成功！ (本地)')
          } else {
            // This case might occur if the task was deleted from another source
            // Or if editing a task that was in buffer but not yet committed (not standard flow here)
            message.error('找不到要編輯的任務 (本地)')
          }
          showTaskModal.value = false
        } catch (error) {
          console.error('Error updating task:', error)
          message.error('任務更新失敗，請檢查網路或聯繫管理員。')
        }
      } else {
        // Add new task to buffer
        taskToProcess.id = tempIdCounter++ // Assign a temporary ID for buffer management
        taskToProcess.isFinish = 0 // Ensure new tasks are unfinished
        bufferedTasks.value.push(taskToProcess)
        message.success('任務已新增至緩衝區！')
        showTaskModal.value = false
      }
    } else {
      message.error('請檢查表單輸入是否正確。')
    }
  })
}

const handleConfirmBufferedTasks = async () => {
  if (bufferedTasks.value.length === 0) {
    message.info('緩衝區無任務可提交。')
    return
  }

  dialog.info({
    title: '確認提交',
    content: `確定要將緩衝區內的 ${bufferedTasks.value.length} 項任務提交至伺服器嗎？`,
    positiveText: '確定提交',
    negativeText: '取消',
    onPositiveClick: async () => {
      // 1. Transform buffered tasks to the backend payload format
      const tasksToSubmit = bufferedTasks.value.map((task) => {
        // Fields to exclude from payload: id (temporary), empName, machineName, updateTime (backend will set)
        return {
          emp: task.emp,
          machine: task.machine, // This is already a JSON string of machine IDs
          startTime: task.startTime ? task.startTime.replace(' ', 'T') : null, // Format to ISO-like with 'T'
          endTime: task.endTime ? task.endTime.replace(' ', 'T') : null, // Format to ISO-like with 'T'
          tag: task.tag,
          description: task.description,
          group: task.group,
          updaterId: task.updaterId,
          isFinish: task.isFinish, // Should be 0 for new tasks
        }
      })

      try {
        // 2. Make the API call to the backend
        //    Replace '/api/task' with your actual batch/bulk creation endpoint if different.
        //    The backend should be configured to accept an array of tasks at this endpoint.
        const response = await axios.post('/api/schedule/auto/ack', tasksToSubmit)

        // 3. Handle success
        if (
          (response.status === 200 || response.status === 201) &&
          response?.data.msg === 'success'
        ) {
          // Or other success codes
          message.success(`${tasksToSubmit.length} 項任務已成功提交至伺服器！`)
          bufferedTasks.value = [] // Clear the buffer
          await loadData() // Refresh the main task list from the server
        } else {
          // Handle cases where API call was "successful" (no network error) but didn't create tasks as expected
          message.error(
            `提交失敗：伺服器回應狀態 ${response.status}. ${response.data?.message || ''}`,
          )
          // Optionally, log response.data for more details
          console.error('Error submitting tasks, server response:', response.data)
        }
      } catch (error: any) {
        // 4. Handle API call error
        console.error('Error confirming buffered tasks:', error)
        let errorMessage = '提交緩衝區任務時發生錯誤。'
        if (axios.isAxiosError(error) && error.response) {
          errorMessage += ` 錯誤：${error.response.status} - ${error.response.data?.message || error.message}`
          // You might want to inspect error.response.data for more specific backend messages
          console.error('Backend error details:', error.response.data)
        } else if (error instanceof Error) {
          errorMessage += ` ${error.message}`
        }
        message.error(errorMessage)
        // Do NOT clear the buffer on error, so the user can retry or inspect.
      }
    },
  })
}

// ----- Main Table Columns & Data -----
const data = ref<Task[]>([])

const handleDelete = (taskToDelete: Task) => {
  dialog.warning({
    title: '確認刪除',
    content: `您確定要刪除任務 "${taskToDelete.empName || '未命名任務'}" (ID: ${taskToDelete.id}) 嗎？此操作將從伺服器永久刪除。`,
    positiveText: '確定刪除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        // 1. Prepare the payload for the DELETE request
        const payload = {
          id: taskToDelete.id,
        }

        // 2. Send the DELETE request to the backend
        // Note: For DELETE requests with a body, axios uses the `data` option in the config object.
        const response = await axios.delete('/api/schedule/task/delete', {
          data: payload,
        })

        // 3. Handle the response
        // Common success statuses for DELETE are 200 OK (if response body is returned) or 204 No Content.
        if (response.status === 200 || response.status === 204) {
          // Remove the task from the local data array
          const index = data.value.findIndex((task) => task.id === taskToDelete.id)
          if (index !== -1) {
            data.value.splice(index, 1)
          }
          message.success(`任務 (ID: ${taskToDelete.id}) 已成功從伺服器刪除。`)
          // Optional: call loadData() if you want to ensure the list is perfectly in sync
          // await loadData();
        } else {
          message.error(
            `刪除任務失敗：伺服器回應狀態 ${response.status}. ${response.data?.message || ''}`,
          )
          console.error('Error deleting task, server response:', response.data)
        }
      } catch (error: any) {
        console.error('Error deleting task:', error)
        let errorMessage = `刪除任務 (ID: ${taskToDelete.id}) 時發生錯誤。`
        if (axios.isAxiosError(error) && error.response) {
          errorMessage += ` 錯誤：${error.response.status} - ${error.response.data?.message || error.message}`
          console.error('Backend error details:', error.response.data)
        } else if (error instanceof Error) {
          errorMessage += ` ${error.message}`
        }
        message.error(errorMessage)
      }
    },
  })
}
const createColumns = ({
  onEdit,
  onDelete,
}: {
  onEdit: (rowData: Task) => void
  onDelete: (rowData: Task) => void
}): DataTableColumns<Task> => [
  { title: 'ID', key: 'id', width: 60, fixed: 'left', sorter: 'default', resizable: true },
  {
    title: '員工ID',
    key: 'emp',
    width: 80,
    sorter: 'default',
    resizable: true,
    render: (row) => row.emp ?? 'N/A',
  },
  { title: '員工名稱', key: 'empName', width: 100, resizable: true, ellipsis: { tooltip: true } },
  {
    title: '機台ID',
    key: 'machine',
    width: 150,
    ellipsis: { tooltip: { placement: 'top', width: 300, style: { maxWidth: '300px' } } },
    render(row) {
      return renderJsonArray(row.machine)
    },
    resizable: true,
  },
  {
    title: '機台名稱',
    key: 'machineName',
    width: 180,
    ellipsis: { tooltip: { placement: 'top', width: 300, style: { maxWidth: '300px' } } },
    render(row) {
      return renderJsonArray(row.machineName)
    },
    resizable: true,
  },
  {
    title: '開始時間',
    key: 'startTime',
    width: 180,
    render: (row) => formatDateTime(row.startTime),
    sorter: (a, b) => new Date(a.startTime || 0).getTime() - new Date(b.startTime || 0).getTime(),
    resizable: true,
  },
  {
    title: '結束時間',
    key: 'endTime',
    width: 180,
    render: (row) => formatDateTime(row.endTime),
    sorter: (a, b) => new Date(a.endTime || 0).getTime() - new Date(b.endTime || 0).getTime(),
    resizable: true,
  },
  { title: '技能標籤', key: 'tag', width: 100, resizable: true, ellipsis: { tooltip: true } },
  {
    title: '任務描述',
    key: 'description',
    width: 200,
    ellipsis: { tooltip: { placement: 'top', width: 300, style: { maxWidth: '300px' } } },
    resizable: true,
  },
  { title: '群組ID', key: 'group', width: 90, resizable: true },
  {
    title: '更新者ID',
    key: 'updaterId',
    width: 90,
    resizable: true,
    render: (row) => row.updaterId ?? 'N/A',
  },
  {
    title: '是否完成',
    key: 'isFinish',
    width: 100,
    align: 'center',
    render(row) {
      return h(
        NTag,
        { type: row.isFinish === 1 ? 'success' : 'warning', size: 'small', round: true },
        { default: () => (row.isFinish === 1 ? '已完成' : '未完成') },
      )
    },
    filterOptions: [
      { label: '已完成', value: 1 },
      { label: '未完成', value: 0 },
    ],
    filter(value, row) {
      return row.isFinish === value
    },
    resizable: true,
  },
  {
    title: '更新時間',
    key: 'updateTime',
    width: 180,
    render: (row) => formatDateTime(row.updateTime),
    sorter: (a, b) => new Date(a.updateTime || 0).getTime() - new Date(b.updateTime || 0).getTime(),
    resizable: true,
  },
  {
    title: '操作',
    key: 'actions',
    width: 130,
    align: 'center',
    fixed: 'right',
    render(row) {
      return h(NSpace, { justify: 'center' }, () => [
        h(
          NPopconfirm,
          { onPositiveClick: () => onDelete(row), positiveText: '確認刪除', negativeText: '取消' },
          {
            trigger: () =>
              h(NTooltip, null, {
                trigger: () =>
                  h(
                    NButton,
                    { strong: true, tertiary: true, circle: true, size: 'small', type: 'error' },
                    { icon: renderIcon(DeleteIcon) },
                  ),
                default: () => '刪除任務',
              }),
            default: () => `確定刪除任務 "${row.empName || '未命名'}" (ID: ${row.id})?`,
          },
        ),
      ])
    },
  },
]
const columns = ref(createColumns({ onEdit: handleEdit, onDelete: handleDelete }))

const bufferColumns = computed(
  (): DataTableColumns<Task> => [
    { title: '員工名稱', key: 'empName', width: 100, resizable: true, ellipsis: { tooltip: true } },
    {
      title: '機台名稱',
      key: 'machineName',
      width: 150, // Changed from machine to machineName for buffer display
      render(row) {
        return renderJsonArray(row.machineName)
      },
      resizable: true,
    },
    {
      title: '開始時間',
      key: 'startTime',
      width: 180,
      render: (row) => formatDateTime(row.startTime),
      resizable: true,
    },
    {
      title: '任務描述',
      key: 'description',
      width: 200,
      ellipsis: { tooltip: true },
      resizable: true,
    },
    { title: '技能標籤', key: 'tag', width: 100, resizable: true },
    {
      title: '操作',
      key: 'actions',
      width: 80,
      align: 'center',
      fixed: 'right',
      render(row) {
        return h(NSpace, { justify: 'center' }, () => [
        h(
          NPopconfirm,
          { onPositiveClick: () => handleRemoveFromBuffer(row), positiveText: '確認刪除', negativeText: '取消' },
          {
            trigger: () =>
              h(NTooltip, null, {
                trigger: () =>
                  h(
                    NButton,
                    { strong: true, tertiary: true, circle: true, size: 'small', type: 'error' },
                    { icon: renderIcon(DeleteIcon) },
                  ),
                default: () => '刪除任務',
              }),
            default: () => `確定刪除任務 "${row.empName || '未命名'}" (ID: ${row.id})?`,
          },
        ),
      ])
      },
    },
  ],
)

const pagination = ref<PaginationProps>({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  itemCount: 0,
  onChange: (page: number) => {
    if (pagination.value) pagination.value.page = page
  },
  onUpdatePageSize: (pageSize: number) => {
    if (pagination.value) {
      pagination.value.pageSize = pageSize
      pagination.value.page = 1
    }
  },
})

watch(
  data,
  (newData) => {
    if (pagination.value) {
      pagination.value.itemCount = newData.length
    }
  },
  { deep: true, immediate: true },
)

const tableMaxHeight = ref<number | undefined>(undefined)
const calculateMaxHeight = () => {
  const headerHeight = 60
  const contentPaddingY = 24 * 2
  const paginationHeight = 56
  const bufferTableApproxHeight =
    bufferedTasks.value.length > 0 ? Math.min(bufferedTasks.value.length, 5) * 40 + 80 : 0
  const extraSpacing = 20
  tableMaxHeight.value =
    window.innerHeight -
    headerHeight -
    contentPaddingY -
    paginationHeight -
    bufferTableApproxHeight -
    extraSpacing
  if (tableMaxHeight.value < 200) tableMaxHeight.value = 200
}

watch(bufferedTasks, calculateMaxHeight, { deep: true })
let MachineapiData: Machine[] = []
let EmpapiData: Employee[] = []

const handleRemoveFromBuffer = (row: Task) => {
  bufferedTasks.value = bufferedTasks.value.filter((item) => item !== row)
}

const handleSelectUpdate = (value: (string | number)[]) => {
  machineOptions.value = MachineapiData.filter((item) => {
    const set = new Set(JSON.parse(item.tags))
    return set.has(value)
  }).map((item) => {
    return {
      label: `${item.name} (${item.id})`,
      value: item.id,
    }
  })
  employeeOptions.value = EmpapiData.filter((item) => {
    const set = new Set(JSON.parse(item.tags))
    return set.has(value)
  }).map((item) => ({
    label: item.name,
    value: item.id,
  }))
}

let interval_id: number = 0
const loadData = async () => {
  try {
    // Simulate API delay and data fetching
    // await new Promise(resolve => setTimeout(resolve, 500));
    // const mockApiData: Task[] = [
    //   // ... sample data
    // ];
    // data.value = mockApiData;

    // Actual API call (if available)
    const TaskApiRes = await axios.get(`/api/task/search/${userdata.group}`)
    const TaskapiData: Task[] = TaskApiRes?.data?.data || []
    data.value = TaskapiData

    const MachineApiRes = await axios.get(`/api/machine/search/${userdata.group}`)
    MachineapiData = MachineApiRes?.data.data || []

    const EmpApiRes = await axios.get(`/api/emp/search/${userdata.group}`)
    EmpapiData = EmpApiRes?.data.data || []

    calculateMaxHeight()
  } catch (error) {
    console.error('Error loading data:', error)
    message.error('無法載入任務資料，請稍後再試。')
    data.value = []
  }
}

onMounted(() => {
  if (userdata.id && userdata.group) {
    loadData()
    interval_id = setInterval(loadData, 60000)
  } else {
    message.warning('無法獲取使用者資訊 (ID或群組)，任務列表可能無法正確載入或部分功能異常。')
    // Potentially try to fetch userdata here if not available or wait for it
  }
  window.addEventListener('resize', calculateMaxHeight)
  calculateMaxHeight()

  // Placeholder: fetch employee and machine options if they are dynamic
  // fetchEmployeeOptions();
  // fetchMachineOptions();
})

onUnmounted(() => {
  window.removeEventListener('resize', calculateMaxHeight)
  clearInterval(interval_id)
})
</script>

<style scoped>
/* Styles remain the same */
.task-container {
  height: calc(100vh - 60px);
  width: 100vw;
  overflow-y: auto;
}
</style>
