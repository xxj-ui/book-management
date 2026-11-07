<template>
  <div class="page">
    <div class="page-title">
      <h2>
        <span class="icon">
          <svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M2 2h20v20H2V2zm2 2v16h16V4H4zm4 4h8v2H8V8zm0 4h8v2H8v-2zm0 4h5v2H8v-2z"/></svg>
        </span>
        分类管理
      </h2>
    </div>

    <!-- 数据概览 -->
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="6" v-for="stat in categoryStats" :key="stat.label">
        <el-card shadow="never" :body-style="{ padding: '18px 22px', cursor: 'default' }">
          <div style="display:flex;align-items:center;gap:14px">
            <div :style="{
              width:'46px',height:'46px',borderRadius:'12px',
              display:'inline-flex',alignItems:'center',justifyContent:'center',
              background: stat.bg, color: stat.color, flexShrink:0
            }">
              <span :style="{fontSize:'22px',fontWeight:700,lineHeight:1}">
                <svg v-if="stat.icon === 'category'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M2 2h20v20H2V2zm2 2v16h16V4H4zm4 4h8v2H8V8zm0 4h8v2H8v-2zm0 4h5v2H8v-2z"/></svg>
                <svg v-else-if="stat.icon === 'book'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h10v2H4v-2z"/><path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2zm14 0v16H6V4h12z"/></svg>
                <svg v-else-if="stat.icon === 'top'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2z"/></svg>
                <svg v-else viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
              </span>
            </div>
            <div>
              <div style="font-size:12px;color:var(--acad-stone);margin-bottom:2px">{{ stat.label }}</div>
              <div style="font-size:24px;font-weight:700;color:var(--acad-navy);line-height:1.2">{{ stat.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索 + 新增 -->
    <div class="toolbar" style="margin-bottom:16px;justify-content:flex-end">
      <el-input
        v-model="searchText"
        placeholder="搜索分类名称..."
        clearable
        style="width:220px"
        @input="currentPage = 1"
      >
        <template #prefix>
          <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="openAddModal">
        <el-icon class="icon-btn">
          <svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
        </el-icon>
        新增分类
      </el-button>
    </div>

    <el-card shadow="never" :body-style="{ padding: '0px' }">
      <el-table :data="pageCategories" style="width:100%" stripe>
        <el-table-column label="#" width="56" align="center">
          <template #default="{ $index }">
            <span style="font-weight:700;color:var(--acad-navy)">
              {{ (currentPage - 1) * pageSize + $index + 1 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="分类名称" min-width="160">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:10px">
              <span :style="{
                display:'inline-flex',
                width:'30px',
                height:'30px',
                borderRadius:'8px',
                background: categoryColors[row.id % categoryColors.length],
                alignItems:'center',
                justifyContent:'center',
                color:'white',
                fontSize:'13px',
                fontWeight:700,
                flexShrink:0
              }">{{ row.name.charAt(0) }}</span>
              <span style="font-weight:600;color:var(--acad-navy);font-size:13px">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="图书数量" width="170" align="left">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:8px">
              <el-progress
                :percentage="Math.round(row.bookCount / maxBookCount * 100)"
                :stroke-width="6"
                :color="categoryColors[row.id % categoryColors.length]"
                :show-text="false"
                style="width:70px"
              />
              <span style="font-size:16px;font-weight:700;color:var(--acad-navy);min-width:30px">
                {{ row.bookCount }}
              </span>
              <span style="font-size:11px;color:var(--acad-stone)">册</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'info'" size="small" effect="plain" style="font-size:11px">
              {{ row.status ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button size="small" circle text @click="openEditModal(row)" title="编辑">
              <el-icon :size="16">
                <svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
              </el-icon>
            </el-button>
            <el-button size="small" circle text type="danger" @click="deleteCategory(row)" title="删除">
              <el-icon :size="16">
                <svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
              </el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div style="margin-top:20px;padding:12px 0 4px 0;display:flex;align-items:center;justify-content:flex-end">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="categories.length"
        layout="total, prev, pager, next"
        background
      />
    </div>

    <!-- 新增分类 -->
    <el-dialog v-model="showAddModal" title="新增分类" width="420px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="分类名称" required>
          <el-input v-model="addForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="addForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="confirmAdd">确认新增</el-button>
      </template>
    </el-dialog>

    <!-- 编辑分类 -->
    <el-dialog v-model="showEditModal" title="编辑分类" width="420px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="分类名称" required>
          <el-input v-model="editForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" :disabled="editCat && editCat.bookCount > 0" />
          <div v-if="editCat && editCat.bookCount > 0" style="font-size:11px;color:var(--acad-danger);margin-top:4px">该分类下有 {{ editCat.bookCount }} 册图书，无法禁用</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display:flex;gap:8px;width:100%">
          <el-button @click="showEditModal = false">取消</el-button>
          <el-button type="danger" plain style="margin-left:auto" @click="deleteCategory(editCat)">删除</el-button>
          <el-button type="primary" @click="confirmEdit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { categoryApi } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'

const categoryColors = ['#4E7A9E', '#C49B5E', '#3D7A56', '#B04A46', '#8B6FAF', '#C99D41', '#4A90D9', '#D4836F', '#5BA39A', '#6B7280']

const categories = ref([])
const showAddModal = ref(false)
const showEditModal = ref(false)
const editCat = ref(null)
const searchText = ref('')

const currentPage = ref(1)
const pageSize = 7

async function fetchCategories() {
  const res = await categoryApi.list()
  categories.value = res.data
}

onMounted(() => { fetchCategories() })

// 数据概览
const categoryStats = computed(() => {
  const totalBooks = categories.value.reduce((s, c) => s + (c.bookCount || 0), 0)
  const topCat = [...categories.value].sort((a, b) => b.bookCount - a.bookCount)[0]
  return [
    { label: '全部分类', value: categories.value.length + ' 个', icon: 'category', bg: '#E8EDF5', color: '#12203E' },
    { label: '图书总量', value: totalBooks + ' 册', icon: 'book', bg: '#F5EDE0', color: '#C49B5E' },
    { label: '最多图书', value: topCat ? topCat.name : '—', icon: 'top', bg: '#FFF8F0', color: '#C99D41' },
    { label: '平均每类', value: categories.value.length ? (totalBooks / categories.value.length).toFixed(0) + ' 册' : '0 册', icon: 'avg', bg: '#E6F0E8', color: '#3D7A56' },
  ]
})

// 按图书数量降序排列，支持搜索过滤
const sortedCategories = computed(() => {
  let list = [...categories.value].sort((a, b) => b.bookCount - a.bookCount)
  if (searchText.value.trim()) {
    const q = searchText.value.trim().toLowerCase()
    list = list.filter(c => c.name.toLowerCase().includes(q))
  }
  return list
})

const maxBookCount = computed(() =>
  Math.max(...categories.value.map(c => c.bookCount), 1)
)

const pageCategories = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return sortedCategories.value.slice(start, start + pageSize)
})

// ─── Add ───
const addForm = ref({ name: '', status: 1 })

function openAddModal() {
  addForm.value = { name: '', status: 1 }
  showAddModal.value = true
}

async function confirmAdd() {
  if (!addForm.value.name.trim()) { ElMessage.warning('请输入分类名称'); return }
  try {
    const res = await categoryApi.add({ name: addForm.value.name.trim(), status: addForm.value.status })
    categories.value.push({ ...res.data, bookCount: 0 })
    showAddModal.value = false
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  }
}

// ─── Edit ───
const editForm = ref({ name: '', status: 1 })

function openEditModal(cat) {
  editCat.value = cat
  editForm.value = { name: cat.name, status: cat.status }
  showEditModal.value = true
}

async function confirmEdit() {
  if (!editForm.value.name.trim()) { ElMessage.warning('请输入分类名称'); return }
  await categoryApi.update(editCat.value.id, { name: editForm.value.name.trim(), status: editForm.value.status })
  const cat = categories.value.find(c => c.id === editCat.value.id)
  if (cat) {
    cat.name = editForm.value.name.trim()
    cat.status = editForm.value.status
  }
  showEditModal.value = false
  ElMessage.success('分类已更新')
}

async function deleteCategory(cat) {
  if (!cat) return
  try {
    await ElMessageBox.confirm(`确认要删除分类「${cat.name}」吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await categoryApi.delete(cat.id)
    categories.value = categories.value.filter(c => c.id !== cat.id)
    showEditModal.value = false
    ElMessage.success('分类已删除')
  } catch (e) {
    if (e && e.message) ElMessage.error(e.message)
  }
}
</script>
