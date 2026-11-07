<template>
  <div v-if="book" class="page" id="page-book-detail">
    <div class="page-title">
      <h2><span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h10v2H4v-2z"/><path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2zm14 0v16H6V4h12z"/></svg></span> 图书详情</h2>
      <el-button icon="ArrowLeft" @click="$router.back()">返回</el-button>
    </div>
    <div id="bookDetailContainer">
      <!-- 基本信息 -->
      <el-row :gutter="20" style="margin-bottom:20px">
        <el-col :xs="24" :sm="24" :md="8" :lg="6">
          <el-card shadow="never" style="text-align:center;padding:8px">
            <div class="book-cover" style="width:100%;height:260px;border-radius:10px;margin-bottom:8px;display:flex;align-items:center;justify-content:center;background:var(--acad-ivory);overflow:hidden;position:relative">
              <img v-if="book.coverUrl" :src="book.coverUrl" style="width:100%;height:100%;object-fit:contain" @error="onCoverError" />
              <svg v-else viewBox="0 0 24 24" width="64" height="64" fill="var(--acad-stone)"><path d="M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h10v2H4v-2z"/><path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2zm14 0v16H6V4h12z"/></svg>
            </div>
            <div style="display:flex;gap:6px;justify-content:center;align-items:center;margin-bottom:6px">
              <el-button size="small" icon="Picture" @click="fetchCover" :loading="fetchingCover">获取封面</el-button>
            </div>
            <div v-if="!book.coverUrl" style="font-size:11px;color:var(--acad-stone);margin-bottom:8px">暂无封面</div>
            <div style="margin-top:8px;font-size:13px;color:var(--acad-stone)">
              <el-rate :model-value="avgRating" disabled style="display:inline-flex" />
              <span style="margin-left:6px;font-weight:600;color:var(--acad-charcoal)">{{ avgRating.toFixed(1) }}</span>
              <span style="margin-left:4px">（{{ comments.length }} 条评论）</span>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="24" :md="16" :lg="18">
          <el-card shadow="never" style="padding:8px">
            <div style="display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:14px">
              <h3 style="font-size:22px;font-weight:700;color:var(--acad-navy);font-family:'Noto Serif SC',serif;margin:0">{{ book.title }}</h3>
              <el-tag v-if="book.isNew" size="small" effect="plain" style="background:#E8EDF5;color:var(--acad-info);border-color:#E8EDF5">新书</el-tag>
            </div>
            <el-row :gutter="10" style="font-size:13px">
              <el-col :span="8"><span style="color:var(--acad-stone)">ISBN</span><br><strong style="font-size:12px">{{ book.isbn }}</strong></el-col>
              <el-col :span="8"><span style="color:var(--acad-stone)">作者</span><br><strong>{{ book.author }}</strong></el-col>
              <el-col :span="8"><span style="color:var(--acad-stone)">出版社</span><br><strong>{{ book.publisher }}</strong></el-col>
              <el-col :span="8"><span style="color:var(--acad-stone)">出版年份</span><br><strong>{{ book.publishDate }}</strong></el-col>
              <el-col :span="8"><span style="color:var(--acad-stone)">分类</span><br><strong>{{ book.category }}</strong></el-col>
              <el-col :span="8"><span style="color:var(--acad-stone)">上架时间</span><br><strong>{{ book.createTime ? book.createTime.substring(0, 10) : "—" }}</strong></el-col>
            </el-row>
            <div style="margin-top:14px;padding-top:14px;border-top:1px solid var(--acad-ivory);display:flex;gap:20px;align-items:center;flex-wrap:wrap">
              <div style="text-align:center"><span style="color:var(--acad-stone);font-size:11px">总册数</span><br><span style="font-size:26px;font-weight:700;color:var(--acad-navy)">{{ book.totalCopies }}</span></div>
              <div style="width:1px;height:32px;background:var(--acad-card-border)"></div>
              <div style="text-align:center"><span style="color:var(--acad-stone);font-size:11px">在架可借</span><br><span style="font-size:26px;font-weight:700;color:var(--acad-success)">{{ book.availableCopies }}</span></div>
              <div style="width:1px;height:32px;background:var(--acad-card-border)"></div>
              <div style="text-align:center"><span style="color:var(--acad-stone);font-size:11px">已借出</span><br><span style="font-size:26px;font-weight:700;color:var(--acad-warning)">{{ book.totalCopies - book.availableCopies }}</span></div>
              <div style="width:1px;height:32px;background:var(--acad-card-border)"></div>
              <div style="text-align:center"><span style="color:var(--acad-stone);font-size:11px">累计借阅</span><br><span style="font-size:26px;font-weight:700;color:var(--acad-navy)">{{ book.totalBorrows ?? 0 }}</span></div>
            </div>
            <div style="margin-top:12px">
              <el-button type="primary" icon="Switch" :disabled="book.availableCopies <= 0 || authStore.isAdmin" @click="openBorrowDialog">借出</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 内容简介 -->
      <el-card shadow="never" style="margin-bottom:20px">
        <template #header>
          <div style="display:flex;align-items:center;gap:8px">
            <el-icon><Star /></el-icon>
            <span style="font-weight:600;font-size:15px">内容简介</span>
          </div>
        </template>
        <p style="font-size:13px;color:var(--acad-charcoal);line-height:1.7">{{ book.description }}</p>
      </el-card>

      <!-- 借阅确认弹窗 -->
      <el-dialog v-model="showBorrowDialog" title="借阅确认" width="460px" :close-on-click-modal="false">
        <template v-if="currentUser">
          <div style="border:1px solid var(--acad-card-border);border-radius:10px;overflow:hidden;margin-bottom:16px">
            <div style="background:var(--acad-ivory);padding:8px 14px;font-size:12px;font-weight:600;color:var(--acad-navy)">📖 图书信息</div>
            <div style="padding:12px 14px;font-size:13px">
              <div style="display:flex;justify-content:space-between;padding:6px 0"><span style="color:var(--acad-stone)">书名</span><strong>{{ book.title }}</strong></div>
              <div style="display:flex;justify-content:space-between;padding:6px 0;border-top:1px solid var(--acad-ivory)"><span style="color:var(--acad-stone)">作者</span><span>{{ book.author }}</span></div>
              <div style="display:flex;justify-content:space-between;padding:6px 0;border-top:1px solid var(--acad-ivory)"><span style="color:var(--acad-stone)">ISBN</span><span style="font-size:12px">{{ book.isbn }}</span></div>
            </div>
          </div>
          <div style="border:1px solid var(--acad-card-border);border-radius:10px;overflow:hidden;margin-bottom:16px">
            <div style="background:var(--acad-ivory);padding:8px 14px;font-size:12px;font-weight:600;color:var(--acad-navy)">👤 读者信息</div>
            <div style="padding:12px 14px;font-size:13px">
              <div style="display:flex;justify-content:space-between;padding:6px 0"><span style="color:var(--acad-stone)">姓名</span><strong>{{ currentUser.name }}</strong></div>
              <div style="display:flex;justify-content:space-between;padding:6px 0;border-top:1px solid var(--acad-ivory)"><span style="color:var(--acad-stone)">学号</span><span>{{ currentUser.id }}</span></div>
              <div style="display:flex;justify-content:space-between;padding:6px 0;border-top:1px solid var(--acad-ivory)"><span style="color:var(--acad-stone)">学历</span><span>{{ currentUser.educationLevel || '教师' }}</span></div>
              <div style="display:flex;justify-content:space-between;padding:6px 0;border-top:1px solid var(--acad-ivory)"><span style="color:var(--acad-stone)">当前已借</span><span style="color:var(--acad-navy);font-weight:600">{{ currentBorrows }} / {{ borrowLimit }} 册</span></div>
            </div>
          </div>
          <div style="border:1px solid var(--acad-card-border);border-radius:10px;overflow:hidden">
            <div style="background:var(--acad-ivory);padding:8px 14px;font-size:12px;font-weight:600;color:var(--acad-navy)">📅 借阅期限</div>
            <div style="padding:12px 14px;font-size:13px">
              <div style="display:flex;justify-content:space-between;padding:6px 0"><span style="color:var(--acad-stone)">借书日期</span><strong>{{ todayStr }}</strong></div>
              <div style="display:flex;justify-content:space-between;padding:6px 0;border-top:1px solid var(--acad-ivory)"><span style="color:var(--acad-stone)">应还日期</span><strong style="color:var(--acad-danger)">{{ dueDateStr }}</strong></div>
              <div style="display:flex;justify-content:space-between;padding:6px 0;border-top:1px solid var(--acad-ivory)"><span style="color:var(--acad-stone)">借阅天数</span><span>{{ borrowDays }} 天</span></div>
            </div>
          </div>
        </template>
        <template #footer>
          <el-button @click="showBorrowDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmBorrow" :disabled="currentBorrows >= borrowLimit">确认借出</el-button>
        </template>
      </el-dialog>

      <!-- 读者评论 -->
      <el-card shadow="never">
        <template #header>
          <div style="display:flex;align-items:center;gap:8px">
            <el-icon><ChatDotSquare /></el-icon>
            <span style="font-weight:600;font-size:15px">读者评论</span>
            <span style="font-size:12px;font-weight:400;color:var(--acad-stone);margin-left:4px">（共 {{ comments.length }} 条）</span>
          </div>
        </template>

        <!-- 发表评论 -->
        <div class="comment-form">
          <div class="comment-form-header">
            <span class="form-label" style="margin-bottom:0">发表评论</span>
            <el-rate v-model="newRating" :texts="['很差','较差','一般','推荐','力荐']" show-text />
          </div>
          <div style="display:flex;gap:10px;margin-top:10px">
            <el-input v-model="newCommentText" placeholder="分享你的阅读感受..." @keyup.enter="submitComment" />
            <el-button type="primary" :disabled="!newRating || !newCommentText.trim()" @click="submitComment">发布</el-button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list" v-if="comments.length > 0">
          <div class="comment-item" v-for="c in sortedComments" :key="c.commentId">
            <el-avatar :size="38" style="background:var(--acad-navy);color:var(--acad-gold-light);font-weight:600">
              {{ c.userName.charAt(0) }}
            </el-avatar>
            <div class="comment-body">
              <div class="comment-meta">
                <span class="comment-user">{{ c.userName }}</span>
                <span class="comment-role">{{ c.userRole }}</span>
                <el-rate :model-value="c.rating" disabled style="display:inline-flex" />
                <span class="comment-date">{{ c.createdAt }}</span>
              </div>
              <div class="comment-content">{{ c.content }}</div>
            </div>
          </div>
        </div>
        <div v-else style="text-align:center;padding:40px 0;color:var(--acad-stone);font-size:13px">
          暂无评论，来写第一条评论吧
        </div>
      </el-card>
    </div>
  </div>
  <div v-else class="page" style="text-align:center;padding:80px;color:var(--acad-stone)">
    <h3>未找到该图书</h3>
    <el-button style="margin-top:16px" @click="$router.back()">返回</el-button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { bookApi } from '@/api/book'
