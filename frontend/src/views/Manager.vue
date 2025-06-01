<template>
  <div class="manager-page-wrapper">
    <div class="manager-content-area" :class="{ 'modal-active-background': showEmployeeFormModal || showMachineFormModal }">
      <header class="manager-header">
        <h1>工人管理</h1>
        <button @click="openAddEmployeeForm" class="add-btn">新增員工</button>
      </header>

      <div class="employee-list">
        <EmployeeCard
          v-for="employee in employees"
          :key="employee.id"
          :employee="employee"
          @add-tag="handleAddEmployeeTag"
          @remove-tag="handleRemoveEmployeeTag"
          @delete-employee="handleDeleteEmployee"
          @edit-employee="openEditEmployeeForm"
        />
        <p v-if="!employees.length" class="no-message">目前沒有員工資料。</p>
      </div>
    </div>
    <div class="manager-content-area" :class="{ 'modal-active-background': showEmployeeFormModal || showMachineFormModal }">
      <header class="manager-header">
        <h1>機器管理</h1>
        <button @click="openAddMachineForm" class="add-btn">新增機器</button>
      </header>
      <div class="machine-list">  
        <MachineCard
          v-for="machine in machines"
          :key="machine.id"
          :machine="machine"
          @add-tag="handleAddMachineTag"
          @remove-tag="handleRemoveMachineTag"
          @delete-machine="handleDeleteMachine"
          @edit-machine="openEditMachineForm"
        />
        <p v-if="!machines.length" class="no-message">目前沒有機器資料。</p>
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
    <div v-if="showMachineFormModal" class="modal-overlay" @click.self="closeMachineFormModal">
      <div class="modal-content-wrapper">
        <MachineForm
          :key="machineToEdit ? `edit-${machineToEdit.id}` : 'new-machine'"
          :machineToEdit="machineToEdit"
          @save="handleSaveMachine"
          @cancel="closeMachineFormModal"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import EmployeeCard from '@/components/EmployeeCard.vue'
import EmployeeForm from '@/components/EmployeeForm.vue' // Changed from AddEmployee
import MachineCard from '@/components/MachineCard.vue' // Placeholder for future machine card component
import MachineForm from '@/components/MachineForm.vue' // Placeholder for future machine form component
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

interface Machine {
  id: number // 機器 ID
  name: string // 顯示名稱
  machineName: string // 機器內部名稱
  usable: number // 是否啟用
  group: string // 所屬群組
  updateTime: string // 更新時間 (ISO string)
  tags: string // JSON 格式標籤陣列
}

// This is the structure of data coming from EmployeeForm's 'save' emit
// It might have an ID if editing, or not if adding.
// Other fields like group, jwt, usable are expected to be present or defaulted.
interface EmployeeFormSavePayload extends Omit<Employee, 'updateTime'> {
  id: number // id is optional if it's a new employee
}

interface MachineFormSavePayload extends Omit<Machine, 'updateTime'> {
  id: number // id is optional if it's a new machine
}

interface EmployeeTagEventPayload {
  employeeId: number
  tag: string
}

interface MachineTagEventPayload {
  machineId: number
  tag: string
}

const employees = ref<Employee[]>([])
const machines = ref<Machine[]>([])

const userdata = useUserData()
const get_employee_list = async () => {
  const res = await axios.get(`/api/emp/search/${userdata.group}`)
  employees.value = res?.data.data.filter((emp: any) => emp.id !== userdata.id)
  console.log(employees.value.filter((emp: any) => emp.id !== userdata.id))
}

const get_machine_list = async () => {
  const res = await axios.get(`/api/machine/search/${userdata.group}`)
  machines.value = res?.data.data
  console.log(machines.value)
}

const showEmployeeFormModal = ref<boolean>(false)
const employeeToEdit = ref<Employee | null>(null)

const showMachineFormModal = ref<boolean>(false)
const machineToEdit = ref<Machine | null>(null) // Placeholder for future machine form logic

