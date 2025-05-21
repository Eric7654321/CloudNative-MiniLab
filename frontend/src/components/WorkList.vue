<template>
  <div class="container">
    <aside class="task-list">
      <h2>我的工作</h2>
      <ul>
        <li v-for="task in tasks" :key="task.id">
          <span :class="{ done: task.done }">{{ task.label }}</span>
        </li>
      </ul>
    </aside>

    <main class="content">
      <h1>這裡是主內容區</h1>
    </main>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, onUnmounted } from 'vue'
import { useCookies } from 'vue3-cookies';
import { useRouter } from 'vue-router';
import axios from 'axios'
import { useUserData } from '@/stores/UserData';

export default defineComponent({
  name: 'TodayTaskList',
  //props: {
  //  employeeId: {
  //    type: Number,
  //    required: true
  //  }
  //},
  setup(/*props*/) {
    const tasks = ref<any[]>([])
    const userdata = useUserData();
    let timer: number

    const { cookies } = useCookies();
    if (cookies.get('token') === null) {
      useRouter().push('/')
    }

    const fetchTodayTasks = async () => {
      try {
        const response = await axios.get(`/api/task/check/today/${userdata.id}`)
        console.log(response)
        console.log(axios.defaults.headers.common['Authorization'])
        tasks.value = response.data.data // 因為你的 Result.success() 包了 data
      } catch (error) {
        console.error('取得任務失敗', error)
      }
    }

    onMounted(() => {
      fetchTodayTasks()
      timer = window.setInterval(fetchTodayTasks, 60000) // 每分鐘刷新一次
    })

    onUnmounted(() => {
      clearInterval(timer)
    })

    return {
      tasks
    }
  }
})
</script>

<style scoped>
.container {
  display: flex;
  padding-top: 60px;
  /* Header 的高度 */
  height: 100vh;
  justify-content: flex-start;
  /* 添加這行來確保向左對齊 */
  margin-right: auto;
  margin-left: 0;
  /* 確保沒有左邊距 */
}

.task-list {
  width: 250px;
  background-color: white;
  padding: 20px;
  border-right: 1px solid #e5e7eb;
  margin-left: 0;
  /* 確保沒有左邊距 */
}

.task-list h2 {
  margin-bottom: 10px;
  color: black;
}

.task-list ul {
  list-style: none;
  padding: 0;
}

.task-list li {
  padding: 8px 0;
  border-bottom: 1px solid #ddd;
  color: black;
}

.task-list .done {
  text-decoration: line-through;
  color: black;
}


.content {
  flex: 1;
  padding: 20px;
}
</style>
