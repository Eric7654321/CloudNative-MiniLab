import { createRouter, createWebHistory } from 'vue-router'
import { useCookies } from 'vue3-cookies';
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/employee',
      name: 'employee',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../components/WorkList.vue'),
    },
    {
      path: '/',
      name: 'root',
      redirect: to => {
        const { cookies } = useCookies();
        if(cookies.get('token') === null) {
          return {path: 'login'}
        }
        return {path: '/employee'}
      }
    }
  ],
})

export default router
