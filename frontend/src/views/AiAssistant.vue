<template>
  <div class="page" style="height:calc(100vh - 60px - 48px);display:flex;flex-direction:column">
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-shrink:0">
      <div style="display:flex;align-items:center;gap:10px">
        <div style="width:36px;height:36px;background:var(--acad-gold);border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:18px">🤖</div>
        <div>
          <h2 style="margin:0;font-size:18px;font-weight:700;color:var(--acad-navy)">AI 智能助手</h2>
          <span style="font-size:12px;color:var(--acad-stone)">基于通义千问 · 图书管理系统问答</span>
        </div>
      </div>
      <div style="display:flex;gap:8px">
        <el-button size="small" @click="refreshIframe" :icon="Refresh">刷新</el-button>
        <el-button size="small" @click="openInNewTab" type="primary" :icon="FullScreen">新窗口打开</el-button>
      </div>
    </div>

    <div ref="iframeContainer" style="flex:1;border-radius:12px;overflow:hidden;border:1px solid var(--acad-card-border);background:white;position:relative">
      <iframe
        v-if="showIframe"
        :src="ragUrl"
        style="width:100%;height:100%;border:none"
        @load="onIframeLoad"
        @error="onIframeError"
        allow="clipboard-read; clipboard-write"
        referrerpolicy="no-referrer"
      />
      <div v-if="loading" class="loading-overlay">
        <el-icon class="is-loading" :size="32" style="color:var(--acad-gold)"><Loading /></el-icon>
        <span style="margin-top:12px;color:var(--acad-stone);font-size:13px">正在加载 AI 助手...</span>
      </div>
      <div v-if="error" class="loading-overlay" style="flex-direction:column;gap:12px">
        <el-icon :size="40" style="color:var(--acad-danger)"><WarningFilled /></el-icon>
        <span style="color:var(--acad-charcoal);font-size:14px">无法连接到 AI 助手服务</span>
        <span style="color:var(--acad-stone);font-size:12px">请确认 Streamlit 服务已在端口 8501 上运行</span>
        <el-button size="small" @click="retryLoad" type="primary">重试</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Refresh, FullScreen, Loading, WarningFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const RAG_PORT = 8501
const userId = computed(() => authStore.user?.dbId || authStore.user?.id || authStore.user?.role || 'anonymous')
const ragUrl = computed(() => `http://localhost:${RAG_PORT}?user_id=${userId.value}`)
const showIframe = ref(false)
const loading = ref(true)
const error = ref(false)

function onIframeLoad() {
  loading.value = false
  error.value = false
}

function onIframeError() {
  loading.value = false
  error.value = true
}

function refreshIframe() {
  loading.value = true
  error.value = false
  showIframe.value = false
  // Use a timestamp to avoid caching
  setTimeout(() => { showIframe.value = true }, 100)
}

function openInNewTab() {
  window.open(`http://localhost:${RAG_PORT}?user_id=${userId.value}`, '_blank')
}

function retryLoad() {
  error.value = false
  loading.value = true
  showIframe.value = false
  setTimeout(() => { showIframe.value = true }, 100)
}

onMounted(() => {
  // Try to check if the RAG service is available
  const img = new Image()
  img.onload = () => { showIframe.value = true }
  img.onerror = () => {
    // The favicon might not exist, but the server might still be running
    // Just try to load the iframe anyway
    setTimeout(() => { showIframe.value = true }, 500)
  }
  img.src = `http://localhost:${RAG_PORT}/favicon.ico?t=${Date.now()}`
  // Timeout fallback
  setTimeout(() => {
    if (!showIframe.value) {
      showIframe.value = true
    }
  }, 3000)
})
</script>

<style scoped>
.loading-overlay {
  position:absolute;
  top:0;left:0;right:0;bottom:0;
  display:flex;
  align-items:center;
  justify-content:center;
  background:var(--acad-bg);
  flex-direction:column;
}
</style>
