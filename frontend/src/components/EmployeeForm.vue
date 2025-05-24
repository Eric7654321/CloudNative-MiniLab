<template>
  <div class="employee-form-container">
    <h2>{{ formTitle }}</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="name">姓名:</label>
        <input type="text" id="name" v-model="formData.name" required />
      </div>
      <div class="form-group">
        <label for="username">帳號:</label>
        <!-- Assuming "帳號" means username -->
        <input type="text" id="username" v-model="formData.username" required />
      </div>
      <div class="form-group">
        <label for="role">選擇身份:</label>
        <select id="role" v-model.number="formData.role">
          <option :value="0">員工</option>
          <option :value="1">管理員</option>
        </select>
      </div>

      <div class="form-group">
        <label>選擇 Tags:</label>
        <div class="checkbox-group">
          <div v-for="tagOption in availableTags" :key="tagOption" class="checkbox-item">
            <input
              type="checkbox"
              :id="'tag-' + tagOption + '-' + (employeeToEdit ? employeeToEdit.id : 'new')"
              :value="tagOption"
              v-model="selectedTags"
            />
            <label :for="'tag-' + tagOption + '-' + (employeeToEdit ? employeeToEdit.id : 'new')">{{
              tagOption
            }}</label>
          </div>
        </div>
        <small v-if="!selectedTags.length && availableTags.length" class="tags-hint"
          >可選多個標籤</small
        >
        <small v-if="!availableTags.length" class="tags-hint">尚無可用標籤</small>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-save">儲存</button>
        <button type="button" @click="handleCancel" class="btn-cancel">取消</button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed, onMounted } from 'vue'

// Define the Employee structure (can be imported if shared)
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

// Data for the form fields
interface FormDataState {
  // Renamed to avoid conflict with global FormData
  name: string
  username: string
  role: 0 | 1
  id?: number // Will be present if editing
}

interface Props {
  employeeToEdit?: Employee | null // Employee data if in edit mode
}

// Emitted payload will include id if editing
// Adjusting to ensure all Employee fields (except updateTime) are potentially part of the payload
// as Manager.vue will spread this over existing or create new.
interface SavePayload extends Omit<Employee, 'updateTime'> {
  id?: number // id is optional for new, but will be present if editing
}

interface Emits {
  (e: 'save', payload: SavePayload): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formData = reactive<FormDataState>({
  name: '',
  username: '',
  role: 0,
})

const availableTags = ref<string[]>(['物性', '電性', '化性']) // Predefined tags
const selectedTags = ref<string[]>([])

const isEditMode = computed(() => !!props.employeeToEdit)
const formTitle = computed(() => (isEditMode.value ? '修改員工資料' : '新增員工'))

const populateFormForEdit = () => {
  if (props.employeeToEdit) {
    formData.id = props.employeeToEdit.id
    formData.name = props.employeeToEdit.name
    formData.username = props.employeeToEdit.username
    formData.role = props.employeeToEdit.role
    try {
      const parsed = JSON.parse(props.employeeToEdit.tags || '[]')
      selectedTags.value = Array.isArray(parsed)
        ? parsed.filter((tag) => typeof tag === 'string')
        : []
    } catch (e) {
      console.error('Error parsing tags for editing:', e)
      selectedTags.value = []
    }
  } else {
    // Reset for add mode
    formData.id = undefined
    formData.name = ''
    formData.username = ''
    formData.role = 0
    selectedTags.value = []
  }
}

onMounted(() => {
  populateFormForEdit()
})

watch(
  () => props.employeeToEdit,
  () => {
    populateFormForEdit()
  },
  { deep: true },
) // Use deep watch if employeeToEdit itself might change structure or for nested objects

const handleSubmit = (): void => {
  // Construct the base payload from form data
  const basePayload: Omit<SavePayload, 'group' | 'jwt' | 'usable' | 'tags'> & {
    id?: number
    tags: string
  } = {
    id: formData.id, // Will be undefined for new, populated for edit
    name: formData.name,
    username: formData.username,
    role: formData.role,
    tags: JSON.stringify(selectedTags.value),
  }

  let finalPayload: SavePayload

  if (isEditMode.value && props.employeeToEdit) {
    // For editing, merge with existing non-editable fields from props.employeeToEdit
    finalPayload = {
      ...props.employeeToEdit, // Start with all fields from the original employee
      ...basePayload, // Override with form data (name, username, role, tags, id)
    }
  } else {
    // For adding, we only send what the form collects, Manager.vue adds defaults
    // But SavePayload expects all Employee fields (minus updateTime). So we add placeholders.
    finalPayload = {
      ...basePayload,
      group: 'NEEDS_DEFAULT', // Placeholder, Manager.vue should provide actual default
      jwt: 'NEEDS_DEFAULT', // Placeholder
      usable: 1, // Default, or Manager.vue provides
      // id is already in basePayload (or undefined)
    } as SavePayload // Type assertion needed as we're building it partially
  }

  emit('save', finalPayload)
}

const handleCancel = (): void => {
  emit('cancel')
}
</script>

<style scoped>
.employee-form-container {
  background-color: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  width: 400px;
  max-width: 90vw; /* Ensure it's responsive on smaller screens */
  margin: auto;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
}

.form-group input[type='text'],
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}
.form-group input[type='text']:focus,
.form-group select:focus {
  border-color: cornflowerblue;
  outline: none;
  box-shadow: 0 0 0 2px rgba(100, 149, 237, 0.2);
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 10px;
}

.checkbox-item {
  display: flex;
  align-items: center;
}

.checkbox-item input[type='checkbox'] {
  margin-right: 8px;
  width: auto;
  accent-color: cornflowerblue;
}

.checkbox-item label {
  font-weight: normal;
  color: #333;
  cursor: pointer;
  margin-bottom: 0;
}

.form-group small.tags-hint {
  display: block;
  margin-top: 5px;
  font-size: 0.8em;
  color: #777;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.form-actions button {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

.btn-save {
  background-color: mediumseagreen;
  color: white;
}
.btn-save:hover {
  background-color: #31a571;
}

.btn-cancel {
  background-color: #f44336;
  color: white;
}
.btn-cancel:hover {
  background-color: #d32f2f;
}
</style>
