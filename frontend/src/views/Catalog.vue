<template>
  <div class="page" id="page-catalog">
    <!-- 轮播 -->
    <el-card shadow="never" :body-style="{ padding: 0 }" style="margin-bottom:20px;overflow:hidden;border-radius:14px">
      <el-carousel height="180px" indicator-position="inside" :interval="4000">
        <el-carousel-item v-for="(slide, idx) in banners" :key="idx">
          <div :style="{ height:'100%', display:'flex', alignItems:'center', justifyContent:'center', backgroundImage: slide.imageUrl ? 'url(' + slide.imageUrl + ')' : 'linear-gradient(135deg, #12203E, #2C4280)', backgroundSize:'cover', backgroundPosition:'center', color:'white', flexDirection:'column', gap:'8px' }">
            <span style="font-size:28px;font-weight:700;font-family:'Noto Serif SC',serif;text-shadow:0 2px 8px rgba(0,0,0,0.15)">{{ slide.title }}</span>
            <span style="font-size:14px;opacity:0.9">{{ slide.subtitle }}</span>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <!-- 快捷统计 -->
    <el-row :gutter="20" style="margin-bottom:24px">
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '16px 20px', cursor:'pointer' }" @click="$router.push('/my-profile')">
          <div style="display:flex;align-items:center;gap:12px">
            <div style="width:42px;height:42px;border-radius:10px;background:#E8EDF5;display:flex;align-items:center;justify-content:center;color:var(--acad-navy);flex-shrink:0">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
            </div>
            <div><div style="font-size:11px;color:var(--acad-stone)">当前借阅</div><div style="font-size:20px;font-weight:700;color:var(--acad-navy)">{{ myBorrowCount }} 册</div></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '16px 20px', cursor:'pointer' }" @click="$router.push('/my-profile')">
          <div style="display:flex;align-items:center;gap:12px">
            <div style="width:42px;height:42px;border-radius:10px;background:#F5EDE0;display:flex;align-items:center;justify-content:center;color:var(--acad-gold);flex-shrink:0">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
            </div>
            <div><div style="font-size:11px;color:var(--acad-stone)">累计借阅</div><div style="font-size:20px;font-weight:700;color:var(--acad-navy)">{{ myTotalBorrows }} 册</div></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '16px 20px' }">
          <div style="display:flex;align-items:center;gap:12px">
            <div style="width:42px;height:42px;border-radius:10px;background:#E6F0E8;display:flex;align-items:center;justify-content:center;color:var(--acad-success);flex-shrink:0">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h10v2H4v-2z"/><path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2zm14 0v16H6V4h12z"/></svg>
            </div>
            <div><div style="font-size:11px;color:var(--acad-stone)">馆藏总数</div><div style="font-size:20px;font-weight:700;color:var(--acad-navy)">{{ totalBooks }} 种</div></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '16px 20px', cursor:'pointer' }" @click="goCategory()">
          <div style="display:flex;align-items:center;gap:12px">
            <div style="width:42px;height:42px;border-radius:10px;background:#F8F0E0;display:flex;align-items:center;justify-content:center;color:var(--acad-warning);flex-shrink:0">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
            </div>
            <div><div style="font-size:11px;color:var(--acad-stone)">查找图书</div><div style="font-size:20px;font-weight:700;color:var(--acad-navy)">浏览全部 →</div></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新书推荐 + 热门排行 -->
    <el-row :gutter="20">
      <el-col :span="17">
        <el-card shadow="never">
          <template #header>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <span style="font-weight:600;font-size:14px">✨ 好书推荐</span>
              <el-button text size="small" @click="goCategory()">查看全部 →</el-button>
            </div>
          </template>
          <el-row :gutter="16">
            <el-col v-for="book in newBooks" :key="book.id" :xs="12" :sm="8" :md="6" style="margin-bottom:16px">
              <el-card shadow="hover" :body-style="{ padding: 0, cursor:'pointer' }" @click="goDetail(book.bookIsbn || book.isbn)" style="overflow:hidden;border-radius:10px">
                <div style="height:140px;background:var(--acad-ivory);display:flex;align-items:center;justify-content:center;border-bottom:1px solid var(--acad-card-border);overflow:hidden;position:relative">
                  <img v-if="book.coverUrl" :src="book.coverUrl" style="width:100%;height:100%;object-fit:contain" @error="e => e.target.style.display='none'" />
                  <svg v-if="!book.coverUrl" viewBox="0 0 24 24" width="40" height="40" fill="var(--acad-mist)"><path d="M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h10v2H4v-2z"/><path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2zm14 0v16H6V4h12z"/></svg>
                  <el-tag type="warning" size="small" style="position:absolute;top:6px;right:6px;font-size:10px;padding:0 5px;height:16px;line-height:16px">荐</el-tag>
                </div>
                <div style="padding:10px 12px">
                  <div style="font-weight:600;color:var(--acad-navy);font-size:12px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ book.bookTitle || book.title }}</div>
                  <div style="font-size:11px;color:var(--acad-stone);margin-top:3px">{{ book.bookAuthor || book.author }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="7">
        <el-card shadow="never">
          <template #header><span style="font-weight:600;font-size:14px">🔥 热门图书 TOP5</span></template>
          <div v-for="(book, idx) in hotBooks" :key="idx" style="display:flex;align-items:center;gap:10px;padding:12px 0;border-bottom:1px solid var(--acad-ivory);cursor:pointer" @click="goDetail(book.isbn || book.bookIsbn)">
            <span :style="{ width:'24px',height:'24px',borderRadius:'6px', display:'flex',alignItems:'center',justifyContent:'center',fontSize:'12px',fontWeight:700, color:'white', background: ['#C49B5E','#9CA3AF','#CD7F32'][idx] || '#F2EDE4', flexShrink:0 }">{{ idx + 1 }}</span>
            <div style="flex:1;min-width:0">
              <div style="font-size:13px;font-weight:500;color:var(--acad-charcoal);overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ book.title }}</div>
              <div style="font-size:11px;color:var(--acad-stone);margin-top:2px">借阅 {{ book.totalBorrows || book.count || 0 }} 次</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { bookApi } from '@/api/book'
import { borrowApi } from '@/api/borrow'
import { bannerApi } from '@/api/banner'
import { recommendApi } from '@/api/recommend'

const router = useRouter()
const authStore = useAuthStore()

const banners = ref([])
const newBooks = ref([])
const hotBooks = ref([])
const myBorrowCount = ref(0)
const myTotalBorrows = ref(0)
const totalBooks = ref(0)

async function fetchData() {
  try {
    const [recRes, hotBooksRes, banRes, booksRes] = await Promise.all([
      recommendApi.list(),
      bookApi.getHotBooks(5),
      bannerApi.list(),
      bookApi.list(),
    ])
    newBooks.value = recRes.data || []
    hotBooks.value = hotBooksRes.data || []
    banners.value = banRes.data || []
    totalBooks.value = booksRes.data?.total || 0

    if (authStore.user?.dbId || authStore.user?.id) {
      const uid = authStore.user.dbId || authStore.user.id
      const borrowRes = await borrowApi.listByUser(uid)
      const records = borrowRes.data || []
      myBorrowCount.value = records.filter(r => r.status === 1 || r.status === 3 || r.status === 4).length
      myTotalBorrows.value = records.length
    }
  } catch (e) {
    console.error('Failed to load catalog data', e)
  }
}

onMounted(fetchData)

function goCategory(category) {
  const query = category ? { category } : {}
  router.push({ name: 'BookList', query })
}

function goDetail(isbn) { router.push('/book-detail/' + isbn) }
</script>
