<template>
  <n-card :title="formTitle">
    <n-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-placement="left"
      label-width="auto"
      require-mark-placement="right-hanging"
      style="max-width: 600px; margin: auto"
    >
      <!--<h2>{{ formTitle }}</h2>-->
      <n-form-item label="機器" path="name">
        <n-input v-model:value="formData.name" placeholder="請輸入機器名稱" />
      </n-form-item>
      <n-form-item label="機器正式名稱" path="machinename">
        <n-input v-model:value="formData.machinename" placeholder="請輸入機器正式名稱" />
      </n-form-item>
      <n-form-item label="選擇狀態" path="role">
        <n-select v-model:value="formData.role" placeholder="請選擇狀態" :options="statusOptions" />
      </n-form-item>

      <n-form-item label="選擇 Tags" path="selectedTags">
        <n-checkbox-group v-model:value="selectedTags">
          <n-space item-style="display: flex;">
            <n-checkbox
              v-for="tagOption in availableTags"
              :key="tagOption"
              :value="tagOption"
              :label="tagOption"
            />
          </n-space>
        </n-checkbox-group>
        <div
          v-if="!selectedTags.length && availableTags.length"
          style="font-size: 12px; color: #aaa; margin-top: 4px"
        >
          可選多個標籤
        </div>
        <div v-if="!availableTags.length" style="font-size: 12px; color: #aaa; margin-top: 4px">
          尚無可用標籤
        </div>
      </n-form-item>

      <n-form-item>
        <n-space>
          <n-button type="primary" @click="handleSubmit">儲存</n-button>
          <n-button @click="handleCancel">取消</n-button>
        </n-space>
      </n-form-item>
    </n-form>
  </n-card>
</template>

<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { ref, reactive, watch, computed, onMounted } from 'vue'

interface Machine {
  id: number // 機器 ID
  name: string // 顯示名稱
  machineName: string // 機器內部名稱
  usable: number // 是否啟用
  group: string // 所屬群組
  updateTime: string // 更新時間 (ISO string)
  tags: string // JSON 格式標籤陣列
}

// Data for the form fields
interface FormDataState {
  // Renamed to avoid conflict with global FormData
  name: string
  machinename: string
  role: number
  id: number // Will be present if editing
}

interface Props {
  machineToEdit?: Machine | null // Employee data if in edit mode
}

// Emitted payload will include id if editing
// Adjusting to ensure all Employee fields (except updateTime) are potentially part of the payload
// as Manager.vue will spread this over existing or create new.
interface SavePayload extends Omit<Machine, 'updateTime'> {
  id: number // id is optional for new, but will be present if editing
}

interface Emits {
  (e: 'save', payload: SavePayload): void
  (e: 'cancel'): void
}
const statusOptions = ref([
  { label: '未啟用', value: 0 },
  { label: '啟用中', value: 1 },
])

// Rules for validation (example)
const rules: FormRules = {
  name: {
    required: true,
    trigger: ['blur', 'input'],
    message: '請輸入機器名稱',
  },
  machinename: {
    required: true,
    trigger: ['blur', 'input'],
    message: '請輸入機器正式名稱',
  },
  role: {
    type: 'number', // Important for n-select with number values
    required: true,
    trigger: ['blur', 'change'],
    message: '請選擇狀態',
  },
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formData = reactive<FormDataState>({
  name: '',
  machinename: '',
  role: 0,
  id: 0,
})

const availableTags = ref<string[]>(['物性', '電性', '化性']) // Predefined tags
const selectedTags = ref<string[]>([])

const isEditMode = computed(() => !!props.machineToEdit)
const formTitle = computed(() => (isEditMode.value ? '修改機器資料' : '新增機器'))

const populateFormForEdit = () => {
  if (props.machineToEdit) {
    formData.id = props.machineToEdit.id
    formData.name = props.machineToEdit.name
    formData.machinename = props.machineToEdit.machineName
    formData.role = props.machineToEdit.usable
    try {
      const parsed = JSON.parse(props.machineToEdit.tags || '[]')
      selectedTags.value = Array.isArray(parsed)
        ? parsed.filter((tag) => typeof tag === 'string')
        : []
    } catch (e) {
      console.error('Error parsing tags for editing:', e)
      selectedTags.value = []
    }
  } else {
    // Reset for add mode
    formData.id = 0
    formData.name = ''
    formData.machinename = ''
    formData.role = 0
    selectedTags.value = []
  }
}

onMounted(() => {
  populateFormForEdit()
})

watch(
  () => props.machineToEdit,
  () => {
    populateFormForEdit()
  },
  { deep: true },
) // Use deep watch if machineToEdit itself might change structure or for nested objects

const formRef = ref<FormInst | null>(null)

const handleSubmit = (e: MouseEvent): void => {
  e.preventDefault()
  formRef?.value?.validate((errors) => {
    if (errors) {
    } else {
      // Construct the base payload from form data
      const basePayload: Omit<SavePayload, 'group' | 'tags'> & {
        id: number
        tags: string
      } = {
        id: formData.id, // Will be undefined for new, populated for edit
        name: formData.name,
        machineName: formData.machinename,
        usable: formData.role,
        tags: JSON.stringify(selectedTags.value),
      }

      let finalPayload: SavePayload

      if (isEditMode.value && props.machineToEdit) {
        // For editing, merge with existing non-editable fields from props.machineToEdit
        finalPayload = {
          ...props.machineToEdit, // Start with all fields from the original machine
          ...basePayload, // Override with form data (name, machinename, role, tags, id)
        }
      } else {
        // For adding, we only send what the form collects, Manager.vue adds defaults
        // But SavePayload expects all Employee fields (minus updateTime). So we add placeholders.
        finalPayload = {
          ...basePayload,
          group: 'NEEDS_DEFAULT', // Placeholder, Manager.vue should provide actual default
        } as SavePayload // Type assertion needed as we're building it partially
      }

      emit('save', finalPayload)
    }
  })
}

const handleCancel = (): void => {
  emit('cancel')
}
</script>