import { commentApi } from '@/api/comment'
import { borrowApi } from '@/api/borrow'
import { configApi } from '@/api/config'

const route = useRoute()
const router = useRouter()

const isbn = computed(() => route.params.isbn)

// ─── 图书数据 ───
const book = ref(null)
const comments = ref([])
const currentBorrows = ref(0)
const borrowLimit = ref(999)
const borrowDays = ref(30)
const fetchingCover = ref(false)

const avgRating = computed(() => {
  const list = comments.value
  if (list.length === 0) return 0
  return list.reduce((s, c) => s + c.rating, 0) / list.length
})

const sortedComments = computed(() => {
  return [...comments.value].sort((a, b) => b.createdAt.localeCompare(a.createdAt))
})

// ─── 借阅弹窗 ───
const showBorrowDialog = ref(false)
const authStore = useAuthStore()
const currentUser = computed(() => authStore.user || {})

const todayStr = new Date().toISOString().split('T')[0]

const dueDateStr = computed(() => {
  const d = new Date()
  d.setDate(d.getDate() + borrowDays.value)
  return d.toISOString().split('T')[0]
})

function openBorrowDialog() {
  if (!book.value || book.value.availableCopies <= 0) {
    ElMessage.warning('该图书暂无在架可借的副本')
    return
  }
  showBorrowDialog.value = true
}

