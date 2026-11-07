<template>
  <div class="page" id="page-borrows">
    <div class="page-title">
      <h2>
        <span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M6.99 11L3 15l3.99 4v-3H14v-2H6.99v-3zM21 9l-3.99-4v3H10v2h7.01v3L21 9z"/></svg></span>
        借阅管理
      </h2>
      <div>
        <el-button v-if="authStore.isAdmin" @click="showSettingsModal = true">
          <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.07.62-.07.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg></el-icon>
          规则设置
        </el-button>
      </div>
    </div>
    <div class="card">
      <el-tabs v-model="borrowTab" style="margin-bottom:0">
        <el-tab-pane label="全部记录" name="records" />
        <el-tab-pane label="超期催还" name="overdue" />
      </el-tabs>

      <!-- ═══ 全部记录 ═══ -->
      <template v-if="borrowTab === 'records'">
      <div class="toolbar">
        <el-input v-model="searchQuery" placeholder="搜索读者 / 图书..." clearable style="width:240px">
          <template #prefix>
            <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg></el-icon>
          </template>
        </el-input>
        <div class="filter-group">
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:130px">
            <el-option value="" label="全部状态" />
            <el-option value="1" label="借阅中" />
            <el-option value="2" label="已归还" />
            <el-option value="3" label="超期" />
            <el-option value="4" label="待审核" />
          </el-select>
          <el-select v-model="timeFilter" placeholder="时间范围" style="width:130px">
            <el-option value="week" label="近一周" />
            <el-option value="month" label="近一月" />
            <el-option value="semester" label="本学期" />
            <el-option value="all" label="全部" />
          </el-select>
        </div>
      </div>

      <el-table :data="paginatedRecords" style="width:100%" stripe>
        <el-table-column label="读者" width="160">
          <template #default="{ row }">
            {{ row.userName }}
          </template>
        </el-table-column>
        <el-table-column label="身份" width="80">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.userRole }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bookTitle" label="图书" min-width="160" />
        <el-table-column prop="borrowDate" label="借书日期" width="120" />
        <el-table-column prop="dueDate" label="应还日期" width="120" />
        <el-table-column label="实还日期" width="110">
          <template #default="{ row }">{{ row.returnDate || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.status===4" type="warning" size="small">待审核</el-tag>
            <el-tag v-else-if="row.status===2" type="info" size="small">已归还</el-tag>
            <el-tag v-else-if="row.overdueDays > 0" type="danger" size="small">超期 {{row.overdueDays}}天</el-tag>
            <el-tag v-else type="success" size="small">借阅中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="滞纳金" width="90">
          <template #default="{ row }">
            {{ getFine(row) || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDetailModal(row)" style="white-space:nowrap">查看</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div style="padding:32px;color:var(--acad-stone);">暂无借阅记录</div>
        </template>
      </el-table>

      <div style="padding:16px;display:flex;justify-content:center;">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="10"
          :total="filteredRecords.length"
          layout="total, prev, pager, next"
          background
        />
      </div>
      </template>
      <!-- ═══ 超期催还 ═══ -->
      <template v-if="borrowTab === 'overdue'">
        <div style="display:flex;gap:24px;padding:16px 0;margin-bottom:8px">
          <div>
            <span style="color:var(--acad-stone);font-size:13px;">当前超期总人数</span><br>
            <span style="font-size:28px;font-weight:700;color:var(--acad-danger)">{{ overdueStats.peopleCount }}</span>
            <span style="color:var(--acad-stone);font-size:13px;margin-left:6px;">人</span>
          </div>
          <div>
            <span style="color:var(--acad-stone);font-size:13px;">超期图书总数</span><br>
            <span style="font-size:28px;font-weight:700;color:var(--acad-warning)">{{ overdueStats.bookCount }}</span>
            <span style="color:var(--acad-stone);font-size:13px;margin-left:6px;">册</span>
          </div>
          <div>
            <span style="color:var(--acad-stone);font-size:13px;">总滞纳金</span><br>
            <span style="font-size:28px;font-weight:700;color:var(--acad-charcoal)">{{ overdueStats.totalFine }} 元</span>
          </div>
          <div style="margin-left:auto;display:flex;align-items:flex-end">
            <el-button @click="batchRemind" :disabled="overdueSelectedIds.length === 0">
              <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z" transform="rotate(180,12,12)"/></svg></el-icon>
              批量催还
            </el-button>
          </div>
        </div>
        <el-table :data="overduePaginated" style="width:100%" stripe>
          <el-table-column width="50">
            <template #header>
              <el-checkbox :model-value="overdueAllSelected" :indeterminate="overdueIndeterminate" @change="overdueToggleAll" />
            </template>
            <template #default="{ row }">
              <el-checkbox :model-value="overdueSelectedIds.includes(row.recordId)" @change="overdueToggle(row)" />
            </template>
          </el-table-column>
          <el-table-column label="读者" width="120">
            <template #default="{ row }">{{ row.userName }}</template>
          </el-table-column>
          <el-table-column label="学历" width="80">
            <template #default="{ row }"><el-tag type="warning" size="small">{{ row.userRole }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="bookTitle" label="图书" min-width="140" />
          <el-table-column prop="borrowDate" label="借书日期" width="110" />
          <el-table-column prop="dueDate" label="应还日期" width="110" />
          <el-table-column label="超期天数" width="90" align="center">
            <template #default="{ row }">
              <span style="color:var(--acad-danger);font-weight:600">{{ overdueDays(row) }}天</span>
            </template>
          </el-table-column>
          <el-table-column label="滞纳金" width="90" align="center">
            <template #default="{ row }">{{ getFine(row) || '0.00' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="remindItem(row)">催还</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="padding:16px 0;display:flex;justify-content:center;">
          <el-pagination
            v-model:current-page="overduePage"
            :page-size="10"
            :total="overdueList.length"
            layout="prev, pager, next"
            background
          />
        </div>
      </template>
    </div>

    <!-- ─── 规则设置 Dialog ─── -->
    <el-dialog v-model="showSettingsModal" title="规则设置" width="780px" :close-on-click-modal="false">
      <el-tabs v-model="settingsTab">
        <el-tab-pane label="借阅规则" name="rules">
          <p style="font-size:13px;color:var(--acad-stone);margin-bottom:16px;line-height:1.6">
            根据读者身份设置不同的借阅权限，修改后即时生效。
            <span v-if="!authStore.isAdmin" style="color:var(--acad-warning)">（仅管理员可修改）</span>
          </p>
          <div style="border:1px solid var(--acad-card-border);border-radius:10px;overflow:hidden">
            <el-row :gutter="0" style="background:var(--acad-ivory);font-size:12px;font-weight:600;color:var(--acad-navy);border-bottom:1px solid var(--acad-card-border)">
              <el-col :span="4" style="padding:10px 16px;border-right:1px solid var(--acad-card-border)">身份</el-col>
              <el-col :span="5" style="padding:10px 16px;border-right:1px solid var(--acad-card-border);text-align:center">最大借阅册数</el-col>
              <el-col :span="5" style="padding:10px 16px;border-right:1px solid var(--acad-card-border);text-align:center">最大借阅天数</el-col>
              <el-col :span="5" style="padding:10px 16px;border-right:1px solid var(--acad-card-border);text-align:center">滞纳金 (元/天)</el-col>
              <el-col :span="5" style="padding:10px 16px;text-align:center">新书借阅</el-col>
            </el-row>
            <div v-for="(rule, idx) in settingsRules" :key="idx" :style="{background: idx % 2 === 0 ? 'white' : '#FAF8F4', borderBottom: idx < settingsRules.length - 1 ? '1px solid var(--acad-card-border)' : 'none'}">
              <el-row :gutter="0" style="align-items:center;min-height:48px">
                <el-col :span="4" style="padding:8px 16px;border-right:1px solid var(--acad-card-border);font-weight:600;font-size:13px;color:var(--acad-navy)">
                  {{ rule._levelName }}
                </el-col>
                <el-col :span="5" style="padding:8px 16px;border-right:1px solid var(--acad-card-border);text-align:center">
                  <el-input-number v-model="rule.maxBooks" :min="1" :max="50" :disabled="!authStore.isAdmin" size="small" controls-position="right" style="width:90px" @change="rulesChanged = true" />
                </el-col>
                <el-col :span="5" style="padding:8px 16px;border-right:1px solid var(--acad-card-border);text-align:center">
                  <el-input-number v-model="rule.maxDays" :min="1" :max="365" :disabled="!authStore.isAdmin" size="small" controls-position="right" style="width:90px" @change="rulesChanged = true" />
                </el-col>
                <el-col :span="5" style="padding:8px 16px;border-right:1px solid var(--acad-card-border);text-align:center">
                  <el-input-number v-model="rule.finePerDay" :min="0" :max="10" :step="0.05" :disabled="!authStore.isAdmin" size="small" controls-position="right" style="width:90px" @change="rulesChanged = true" />
                </el-col>
                <el-col :span="5" style="padding:8px 16px;text-align:center">
                  <el-input-number v-model="rule.maxNewBooks" :min="0" :max="50" :disabled="!authStore.isAdmin" size="small" controls-position="right" style="width:90px" @change="rulesChanged = true" />
                </el-col>
              </el-row>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="寒暑假设置" name="holiday">
          <p style="font-size:12px;color:var(--acad-stone);margin-bottom:12px;line-height:1.6">
            设置寒暑假日期后，系统会自动将到期日在假期内的图书延期至开学。
            <span v-if="!authStore.isAdmin" style="color:var(--acad-warning)">（仅管理员可修改）</span>
          </p>
          <div style="background:var(--acad-ivory);border-radius:10px;padding:16px;margin-bottom:16px">
            <div style="font-size:13px;font-weight:600;color:var(--acad-navy);margin-bottom:12px">☀️ 暑假延期设置</div>
            <el-form :model="summerSettings" label-position="top">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="起始日期">
                    <el-date-picker v-model="summerSettings.startDate" type="date" placeholder="选择日期" :disabled="!authStore.isAdmin" value-format="YYYY-MM-DD" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="结束日期">
                    <el-date-picker v-model="summerSettings.endDate" type="date" placeholder="选择日期" :disabled="!authStore.isAdmin" value-format="YYYY-MM-DD" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item>
                    <template #label>
                      <span style="font-weight:500">延期天数</span>
                      <span style="font-size:12px;color:var(--acad-stone);margin-left:8px;font-weight:400">到期日在暑假期间自动延期指定天数</span>
                    </template>
                    <el-input-number v-model="summerSettings.extensionDays" :min="0" :max="180" :disabled="!authStore.isAdmin" style="width:140px" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                </el-col>
              </el-row>
            </el-form>
          </div>
          <div style="background:var(--acad-ivory);border-radius:10px;padding:16px">
            <div style="font-size:13px;font-weight:600;color:var(--acad-navy);margin-bottom:12px">❄️ 寒假延期设置</div>
            <el-form :model="winterSettings" label-position="top">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="起始日期">
                    <el-date-picker v-model="winterSettings.startDate" type="date" placeholder="选择日期" :disabled="!authStore.isAdmin" value-format="YYYY-MM-DD" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="结束日期">
                    <el-date-picker v-model="winterSettings.endDate" type="date" placeholder="选择日期" :disabled="!authStore.isAdmin" value-format="YYYY-MM-DD" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item>
                    <template #label>
                      <span style="font-weight:500">延期天数</span>
                      <span style="font-size:12px;color:var(--acad-stone);margin-left:8px;font-weight:400">到期日在寒假期间自动延期指定天数</span>
                    </template>
                    <el-input-number v-model="winterSettings.extensionDays" :min="0" :max="180" :disabled="!authStore.isAdmin" style="width:140px" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                </el-col>
              </el-row>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showSettingsModal = false">关闭</el-button>
        <el-button v-if="authStore.isAdmin" type="primary" @click="saveSettings">
          <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg></el-icon>
          保存设置
        </el-button>
      </template>
    </el-dialog>

    <!-- 借阅详情 Dialog -->
    <el-dialog v-model="showDetailModal" title="借阅详情" width="560px" :close-on-click-modal="false">
      <template v-if="detailRecord">
        <div style="border:1px solid var(--acad-card-border);border-radius:8px;overflow:hidden">
          <el-row :gutter="0">
            <el-col :span="12">
              <div class="detail-cell">
                <span class="detail-label">读者姓名</span>
                <span class="detail-value">{{ detailRecord.userName }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-cell detail-cell-right">
                <span class="detail-label">读者身份</span>
                <span class="detail-value">{{ detailRecord.userRole }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="0">
            <el-col :span="24">
              <div class="detail-cell detail-cell-top">
                <span class="detail-label">图书名称</span>
                <span class="detail-value" style="font-weight:600;color:var(--acad-navy);font-size:15px">{{ detailRecord.bookTitle }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="0">
            <el-col :span="12">
              <div class="detail-cell detail-cell-top">
                <span class="detail-label">条码号</span>
                <span class="detail-value" style="font-family:'Courier New',monospace;font-size:13px">{{ getBarcode(detailRecord) }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-cell detail-cell-top detail-cell-right">
                <span class="detail-label">ISBN</span>
                <span class="detail-value" style="font-family:'Courier New',monospace;font-size:12px">{{ detailRecord.isbn }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="0">
            <el-col :span="8">
              <div class="detail-cell detail-cell-top">
                <span class="detail-label">借书日期</span>
                <span class="detail-value">{{ detailRecord.borrowDate }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-cell detail-cell-top">
                <span class="detail-label">应还日期</span>
                <span class="detail-value">{{ detailRecord.dueDate }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-cell detail-cell-top detail-cell-right">
                <span class="detail-label">实还日期</span>
                <span class="detail-value">{{ detailRecord.returnDate || '—' }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="0" style="border-bottom:none">
            <el-col :span="8">
              <div class="detail-cell detail-cell-top" style="border-bottom:none">
                <span class="detail-label">借阅状态</span>
                <span class="detail-value">
                  <el-tag v-if="detailRecord.status===1" type="success" size="small">借阅中</el-tag>
                  <el-tag v-else-if="detailRecord.status===3" type="danger" size="small">超期</el-tag>
                  <el-tag v-else-if="detailRecord.status===4" type="warning" size="small">待审核</el-tag>
                  <el-tag v-else type="info" size="small">已归还</el-tag>
                </span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-cell detail-cell-top" style="border-bottom:none">
                <span class="detail-label">续借次数</span>
                <span class="detail-value">{{ detailRecord.renewCount || 0 }} 次</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-cell detail-cell-top detail-cell-right" style="border-bottom:none">
                <span class="detail-label">滞纳金</span>
                <span class="detail-value" :style="{color: getFine(detailRecord) ? 'var(--acad-danger)' : 'var(--acad-stone)', fontWeight: getFine(detailRecord) ? 600 : 400}">{{ getFine(detailRecord) || '无' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </template>
      <template #footer>
        <el-button @click="showDetailModal = false" class="detail-close-btn">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { borrowApi } from '@/api/borrow'
import { configApi } from '@/api/config'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const allBorrowRecords = ref([])
const fineRecords = ref([])
const allBorrowConfig = ref([])

const searchQuery = ref('')
const statusFilter = ref('')
const timeFilter = ref('')
const currentPage = ref(1)
const borrowTab = ref('records')

// ─── 超期催还 ───
const overdueSelectedIds = ref([])
const overduePage = ref(1)

const overdueList = computed(() =>
  allBorrowRecords.value.filter(r => r.overdueDays > 0)
)

const overduePaginated = computed(() => {
  const start = (overduePage.value - 1) * 10
  return overdueList.value.slice(start, start + 10)
})

const overdueStats = computed(() => {
  const people = new Set(overdueList.value.map(r => r.userId))
  const totalFine = overdueList.value.reduce((sum, r) => {
    return sum + (r.fineAmount || 0)
  }, 0)
  return { peopleCount: people.size, bookCount: overdueList.value.length, totalFine: totalFine.toFixed(2) }
})

const overdueAllSelected = computed(() => {
  if (overduePaginated.value.length === 0) return false
  return overduePaginated.value.every(r => overdueSelectedIds.value.includes(r.recordId))
})

const overdueIndeterminate = computed(() => {
  if (overduePaginated.value.length === 0) return false
  const some = overduePaginated.value.some(r => overdueSelectedIds.value.includes(r.recordId))
  return some && !overdueAllSelected.value
})

function overdueToggle(row) {
  const idx = overdueSelectedIds.value.indexOf(row.recordId)
  if (idx >= 0) overdueSelectedIds.value.splice(idx, 1)
  else overdueSelectedIds.value.push(row.recordId)
}

function overdueToggleAll() {
  if (overdueAllSelected.value) {
    const ids = overduePaginated.value.map(r => r.recordId)
    overdueSelectedIds.value = overdueSelectedIds.value.filter(id => !ids.includes(id))
  } else {
    for (const r of overduePaginated.value) {
      if (!overdueSelectedIds.value.includes(r.recordId)) overdueSelectedIds.value.push(r.recordId)
    }
  }
}

async function remindItem(item) {
  try {
    await borrowApi.batchRemind([item.recordId])
    ElMessage.success(`已发送催还通知给 ${item.userName}`)
  } catch (e) {
    ElMessage.error('发送失败')
  }
}

async function batchRemind() {
  if (overdueSelectedIds.value.length === 0) return
  try {
    await borrowApi.batchRemind(overdueSelectedIds.value)
    ElMessage.success(`批量催还通知已发送，共 ${overdueSelectedIds.value.length} 条`)
  } catch (e) {
    ElMessage.error('发送失败')
  }
}

// ─── 借阅记录列表 ───
const today = new Date()

function daysBetween(dateStr) {
  const d = new Date(dateStr)
  return Math.floor((today.getTime() - d.getTime()) / (1000 * 60 * 60 * 24))
}

function overdueDays(r) {
  if (r.overdueDays !== undefined && r.overdueDays !== null) return r.overdueDays
  if (r.dueDate && new Date(r.dueDate) < new Date()) {
    return Math.max(0, daysBetween(r.dueDate))
  }
  return 0
}

function getFine(r) {
  if (r.fineAmount !== undefined && r.fineAmount !== null && r.fineAmount > 0) {
    return `¥${Number(r.fineAmount).toFixed(2)}`
  }
  const days = overdueDays(r)
  if (days > 0) return `¥${(days * 0.10).toFixed(2)}`
  return null
  return null
}

function getBarcode(r) {
  return r?.barcode || '—'
}

const showDetailModal = ref(false)
const detailRecord = ref(null)

function openDetailModal(r) {
  detailRecord.value = r
  showDetailModal.value = true
}

function viewRecord(r) {
  ElMessage.info(`记录详情\n记录ID：${r.recordId}\n图书：${r.bookTitle}\n读者：${r.userName}\n借书日期：${r.borrowDate}\n应还日期：${r.dueDate}`)
}

const filteredRecords = computed(() => {
  let records = [...allBorrowRecords.value]
  const q = searchQuery.value.trim().toLowerCase()
  if (q) {
    records = records.filter(r =>
      r.userName.toLowerCase().includes(q) ||
      r.bookTitle.toLowerCase().includes(q)
    )
  }
  const st = statusFilter.value
  if (st) {
    records = records.filter(r => r.status === parseInt(st))
  }
  const tf = timeFilter.value
  if (tf && tf !== 'all') {
    let cutoff
    const now = new Date()
    if (tf === 'week') cutoff = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
    else if (tf === 'month') cutoff = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
    else if (tf === 'semester') cutoff = new Date('2026-02-01')
    if (cutoff) records = records.filter(r => new Date(r.borrowDate) >= cutoff)
  }
  return records
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredRecords.value.length / 10)))

const paginatedRecords = computed(() => {
  if (currentPage.value > totalPages.value) currentPage.value = 1
  const start = (currentPage.value - 1) * 10
  return filteredRecords.value.slice(start, start + 10)
})

const displayPages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const pages = []
  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    pages.push(1)
    if (current > 3) pages.push('...')
    const start = Math.max(2, Math.min(current - 1, total - 4))
    const end = Math.min(total - 1, Math.max(current + 1, 5))
    for (let i = start; i <= end; i++) pages.push(i)
    if (current < total - 2) pages.push('...')
    pages.push(total)
  }
  return pages
})

// ─── 规则设置 Modal ───
const showSettingsModal = ref(false)
const settingsTab = ref('rules')
const rulesChanged = ref(false)
const holidayChanged = ref(false)

const settingsRules = ref([])

const summerSettings = ref({
  startDate: '2026-07-04',
  endDate: '2026-08-31',
  extensionDays: 40,
})

const winterSettings = ref({
  startDate: '2027-01-11',
  endDate: '2027-02-20',
  extensionDays: 30,
})

async function saveSettings() {
  try {
    if (settingsTab.value === 'rules') {
      const payload = settingsRules.value.map(({ _levelName, ...rest }) => rest)
      await configApi.updateBorrowConfigs(payload)
      rulesChanged.value = false
    }
    if (settingsTab.value === 'holiday') {
      holidayChanged.value = false
    }
    ElMessage.success('设置已保存')
    showSettingsModal.value = false
    // 刷新借阅规则数据
    try { const r = await configApi.getBorrowConfigs(); settingsRules.value = (r.data || []).map(c => ({ ...c, _levelName: c.educationLevel === 1 ? '高职' : c.educationLevel === 2 ? '本科' : c.educationLevel === 3 ? '硕士' : c.educationLevel === 4 ? '博士' : '教师' })) } catch(e) {}
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

// ─── 数据加载 ───
onMounted(() => { fetchData() })

async function fetchData() {
  try {
    const [borrowRes, configRes] = await Promise.all([
      borrowApi.list({ page: 1, size: 999 }),
      configApi.getBorrowConfigs()
    ])
    if (borrowRes.code === 200) {
      const raw = borrowRes.data?.records || borrowRes.data || []
      const roleMap = { 1: '高职', 2: '本科', 3: '硕士', 4: '博士' }
      allBorrowRecords.value = raw.map(r => ({
        ...r,
        recordId: r.id,
        userRole: roleMap[r.userRoleName] || r.userRoleName || '教师',
      }))
    }
    if (configRes.code === 200) {
      allBorrowConfig.value = configRes.data || []
      settingsRules.value = (configRes.data || []).map(r => ({
        ...r,
        _levelName: r.educationLevel === 1 ? '高职' : r.educationLevel === 2 ? '本科' : r.educationLevel === 3 ? '硕士' : r.educationLevel === 4 ? '博士' : '教师'
      }))
    }
  } catch (e) {
    ElMessage.error('数据加载失败')
  }
}
</script>

<style scoped>
.detail-cell {
  padding: 14px 18px;
  border-bottom: 1px solid #E8E2D8;
  border-right: 1px solid #E8E2D8;
  height: 100%;
  background: var(--acad-white);
  transition: background 0.15s;
}
.detail-cell-right {
  border-right: none;
}
/* 每行交替底色 */
.el-row:nth-child(even) .detail-cell {
  background: #FAF8F4;
}
/* 每行最后一条去掉底部边框（由容器统一控制） */
.el-row:last-child .detail-cell {
  border-bottom: none;
}
.detail-cell-top {
  border-top: 1px solid #E8E2D8;
}
.detail-label {
  display: block;
  font-size: 11px;
  color: var(--acad-stone);
  margin-bottom: 2px;
  line-height: 1.4;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}
.detail-value {
  font-size: 14px;
  color: var(--acad-charcoal);
  line-height: 1.5;
  word-break: break-all;
}
/* 关闭按钮 */
.detail-close-btn {
  background: var(--acad-ivory);
  border-color: var(--acad-card-border);
  color: var(--acad-charcoal);
  font-weight: 500;
}
.detail-close-btn:hover {
  background: #E5DCD0;
  color: var(--acad-navy);
}
</style>
