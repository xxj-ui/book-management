<template>
  <div>
    <div class="sidebar-logo">
      <div class="logo-symbol">
        <svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
          <path d="M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h10v2H4v-2z"/>
          <path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2zm14 0v16H6V4h12z"/>
        </svg>
      </div>
      <span class="logo-text" :class="{ 'logo-collapsed': isCollapsed }">图书管理系统</span>
    </div>

    <el-menu
      :default-active="currentRoute"
      :collapse="isCollapsed"
      router
      background-color="#0F1B35"
      text-color="rgba(255,255,255,0.55)"
      active-text-color="#FFFFFF"
    >
      <template v-if="authStore.isAdmin">
        <el-menu-item-group title="概览">
          <el-menu-item index="/dashboard">
            <el-icon><Menu /></el-icon>
            <template #title>工作台</template>
          </el-menu-item>
          <el-menu-item index="/home-recommend">
            <el-icon><Collection /></el-icon>
            <template #title>首页推荐</template>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group title="管理">
          <el-menu-item index="/categories">
            <el-icon><Tickets /></el-icon>
            <template #title>分类管理</template>
          </el-menu-item>
          <el-menu-item index="/books">
            <el-icon><Reading /></el-icon>
            <template #title>图书管理</template>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/borrows">
            <el-icon><Switch /></el-icon>
            <template #title>借阅记录</template>
          </el-menu-item>
          <el-menu-item index="/reports">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>统计报表</template>
          </el-menu-item>
          <el-menu-item index="/my-profile">
            <el-icon><UserFilled /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
        </el-menu-item-group>
      </template>

      <template v-else>
        <el-menu-item-group title="读者服务">
          <el-menu-item index="/catalog">
            <el-icon><Search /></el-icon>
            <template #title>馆藏书目</template>
          </el-menu-item>
          <el-menu-item index="/ai-assistant">
            <el-icon><Cpu /></el-icon>
            <template #title>AI 助手</template>
          </el-menu-item>
          <el-menu-item index="/my-profile">
            <el-icon><UserFilled /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
        </el-menu-item-group>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  isCollapsed: {
    type: Boolean,
    default: false
  }
})

const authStore = useAuthStore()
const route = useRoute()

const currentRoute = computed(() => route.path)
</script>

<style scoped>
.logo-collapsed {
  opacity: 0;
}
</style>
