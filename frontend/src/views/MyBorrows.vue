<template>
  <div class="page" id="page-my-borrows">
    <div class="page-title">
      <h2>
        <span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M3 13h2v-2H3v2zm0 5h2v-2H3v2zm0-10h2V6H3v2zm4 4h14v-2H7v2zm0 5h14v-2H7v2zM7 6v2h14V6H7z"/></svg></span>
        我的借阅
      </h2>
      <div>
        <span class="subtitle">当前借阅 <span style="font-weight:600;color:var(--el-color-primary)">{{ currentBorrows.length }}</span> 册 · 还可借 <span style="font-weight:600;color:var(--el-color-success)">{{ remainingBooks }}</span> 册</span>
      </div>
    </div>
    <div class="borrow-grid">
      <el-card
        v-for="record in currentBorrows"
        :key="record.recordId"
        shadow="hover"
        :body-style="{ padding: '16px' }"
        :style="record.status === 'overdue' ? { borderLeft: '3px solid var(--el-color-warning)' } : {}"
      >
        <div class="book-title">{{ record.bookTitle }}</div>
        <div class="book-meta">{{ getBookAuthor(record.isbn) }} · {{ getBookPublisher(record.isbn) }}</div>
        <el-divider style="margin:10px 0" />
        <div class="info-row"><span class="label">借书日期</span><span class="value">{{ record.borrowDate }}</span></div>
        <div class="info-row">
          <span class="label">应还日期</span>
          <span class="value" :style="{ color: record.status === 'overdue' ? 'var(--el-color-warning)' : 'var(--el-color-success)' }">{{ record.dueDate }}</span>
        </div>
        <div v-if="record.status === 'overdue'" class="info-row">
          <span class="label">超期天数</span>
          <span class="value" style="color:var(--el-color-danger);font-weight:600">{{ daysOverdue(record) }} 天</span>
        </div>
        <div v-else class="info-row">
          <span class="label">剩余天数</span>
          <span class="value" style="color:var(--el-color-success);font-weight:600">{{ daysRemaining(record) }} 天</span>
        </div>
        <div v-if="record.status === 'overdue'" style="margin-top:10px;padding:10px 14px;background:var(--el-color-danger-light-9);border-radius:6px;display:flex;justify-content:space-between;align-items:center">
          <span style="font-size:13px;color:var(--el-color-danger)">滞纳金</span>
          <span style="font-size:18px;font-weight:600;color:var(--el-color-danger)">{{ calcFine(record).toFixed(2) }} 元</span>
        </div>
        <div v-if="record.status === 'overdue'" style="margin-top:10px">
          <el-button type="primary" size="small" style="width:100%" @click="returnAndPay(record)">
            <el-icon><WarningFilled /></el-icon> 归还并缴费
          </el-button>
        </div>
        <div v-else style="margin-top:12px">
          <el-tag type="success" size="small" effect="plain">借阅中</el-tag>
        </div>
      </el-card>
    </div>
    <el-card shadow="never" style="margin-top:20px">
      <template #header>
        <span style="font-weight:600;font-size:16px">
          <el-icon style="vertical-align:-2px;margin-right:6px"><List /></el-icon>
          历史借阅记录
        </span>
      </template>
      <el-table :data="historyBorrows" stripe style="width:100%" empty-text="暂无历史借阅记录">
        <el-table-column prop="bookTitle" label="图书" />
        <el-table-column prop="borrowDate" label="借书日期" width="120" />
        <el-table-column label="还书日期" width="120">
          <template #default="{ row }">
            {{ row.returnDate || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="借阅天数" width="100">
          <template #default="{ row }">
            {{ row.borrowDays || '—' }} 天
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100">
          <template #default>
            <el-tag type="info" size="small" effect="plain">已归还</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { borrowApi } from '@/api/borrow'
import { configApi } from '@/api/config'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const allRecords = ref([])
const borrowConfigs = ref([])

const currentUserId = computed(() => authStore.user?.id || '')

const userConfig = computed(() => {
  const role = authStore.user?.role || ''
  const edu = authStore.user?.educationLevel || ''
  const roleMap = { student: 1, teacher: 2, admin: 3 }
  return borrowConfigs.value.find(c =>
    c.role === (roleMap[role] || 1) &&
    (c.educationLevel === (edu === '本科' ? 2 : edu === '硕士' ? 3 : edu === '博士' ? 4 : null) || !c.educationLevel)
  ) || null
})

const userBorrows = computed(() => {
  return allRecords.value.filter(r => String(r.userId) === currentUserId.value)
})

const currentBorrows = computed(() => {
  return userBorrows.value.filter(r => r.status === 1 || r.status === 3 || r.status === 4)
})

const historyBorrows = computed(() => {
  return userBorrows.value.filter(r => r.status === 2)
})

const remainingBooks = computed(() => {
  const maxBooks = userConfig.value?.maxBooks || 0
  return Math.max(0, maxBooks - currentBorrows.value.length)
})

onMounted(async () => {
  if (!currentUserId.value) return
  try {
    const [borRes, cfgRes] = await Promise.all([
      borrowApi.listByUser(currentUserId.value),
      configApi.getBorrowConfigs()
    ])
    allRecords.value = borRes.data || []
    borrowConfigs.value = cfgRes.data || []
  } catch (e) {
    console.error(e)
  }
})

function getBookByIsbn(isbn) {
  return null // book detail is embedded in borrow record
}

function getBookAuthor(isbn) { return '' }
function getBookPublisher(isbn) { return '' }

function daysRemaining(record) {
  const now = new Date()
  const due = new Date(record.dueDate)
  const diff = Math.ceil((due - now) / (1000 * 60 * 60 * 24))
  return Math.max(0, diff)
}

function daysOverdue(record) {
  const now = new Date()
  const due = new Date(record.dueDate)
  if (!due || isNaN(due)) return 0
  const diff = Math.ceil((now - due) / (1000 * 60 * 60 * 24))
  return Math.max(0, diff)
}

function calcFine(record) {
  const days = daysOverdue(record)
  const finePerDay = userConfig.value?.finePerDay || 0.10
  return days * finePerDay
}

function returnAndPay(record) {
  ElMessage.info('归还并缴费：' + record.bookTitle)
}
</script>
