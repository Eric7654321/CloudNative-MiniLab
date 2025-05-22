<script setup lang="ts">
import axios from 'axios';
import { defineAsyncComponent, onMounted, onUnmounted, ref } from 'vue';

import { useUserData } from '@/stores/UserData';
//import { RoleToString } from '../get_employee.ts';
import EmployeeCard from '@/components/EmployeeCard.vue';

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

const userdata = useUserData();
const showForm = ref(false)

let employees = ref();
const get_employee_list = async () => {
  const res = await axios.get(`/api/emp/search/${userdata.group}`)
  employees.value = res?.data.data.filter((emp: any) => emp.id !== userdata.id)
  console.log(employees.value.filter((emp: any) => emp.id !== userdata.id))
}

const AddEmployee = defineAsyncComponent(() => import('@/components/AddEmployee.vue'));

const showAddEmployeeModal = ref(false);

const openAddModal = () => {
  showAddEmployeeModal.value = true;
};

const closeAddModal = () => {
  showAddEmployeeModal.value = false;
};

const handleSaveEmployee = async (newEmployeeData: Employee) => {
  try {
    const result = await axios.post('/api/emp/insert', {
      username: newEmployeeData.username,
      name: newEmployeeData.name,
      password: '12345678',
      group: userdata.group,
      usable: 1,
      role: newEmployeeData.role,
    })
    console.log(result)
    await get_employee_list()
  } catch (e) {
    alert(e)
  }

  closeAddModal();
};

// Handle ESC key to close modal
const handleEscKey = (event: any) => {
  if (event.key === 'Escape' && showAddEmployeeModal.value) {
    closeAddModal();
  }

};
const handleDeleteEmployee = async (employeeIdToDelete: number) => {
  // Optional: Add a confirmation here if not done in EmployeeCard
  const employeeToDelete = employees.value.find((emp: any) => emp.id === employeeIdToDelete);
  if (employeeToDelete) {
    if (confirm(`您確定要刪除員工 "${employeeToDelete.name}" (ID: ${employeeIdToDelete}) 嗎？`)) {
      try {
        employees.value = employees.value.filter((employee: any) => employee.id !== employeeIdToDelete);
        await axios.delete('/api/emp/delete', {
          data: { id: employeeIdToDelete },
          headers: {
            'Content-Type': 'application/json'

          }
        })

        console.log(`Employee with ID ${employeeIdToDelete} deleted.`);
        await get_employee_list()

      } catch (e) {

      }
    }
  } else {
    console.warn(`Attempted to delete non-existent employee with ID: ${employeeIdToDelete}`);
  }
};

let time: number;

onMounted(() => {
  get_employee_list()
  time = setInterval(get_employee_list, 600000)
  window.addEventListener('keydown', handleEscKey);
});

onUnmounted(() => {
  clearInterval(time)
  window.removeEventListener('keydown', handleEscKey);
});
</script>

<template>
  <div class="manager-page-wrapper">
    <div class="manager-container" :class="{ 'modal-open': showAddEmployeeModal }">
      <header class="manager-header">
        <h1>工人管理</h1>
        <button @click="openAddModal" class="add-employee-btn">新增員工</button>
      </header>

      <div class="employee-list">
        <EmployeeCard v-for="employee in employees" :key="employee.id" :employee="employee"
          @delete-employee="handleDeleteEmployee" />
        <p v-if="!employees?.length">目前沒有員工資料。</p>
      </div>
    </div>

    <!-- Modal for AddEmployee -->
    <div v-if="showAddEmployeeModal" class="modal-overlay" @click.self="closeAddModal">
      <div class="modal-content-wapper">
        <AddEmployee @save="handleSaveEmployee" @cancel="closeAddModal" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.manager-page-wrapper {
  position: relative;
}

.manager-content-area {
  padding: 20px;
  font-family: 'Arial', sans-serif;
  transition: filter 0.3s ease-in-out;
  background-color: #f4f7f6;
  /* Or your page's default background */
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
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  /* Responsive grid */
  gap: 20px;
}

/* Modal Styles */
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
}

.modal-content {
  animation: fadeInModal 0.3s ease-out;
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
