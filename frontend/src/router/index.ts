import { createRouter, createWebHistory } from 'vue-router'
import { useCookies } from 'vue3-cookies'
import { useUserData } from '@/stores/UserData'
import HomeView from '../views/HomeView.vue'
import VueCookies from 'vue3-cookies'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requireAuth: false },
    },
    {
      path: '/loginRedirect',
      name: 'loginRedirect',
      redirect: (to) => {
        if (useUserData().role === 0) return { path: '/employee' }
        else return { path: '/manager' }
      },
      meta: { requireAuth: true },
    },
    {
      path: '/manager',
      name: 'manager',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/Manager.vue'),
      //props: route => ({ employeeId: Number(route.query.employeeId) }),
      meta: { requireAuth: true, manager: true },
    },
    {
      path: '/employee',
      name: 'employee',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../components/WorkList.vue'),
      //props: route => ({ employeeId: Number(route.query.employeeId) }),
      meta: { requireAuth: true },
    },
    {
      path: '/',
      name: 'root',
      redirect: (to) => {
        if (useUserData().isAuth === false) {
          return { path: '/login' }
        }
        return { path: '/loginRedirect' }
      },
    },
    {
      path: '/message',
      name: 'Message',
      component: () => import('../views/Message.vue'),
      meta: { requireAuth: true },
    },
  ],
})

router.beforeEach((to, from) => {
  console.log(useUserData().isAuth)
  if (
    to.meta.requireAuth !== null &&
    to.meta.requireAuth === true &&
    useUserData().isAuth === false
  ) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }
  if (to.path === '/login' && useUserData().isAuth) {
    return {
      path: '/loginRedirect',
    }
  }

  if (to.meta.manager === true && useUserData().role === 0) {
    return {
      path: '/employee',
    }
  }
})

export default router
