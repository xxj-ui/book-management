<template>
  <div class="page">
    <div class="page-title">
      <h2>
        <span class="icon">
          <svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/></svg>
        </span>
        工作台 — 归还上架
      </h2>
      <div>
        <el-button type="primary" @click="batchShelf" :disabled="selectedIds.length === 0">
          <el-icon class="icon-btn">
            <svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M3 13h2v-2H3v2zm0 5h2v-2H3v2zm0-10h2V6H3v2zm4 4h14v-2H7v2zm0 5h14v-2H7v2zM7 6v2h14V6H7z"/></svg>
          </el-icon>
          批量上架
        </el-button>
      </div>
    </div>

    <el-card shadow="never" :body-style="{ padding: '0px' }">
      <el-table
        :data="pageRecords"
        :row-style="({row}) => row._done ? { textDecoration: 'line-through', opacity: 0.6 } : {}"
        style="width:100%"
        stripe
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="55" :selectable="(row) => !row._done" />
        <el-table-column label="图书" min-width="180">
          <template #default="{ row }">
            <span class="title-link">{{ row.title }}</span>
            <el-tag v-if="row.isNew" size="small" type="info" effect="plain" style="margin-left:4px;font-size:10px;padding:0 6px;height:18px;line-height:18px;border-radius:3px">新书</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="条形码 / ISBN" width="180">
          <template #default="{ row }">
            <span class="shelve-barcode">{{ row.barcode }}</span><br>
            <span style="font-size:10px;color:#bbb">{{ row.isbn }}</span>
          </template>
        </el-table-column>
        <el-table-column label="归还者" width="100">
          <template #default="{ row }">
            {{ row.returner }}
          </template>
        </el-table-column>
        <el-table-column label="身份" width="80">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.returnerRole }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="returnTime" label="归还时间" width="160" />
        <el-table-column prop="shelf" label="架位" width="120" />
        <el-table-column label="操作" width="200" align="right" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :disabled="row._done" @click="confirmShelf(row)" style="min-width:76px">
              {{ row._done ? '已上架' : '确定上架' }}
            </el-button>
            <el-button type="danger" size="small" :disabled="row._done" @click="markLost(row)" style="margin-left:4px">
              丢失
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div style="display:flex;align-items:center;justify-content:space-between;margin-top:16px">
      <span style="font-size:13px;color:#666">共 {{ activeRecords.length }} 条待上架，每页 {{ pageSize }} 条</span>
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="activeRecords.length"
        layout="prev, pager, next"
        background
        small
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dashboardApi } from '@/api/dashboard'
import { ElMessage, ElMessageBox } from 'element-plus'

const records = ref([])
const selectedIds = ref([])
const page = ref(1)
const pageSize = 10
const loading = ref(false)

/** 从后端获取归还记录 */
async function fetchReturns() {
  loading.value = true
  try {
    const res = await dashboardApi.getReturns()
    // 标记本地状态字段
    records.value = (res.data || []).map(r => ({
      ...r,
      _selected: false,
      _done: false,
      _removed: false
    }))
  } catch (e) {
    ElMessage.error('获取归还记录失败: ' + e.message)
    records.value = []
  } finally {
    loading.value = false
  }
}

const activeRecords = computed(() => records.value.filter(r => !r._removed))
const totalPages = computed(() => Math.ceil(activeRecords.value.length / pageSize))
const pageRecords = computed(() => {
  const start = (page.value - 1) * pageSize
  return activeRecords.value.slice(start, start + pageSize)
})

function onSelectionChange(selected) {
  selectedIds.value = selected.map(r => r.id)
}

async function confirmShelf(r) {
  if (r._done) return
  try {
    await dashboardApi.confirmShelf(r.id)
    r._done = true
    setTimeout(() => {
      r._removed = true
      if (page.value > totalPages.value && totalPages.value > 0) {
        page.value = totalPages.value
      }
    }, 400)
  } catch (e) {
    ElMessage.error('上架失败: ' + e.message)
  }
}

async function markLost(r) {
  if (r._done) return
  try {
    await ElMessageBox.confirm(`确认标记图书《${r.title}》(${r.barcode}) 为丢失？`, '提示', {
      confirmButtonText: '确认丢失', cancelButtonText: '取消', type: 'warning'
    })
    await dashboardApi.markLost(r.id)
    r._done = true
    setTimeout(() => { r._removed = true }, 400)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败: ' + e.message)
  }
}

async function batchShelf() {
  if (selectedIds.value.length === 0) return
  try {
    await dashboardApi.batchShelf(selectedIds.value)
    records.value.forEach(r => {
      if (selectedIds.value.includes(r.id) && !r._done) {
        r._done = true
        setTimeout(() => { r._removed = true }, 400)
      }
    })
    selectedIds.value = []
    ElMessage.success('批量上架成功')
  } catch (e) {
    ElMessage.error('批量上架失败: ' + e.message)
  }
}

onMounted(fetchReturns)
</script>