function onCoverError(e) {
  e.target.style.display = 'none'
  e.target.nextElementSibling?.style.removeProperty('display')
}

async function fetchCover() {
  if (!book.value?.id) return
  fetchingCover.value = true
  try {
    await bookApi.fetchCover(book.value.id)
    ElMessage.success('封面获取成功')
    await fetchData()
  } catch (e) {
    ElMessage.error(e.message || '获取封面失败')
  } finally {
    fetchingCover.value = false
  }
}

// ─── API 数据获取 ───
async function fetchData() {
  try {
    const res = await bookApi.getByIsbn(isbn.value)
    const data = res.data
    book.value = {
      ...data,
      publishDate: data.publishYear,
      category: data.categoryName,
    }
  } catch (e) {
    book.value = null
  }
}

async function fetchCommentsAndBorrows() {
  try {
    if (!book.value) return

    const promises = [commentApi.listByBook(book.value.id)]
    promises.push(configApi.getBorrowConfigs())

    if (currentUser.value?.dbId || currentUser.value?.id) {
      promises.push(borrowApi.listByUser(currentUser.value.dbId || currentUser.value.id))
    }

    const results = await Promise.all(promises)

    // 解析评论
    const commentsRes = results[0]
    comments.value = (commentsRes.data || []).map(c => ({
      commentId: c.id,
      userName: c.userName || '',
      userRole: c.userRoleName || '',
      rating: c.rating || 0,
      content: c.content || '',
      createdAt: c.createTime ? String(c.createTime).substring(0, 10) : '',
    }))

    // 解析当前用户的借阅记录，统计借阅中+超期的数量
    if (results.length > 2) {
      const borrowsRes = results[2]
      if (borrowsRes && borrowsRes.data) {
        currentBorrows.value = borrowsRes.data.filter(
          r => r.status === 1 || r.status === 3 || r.status === 4
        ).length
      }
    }

    // 从借阅配置获取限额
    const configRes = results[1]
    if (configRes && configRes.data) {
      const role = currentUser.value?.role === 'teacher' ? 2 : 1
      const eduMap = { '高职': 1, '本科': 2, '硕士': 3, '博士': 4 }
      const edu = eduMap[currentUser.value?.educationLevel]
      const matched = configRes.data.find(c => c.role === role && (edu ? c.educationLevel === edu : !c.educationLevel))
      if (matched) {
        borrowLimit.value = matched.maxBooks
        borrowDays.value = matched.maxDays
      }
    }
  } catch (e) {
    console.error('获取评论和借阅信息失败', e)
  }
}

