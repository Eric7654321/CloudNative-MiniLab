<template>
  <div class="container">
    <aside class="message-list">
      <h2>我的訊息</h2>
      <ul>
        <!-- <li v-for="message in tasks" :key="message.id">
          <span :class="{ done: message.done }">{{ message.label }}</span>
        </li> -->
      </ul>
    </aside>

    <main class="content">
      <h1>訊息中心</h1>
    </main>
  </div>
</template>

<script lang="ts">

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
  margin-left: 0; /* 確保沒有左邊距 */
}

.message-list {
  width: 250px;
  background-color: white;
  padding: 20px;
  border-right: 1px solid #e5e7eb;
  margin-left: 0;
  /* 確保沒有左邊距 */
}

.message-list h2 {
  margin-bottom: 10px;
  color: black;
}

.message-list ul {
  list-style: none;
  padding: 0;
}

.message-list li {
  padding: 8px 0;
  border-bottom: 1px solid #ddd;
  color: black;
}
</style>