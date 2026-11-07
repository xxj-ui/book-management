<template>
  <div class="page" id="page-overdue">
    <div class="page-title">
      <h2>
        <span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/></svg></span>
        超期催还
      </h2>
      <div>
        <el-button @click="batchRemind">
          <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z" transform="rotate(180,12,12)"/></svg></el-icon>
          批量催还
        </el-button>
      </div>
    </div>
    <!-- Stats bar -->
    <div class="card" style="margin-bottom:16px">
      <div class="stat-card" style="border:none;box-shadow:none;padding:0;gap:32px;cursor:default">
        <div>
          <span style="color:var(--acad-stone);font-size:13px;">当前超期总人数</span><br>
          <span style="font-size:32px;font-weight:700;color:var(--acad-danger)">{{ stats.peopleCount }}</span>
          <span style="color:var(--acad-stone);font-size:13px;margin-left:8px;">人</span>
        </div>
        <div>
          <span style="color:var(--acad-stone);font-size:13px;">超期图书总数</span><br>
          <span style="font-size:32px;font-weight:700;color:var(--acad-warning)">{{ stats.bookCount }}</span>
          <span style="color:var(--acad-stone);font-size:13px;margin-left:8px;">册</span>
        </div>
        <div>
          <span style="color:var(--acad-stone);font-size:13px;">总滞纳金</span><br>
          <span style="font-size:32px;font-weight:700;color:var(--acad-charcoal)">{{ stats.totalFine }} 元</span>
        </div>
      </div>
    </div>
    <!-- Table -->
    <div class="card">
      <el-table :data="paginatedList" style="width:100%">
        <el-table-column width="50">
          <template #header>
            <el-checkbox :model-value="isAllSelected" :indeterminate="isIndeterminate" @change="toggleSelectAll" />
          </template>
          <template #default="{ row }">
            <el-checkbox :model-value="selectedIds.includes(makeKey(row))" @change="() => toggleSelect(row)" />
          </template>
        </el-table-column>
        <el-table-column label="读者" min-width="100">
          <template #default="{ row }">
            {{ row.userName }}
          </template>
        </el-table-column>
        <el-table-column label="学历" width="100">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.educationLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bookTitle" label="图书" min-width="140" />
        <el-table-column prop="borrowDate" label="借书日期" width="120" />
        <el-table-column prop="dueDate" label="应还日期" width="120" />
        <el-table-column label="超期天数" width="100">
          <template #default="{ row }">
            <span style="color:var(--acad-danger);font-weight:600">{{ row.overdueDays }}天</span>
          </template>
        </el-table-column>
        <el-table-column label="滞纳金" width="90">
          <template #default="{ row }">
            {{ row.fineAmount }}元
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="remindItem(row)">催还</el-button>
            <el-button size="small" type="primary" link @click="openExtendModal(row)">
              <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zm0-12H5V6h14v2zM9 14H7v-2h2v2zm4 0h-2v-2h2v2zm4 0h-2v-2h2v2zm-8 4H7v-2h2v2zm4 0h-2v-2h2v2zm4 0h-2v-2h2v2z"/></svg></el-icon>
              延期
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div style="padding:32px;color:var(--acad-stone);">暂无超期记录</div>
        </template>
      </el-table>
      <div style="padding:16px;display:flex;justify-content:center;">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="overdueList.length"
          layout="prev, pager, next"
          background
        />
      </div>
    </div>

    <!-- 延期弹窗 -->
    <el-dialog v-model="showExtendModal" title="设置延期日期" width="400px" :close-on-click-modal="false">
      <template v-if="extendItemData">
        <p style="font-size:13px;color:var(--acad-stone);margin-bottom:4px">读者：<strong>{{ extendItemData.userName }}</strong></p>
        <p style="font-size:13px;color:var(--acad-stone);margin-bottom:16px">图书：<strong>{{ extendItemData.bookTitle }}</strong></p>
        <el-form label-position="top">
          <el-form-item label="新的应还日期">
            <el-date-picker v-model="extendDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="showExtendModal = false">取消</el-button>
        <el-button type="primary" @click="confirmExtend" :disabled="!extendDate">确认延期</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { borrowApi } from '@/api/borrow'

const overdueList = ref([])

onMounted(async () => {
  try {
    const res = await borrowApi.listOverdue()
    overdueList.value = (res.data || res).map(item => ({
      ...item,
      educationLevel: item.userRoleName
    }))
  } catch (e) {
    ElMessage.error('获取超期数据失败')
  }
})

const selectedIds = ref([])
const currentPage = ref(1)

function makeKey(item) {
  return item.userId + item.isbn
}

const isAllSelected = computed(() => {
  if (paginatedList.value.length === 0) return false
  return paginatedList.value.every(item => selectedIds.value.includes(makeKey(item)))
})

const isIndeterminate = computed(() => {
  if (paginatedList.value.length === 0) return false
  const someSelected = paginatedList.value.some(item => selectedIds.value.includes(makeKey(item)))
  return someSelected && !isAllSelected.value
})

function toggleSelect(item) {
  const key = makeKey(item)
  const idx = selectedIds.value.indexOf(key)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(key)
  }
}

function toggleSelectAll() {
  if (isAllSelected.value) {
    const keys = paginatedList.value.map(makeKey)
    selectedIds.value = selectedIds.value.filter(id => !keys.includes(id))
  } else {
    for (const item of paginatedList.value) {
      const key = makeKey(item)
      if (!selectedIds.value.includes(key)) {
        selectedIds.value.push(key)
      }
    }
  }
}

const stats = computed(() => {
  const uniquePeople = new Set(overdueList.value.map(item => item.userId))
  const totalFine = overdueList.value.reduce((sum, item) => sum + item.fineAmount, 0)
  return {
    peopleCount: uniquePeople.size,
    bookCount: overdueList.value.length,
    totalFine: totalFine.toFixed(2)
  }
})

const pageSize = 10

const showExtendModal = ref(false)
const extendItemData = ref(null)
const extendDate = ref('')

function openExtendModal(item) {
  extendItemData.value = item
  // 默认在应还日期上加7天
  const d = new Date(item.dueDate)
  d.setDate(d.getDate() + 7)
  extendDate.value = d.toISOString().split('T')[0]
  showExtendModal.value = true
}

function confirmExtend() {
  if (!extendDate.value) return
  ElMessage.success(`已延期成功！\n图书：${extendItemData.value.bookTitle}\n新应还日期：${extendDate.value}`)
  showExtendModal.value = false
  extendItemData.value = null
}

const totalPages = computed(() => Math.max(1, Math.ceil(overdueList.value.length / pageSize)))

const paginatedList = computed(() => {
  if (currentPage.value > totalPages.value) currentPage.value = 1
  const start = (currentPage.value - 1) * pageSize
  return overdueList.value.slice(start, start + pageSize)
})

function remindItem(item) {
  ElMessage.info(`已发送催还通知给 ${item.userName}\n图书：${item.bookTitle}\n超期 ${item.overdueDays} 天`)
}

function batchRemind() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择需要催还的读者')
    return
  }
  const names = overdueList.value
    .filter(item => selectedIds.value.includes(makeKey(item)))
    .map(item => item.userName)
  ElMessage.info(`批量催还通知已发送\n共 ${selectedIds.value.length} 条记录\n接收人：${[...new Set(names)].join('、')}`)
}
</script>
