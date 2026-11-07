<template>
  <div class="page">
    <div class="page-title">
      <h2>
        <span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/></svg></span>
        我的通知
      </h2>
      <el-button v-if="notifications.length > 0" size="small" @click="markAllRead">全部已读</el-button>
    </div>

    <el-card shadow="never" :body-style="{ padding: '0px' }">
      <div v-if="notifications.length === 0" style="text-align:center;padding:60px 0;color:var(--acad-stone)">
        <el-icon :size="40" style="color:var(--acad-mist);margin-bottom:12px"><Bell /></el-icon>
        <p>暂无通知</p>
      </div>
      <div v-for="n in notifications" :key="n.id" style="display:flex;align-items:flex-start;gap:14px;padding:16px 20px;border-bottom:1px solid var(--acad-card-border);cursor:pointer;transition:background 0.15s" @click="showDetail(n)">
        <div :style="{ width:'8px',height:'8px',borderRadius:'50%',background:n.isRead===0?'var(--acad-danger)':'transparent',flexShrink:0,marginTop:'6px' }" />
        <div style="flex:1;min-width:0">
          <div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">
            <el-tag :type="tagType(n.type)" size="small" effect="plain">{{ typeLabel(n.type) }}</el-tag>
            <span style="font-size:12px;color:var(--acad-stone)">{{ n.createTime }}</span>
            <span v-if="n.isRead===0" style="font-size:11px;color:var(--acad-danger);font-weight:500">未读</span>
          </div>
          <div style="font-weight:600;font-size:14px;color:var(--acad-navy);margin-bottom:4px">{{ n.title }}</div>
          <div style="font-size:13px;color:var(--acad-charcoal);line-height:1.5;overflow:hidden;text-overflow:ellipsis;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical">{{ n.content }}</div>
        </div>
      </div>
    </el-card>

    <!-- 通知详情弹窗 -->
    <el-dialog v-model="showDialog" :title="detail?.title || ''" width="500px" :close-on-click-modal="true">
      <template v-if="detail">
        <div style="display:flex;gap:8px;margin-bottom:16px">
          <el-tag :type="tagType(detail.type)" size="small" effect="plain">{{ typeLabel(detail.type) }}</el-tag>
          <span style="font-size:12px;color:var(--acad-stone)">{{ detail.createTime }}</span>
        </div>
        <div style="font-size:14px;color:var(--acad-charcoal);line-height:1.8;white-space:pre-wrap">{{ detail.content }}</div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { notificationApi } from '@/api/notification'
import { ElMessage } from 'element-plus'

const notifications = ref([])
const showDialog = ref(false)
const detail = ref(null)

onMounted(async () => {
  try {
    const res = await notificationApi.list()
    notifications.value = res.data || []
  } catch (e) {
    ElMessage.error('获取通知失败')
  }
})

function tagType(type) {
  return { 1: 'warning', 2: 'danger', 3: 'info' }[type] || 'info'
}

function typeLabel(type) {
  return { 1: '假期延期', 2: '超期催还', 3: '还书提醒' }[type] || '通知'
}

async function showDetail(n) {
  detail.value = n
  showDialog.value = true
  // 未读则标记已读
  if (n.isRead === 0) {
    try {
      await notificationApi.markRead(n.id)
      n.isRead = 1
    } catch (e) {}
  }
}

async function markAllRead() {
  try {
    await notificationApi.markAllRead()
    notifications.value.forEach(n => { n.isRead = 1 })
    ElMessage.success('全部已读')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}
</script>
