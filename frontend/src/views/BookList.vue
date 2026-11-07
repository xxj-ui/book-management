<template>
  <div class="page">
    <div class="page-title">
      <h2><span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg></span>
        查找书籍
      </h2>
    </div>

    <!-- 搜索栏 -->
    <el-card shadow="never" :body-style="{ padding: '16px 24px' }" style="margin-bottom:24px">
      <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
        <el-input v-model="searchText" placeholder="输入书名、作者或关键词..." clearable style="flex:1;min-width:240px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="selectedCategory" placeholder="全部分类" clearable style="width:150px">
          <el-option value="" label="全部分类" />
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.name" />
        </el-select>
        <span style="font-size:12px;color:var(--acad-stone)">共 {{ filteredBooks.length }} 种</span>
      </div>
    </el-card>

    <!-- 图书卡片网格 -->
    <el-row :gutter="16">
      <el-col v-for="book in filteredBooks" :key="book.isbn" :xs="12" :sm="8" :md="6" :lg="4" style="margin-bottom:16px">
        <el-card shadow="hover" :body-style="{ padding: 0, cursor:'pointer' }" @click="goDetail(book.isbn)" style="overflow:hidden;border-radius:12px">
          <!-- 封面区 -->
          <div style="height:160px;background:var(--acad-ivory);display:flex;align-items:center;justify-content:center;position:relative;border-bottom:1px solid var(--acad-card-border);overflow:hidden">
            <img v-if="book.coverUrl" :src="book.coverUrl" style="width:100%;height:100%;object-fit:contain" @error="e => e.target.style.display='none'" />
            <svg v-if="!book.coverUrl" viewBox="0 0 24 24" width="48" height="48" fill="var(--acad-mist)"><path d="M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h10v2H4v-2z"/><path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2zm14 0v16H6V4h12z"/></svg>
            <el-tag v-if="book.isNew" type="warning" size="small" style="position:absolute;top:8px;right:8px;font-size:10px;padding:0 6px;height:18px;line-height:18px">新书</el-tag>
            <div v-if="book.availableCopies <= 0" style="position:absolute;top:0;left:0;right:0;bottom:0;background:rgba(255,255,255,0.6);display:flex;align-items:center;justify-content:center">
              <span style="background:var(--acad-danger);color:white;padding:2px 12px;border-radius:4px;font-size:11px;font-weight:600">暂无在架</span>
            </div>
          </div>
          <!-- 信息区 -->
          <div style="padding:12px 14px">
            <div style="font-weight:600;color:var(--acad-navy);font-size:13px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ book.title }}</div>
            <div style="font-size:11px;color:var(--acad-stone);margin:4px 0">{{ book.author }}</div>
            <div style="display:flex;align-items:center;justify-content:space-between;margin-top:6px">
              <el-tag v-if="book.availableCopies > 0" type="success" size="small" style="font-size:10px;padding:0 6px;height:18px;line-height:18px">在架 {{ book.availableCopies }}</el-tag>
              <span style="font-size:10px;color:var(--acad-mist)">{{ book.category }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="!filteredBooks.length" style="text-align:center;padding:60px 0;color:var(--acad-stone);font-size:14px">
      <el-icon :size="40" style="color:var(--acad-mist);margin-bottom:12px"><Search /></el-icon>
      <p>未找到匹配的图书</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { bookApi } from '@/api/book'
import { categoryApi } from '@/api/category'

const route = useRoute()
const router = useRouter()
const searchText = ref('')
const selectedCategory = ref(route.query.category || '')
const booksData = ref([])
const categories = ref([])

onMounted(async () => {
  try {
    const [bookRes, catRes] = await Promise.all([
      bookApi.list({ page: 1, size: 999 }),
      categoryApi.list()
    ])
    booksData.value = bookRes.data?.records || []
    categories.value = (catRes.data || []).filter(c => c.status === 1)
  } catch (e) { console.error(e) }
})

const filteredBooks = computed(() => {
  let list = [...booksData.value]
  const q = searchText.value.trim().toLowerCase()
  if (q) list = list.filter(b => (b.title || '').toLowerCase().includes(q) || (b.author || '').toLowerCase().includes(q) || (b.isbn || '').includes(q))
  if (selectedCategory.value) list = list.filter(b => b.categoryName === selectedCategory.value)
  return list
})

function goDetail(isbn) { router.push('/book-detail/' + isbn) }

watch(() => route.query.category, (val) => {
  if (val) selectedCategory.value = val
})
</script>
