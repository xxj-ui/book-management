<template>
  <div class="page" id="page-books">
    <div class="page-title">
      <h2><span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h16v2H4v-2z"/><path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2zm14 0v16H6V4h12z"/></svg></span> 图书管理</h2>
      <el-button type="primary" icon="Plus" @click="openAddModal">新增图书</el-button>
    </div>
    <div class="card">
      <div class="toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索书名、作者、ISBN..."
          prefix-icon="Search"
          style="width:320px"
          clearable
        />
        <div class="filter-group">
          <el-select v-model="selectedCategory" placeholder="全部类别" style="width:150px" clearable>
            <el-option value="" label="全部类别" />
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.name" />
          </el-select>
        </div>
      </div>
      <el-table :data="paginatedBooks" stripe style="width:100%" empty-text="暂无匹配的图书">
        <el-table-column label="ISBN" width="180">
          <template #default="{ row }">
            <span style="white-space:nowrap;font-family:'Courier New',monospace;font-size:12px;color:var(--acad-stone)">{{ row.isbn }}</span>
          </template>
        </el-table-column>
        <el-table-column label="书名" min-width="160">
          <template #default="{ row }">
            <router-link :to="'/book-detail/' + row.isbn" class="title-link">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="publisher" label="出版社" width="140" />
        <el-table-column label="总册/在架" width="90" align="center">
          <template #default="{ row }">
            {{ row.totalCopies }} / {{ row.availableCopies }}
          </template>
        </el-table-column>
        <el-table-column label="出版年份" width="100" align="center">
          <template #default="{ row }">
            {{ row.publishYear || '' }}
          </template>
        </el-table-column>
        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" style="font-size:11px;padding:0 6px;white-space:nowrap">{{ row.categoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <div style="display:flex;gap:6px;justify-content:center">
              <el-button size="small" circle @click="openEditModal(row)" style="font-size:16px">
                <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg></el-icon>
              </el-button>
              <el-button size="small" circle type="danger" @click="deleteBook(row)" style="font-size:16px">
                <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;display:flex;justify-content:center">
        <el-pagination
          v-if="filteredBooks.length > 0"
          background
          layout="total, prev, pager, next"
          :total="filteredBooks.length"
          :page-size="pageSize"
          v-model:current-page="currentPage"
        />
      </div>
    </div>

    <!-- ============ Add/Edit Book Modal ============ -->
    <el-dialog
      v-model="showAddModal"
      :title="isEditing ? '编辑图书' : '新增图书'"
      :width="isEditing ? '680px' : '580px'"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="addForm" label-position="top">
        <div style="font-size:13px;font-weight:600;color:var(--acad-navy);margin-bottom:12px;padding-bottom:8px;border-bottom:1px solid var(--acad-ivory)">
          <el-icon><Notebook /></el-icon> 图书信息
        </div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="ISBN" required>
              <el-input v-model="addForm.isbn" placeholder="978-X-XXX-XXXXX-X" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="书名" required>
              <el-input v-model="addForm.title" placeholder="请输入书名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者" required>
              <el-input v-model="addForm.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" required>
              <el-input v-model="addForm.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="出版年份">
              <el-select v-model="addForm.publishYear" style="width:100%">
                <el-option v-for="y in yearOptions" :key="y" :label="y" :value="y" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="addForm.categoryId" style="width:100%">
                <el-option value="" label="请选择分类" />
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="内容简介">
          <el-input v-model="addForm.description" type="textarea" :rows="3" placeholder="可选，简要介绍图书内容..." />
        </el-form-item>

        <el-form-item label="封面图片（可选）">
          <div style="display:flex;align-items:center;gap:12px">
            <el-upload
              :show-file-list="false"
              :before-upload="beforeCoverUpload"
              action="#"
              accept="image/*"
            >
              <el-button size="small" type="primary">选择图片</el-button>
            </el-upload>
            <span v-if="addForm.coverUrl" style="font-size:12px;color:var(--acad-stone)">已选择</span>
          </div>
          <div v-if="addForm.coverUrl" style="margin-top:8px;border-radius:8px;overflow:hidden;width:120px;height:160px;border:1px solid var(--acad-card-border);position:relative">
            <img :src="addForm.coverUrl" style="width:100%;height:100%;object-fit:cover" />
            <el-button size="small" circle type="danger" @click.stop="removeCover" style="position:absolute;top:4px;right:4px;width:24px;height:24px;padding:0;min-width:unset;z-index:10">
              <el-icon :size="14"><svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg></el-icon>
            </el-button>
          </div>
        </el-form-item>

        <!-- 非编辑模式下显示副本管理 -->
        <template v-if="!isEditing">
          <div style="font-size:13px;font-weight:600;color:var(--acad-navy);margin-bottom:8px">
            <el-icon><Collection /></el-icon> 副本管理
          </div>
          <div id="copyEntries">
            <div
              v-for="(copy, cIdx) in addForm.copies"
              :key="cIdx"
              style="background:var(--acad-ivory);border-radius:8px;padding:10px 14px;margin-bottom:8px;position:relative;"
            >
              <div style="font-size:11px;color:var(--acad-stone);margin-bottom:6px;display:flex;justify-content:space-between;align-items:center">
                <span>副本 {{ cIdx + 1 }}</span>
                <el-button v-if="addForm.copies.length > 1" link type="danger" size="small" @click="removeCopy(cIdx)">
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
              <div style="margin-bottom:8px">
                <div style="margin-bottom:6px"><el-input v-model="copy.barcode" placeholder="条码号" /></div>
                <el-row :gutter="12" style="margin-bottom:6px">
                  <el-col :span="8">
                    <el-select v-model="copy.shelf_floor" style="width:100%">
                      <el-option label="1F" value="1F" />
                      <el-option label="2F" value="2F" />
                      <el-option label="3F" value="3F" />
                    </el-select>
                  </el-col>
                  <el-col :span="8">
                    <el-select v-model="copy.shelf_zone" style="width:100%">
                      <el-option label="A区" value="A区" />
                      <el-option label="B区" value="B区" />
                      <el-option label="C区" value="C区" />
                    </el-select>
                  </el-col>
                  <el-col :span="8">
                    <el-select v-model="copy.shelf_no" style="width:100%">
                      <el-option v-for="n in 15" :key="n" :label="String(n).padStart(2,'0')+'架'" :value="String(n).padStart(2,'0')+'架'" />
                    </el-select>
                  </el-col>
                </el-row>
                <div><el-input v-model="copy.date" type="date" style="font-size:13px" /></div>
              </div>
            </div>
          </div>
          <el-button size="small" @click="addCopy" style="margin-bottom:8px">
            <el-icon><Plus /></el-icon> 再添加一本
          </el-button>
        </template>

        <!-- 编辑模式下显示副本列表 -->
        <template v-if="isEditing">
          <div style="font-size:13px;font-weight:600;color:var(--acad-navy);margin:16px 0 8px;display:flex;align-items:center;justify-content:space-between">
            <span><el-icon><Collection /></el-icon> 副本管理（共 {{ existingCopies.length }} 册）</span>
            <el-button size="small" icon="Plus" @click="showAddCopyModal = true">添加副本</el-button>
          </div>
          <el-table :data="existingCopies" size="small" max-height="240" style="width:100%" empty-text="暂无副本">
            <el-table-column label="购买时间" width="100">
              <template #default="{ row }">{{ row.purchaseDate || '—' }}</template>
            </el-table-column>
            <el-table-column prop="barcode" label="条码号" />
            <el-table-column label="架位" width="140">
              <template #default="{ row }">{{ row.shelfFloor }}-{{ row.shelfZone }}-{{ row.shelfNo }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.status===1" type="success" size="small">在架</el-tag>
                <el-tag v-else-if="row.status===2" type="warning" size="small">借出</el-tag>
                <el-tag v-else-if="row.status===4" type="danger" size="small">丢失</el-tag>
                <el-tag v-else type="info" size="small">待上架</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="借阅人" width="80">
              <template #default="{ row }">
                {{ row.borrowerName || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110" align="center">
              <template #default="{ row }">
                <div style="display:flex;gap:4px;justify-content:center">
                  <el-button icon="Edit" size="small" :disabled="row.status===2" @click="openEditCopyModal(row)" />
                  <el-button icon="Delete" size="small" type="danger" :disabled="row.status===2" @click="deleteCopy(row)" />
                </div>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="closeAddModal">取消</el-button>
        <el-button type="primary" @click="confirmAdd">
          {{ isEditing ? '保存修改' : '确认新增' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 添加副本对话框 -->
    <el-dialog v-model="showAddCopyModal" title="添加副本" :width="'600px'" :close-on-click-modal="false" destroy-on-close>
      <p style="font-size:13px;color:var(--acad-stone);margin-bottom:12px">
        为《{{ editingBookTitle }}》添加新的物理副本
      </p>
      <div id="singleCopyEntries">
        <div
          v-for="(cp, cIdx) in newCopies"
          :key="cIdx"
          style="background:var(--acad-ivory);border-radius:8px;padding:10px 14px;margin-bottom:8px;position:relative;"
        >
          <div style="font-size:11px;color:var(--acad-stone);margin-bottom:6px;display:flex;justify-content:space-between;align-items:center">
            <span>副本 {{ cIdx + 1 }}</span>
            <el-button v-if="newCopies.length > 1" link type="danger" size="small" @click="removeNewCopy(cIdx)">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div style="margin-bottom:6px"><el-input v-model="cp.barcode" placeholder="条码号" /></div>
          <el-row :gutter="8" style="margin-bottom:6px">
            <el-col :span="8">
              <el-select v-model="cp.floor" style="width:100%">
                <el-option label="1F" value="1F" />
                <el-option label="2F" value="2F" />
                <el-option label="3F" value="3F" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="cp.zone" style="width:100%">
                <el-option label="A区" value="A区" />
                <el-option label="B区" value="B区" />
                <el-option label="C区" value="C区" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="cp.shelf" style="width:100%">
                <el-option v-for="n in 15" :key="n" :label="String(n).padStart(2,'0')+'架'" :value="String(n).padStart(2,'0')+'架'" />
              </el-select>
            </el-col>
          </el-row>
          <el-row :gutter="8">
            <el-col :span="12">
              <el-input v-model="cp.date" type="date" style="font-size:13px" />
            </el-col>
          </el-row>
        </div>
      </div>
      <el-button size="small" icon="Plus" @click="addNewCopy" style="margin-bottom:8px">再添加一本</el-button>
      <template #footer>
        <el-button @click="showAddCopyModal=false">取消</el-button>
        <el-button type="primary" @click="confirmAddCopies">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 编辑副本对话框 -->
    <el-dialog v-model="showEditCopyModal" title="编辑副本" :width="'600px'" :close-on-click-modal="false" destroy-on-close>
      <template v-if="editCopyItem">
        <div>
          <div style="margin-bottom:6px">
            <div style="font-size:12px;font-weight:500;color:var(--acad-charcoal);margin-bottom:4px">条码号</div>
            <el-input v-model="editCopyItem.barcode" placeholder="条码号" />
          </div>
          <div style="margin-bottom:6px">
            <div style="font-size:12px;font-weight:500;color:var(--acad-charcoal);margin-bottom:4px">架位</div>
            <el-row :gutter="8">
              <el-col :span="8">
                <el-select v-model="editCopyItem.shelfFloor" style="width:100%">
                  <el-option label="1F" value="1F" />
                  <el-option label="2F" value="2F" />
                  <el-option label="3F" value="3F" />
                </el-select>
              </el-col>
              <el-col :span="8">
                <el-select v-model="editCopyItem.shelfZone" style="width:100%">
                  <el-option label="A区" value="A区" />
                  <el-option label="B区" value="B区" />
                  <el-option label="C区" value="C区" />
                </el-select>
              </el-col>
              <el-col :span="8">
                <el-select v-model="editCopyItem.shelfNo" style="width:100%">
                  <el-option v-for="n in 15" :key="n" :label="String(n).padStart(2,'0')+'架'" :value="String(n).padStart(2,'0')+'架'" />
                </el-select>
              </el-col>
            </el-row>
          </div>
          <div style="margin-bottom:6px">
            <div style="font-size:12px;font-weight:500;color:var(--acad-charcoal);margin-bottom:4px">日期</div>
            <el-row :gutter="8">
              <el-col :span="12">
                <el-input v-model="editCopyItem.date" type="date" style="font-size:13px" />
              </el-col>
              <el-col :span="12">
                <el-select v-model="editCopyItem.editStatus" style="width:100%">
                  <el-option label="在架" value="在架" />
                  <el-option label="借出" value="借出" />
                  <el-option label="待上架" value="待上架" />
                  <el-option label="丢失" value="丢失" />
                </el-select>
              </el-col>
            </el-row>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="showEditCopyModal=false">取消</el-button>
        <el-button type="primary" @click="saveEditCopy">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookApi } from '@/api/book'
import { categoryApi } from '@/api/category'
import { copyApi } from '@/api/copy'
import { fileApi } from '@/api/file'

// Reactive data
const booksData = ref([])
const categories = ref([])

// Filtering state
const searchQuery = ref('')
const selectedCategory = ref('')
const currentPage = ref(1)
const pageSize = 10

const filteredBooks = computed(() => {
  let result = [...booksData.value]
  const q = searchQuery.value.trim().toLowerCase()
  if (q) {
    result = result.filter(b =>
      b.title.toLowerCase().includes(q) ||
      b.author.toLowerCase().includes(q) ||
      b.isbn.includes(q)
    )
  }
  if (selectedCategory.value) {
    result = result.filter(b => b.categoryName === selectedCategory.value)
  }
  return result
})

const totalPages = computed(() => Math.ceil(filteredBooks.value.length / pageSize) || 1)

const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredBooks.value.slice(start, start + pageSize)
})

const displayPages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }
  const pages = [1]
  if (current > 3) pages.push('...')
  const start = Math.max(2, current - 1)
  const end = Math.min(total - 1, current + 1)
  for (let i = start; i <= end; i++) pages.push(i)
  if (current < total - 2) pages.push('...')
  if (total > 1) pages.push(total)
  return pages
})

