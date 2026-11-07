<template>
  <div class="page">
    <div class="page-title">
      <h2><span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg></span> 首页推荐管理</h2>
    </div>

    <el-tabs v-model="tab">
      <!-- ═══ 好书推荐 ═══ -->
      <el-tab-pane label="好书推荐" name="books">
        <div style="display:flex;gap:12px;margin-bottom:16px;align-items:center">
          <el-autocomplete
            v-model="searchText"
            :fetch-suggestions="searchBooks"
            placeholder="输入书名关键词搜索..."
            style="width:320px"
            :trigger-on-focus="false"
            clearable
            @select="onBookSelected"
          >
            <template #default="{ item }">
              <div style="display:flex;justify-content:space-between;align-items:center;padding:4px 0">
                <div style="flex:1;min-width:0">
                  <div style="font-size:13px;font-weight:500;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ item.title }}</div>
                  <div style="font-size:11px;color:#999">{{ item.author }} · {{ item.isbn }}</div>
                </div>
                <el-tag v-if="item._already" size="small" type="info">已推荐</el-tag>
              </div>
            </template>
          </el-autocomplete>
          <el-button type="primary" @click="addRecommend" :disabled="!selectedBook">添加推荐</el-button>
        </div>
        <el-table :data="recommendList" stripe style="width:100%">
          <el-table-column label="排序" width="80">
            <template #default="{ row, $index }">
              <div style="display:flex;gap:4px">
                <el-button size="small" :disabled="$index===0" @click="moveUp($index)">↑</el-button>
                <el-button size="small" :disabled="$index===recommendList.length-1" @click="moveDown($index)">↓</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="bookTitle" label="书名" min-width="200" />
          <el-table-column prop="bookAuthor" label="作者" width="150" />
          <el-table-column prop="bookIsbn" label="ISBN" width="160" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button size="small" type="danger" circle @click="deleteRecommend(row)">✕</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ═══ 轮播图 ═══ -->
      <el-tab-pane label="轮播图" name="banners">
        <el-button type="primary" @click="showBannerDialog = true; bannerForm={title:'',subtitle:'',sortOrder:1,status:1}" style="margin-bottom:16px">新增轮播</el-button>
        <el-table :data="bannerList" stripe style="width:100%">
          <el-table-column label="排序" width="60">
            <template #default="{ row }">{{ row.sortOrder }}</template>
          </el-table-column>
          <el-table-column label="标题" min-width="160">
            <template #default="{ row }">
              <div style="display:flex;align-items:center;gap:8px">
                <strong>{{ row.title }}</strong>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="subtitle" label="副标题" min-width="200" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button size="small" circle @click="editBanner(row)">✎</el-button>
              <el-button size="small" type="danger" circle @click="deleteBanner(row)">✕</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 轮播编辑弹窗 -->
    <el-dialog v-model="showBannerDialog" :title="editingBanner?'编辑轮播':'新增轮播'" width="500px">
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="bannerForm.title" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="bannerForm.subtitle" /></el-form-item>
	        <el-form-item label="背景图片">
	          <div style="display:flex;gap:12px;align-items:flex-start;flex-direction:column">
	            <el-upload :show-file-list="false" action="#" accept="image/*" :before-upload="uploadBannerImage">
	              <el-button size="small" type="primary">选择图片</el-button>
	            </el-upload>
	            <div v-if="bannerForm.imageUrl" style="width:100%;height:100px;border-radius:8px;overflow:hidden;border:1px solid var(--acad-card-border);background-size:cover;background-position:center" :style="{ backgroundImage: 'url(' + bannerForm.imageUrl + ')' }"></div>
	          </div>
	        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="排序"><el-input-number v-model="bannerForm.sortOrder" :min="1" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-switch v-model="bannerForm.status" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showBannerDialog=false">取消</el-button>
        <el-button type="primary" @click="saveBanner">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { recommendApi } from '@/api/recommend'
import { bannerApi } from '@/api/banner'
import { bookApi } from '@/api/book'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fileApi } from '@/api/file'

const tab = ref('books')

// ─── 好书推荐 ───
const recommendList = ref([])
const searchText = ref('')
const selectedBook = ref(null)
const bookSearchCache = ref([])

