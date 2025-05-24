<template>
  <div class="manager-page-wrapper">
    <div class="manager-content-area" :class="{ 'modal-active-background': showEmployeeFormModal }">
      <header class="manager-header">
        <h1>工人管理</h1>
        <button @click="openAddEmployeeForm" class="add-employee-btn">新增員工</button>
      </header>

      <div class="employee-list">
        <EmployeeCard
          v-for="employee in employees"
          :key="employee.id"
          :employee="employee"
          @add-tag="handleAddTag"
          @remove-tag="handleRemoveTag"
          @delete-employee="handleDeleteEmployee"
          @edit-employee="openEditEmployeeForm"
        />
        <p v-if="!employees.length" class="no-employees-message">目前沒有員工資料。</p>
      </div>
    </div>

    <!-- Modal for EmployeeForm (Add/Edit) -->
    <div v-if="showEmployeeFormModal" class="modal-overlay" @click.self="closeEmployeeFormModal">
      <div class="modal-content-wrapper">
        <EmployeeForm
          :key="employeeToEdit ? `edit-${employeeToEdit.id}` : 'new-employee'"
          :employeeToEdit="employeeToEdit"
          @save="handleSaveEmployee"
          @cancel="closeEmployeeFormModal"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import EmployeeCard from '@/components/EmployeeCard.vue'
import EmployeeForm from '@/components/EmployeeForm.vue' // Changed from AddEmployee
import { useUserData } from '@/stores/UserData'
import axios from 'axios'

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

// This is the structure of data coming from EmployeeForm's 'save' emit
// It might have an ID if editing, or not if adding.
// Other fields like group, jwt, usable are expected to be present or defaulted.
interface EmployeeFormSavePayload extends Omit<Employee, 'updateTime'> {
  id: number // id is optional if it's a new employee
}

interface TagEventPayload {
  employeeId: number
  tag: string
}

const employees = ref<Employee[]>([])

const userdata = useUserData()
const get_employee_list = async () => {
  const res = await axios.get(`/api/emp/search/${userdata.group}`)
  employees.value = res?.data.data.filter((emp: any) => emp.id !== userdata.id)
  console.log(employees.value.filter((emp: any) => emp.id !== userdata.id))
}

const showEmployeeFormModal = ref<boolean>(false)
const employeeToEdit = ref<Employee | null>(null)

const openAddEmployeeForm = (): void => {
  employeeToEdit.value = null
  showEmployeeFormModal.value = true
}

const openEditEmployeeForm = (employeeId: number): void => {
  const foundEmployee = employees.value.find((emp) => emp.id === employeeId)
  if (foundEmployee) {
    employeeToEdit.value = { ...foundEmployee } // Pass a copy
    showEmployeeFormModal.value = true
  } else {
    console.error(`Employee with ID ${employeeId} not found for editing.`)
  }
}

const closeEmployeeFormModal = (): void => {
  showEmployeeFormModal.value = false
  employeeToEdit.value = null
}

const handleSaveEmployee = async (formData: EmployeeFormSavePayload) => {
  if (formData.id !== undefined) {
    // If ID exists (even 0), it's an update
    const index = employees.value.findIndex((emp) => emp.id === formData.id)
    if (index !== -1) {
      const emp = employees.value[index]
      try {
        await axios.put(
          '/api/emp/tag/update',
          { empId: emp.id, tags: formData.tags },
          { headers: { 'Content-Type': 'application/json' } },
        )
        await get_employee_list()
        console.log('Employee updated:', employees.value[index])
      } catch (error) {
        console.log(error)
      }

      // Placeholder for API call: await api.updateEmployee(employees.value[index]);
    } else {
      console.error('Failed to find employee for update with ID:', formData.id)
    }
  } else {
    // No ID, so it's a new employee
    const newEmployee = {
      // Provide defaults for fields not in EmployeeForm or use what EmployeeForm sent
      username: formData.username,
      name: formData.name,
      password: '12345678',
      group: userdata.group, // Default if not provided by form
      usable: formData.usable !== undefined ? formData.usable : 1, // Default if not provided
      role: formData.role,
      tags: formData.tags, // This is already a JSON string from EmployeeForm
    }
    try {
      await axios.post('/api/emp/insert', newEmployee)
      await get_employee_list()

      console.log('Employee added:', newEmployee)
    } catch (error) {
      console.log(error)
    }
    // Placeholder for API call: await api.addEmployee(newEmployee);
  }
  closeEmployeeFormModal()
}

