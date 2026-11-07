<template>
  <!-- 读者布局：无侧边栏 -->
  <div v-if="authStore.isLoggedIn && !authStore.isAdmin" style="height:100vh;overflow:hidden;display:flex;flex-direction:column">
    <header style="height:60px;background:var(--acad-white);display:flex;align-items:center;padding:0 24px;border-bottom:1px solid var(--acad-card-border);flex-shrink:0;gap:16px;position:relative">
      <div style="display:flex;align-items:center;gap:10px">
        <div style="width:34px;height:34px;background:var(--acad-gold);border-radius:8px;display:flex;align-items:center;justify-content:center;flex-shrink:0">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="var(--acad-sidebar-bg)"><path d="M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h10v2H4v-2z"/><path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2zm14 0v16H6V4h12z"/></svg>
        </div>
        <span style="font-family:'Noto Serif SC',serif;font-size:16px;font-weight:700;color:var(--acad-gold-light)">图书管理系统</span>
      </div>
      <div style="font-size:13px;color:var(--acad-stone);flex:1;text-align:center;display:flex;align-items:center;justify-content:center;gap:4px">
        <router-link to="/catalog" :style="{ color: currentPath === '/catalog' ? 'var(--acad-navy)' : 'var(--acad-stone)', textDecoration:'none', fontWeight: currentPath === '/catalog' ? 600 : 400, margin:'0 4px', padding:'6px 16px', borderRadius:'8px', background: currentPath === '/catalog' ? 'var(--acad-ivory)' : 'transparent', transition:'all 0.2s' }">首页</router-link>
        <router-link to="/search-books" :style="{ color: currentPath.startsWith('/search-books') ? 'var(--acad-navy)' : 'var(--acad-stone)', textDecoration:'none', fontWeight: currentPath.startsWith('/search-books') ? 600 : 400, margin:'0 4px', padding:'6px 16px', borderRadius:'8px', background: currentPath.startsWith('/search-books') ? 'var(--acad-ivory)' : 'transparent', transition:'all 0.2s' }">查找书籍</router-link>
        <router-link to="/ai-assistant" :style="{ color: currentPath === '/ai-assistant' ? 'var(--acad-navy)' : 'var(--acad-stone)', textDecoration:'none', fontWeight: currentPath === '/ai-assistant' ? 600 : 400, margin:'0 4px', padding:'6px 16px', borderRadius:'8px', background: currentPath === '/ai-assistant' ? 'var(--acad-ivory)' : 'transparent', transition:'all 0.2s' }"><span style="margin-right:4px"></span>AI 助手</router-link>
        <router-link to="/my-profile" :style="{ color: currentPath === '/my-profile' ? 'var(--acad-navy)' : 'var(--acad-stone)', textDecoration:'none', fontWeight: currentPath === '/my-profile' ? 600 : 400, margin:'0 4px', padding:'6px 16px', borderRadius:'8px', background: currentPath === '/my-profile' ? 'var(--acad-ivory)' : 'transparent', transition:'all 0.2s' }">个人中心</router-link>
      </div>
      <div style="display:flex;align-items:center;gap:10px">
        <el-badge :value="unreadCount" :hidden="unreadCount===0" :max="99" style="margin-right:4px">
          <el-button @click="$router.push('/notifications')" style="width:36px;height:36px;padding:0;border:none;background:transparent;border-radius:8px;color:var(--acad-stone)">
            <el-icon :size="20"><Bell /></el-icon>
          </el-button>
        </el-badge>
        <el-dropdown trigger="click" @command="handleLogout">
          <div style="display:flex;align-items:center;gap:8px;padding:4px 8px;border-radius:8px;cursor:pointer">
            <el-avatar :size="32" style="background:var(--acad-navy);color:var(--acad-gold-light);font-weight:600">{{ avatarChar }}</el-avatar>
            <div style="line-height:1.3">
              <div style="font-size:13px;font-weight:600;color:var(--acad-charcoal)">{{ userName }}</div>
              <span style="font-size:10px;padding:1px 6px;border-radius:4px;background:var(--acad-ivory);color:var(--acad-navy)">{{ roleLabel }}</span>
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon> 个人中心
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    <main style="flex:1;overflow-y:auto;padding:24px 28px;background:var(--acad-bg)">
      <router-view />
    </main>
  </div>

  <!-- 管理员布局：保留侧边栏 -->
  <el-container v-else-if="authStore.isLoggedIn && authStore.isAdmin" style="height:100vh;overflow:hidden">
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar-custom">
      <Sidebar :is-collapsed="isCollapsed" />
    </el-aside>
    <el-container direction="vertical">
      <el-header style="height:auto;padding:0;overflow:visible;background:transparent;border:none">
        <AppHeader :is-admin="true" @toggle-sidebar="toggleSidebar" />
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <Login v-else />
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { notificationApi } from '@/api/notification'
import Sidebar from '@/components/Sidebar.vue'
import AppHeader from '@/components/AppHeader.vue'
import Login from '@/views/Login.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const isCollapsed = ref(false)

const currentPath = computed(() => route.path)

const userName = computed(() => authStore.user?.name || '用户')
const avatarChar = computed(() => userName.value.charAt(0))
const unreadCount = ref(0)

async function fetchUnread() {
  if (!authStore.isLoggedIn || authStore.isAdmin) return
  try {
    const res = await notificationApi.unreadCount()
    unreadCount.value = res.data?.count || 0
  } catch (e) {}
}

onMounted(() => {
  fetchUnread()
  // 每 30 秒轮询一次未读数
  setInterval(fetchUnread, 30000)
})

// 路由变化时刷新未读数
watch(() => route.path, () => {
  if (!authStore.isAdmin) fetchUnread()
})
const roleLabel = computed(() => {
  const role = authStore.user?.role
  const edu = authStore.user?.educationLevel
  if (edu) return edu
  const map = { admin: '系统管理员', teacher: '教师', student: '学生' }
  return map[role] || role || ''
})

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
}

function handleLogout(cmd) {
  if (cmd === 'logout') {
    authStore.logout()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/my-profile')
  }
}
</script>

<style scoped>
.sidebar-custom {
  background: var(--acad-sidebar-bg);
  overflow: hidden;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
