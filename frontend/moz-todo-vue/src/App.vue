<template>
  <!-- <img alt="Vue logo" src="./assets/logo.png"> -->
  <!-- <HelloWorld msg="Welcome to Your Vue.js App"/> -->
  <div id="app">
    <h1>To-Do-List</h1>
    <ul>
      <li v-for="item in ToDoItems" :key="item.id">
        <to-do-item :label="item.label" :done="item.done" :id="item.id"></to-do-item>
      </li>
      <!-- <to-do-item label="aaaaa" :done="true"></to-do-item> -->
    </ul>
  </div>
</template>

<script>
// import HelloWorld from './components/HelloWorld.vue'
import ToDoItem from './components/ToDoItem.vue'
// import { nanoid } from 'nanoid'
export default {
  name: 'App',
  components: {
  //   HelloWorld
    ToDoItem
  },
  data() {
    return {
      ToDoItems: [
        { id: '1', label: 'Learn Vue.js', done: false },
        { id: '2', label: 'Learn Vuex', done: false },
        { id: '3', label: 'Learn Vue Router', done: false },
        { id: '4', label: 'Learn Vue CLI', done: false },
        { id: '5', label: 'Learn Vue Test Utils', done: false }
      ]
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
