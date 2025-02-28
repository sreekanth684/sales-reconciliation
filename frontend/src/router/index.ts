import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import FileUpload from '../components/FileUpload.vue'
import ColumnMapping from '../components/ColumnMapping.vue'
import Reports from '../components/Reports.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/home',
      name: 'home',
      component: HomeView,
    },
    { path: '/', component: FileUpload },
    { path: '/mapping', component: ColumnMapping },
    { path: '/reports', component: Reports },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
  ],
})

export default router
