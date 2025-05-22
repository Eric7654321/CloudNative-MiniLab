<template>
  <div class="employee-card">
    <div class="employee-attribute">
      <strong class="name-label">姓名:</strong>
      <span class="name-value">{{ employee.name }}</span>
    </div>
    <div class="employee-attribute">
      <strong class="role-label">身份:</strong>
      <span class="role-value">{{ displayRole }}</span>
    </div>
    <div class="employee-attribute" v-if="parsedTags.length">
      <strong class="tags-label">Tags:</strong>
      <div class="tags-container">
        <span v-for="tag in parsedTags" :key="tag" class="tag-item">{{ tag }}</span>
      </div>
    </div>
    <div class="employee-attribute subtle-info">
      <span>ID: {{ employee.id }}</span>
      <span>Username: {{ employee.username }}</span>
    </div>
    <div class="employee-attribute subtle-info">
      <span>更新時間: {{ new Date(employee.updateTime).toLocaleString() }}</span>
    </div>
    <div class="employee-actions">
      <button @click="requestDeleteEmployee" class="delete-employee-btn">刪除員工</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

// Assuming Employee interface is available (e.g., imported or defined above)
interface Employee {
  group: string;
  id: number;
  jwt: string;
  name: string;
  role: 0 | 1;
  tags: string; // JSON string of tags
  updateTime: string;
  usable: 1 | 0;
  username: string;
}

interface Props {
  employee: Employee;
}

interface Emits {
  (e: 'add-tag', payload: { employeeId: number; tag: string }): void;
  (e: 'remove-tag', payload: { employeeId: number; tag: string }): void;
  (e: 'delete-employee', employeeId: number): void; // ADDED: Emit for delete
}

const props = defineProps < Props > ();
const emit = defineEmits < Emits > ();

const newTag = ref < string > ('');
// const showAddTagInput = ref<boolean>(false); // Kept for consistency if used

const RoleToString = (role: 0 | 1): string => {
  if (role === 0) return '員工';
  if (role === 1) return '管理員';
  return '未知';
};

const displayRole = computed < string > (() => RoleToString(props.employee.role));

const parsedTags = computed < string[] > (() => {
  try {
    if (props.employee.tags && typeof props.employee.tags === 'string') {
      const tagsArray = JSON.parse(props.employee.tags);
      return Array.isArray(tagsArray) ? tagsArray.filter(tag => typeof tag === 'string') : [];
    }
    return [];
  } catch (e) {
    console.error(
      `Error parsing tags for employee ${props.employee.id}:`,
      e,
      "Tags string:",
      props.employee.tags
    );
    return [];
  }
});

// ADDED: Method to request deleting the employee
const requestDeleteEmployee = (): void => {
  // Optional: Add a confirmation dialog here
  // if (confirm(`確定要刪除員工 "${props.employee.name}" 嗎？`)) {
  //   emit('delete-employee', props.employee.id);
  // }
  emit('delete-employee', props.employee.id); // Emitting directly for now
};
</script>



<style scoped>
.employee-card {
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

.employee-attribute {
  display: flex;
  align-items: baseline;
  /* Align label and value nicely */
  gap: 8px;
  /* Space between label and value */
}

.employee-attribute strong {
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
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.tag-item {
  background-color: lightslategray;
  color: white;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 0.9em;
}

.subtle-info {
  font-size: 0.8em;
  color: #666;
  display: flex;
  flex-direction: column;
  /* Stack ID and Username if they wrap */
  gap: 2px;
}

.subtle-info span {
  margin-right: 10px;
}

.employee-actions {
  margin-top: 15px;
  /* Space above the action buttons */
  padding-top: 10px;
  /* Optional: add a separator line */
  border-top: 1px solid #eee;
  /* Optional: separator line */
  display: flex;
  justify-content: flex-end;
  /* Align button to the right */
}

.delete-employee-btn {
  padding: 8px 15px;
  background-color: #dc3545;
  /* Bootstrap danger color */
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
  /* Darker shade for hover */
}
</style>