const _updateEmployeeTags = (
  employeeId: number,
  updateFn: (currentTags: string[]) => string[],
): void => {
  const employee = employees.value.find((emp) => emp.id === employeeId)
  if (employee) {
    let currentTags: string[] = []
    try {
      if (typeof employee.tags === 'string' && employee.tags.trim() !== '') {
        const parsed = JSON.parse(employee.tags)
        if (Array.isArray(parsed)) {
          currentTags = parsed.filter((tag) => typeof tag === 'string')
        }
      }
    } catch (e) {
      /* ... error handling ... */
    }

    const updatedTags = updateFn([...currentTags])
    if (
      JSON.stringify(updatedTags) !== JSON.stringify(currentTags) ||
      employee.tags !== JSON.stringify(currentTags)
    ) {
      employee.tags = JSON.stringify(updatedTags)
      employee.updateTime = new Date().toISOString()
      // Placeholder: console.log(`Updating tags for employee ${employeeId} via API...`);
    }
  }
}

const handleAddTag = async ({ employeeId, tag }: TagEventPayload) => {
  _updateEmployeeTags(employeeId, (tagsArray) => {
    if (!tagsArray.includes(tag)) {
      return [...tagsArray, tag]
    }
    return tagsArray
  })
  const emp = employees.value.find((emp) => emp.id === employeeId)

  try {
    await axios.put(
      '/api/emp/tag/update',
      { empId: employeeId, tags: emp?.tags },
      { headers: { 'Content-Type': 'application/json' } },
    )
    await get_employee_list()
    console.log('Employee updated:', emp)
  } catch (error) {
    console.log(error)
  }
}

const handleRemoveTag = async ({ employeeId, tag }: TagEventPayload) => {
  _updateEmployeeTags(employeeId, (tagsArray) => {
    const index = tagsArray.indexOf(tag)
    if (index > -1) {
      const newTagsArray = [...tagsArray]
      newTagsArray.splice(index, 1)
      return newTagsArray
    }
    return tagsArray
  })
  const emp = employees.value.find((emp) => emp.id === employeeId)

  try {
    await axios.put(
      '/api/emp/tag/update',
      { empId: employeeId, tags: emp?.tags },
      { headers: { 'Content-Type': 'application/json' } },
    )
    await get_employee_list()
    console.log('Employee updated:', emp)
  } catch (error) {
    console.log(error)
  }
}

const handleDeleteEmployee = async (employeeIdToDelete: number) => {
  const employeeToDelete = employees.value.find((emp) => emp.id === employeeIdToDelete)
  if (employeeToDelete) {
    if (confirm(`您確定要刪除員工 "${employeeToDelete.name}" (ID: ${employeeIdToDelete}) 嗎？`)) {
      employees.value = employees.value.filter((employee) => employee.id !== employeeIdToDelete)
      try {
        await axios.delete('/api/emp/delete', {
          data: { id: employeeIdToDelete },
          headers: {
            'Content-Type': 'application/json',
          },
        })
        await get_employee_list()

        console.log(`Employee with ID ${employeeIdToDelete} deleted locally.`)
      } catch (e) {}

      // Placeholder for API call: await api.deleteEmployee(employeeIdToDelete);
    }
  }
}

const handleEscKey = (event: KeyboardEvent): void => {
  if (event.key === 'Escape' && showEmployeeFormModal.value) {
    closeEmployeeFormModal()
  }
}

onMounted(() => {
  get_employee_list()
  window.addEventListener('keydown', handleEscKey)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleEscKey)
})
</script>

<style scoped>
.manager-page-wrapper {
  position: relative;
}

.manager-content-area {
  padding: 20px;
  font-family: 'Arial', sans-serif;
  transition: filter 0.3s ease-in-out;
  background-color: #f4f7f6;
  min-height: 100vh;
  /* Ensure it takes full viewport height if content is short */
}

.manager-content-area.modal-active-background {
  filter: blur(3px) brightness(0.7);
}

.manager-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #eee;
}

.manager-header h1 {
  margin: 0;
  color: #333;
}

.add-employee-btn {
  padding: 10px 15px;
  background-color: cornflowerblue;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1em;
}

.add-employee-btn:hover {
  background-color: #5682d8;
}

.employee-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  /* Slightly wider for new buttons */
  gap: 20px;
}

.no-employees-message {
  text-align: center;
  color: #777;
  margin-top: 20px;
  font-style: italic;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  overflow-y: auto;
  /* Allow modal content to scroll if it's too tall */
  padding: 20px;
  /* Add padding for smaller screens so modal doesn't touch edges */
  box-sizing: border-box;
}

.modal-content-wrapper {
  animation: fadeInModal 0.3s ease-out;
  /* Max height can be set here if EmployeeForm doesn't control its own scrolling */
  /* max-height: 90vh; */
  /* overflow-y: auto; */
}

@keyframes fadeInModal {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
