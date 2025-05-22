<script setup lang="ts">
import { ref, reactive } from 'vue';

const emit = defineEmits(['save', 'cancel']);

const formData = reactive({
  name: '',
  username: '',
  role: 0, // Default to '員工'
  // tags will be handled by rawTags and processed on save
});

const rawTags = ref(''); // For comma-separated input

const availableTags = ref<string[]>(['物性', '電性', '化性']);
const selectedTags = ref<string[]>([]); // Holds the currently checked tags

const handleSubmit = () => {
  const newEmployeeData = {
    ...formData,
    tags: JSON.stringify(selectedTags.value), // Store tags as a JSON string
    // Default values for other fields as per your example data structure
    group: "114514", // Example default
    jwt: "...",      // Placeholder
    updateTime: new Date().toISOString(),
    usable: 1 as (1 | 0)
  };
  emit('save', newEmployeeData);
  resetForm();
};

const handleCancel = () => {
  emit('cancel');
  resetForm();
};

const resetForm = () => {
  formData.name = '';
  formData.username = '';
  formData.role = 0;
  rawTags.value = '';
};
</script>

<template>
  <div class="add-employee-form">
    <h2>新增員工</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="name">姓名:</label>
        <input type="text" id="name" v-model="formData.name" required />
      </div>
      <div class="form-group">
        <label for="username">帳號</label>
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
            <input type="checkbox" :id="'tag-' + tagOption" :value="tagOption" v-model="selectedTags" />
            <label :for="'tag-' + tagOption">{{ tagOption }}</label>
          </div>
        </div>
        <small v-if="!selectedTags.length" class="tags-hint">可選多個標籤</small>
      </div>
      <div class="form-actions">
        <button type="submit" class="btn-save">儲存</button>
        <button type="button" @click="handleCancel" class="btn-cancel">取消</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.add-employee-form {
  background-color: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  width: 400px;
  /* Or max-width */
  margin: auto;
  /* Centering if used outside modal */
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

.form-group input[type="text"],
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  /* Important for width 100% */
}

.form-group input[type="text"]:focus,
.form-group select:focus {
  border-color: cornflowerblue;
  outline: none;
  box-shadow: 0 0 0 2px rgba(100, 149, 237, 0.2);
}

.form-group small {
  display: block;
  margin-top: 3px;
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

.checkbox-group {
  display: flex;
  flex-direction: column;
  /* Stack checkboxes vertically */
  gap: 8px;
  /* Space between checkbox items */
  padding: 5px 0;
  /* Some padding around the group */
  border: 1px solid #eee;
  /* Optional: light border */
  border-radius: 4px;
  padding: 10px;
}

.checkbox-item {
  display: flex;
  align-items: center;
}

.checkbox-item input[type="checkbox"] {
  margin-right: 8px;
  width: auto;
  /* Override default width:100% for inputs */
  accent-color: cornflowerblue;
  /* Color the checkbox itself */
}

.checkbox-item label {
  font-weight: normal;
  /* Override bold from .form-group label if needed */
  color: #333;
  cursor: pointer;
  margin-bottom: 0;
  /* Reset margin from general label style */
}
</style>
