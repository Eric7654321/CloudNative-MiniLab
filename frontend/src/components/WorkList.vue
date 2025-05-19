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
export default {
  data() {
    return {
      tasks: [
        { id: 1, label: '完成報告', done: false },
        { id: 2, label: '開會', done: true },
        { id: 3, label: '寫前端 UI', done: false }
      ]
    };
  }
};
</script>

<style scoped>
.container {
  display: flex;
  padding-top: 60px; /* Header 的高度 */
  height: 100vh;
  justify-content: flex-start; /* 添加這行來確保向左對齊 */
}

.task-list {
  width: 250px;
  background-color: white;
  padding: 20px;
  border-right: 1px solid #e5e7eb;
  margin-left: 0; /* 確保沒有左邊距 */
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