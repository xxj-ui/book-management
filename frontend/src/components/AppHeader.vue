<template>
  <div class="main-header">
    <el-button v-if="isAdmin" class="toggle-btn" @click="emit('toggleSidebar')" style="width:36px;height:36px;padding:0;border:none;background:var(--acad-ivory);border-radius:8px;color:var(--acad-navy);flex-shrink:0">
      <el-icon :size="20"><Operation /></el-icon>
    </el-button>
    <div class="header-breadcrumb">
      <strong>{{ pageTitle }}</strong>
    </div>
    <div class="header-right">
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="34" style="background:var(--acad-navy);color:var(--acad-gold-light);font-weight:600;flex-shrink:0">
            {{ avatarChar }}
          </el-avatar>
          <div class="user-detail">
            <div class="name">{{ userName }}</div>
            <span class="role-tag">{{ roleLabel }}</span>
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
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  isAdmin: { type: Boolean, default: false }
})

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const emit = defineEmits(['toggleSidebar'])

const pageTitle = computed(() => route.meta?.title || '')

const userName = computed(() => authStore.user?.name || '用户')

const avatarChar = computed(() => {
  const name = userName.value
  return name ? name.charAt(0) : '用'
})

/** 角色标签映射 */
const roleLabelMap = {
  admin: '系统管理员',
  teacher: '教师',
  student: '学生'
}

const roleLabel = computed(() => {
  const role = authStore.user?.role
  if (authStore.user?.educationLevel) {
    return authStore.user.educationLevel
  }
  return roleLabelMap[role] || role || ''
})

function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/my-profile')
  } else if (cmd === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>
