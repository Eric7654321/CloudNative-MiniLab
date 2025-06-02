<template>
  <div class="machine-card">
    <div class="machine-attribute">
      <strong class="name-label">機器:</strong>
      <span class="name-value">{{ machine.name }}</span>
    </div>
    <div class="machine-attribute">
      <strong class="role-label">狀態:</strong>
      <span class="role-value">{{ displayRole }}</span>
    </div>

    <div class="machine-attribute tags-section">
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
      <input
        type="text"
        v-model="newTag"
        @keyup.enter="requestAddTag"
        placeholder="新增標籤到此機器"
        class="add-tag-input"
      />
      <button @click="requestAddTag" class="add-tag-btn" :disabled="!newTag.trim()">新增</button>
    </div>

    <div class="machine-attribute subtle-info">
      <span>ID: {{ machine.id }}</span>
      <span>機器正式名稱: {{ machine.machineName }}</span>
    </div>
    <div class="machine-attribute subtle-info">
      <span>更新時間: {{ new Date(machine.updateTime).toLocaleString() }}</span>
    </div>
    <div class="machine-actions">
      <button @click="requestEditMachine" class="edit-machine-btn">修改資料</button>
      <button @click="requestDeleteMachine" class="delete-machine-btn">刪除機器</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Machine {
  id: number // 機器 ID
  name: string // 顯示名稱
  machineName: string // 機器內部名稱
  usable: number // 是否啟用
  group: string // 所屬群組
  updateTime: string // 更新時間 (ISO string)
  tags: string // JSON 格式標籤陣列
}

interface Props {
  machine: Machine
}

interface Emits {
  (e: 'add-tag', payload: { machineId: number; tag: string }): void
  (e: 'remove-tag', payload: { machineId: number; tag: string }): void
  (e: 'delete-machine', machineId: number): void
  (e: 'edit-machine', machineId: number): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const newTag = ref<string>('')

const RoleToString = (role: number): string => {
  if (role === 0) return '未啟用'
  if (role === 1) return '啟用中'
  return '未知'
}

const displayRole = computed<string>(() => RoleToString(props.machine.usable))

const parsedTags = computed<string[]>(() => {
  try {
    if (props.machine.tags && typeof props.machine.tags === 'string') {
      const tagsArray = JSON.parse(props.machine.tags)
      return Array.isArray(tagsArray) ? tagsArray.filter((tag) => typeof tag === 'string') : []
    }
    return []
  } catch (e) {
    console.error(
      `Error parsing tags for machine ${props.machine.id}:`,
      e,
      'Tags string:',
      props.machine.tags,
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
    emit('add-tag', { machineId: props.machine.id, tag: tagToAdd })
    newTag.value = ''
  }
}

const requestRemoveTag = (tagToRemove: string): void => {
  emit('remove-tag', { machineId: props.machine.id, tag: tagToRemove })
}

const requestDeleteMachine = (): void => {
  emit('delete-machine', props.machine.id)
}

const requestEditMachine = (): void => {
  emit('edit-machine', props.machine.id)
}
</script>

<style scoped>
.machine-card {
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  background-color: #fff;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.machine-attribute {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.machine-attribute.tags-section {
  align-items: flex-start;
}

.machine-attribute strong {
  font-weight: bold;
}

.name-label {
  color: #333;
}
.name-value {
  color: cornflowerblue;
  font-weight: bold;
  font-size: 1.1em;
}

.role-label {
  color: #333;
}
.role-value {
  color: mediumseagreen;
  font-style: italic;
}

.tags-label {
  color: coral;
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
  background-color: lightslategray;
  color: white;
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
  color: white;
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
  color: #666;
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 5px;
}
.subtle-info span {
  margin-right: 10px;
}

.machine-actions {
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.edit-machine-btn {
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
.edit-machine-btn:hover {
  background-color: #e0a800;
}

.delete-machine-btn {
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
.delete-machine-btn:hover {
  background-color: #c82333;
}
</style>