onMounted(async () => {
  await fetchData()
  if (book.value) {
    await fetchCommentsAndBorrows()
  }
})

// ─── 借阅确认 ───
async function confirmBorrow() {
  try {
    await borrowApi.borrow({
      userId: currentUser.value.dbId || currentUser.value.id,
      bookId: book.value.id,
    })
    ElMessage.success(`借出成功！\n图书：${book.value.title}\n借阅人：${currentUser.value.name}\n应还日期：${dueDateStr.value}`)
    showBorrowDialog.value = false
    // 刷新数据
    await fetchData()
    await fetchCommentsAndBorrows()
  } catch (e) {
    ElMessage.error(e.message || '借阅失败')
  }
}

// ─── 新评论 ───
const newRating = ref(0)
const newCommentText = ref('')

async function submitComment() {
  if (!newRating.value || !newCommentText.value.trim() || !book.value) return

  try {
    await commentApi.add({
      bookId: book.value.id,
      rating: newRating.value,
      content: newCommentText.value.trim(),
    })
    newRating.value = 0
    newCommentText.value = ''
    ElMessage.success('评论发表成功')
    // 刷新评论列表
    const res = await commentApi.listByBook(book.value.id)
    comments.value = (res.data || []).map(c => ({
      commentId: c.id,
      userName: c.userName || '',
      userRole: c.userRoleName || '',
      rating: c.rating || 0,
      content: c.content || '',
      createdAt: c.createTime ? String(c.createTime).substring(0, 10) : '',
    }))
  } catch (e) {
    ElMessage.error(e.message || '评论发表失败')
  }
}
</script>

<style scoped>
/* ─── 评论样式 ─── */
.comment-form {
  background: var(--acad-ivory);
  border-radius: 10px;
  padding: 16px 18px;
  margin-bottom: 20px;
}

.comment-form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-input {
  flex: 1;
  height: 38px;
  padding: 0 14px;
  border: 1.5px solid var(--acad-card-border);
  border-radius: 8px;
  font-size: 13px;
  font-family: inherit;
  outline: none;
  transition: border-color 0.2s;
  background: var(--acad-white);
}

.comment-input:focus {
  border-color: var(--acad-gold);
  box-shadow: 0 0 0 3px rgba(196, 155, 94, 0.10);
}

/* ─── 星标 ─── */
.star-display {
  display: inline-flex;
  gap: 1px;
}

.star {
  color: #ddd;
  font-size: 16px;
  line-height: 1;
}

.star.filled {
  color: var(--acad-gold);
}

.star-input {
  display: inline-flex;
  gap: 2px;
  align-items: center;
}

.star-input .star.clickable {
  cursor: pointer;
  font-size: 22px;
  transition: color 0.15s, transform 0.15s;
}

.star-input .star.clickable:hover {
  transform: scale(1.2);
}

/* ─── 评论列表 ─── */
.comment-list {
  display: flex;
  flex-direction: column;
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid var(--acad-ivory);
  animation: fadeSlideUp 0.3s ease both;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: var(--acad-navy);
  color: var(--acad-gold-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.comment-user {
  font-weight: 600;
  color: var(--acad-navy);
}

.comment-role {
  background: var(--acad-ivory);
  padding: 0 6px;
  border-radius: 3px;
  font-size: 10px;
  color: var(--acad-stone);
}

.comment-date {
  color: #bbb;
  margin-left: auto;
  font-size: 11px;
}

.comment-content {
  font-size: 13px;
  color: var(--acad-charcoal);
  line-height: 1.6;
}

@keyframes fadeSlideUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