watch(filteredBooks, () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value
  }
})

// Add/Edit Book Modal
const showAddModal = ref(false)
const isEditing = ref(false)
const editingIsbn = ref('')
const editingBookId = ref(null)

/** 当前正在编辑/查看的图书标题（用于添加副本弹窗） */
const editingBookTitle = computed(() => {
  if (!editingIsbn.value) return ''
  const book = booksData.value.find(b => b.isbn === editingIsbn.value)
  return book ? book.title : editingIsbn.value
})

const yearOptions = computed(() => {
  const year = new Date().getFullYear()
  const years = []
  for (let y = year + 1; y >= 1980; y--) {
    years.push(y)
  }
  return years
})

const addForm = reactive({
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  publishYear: new Date().getFullYear(),
  categoryId: '',
  description: '',
  coverUrl: '',
  copies: [{ barcode: '', shelf_floor: "3F", shelf_zone: "A区", shelf_no: "12架", date: new Date().toISOString().split('T')[0] }]
})

function resetAddForm() {
  addForm.isbn = ''
  addForm.title = ''
  addForm.author = ''
  addForm.publisher = ''
  addForm.publishYear = new Date().getFullYear()
  addForm.categoryId = ''
  addForm.description = ''
  addForm.coverUrl = ''
  addForm.copies = [{ barcode: '', shelf_floor: "3F", shelf_zone: "A区", shelf_no: "12架", date: new Date().toISOString().split('T')[0] }]
}

