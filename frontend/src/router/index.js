import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Books from '../views/Books.vue'
import Users from '../views/Users.vue'
import Borrows from '../views/Borrows.vue'
import Reports from '../views/Reports.vue'
import Catalog from '../views/Catalog.vue'
import MyProfile from '../views/MyProfile.vue'
import Categories from '../views/Categories.vue'
import BookDetail from '../views/BookDetail.vue'
import BookList from '../views/BookList.vue'
import Notifications from '../views/Notifications.vue'
import HomeRecommend from '../views/HomeRecommend.vue'
import AiAssistant from '../views/AiAssistant.vue'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'Dashboard', component: Dashboard, meta: { title: '工作台', role: 'admin' } },
  { path: '/books', name: 'Books', component: Books, meta: { title: '图书管理', role: 'admin' } },
  { path: '/users', name: 'Users', component: Users, meta: { title: '用户管理', role: 'admin' } },
  { path: '/borrows', name: 'Borrows', component: Borrows, meta: { title: '借阅管理', role: 'admin' } },
  { path: '/overdue', redirect: '/borrows' },
  { path: '/reports', name: 'Reports', component: Reports, meta: { title: '统计报表', role: 'admin' } },
  { path: '/catalog', name: 'Catalog', component: Catalog, meta: { title: '馆藏书目' } },
  { path: '/my-borrows', redirect: '/my-profile' },
  { path: '/my-profile', name: 'MyProfile', component: MyProfile, meta: { title: '个人中心' } },
  { path: '/categories', name: 'Categories', component: Categories, meta: { title: '分类管理', role: 'admin' } },
  { path: '/holiday', redirect: '/borrows' },
  { path: '/book-detail/:isbn', name: 'BookDetail', component: BookDetail, meta: { title: '图书详情' } },
  { path: '/search-books', name: 'BookList', component: BookList, meta: { title: '查找书籍' } },
  { path: '/ai-assistant', name: 'AiAssistant', component: AiAssistant, meta: { title: 'AI 助手' } },
  { path: '/home-recommend', name: 'HomeRecommend', component: HomeRecommend, meta: { title: '首页推荐管理', role: 'admin' } },
  { path: '/notifications', name: 'Notifications', component: Notifications, meta: { title: '我的通知' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router