onMounted(async () => {
  const [recRes, bookRes, banRes] = await Promise.all([
    recommendApi.list(), bookApi.list({ page: 1, size: 999 }), bannerApi.adminList()
  ])
  recommendList.value = recRes.data || []
  bookSearchCache.value = bookRes.data?.records || []
  bannerList.value = banRes.data || []
})

/** 远程搜索图书 */
async function searchBooks(query, cb) {
  if (!query) return cb([])
  // 优先在缓存中搜索，不足时调用 API
  let results = bookSearchCache.value.filter(b =>
    b.title.toLowerCase().includes(query.toLowerCase()) ||
    b.author.toLowerCase().includes(query.toLowerCase()) ||
    b.isbn.includes(query)
  )
  if (results.length === 0) {
    try {
      const res = await bookApi.list({ keyword: query, page: 1, size: 20 })
      results = res.data?.records || []
    } catch (e) { return cb([]) }
  }
  // 标记已推荐的图书
  const recommendedIds = new Set(recommendList.value.map(r => r.bookId))
  cb(results.slice(0, 10).map(b => ({
    ...b,
    value: b.title,
    _already: recommendedIds.has(b.id),
  })))
}

function onBookSelected(item) {
  if (!item._already) {
    selectedBook.value = item
  } else {
    selectedBook.value = null
    searchText.value = ''
  }
}

async function addRecommend() {
  if (!selectedBook.value) return
  if (recommendList.value.find(r => r.bookId === selectedBook.value.id)) {
    ElMessage.warning('该书已在推荐列表中'); searchText.value = ''; selectedBook.value = null; return
  }
  try {
    await recommendApi.add({ bookId: selectedBook.value.id, sortOrder: recommendList.value.length + 1 })
    searchText.value = ''
    selectedBook.value = null
    const recRes = await recommendApi.list()
    recommendList.value = recRes.data || []
    ElMessage.success('已添加')
  } catch (e) { ElMessage.error('添加失败') }
}

async function deleteRecommend(row) {
  try {
    await ElMessageBox.confirm(`确认移除《${row.bookTitle}》？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await recommendApi.delete(row.id)
    recommendList.value = recommendList.value.filter(r => r.id !== row.id)
    ElMessage.success('已移除')
  } catch (e) {
    if (e && e.message) ElMessage.error(e.message)
  }
}

async function moveUp(idx) {
  if (idx === 0) return
  const list = [...recommendList.value];
  [list[idx - 1], list[idx]] = [list[idx], list[idx - 1]]
  await saveSort(list)
}

async function moveDown(idx) {
  if (idx === recommendList.value.length - 1) return
  const list = [...recommendList.value];
  [list[idx], list[idx + 1]] = [list[idx + 1], list[idx]]
  await saveSort(list)
}

async function saveSort(list) {
  recommendList.value = list
  const payload = list.map((r, i) => ({ id: r.id, sortOrder: i + 1 }))
  await recommendApi.updateSort(payload)
}

// ─── 轮播图 ───
const bannerList = ref([])
const showBannerDialog = ref(false)
const editingBanner = ref(null)
const bannerForm = ref({})

function editBanner(row) {
  editingBanner.value = row
  bannerForm.value = { ...row }
  showBannerDialog.value = true
}

async function uploadBannerImage(file) {
  const isImage = file.type.startsWith("image/")
  if (!isImage) { ElMessage.warning("只能上传图片文件"); return false }
  try {
    const res = await fileApi.upload(file)
    bannerForm.value.imageUrl = res.data || res
    ElMessage.success("图片上传成功")
  } catch (e) { ElMessage.error("上传失败") }
  return false
}

async function saveBanner() {
  try {
    if (editingBanner.value) {
      await bannerApi.update(editingBanner.value.id, bannerForm.value)
    } else {
      await bannerApi.add(bannerForm.value)
    }
    ElMessage.success('保存成功')
    showBannerDialog.value = false
    editingBanner.value = null
    const res = await bannerApi.adminList()
    bannerList.value = res.data || []
  } catch (e) { ElMessage.error('保存失败') }
}

async function deleteBanner(row) {
  try {
    await ElMessageBox.confirm(`确认删除轮播"${row.title}"？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await bannerApi.delete(row.id)
    bannerList.value = bannerList.value.filter(b => b.id !== row.id)
    ElMessage.success('已删除')
  } catch (e) {
    if (e && e.message) ElMessage.error(e.message)
  }
}
</script>