function openAddModal() {
  isEditing.value = false
  editingIsbn.value = ''
  editingBookId.value = null
  resetAddForm()
  showAddModal.value = true
}

function openEditModal(book) {
  isEditing.value = true
  editingIsbn.value = book.isbn
  editingBookId.value = book.id
  addForm.isbn = book.isbn
  addForm.title = book.title
  addForm.author = book.author
  addForm.publisher = book.publisher
  addForm.publishYear = book.publishYear || (book.publishDate ? parseInt(book.publishDate.substring(0, 4)) : new Date().getFullYear())
  addForm.categoryId = book.categoryId || ''
  addForm.description = book.description || ''
  addForm.coverUrl = book.coverUrl || ''
  addForm.copies = []
  showAddModal.value = true
  fetchExistingCopies()
}

function closeAddModal() {
  showAddModal.value = false
  isEditing.value = false
}

// 副本管理
function addCopy() {
  addForm.copies.push({ barcode: '', shelf_floor: "3F", shelf_zone: "A区", shelf_no: "12架", date: new Date().toISOString().split('T')[0] })
}

function removeCopy(idx) {
  if (addForm.copies.length > 1) {
    addForm.copies.splice(idx, 1)
  }
}

// 编辑模式下的副本管理
const showAddCopyModal = ref(false)
const showEditCopyModal = ref(false)
const editCopyItem = ref(null)
const existingCopies = ref([])