const openAddEmployeeForm = (): void => {
  employeeToEdit.value = null
  showEmployeeFormModal.value = true
}
const openAddMachineForm = (): void => {
  machineToEdit.value = null // Reset for new machine
  showMachineFormModal.value = true
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
const openEditMachineForm = (machineId: number): void => {
  const foundMachine = machines.value.find((mach) => mach.id === machineId)
  if (foundMachine) {
    machineToEdit.value = { ...foundMachine } // Pass a copy
    showMachineFormModal.value = true
  } else {
    console.error(`Machine with ID ${machineId} not found for editing.`)
  }
}

const closeEmployeeFormModal = (): void => {
  showEmployeeFormModal.value = false
  employeeToEdit.value = null
}

const closeMachineFormModal = (): void => {
  showMachineFormModal.value = false
  machineToEdit.value = null // Reset for future use
}

const handleSaveEmployee = async (formData: EmployeeFormSavePayload) => {
  console.log('Saving employee data:', formData)
  // if (formData.id !== undefined) {
  if (formData.id !== 0) {
    // If ID exists (even 0), it's an update
    const index = employees.value.findIndex((emp) => emp.id === formData.id)
    if (index !== -1) {
      const emp = employees.value[index]
      try {
        emp.name = formData.name
        emp.username = formData.username
        emp.role = formData.role
        emp.tags = formData.tags // This is already a JSON string from EmployeeForm
        await axios.put(
          '/api/emp/tag/update',
          { empId: emp.id, tags: formData.tags },
          { headers: { 'Content-Type': 'application/json' } },
        )
        await axios.put(
          '/api/emp/update',
          emp,
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
const handleSaveMachine = async (formData: MachineFormSavePayload) => {
  console.log('Saving machine data:', formData)
  // console.log('Saving machine data:', formData)
  if (formData.id !== 0) {
    // If ID exists, it's an update
    const index = machines.value.findIndex((mach) => mach.id === formData.id)
    if (index !== -1) {
      const mach = machines.value[index]
      try {
        mach.name = formData.name
        mach.machineName = formData.machineName
        mach.usable = formData.usable
        mach.tags = formData.tags // This is already a JSON string from MachineForm
        await axios.put(
          '/api/machine/tag/update',
          { machineId: mach.id, tags: formData.tags },
          { headers: { 'Content-Type': 'application/json' } },
        )
        await axios.put(
          '/api/machine/update',
          mach,
          { headers: { 'Content-Type': 'application/json' } },
        )
        await get_machine_list()
        console.log('Machine updated:', machines.value[index])
      } catch (error) {
        console.log(error)
      }

      // Placeholder for API call: await api.updateMachine(machines.value[index]);
    } else {
      console.error('Failed to find machine for update with ID:', formData.id)
    }
  } else {
    // No ID, so it's a new machine
    const newMachine = {
      name: formData.name,
      machineName: formData.machineName,
      group: userdata.group, // Default if not provided by form
      usable: formData.usable !== undefined ? formData.usable : 0, // Default if not provided
      tags: formData.tags, // This is already a JSON string from MachineForm
    }
    try {
      await axios.post('/api/machine/insert', newMachine)
      await get_machine_list()

      console.log('Machine added:', newMachine)
    } catch (error) {
      console.log(error)
    }
    // Placeholder for API call: await api.addMachine(newMachine);
  }
  closeMachineFormModal()
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

const _updateMachineTags = (
  machineId: number,
  updateFn: (currentTags: string[]) => string[],
): void => {
  const machine = machines.value.find((mach) => mach.id === machineId)
  if (machine) {
    let currentTags: string[] = []
    try {
      if (typeof machine.tags === 'string' && machine.tags.trim() !== '') {
        const parsed = JSON.parse(machine.tags)
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
      machine.tags !== JSON.stringify(currentTags)
    ) {
      machine.tags = JSON.stringify(updatedTags)
      machine.updateTime = new Date().toISOString()
      // Placeholder: console.log(`Updating tags for machine ${machineId} via API...`);
    }
  }
}

const handleAddEmployeeTag = async ({ employeeId, tag }: EmployeeTagEventPayload) => {
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

const handleAddMachineTag = async ({ machineId, tag }: MachineTagEventPayload) => {
  _updateMachineTags(machineId, (tagsArray) => {
    if (!tagsArray.includes(tag)) {
      return [...tagsArray, tag]
    }
    return tagsArray
  })
  const mach = machines.value.find((mach) => mach.id === machineId)

  try {
    await axios.put(
      '/api/machine/tag/update',
      { machineId: machineId, tags: mach?.tags },
      { headers: { 'Content-Type': 'application/json' } },
    )
    await get_machine_list()
    console.log('Machine updated:', mach)
  } catch (error) {
    console.log(error)
  }
}

const handleRemoveEmployeeTag = async ({ employeeId, tag }: EmployeeTagEventPayload) => {
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

const handleRemoveMachineTag = async ({ machineId, tag }: MachineTagEventPayload) => {
  _updateMachineTags(machineId, (tagsArray) => {
    const index = tagsArray.indexOf(tag)
    if (index > -1) {
      const newTagsArray = [...tagsArray]
      newTagsArray.splice(index, 1)
      return newTagsArray
    }
    return tagsArray
  })
  const mach = machines.value.find((mach) => mach.id === machineId)

  try {
    await axios.put(
      '/api/machine/tag/update',
      { machineId: machineId, tags: mach?.tags },
      { headers: { 'Content-Type': 'application/json' } },
    )
    await get_machine_list()
    console.log('Machine updated:', mach)
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

const handleDeleteMachine = async (machineIdToDelete: number) => {
  const machineToDelete = machines.value.find((mach) => mach.id === machineIdToDelete)
  if (machineToDelete) {
    if (confirm(`您確定要刪除機器 "${machineToDelete.name}" (ID: ${machineIdToDelete}) 嗎？`)) {
      machines.value = machines.value.filter((machine) => machine.id !== machineIdToDelete)
      try {
        await axios.delete('/api/machine/delete', {
          data: { id: machineIdToDelete },
          headers: {
            'Content-Type': 'application/json',
          },
        })
        await get_machine_list()

        console.log(`Machine with ID ${machineIdToDelete} deleted locally.`)
      } catch (e) {}

      // Placeholder for API call: await api.deleteMachine(machineIdToDelete);
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
  get_machine_list()
  window.addEventListener('keydown', handleEscKey)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleEscKey)
})
</script>

<style scoped>
.manager-page-wrapper {
  position: relative;
  padding-top: 60px;
  height: 100vh;
  width: 100vw;
  display: flex;
  overflow: hidden;
}

.manager-content-area {
  padding: 20px;
  font-family: 'Arial', sans-serif;
  transition: filter 0.3s ease-in-out;
  background-color: #f4f7f6;
  width: 50vw; /* Adjusted to fit two columns */
  height: 100vh-60px;
  overflow-y: scroll;
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

.add-btn {
  padding: 10px 15px;
  background-color: cornflowerblue;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1em;
}

.add-btn:hover {
  background-color: #5682d8;
}

.employee-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  /* Slightly wider for new buttons */
  gap: 20px;
}
.machine-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  /* Slightly wider for new buttons */
  gap: 20px;
} 

.no-message {
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
