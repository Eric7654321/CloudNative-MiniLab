<template>
  <n-el tag="div" class="employee-card">
    <div class="employee-attribute">
      <strong class="name-label">姓名:</strong>
      <span class="name-value">{{ employee.name }}</span>
    </div>
    <div class="employee-attribute">
      <strong class="role-label">身份:</strong>
      <span class="role-value">{{ displayRole }}</span>
    </div>

    <div class="employee-attribute tags-section">
      <strong class="tags-label">Tags:</strong>
      <div class="tags-container">
        <span v-for="tag in parsedTags" :key="tag" class="tag-item">
          {{ tag }}
          <button @click="requestRemoveTag(tag)" class="remove-tag-btn" title="移除標籤">×</button>
        </span>
        <span v-if="!parsedTags.length" class="no-tags">尚無標籤</span>
      </div>
    </div>

    <div class="add-tag-section">
      <n-input
        type="text"
        v-model:value="newTag"
        @keyup.enter="requestAddTag"
        placeholder="新增標籤到此員工"
      />
      <n-button @click="requestAddTag" :disabled="!newTag.trim()">新增</n-button>
    </div>

    <div class="employee-attribute subtle-info">
      <span>ID: {{ employee.id }}</span>
      <span>Username: {{ employee.username }}</span>
    </div>
    <div class="employee-attribute subtle-info">
      <span>更新時間: {{ new Date(employee.updateTime).toLocaleString() }}</span>
    </div>
    <div class="employee-actions">
      <n-button @click="requestEditEmployee" type="warning">修改資料</n-button>
      <n-button @click="requestDeleteEmployee" type="error">刪除員工</n-button>
    </div>
  </n-el>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Employee {
  group: string
  id: number
  jwt: string
  name: string
  role: 0 | 1
  tags: string // JSON string of tags
  updateTime: string
  usable: 1 | 0
  username: string
}

interface Props {
  employee: Employee
}

interface Emits {
  (e: 'add-tag', payload: { employeeId: number; tag: string }): void
  (e: 'remove-tag', payload: { employeeId: number; tag: string }): void
  (e: 'delete-employee', employeeId: number): void
  (e: 'edit-employee', employeeId: number): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const newTag = ref<string>('')

const RoleToString = (role: 0 | 1): string => {
  if (role === 0) return '員工'
  if (role === 1) return '管理員'
  return '未知'
}

const displayRole = computed<string>(() => RoleToString(props.employee.role))

const parsedTags = computed<string[]>(() => {
  try {
    if (props.employee.tags && typeof props.employee.tags === 'string') {
      const tagsArray = JSON.parse(props.employee.tags)
      return Array.isArray(tagsArray) ? tagsArray.filter((tag) => typeof tag === 'string') : []
    }
    return []
  } catch (e) {
    console.error(
      `Error parsing tags for employee ${props.employee.id}:`,
      e,
      'Tags string:',
      props.employee.tags,
    )
    return []
  }
})

const requestAddTag = (): void => {
  const tagToAdd = newTag.value.trim()
  if (tagToAdd) {
    if (parsedTags.value.includes(tagToAdd)) {
      newTag.value = ''
      return
    }
    emit('add-tag', { employeeId: props.employee.id, tag: tagToAdd })
    newTag.value = ''
  }
}

const requestRemoveTag = (tagToRemove: string): void => {
  emit('remove-tag', { employeeId: props.employee.id, tag: tagToRemove })
}

const requestDeleteEmployee = (): void => {
  emit('delete-employee', props.employee.id)
}

const requestEditEmployee = (): void => {
  emit('edit-employee', props.employee.id)
}
</script>

<style scoped>
.employee-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: var(--box-shadow-1);
  background-color: var(--card-color);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.employee-attribute {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.employee-attribute.tags-section {
  align-items: flex-start;
}

.employee-attribute strong {
  font-weight: bold;
}

.name-label {
  color: var(--text-color-1);
}
.name-value {
  color: var(--info-color-pressed);
  font-weight: bold;
  font-size: 1.1em;
}

.role-label {
  color: var(--text-color-1);
}
.role-value {
  color: var(--primary-color);
  font-style: italic;
}

.tags-label {
  color: var(--warning-color-suppl);
  white-space: nowrap;
  margin-top: 3px;
}
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 8px;
  flex-grow: 1;
}
.tag-item {
  background-color: var(--tag-color);
  color: var(--text-color);
  padding: 4px 8px 4px 10px;
  border-radius: 12px;
  font-size: 0.9em;
  display: inline-flex;
  align-items: center;
  transition: background-color 0.2s ease;
}
.tag-item:hover {
  background-color: #616b75;
}

.no-tags {
  font-style: italic;
  color: #888;
  font-size: 0.9em;
  padding: 4px 0;
}

.remove-tag-btn {
  background: none;
  border: none;
  color: var(--text-color);
  cursor: pointer;
  margin-left: 6px;
  font-size: 1.2em;
  line-height: 1;
  padding: 0 3px;
  opacity: 0.7;
  transition: opacity 0.2s ease;
}
.remove-tag-btn:hover {
  opacity: 1;
  color: #ffdddd;
}

.add-tag-section {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  align-items: center;
}
.add-tag-input {
  flex-grow: 1;
  padding: 7px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9em;
}
.add-tag-input:focus {
  outline: none;
  border-color: cornflowerblue;
  box-shadow: 0 0 0 2px rgba(100, 149, 237, 0.2);
}
.add-tag-btn {
  padding: 7px 12px;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9em;
  transition: background-color 0.2s ease;
}
.add-tag-btn:hover:not(:disabled) {
  background-color: #545b62;
}
.add-tag-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  opacity: 0.7;
}

.subtle-info {
  font-size: 0.8em;
  color: var(--text-color-2);
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 5px;
}
.subtle-info span {
  margin-right: 10px;
}

.employee-actions {
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.edit-employee-btn {
  padding: 8px 15px;
  background-color: #ffc107;
  color: #212529;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9em;
  font-weight: bold;
  transition: background-color 0.2s ease;
}
.edit-employee-btn:hover {
  background-color: #e0a800;
}

.delete-employee-btn {
  padding: 8px 15px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9em;
  font-weight: bold;
  transition: background-color 0.2s ease;
}
.delete-employee-btn:hover {
  background-color: #c82333;
}
</style>