const newCopies = ref([{ barcode: '', shelf_floor: "3F", shelf_zone: "A区", shelf_no: "12架", date: new Date().toISOString().split('T')[0] }])

function addNewCopy() {
  newCopies.value.push({ barcode: '', shelf_floor: "3F", shelf_zone: "A区", shelf_no: "12架", date: new Date().toISOString().split('T')[0] })
}

function removeNewCopy(idx) {
  if (newCopies.value.length > 1) {
    newCopies.value.splice(idx, 1)
  }
}

function openEditCopyModal(cp) {
  editCopyItem.value = cp
  if (cp) {
    const statusMap = { 1: '在架', 2: '借出', 3: '待上架', 4: '丢失' }
    cp.editStatus = statusMap[cp.status] || '在架'
    cp.date = cp.purchaseDate || new Date().toISOString().split('T')[0]
  }
  showEditCopyModal.value = true
}

async function saveEditCopy() {
  if (editCopyItem.value) {
    if (!editCopyItem.value.barcode) {
      ElMessage.warning('条码号为必填项')
      return
    }
    try {
      await copyApi.update(editCopyItem.value.id || editCopyItem.value.copyId, {
        barcode: editCopyItem.value.barcode,
        shelfFloor: editCopyItem.value.shelfFloor,
        shelfZone: editCopyItem.value.shelfZone,
        shelfNo: editCopyItem.value.shelfNo,
        status: editCopyItem.value.editStatus === '在架' ? 1 : editCopyItem.value.editStatus === '借出' ? 2 : editCopyItem.value.editStatus === '丢失' ? 4 : 3,
        purchaseDate: editCopyItem.value.date
      })
      ElMessage.success('副本信息已更新')
      showEditCopyModal.value = false
      await fetchExistingCopies()
    } catch (e) {
      ElMessage.error(e.message || '更新副本失败')
    }
  }
}

async function confirmAddCopies() {
  if (newCopies.value.some(c => !c.barcode)) {
    ElMessage.warning('请填写条码号')
    return
  }
  try {
    await copyApi.batchAdd(newCopies.value.map(c => ({
      bookId: editingBookId.value,
      barcode: c.barcode,
      shelfFloor: c.floor,
      shelfZone: c.zone,
      shelfNo: c.shelf,
      purchaseDate: c.date
    })))
    ElMessage.success(`成功添加 ${newCopies.value.length} 个副本`)
    showAddCopyModal.value = false
    newCopies.value = [{ barcode: '', shelf_floor: "3F", shelf_zone: "A区", shelf_no: "12架", date: new Date().toISOString().split('T')[0] }]
    await fetchExistingCopies()
  } catch (e) {
    ElMessage.error('添加副本失败')
  }
}

async function deleteCopy(row) {
  try {
    await ElMessageBox.confirm(`确定要删除副本 ${row.barcode} 吗？`, '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await copyApi.delete(row.id)
    ElMessage.success('副本已删除')
    await fetchExistingCopies()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除副本失败')
  }
}

async function fetchExistingCopies() {
  if (!editingBookId.value) {
    existingCopies.value = []
    return
  }
  try {
    const res = await copyApi.listByBook(editingBookId.value)
    existingCopies.value = res.data || res || []
  } catch (e) {
    existingCopies.value = []
  }
}

async function confirmAdd() {
  if (!addForm.isbn || !addForm.title || !addForm.author || !addForm.publisher) {
    ElMessage.warning('请填写完整信息（ISBN、书名、作者、出版社为必填项）')
    return
  }
  if (isEditing.value) {
    try {
      await bookApi.update(editingBookId.value, {
        isbn: addForm.isbn,
        title: addForm.title,
        author: addForm.author,
        publisher: addForm.publisher,
        publishYear: addForm.publishYear,
        categoryId: addForm.categoryId || null,
        description: addForm.description,
        coverUrl: addForm.coverUrl
      })
      ElMessage.success('图书信息已更新')
      await fetchBooks()
    } catch (e) {
      ElMessage.error(e.message || '更新图书失败')
    }
  } else {
    if (addForm.copies.some(c => !c.barcode)) {
      ElMessage.warning('副本条码号为必填项，请填写完整')
      return
    }
    try {
      const res = await bookApi.add({
        isbn: addForm.isbn,
        title: addForm.title,
        author: addForm.author,
        publisher: addForm.publisher,
        publishYear: addForm.publishYear,
        categoryId: addForm.categoryId || null,
        description: addForm.description,
        coverUrl: addForm.coverUrl,
        copies: addForm.copies.filter(c => c.barcode).map(c => ({
          barcode: c.barcode,
          shelfFloor: c.shelf_floor,
          shelfZone: c.shelf_zone,
          shelfNo: c.shelf_no,
          purchaseDate: c.date
        }))
      })
      ElMessage.success(`已添加图书《${addForm.title}》`)
      closeAddModal()
      await fetchBooks()
    } catch (e) {
      ElMessage.error(e.message || '添加图书失败')
    }
    return
  }
  closeAddModal()
}

function deleteBook(book) {
  ElMessageBox.confirm(
    `确定要删除《${book.title}》吗？此操作不可撤销。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await bookApi.delete(book.id)
      ElMessage.success('图书已删除')
      await fetchBooks()
    } catch (e) {
      ElMessage.error(e.message || '删除图书失败')
    }
  }).catch(() => {})
}

/** 封面上传 */
async function beforeCoverUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) { ElMessage.warning('只能上传图片文件'); return false }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) { ElMessage.warning('图片大小不能超过 10MB'); return false }
  try {
    const res = await fileApi.upload(file)
    addForm.coverUrl = res.data || res
    ElMessage.success('封面上传成功')
  } catch (e) {
    ElMessage.error('封面上传失败')
  }
  return false
}

function removeCover() {
  addForm.coverUrl = ''
}

// Lifecycle
onMounted(() => {
  fetchBooks()
  fetchCategories()
})

async function fetchBooks(keyword, categoryId) {
  try {
    const params = { page: 1, size: 999 }
    if (keyword) params.keyword = keyword
    if (categoryId) params.categoryId = categoryId
    const res = await bookApi.list(params)
    booksData.value = res.data?.records || res.data || []
  } catch (e) {
    ElMessage.error('获取图书列表失败')
  }
}

async function fetchCategories() {
  try {
    const res = await categoryApi.list()
    categories.value = (res.data || res || []).filter(c => c.status === 1)
  } catch (e) {
    ElMessage.error('获取分类列表失败')
  }
}
</script>
