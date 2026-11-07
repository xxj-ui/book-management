<template>
  <div class="page">
    <div class="page-title">
      <h2><span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg></span> 个人中心</h2>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：用户卡片 -->
      <el-col :span="7">
        <el-card shadow="never" :body-style="{ padding: '28px 24px', textAlign: 'center' }">
          <el-avatar :size="72" style="background:var(--acad-navy);color:var(--acad-gold-light);font-size:30px;font-weight:700;margin:0 auto 12px">{{ user.name?.charAt(0) }}</el-avatar>
          <h3 style="font-size:20px;font-weight:700;color:var(--acad-navy);margin-bottom:2px">{{ user.name }}</h3>
          <div style="font-size:12px;color:var(--acad-stone);margin-bottom:12px">{{ user.id }}</div>
          <div style="margin-bottom:16px;display:flex;gap:6px;justify-content:center;flex-wrap:wrap">
            <el-tag v-if="user.educationLevel" size="small" effect="plain">{{ user.educationLevel }}</el-tag>
            <el-tag v-else-if="user.role==='teacher'" size="small" effect="plain">教师</el-tag>
          </div>
          <el-divider style="margin:12px 0" />
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
            <div style="background:var(--acad-ivory);border-radius:8px;padding:12px 8px">
              <div style="font-size:22px;font-weight:700;color:var(--acad-navy)">{{ currentBorrows.length }}</div>
              <div style="font-size:11px;color:var(--acad-stone)">当前借阅</div>
            </div>
            <div style="background:var(--acad-ivory);border-radius:8px;padding:12px 8px">
              <div style="font-size:22px;font-weight:700;color:var(--acad-navy)">{{ remaining }}</div>
              <div style="font-size:11px;color:var(--acad-stone)">可借册数</div>
            </div>
          </div>
          <el-divider style="margin:16px 0" />
          <div style="text-align:left;font-size:13px">
            <div style="display:flex;justify-content:space-between;padding:6px 0">
              <span style="color:var(--acad-stone)">学院</span>
              <span style="color:var(--acad-charcoal);font-weight:500">{{ user.department || user.departmentName || '—' }}</span>
            </div>
            <div style="display:flex;justify-content:space-between;padding:6px 0;border-top:1px solid var(--acad-ivory)">
              <span style="color:var(--acad-stone)">班级/专业</span>
              <span style="color:var(--acad-charcoal);font-weight:500">{{ user.className || user.major || '—' }}</span>
            </div>
            <div style="display:flex;justify-content:space-between;padding:6px 0;border-top:1px solid var(--acad-ivory)">
              <span style="color:var(--acad-stone)">累计借阅</span><span style="color:var(--acad-charcoal);font-weight:500">{{ user.totalBorrows || 0 }} 册</span>
            </div>
            <div style="display:flex;justify-content:space-between;padding:6px 0;border-top:1px solid var(--acad-ivory)">
              <span style="color:var(--acad-stone)">读者状态</span>
              <el-tag :type="user.status==='active'?'success':'danger'" size="small" effect="plain">{{ user.status==='active'?'正常':'超期锁定' }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：标签页 -->
      <el-col :span="17">
        <el-card shadow="never" :body-style="{ padding: '0px' }">
          <el-tabs v-model="activeTab" style="padding:0 24px">
            <!-- 借阅权限 -->
            <el-tab-pane label="借阅权限" name="borrow-info">
              <div style="padding:4px 0">
                <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:20px">
                  <div style="background:var(--acad-ivory);border-radius:10px;padding:14px 16px">
                    <div style="font-size:11px;color:var(--acad-stone);margin-bottom:4px">最大借阅册数</div>
                    <div style="font-size:20px;font-weight:700;color:var(--acad-navy)">{{ borrowCfg.maxBooks }} <span style="font-size:12px;font-weight:400;color:var(--acad-stone)">册</span></div>
                  </div>
                  <div style="background:var(--acad-ivory);border-radius:10px;padding:14px 16px">
                    <div style="font-size:11px;color:var(--acad-stone);margin-bottom:4px">最长借阅天数</div>
                    <div style="font-size:20px;font-weight:700;color:var(--acad-navy)">{{ borrowCfg.maxDays }} <span style="font-size:12px;font-weight:400;color:var(--acad-stone)">天</span></div>
                  </div>
                  <div style="background:var(--acad-ivory);border-radius:10px;padding:14px 16px">
                    <div style="font-size:11px;color:var(--acad-stone);margin-bottom:4px">超期罚金</div>
                    <div style="font-size:20px;font-weight:700;color:var(--acad-navy)">{{ borrowCfg.finePerDay }} <span style="font-size:12px;font-weight:400;color:var(--acad-stone)">元/天</span></div>
                  </div>
                </div>

                <div style="font-size:14px;font-weight:600;color:var(--acad-navy);margin-bottom:12px;display:flex;align-items:center;gap:8px">
                  <el-icon><Reading /></el-icon> 当前借阅图书
                </div>
                <el-table :data="currentBorrows" stripe style="width:100%" empty-text="暂无借阅">
                  <el-table-column label="图书" min-width="160">
                    <template #default="{ row }">
                      <el-link type="primary" @click="$router.push('/book-detail/' + row.isbn)">{{ row.bookTitle }}</el-link>
                    </template>
                  </el-table-column>
                  <el-table-column prop="borrowDate" label="借书日期" width="120" />
                  <el-table-column prop="dueDate" label="应还日期" width="120" />
                  <el-table-column label="状态" width="80">
                    <template #default="{ row }">
                      <el-tag v-if="row.overdueDays>0" type="danger" size="small" effect="plain">超期</el-tag>
                      <el-tag v-else-if="row.status===4" type="warning" size="small" effect="plain">待审核</el-tag>
                      <el-tag v-else-if="row.status===2" type="info" size="small" effect="plain">已归还</el-tag>
                      <el-tag v-else type="success" size="small" effect="plain">正常</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="100">
                    <template #default="{ row }">
                      <el-button v-if="row.status===4" size="small" type="warning" link disabled>待审核</el-button>
                      <el-button v-else-if="row.status===2" size="small" type="info" link disabled>已归还</el-button>
                      <el-button v-else size="small" type="primary" link @click="returnBook(row)">归还</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-tab-pane>

            <!-- 借阅历史 -->
            <el-tab-pane label="借阅历史" name="history">
              <div style="padding:4px 0">
                <el-table :data="fullBorrowHistory" stripe style="width:100%" empty-text="暂无借阅记录">
                  <el-table-column prop="bookTitle" label="图书" min-width="160" />
                  <el-table-column prop="borrowDate" label="借书日期" width="120" />
                  <el-table-column prop="dueDate" label="应还日期" width="120" />
                  <el-table-column prop="returnDate" label="还书日期" width="120">
                    <template #default="{ row }">{{ row.returnDate || '—' }}</template>
                  </el-table-column>
                </el-table>
              </div>
            </el-tab-pane>

            <!-- 待缴费清单 -->
            <el-tab-pane label="待缴费清单" name="fines">
              <div style="padding:4px 0">
                <el-table :data="finesList" stripe style="width:100%" empty-text="暂无待缴费用">
                  <el-table-column prop="bookTitle" label="图书" min-width="160" />
                  <el-table-column prop="fineAmount" label="滞纳金" width="120" align="center">
                    <template #default="{ row }">¥{{ Number(row.fineAmount).toFixed(2) }}</template>
                  </el-table-column>
                  <el-table-column label="状态" width="80">
                    <template #default="{ row }">
                      <el-tag type="warning" size="small">未支付</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="80">
                    <template #default="{ row }">
                      <el-button size="small" type="primary" link @click="payFine(row)">缴费</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-tab-pane>

            <!-- 安全设置 -->
            <el-tab-pane label="安全设置" name="security">
              <div style="display:flex;gap:16px;padding:8px 0">
                <el-card shadow="hover" :body-style="{ padding: '24px', cursor:'pointer' }" @click="showPwdDialog = true" style="flex:1;text-align:center">
                  <el-icon :size="32" style="color:var(--acad-navy);margin-bottom:8px"><EditPen /></el-icon>
                  <div style="font-size:14px;font-weight:600;color:var(--acad-navy)">修改密码</div>
                  <div style="font-size:12px;color:var(--acad-stone);margin-top:4px">需要验证当前密码和短信验证码</div>
                </el-card>
                <el-card shadow="hover" :body-style="{ padding: '24px', cursor:'pointer' }" @click="showPhoneDialog = true" style="flex:1;text-align:center">
                  <el-icon :size="32" style="color:var(--acad-gold);margin-bottom:8px"><Iphone /></el-icon>
                  <div style="font-size:14px;font-weight:600;color:var(--acad-navy)">修改手机号</div>
                  <div style="font-size:12px;color:var(--acad-stone);margin-top:4px">需要验证短信验证码</div>
                </el-card>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <!-- 密码弹窗 -->
        <el-dialog v-model="showPwdDialog" title="修改密码" width="420px" :close-on-click-modal="false">
          <template v-if="pwdStep === 1">
            <p style="font-size:13px;color:var(--acad-stone);margin-bottom:16px">请先验证身份</p>
            <el-form label-position="top">
              <el-form-item label="当前手机号"><el-input :model-value="currentPhone" disabled /></el-form-item>
              <el-form-item label="当前密码"><el-input v-model="pwdForm.current" type="password" placeholder="请输入当前密码" show-password /></el-form-item>
              <el-form-item label="短信验证码">
                <div style="display:flex;gap:8px">
                  <el-input v-model="pwdForm.code" placeholder="请输入验证码" style="flex:1" />
                  <el-button :disabled="pwdSmsCountdown > 0" @click="sendPwdSms" style="width:110px">{{ pwdSmsCountdown > 0 ? pwdSmsCountdown + 's' : '获取验证码' }}</el-button>
                </div>
              </el-form-item>
            </el-form>
          </template>
          <template v-if="pwdStep === 2">
            <p style="font-size:13px;color:var(--acad-success);margin-bottom:16px">✅ 身份验证通过，请设置新密码</p>
            <el-form label-position="top">
              <el-row :gutter="12">
                <el-col :span="12"><el-form-item label="新密码"><el-input v-model="pwdForm.newPwd" type="password" placeholder="请输入新密码" show-password /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="确认新密码"><el-input v-model="pwdForm.confirm" type="password" placeholder="请再次输入" show-password /></el-form-item></el-col>
              </el-row>
            </el-form>
          </template>
          <template #footer>
            <el-button @click="showPwdDialog = false">取消</el-button>
            <el-button v-if="pwdStep === 1" type="primary" :loading="pwdVerifying" @click="verifyPwd" :disabled="!pwdForm.current || !pwdForm.code">验证身份</el-button>
            <el-button v-if="pwdStep === 2" type="primary" @click="changePwd" :disabled="!pwdForm.newPwd || !pwdForm.confirm">确认修改</el-button>
          </template>
        </el-dialog>

        <!-- 手机号弹窗 -->
        <el-dialog v-model="showPhoneDialog" title="修改手机号" width="420px" :close-on-click-modal="false">
          <template v-if="phoneStep === 1">
            <p style="font-size:13px;color:var(--acad-stone);margin-bottom:16px">请先验证身份</p>
            <el-form label-position="top">
              <el-form-item label="当前手机号"><el-input :model-value="currentPhone" disabled /></el-form-item>
              <el-form-item label="短信验证码">
                <div style="display:flex;gap:8px"><el-input v-model="phoneForm.code" placeholder="请输入验证码" style="flex:1" /><el-button :disabled="phoneSmsCountdown > 0" @click="sendPhoneSms" style="width:110px">{{ phoneSmsCountdown > 0 ? phoneSmsCountdown + 's' : '获取验证码' }}</el-button></div>
              </el-form-item>
            </el-form>
          </template>
          <template v-if="phoneStep === 2">
            <p style="font-size:13px;color:var(--acad-success);margin-bottom:16px">✅ 身份验证通过，请输入新手机号</p>
            <el-form label-position="top"><el-form-item label="新手机号"><el-input v-model="phoneForm.newPhone" placeholder="请输入新手机号" maxlength="11" /></el-form-item></el-form>
          </template>
          <template #footer>
            <el-button @click="showPhoneDialog = false">取消</el-button>
            <el-button v-if="phoneStep === 1" type="primary" :loading="phoneVerifying" @click="verifyPhone" :disabled="!phoneForm.code">验证身份</el-button>
            <el-button v-if="phoneStep === 2" type="primary" @click="changePhone" :disabled="!phoneForm.newPhone">确认修改</el-button>
          </template>
        </el-dialog>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { borrowApi } from '@/api/borrow'
import { authApi } from '@/api/auth'
import { configApi } from '@/api/config'
import { fineApi } from '@/api/fine'
import { ElMessage, ElMessageBox } from 'element-plus'

const authStore = useAuthStore()
const activeTab = ref('borrow-info')
const user = computed(() => authStore.user || {})

// ─── 数据 ───
const myBorrows = ref([])
const fullBorrowHistory = ref([])
const finesList = ref([])
const borrowCfg = ref({ maxBooks: 8, maxDays: 30, finePerDay: 0.10 })

const currentBorrows = computed(() => myBorrows.value.filter(r =>
  r.status === 1 || r.status === 3 || r.status === 4
))
const remaining = computed(() => (borrowCfg.value.maxBooks || 0) - currentBorrows.value.length)
const currentPhone = computed(() => authStore.user?.phone || '')

// ─── 归还 ───
async function returnBook(row) {
  try {
    await borrowApi.returnBook(row.recordId || row.id)
    ElMessage.success('归还申请已提交，等待管理员审核上架')
    await fetchData()
  } catch (e) { ElMessage.error(e.message || '归还失败') }
}

// ─── 缴费 ───
async function payFine(row) {
  try {
    await fineApi.pay(row.id)
    await ElMessageBox.alert(`缴费成功！\n图书：《${row.bookTitle}》\n金额：¥${Number(row.fineAmount).toFixed(2)}`, '缴费成功', { type: 'success', confirmButtonText: '确定' })
    finesList.value = finesList.value.filter(f => f !== row)
  } catch (e) { ElMessage.error(e.message || '缴费失败') }
}

// ─── 安全设置 ───
const showPwdDialog = ref(false)
const showPhoneDialog = ref(false)
const pwdStep = ref(1)
const phoneStep = ref(1)
const pwdForm = ref({ current: '', code: '', newPwd: '', confirm: '' })
const phoneForm = ref({ newPhone: '', code: '' })
const pwdSmsCountdown = ref(0)
const phoneSmsCountdown = ref(0)
const pwdVerifying = ref(false)
const phoneVerifying = ref(false)
let pwdTimer = null, phoneTimer = null

async function sendPwdSms() {
  try {
    await authApi.sendSmsCode(currentPhone.value)
    pwdSmsCountdown.value = 60
    if (pwdTimer) clearInterval(pwdTimer)
    pwdTimer = setInterval(() => { pwdSmsCountdown.value--; if (pwdSmsCountdown.value <= 0) { clearInterval(pwdTimer); pwdTimer = null } }, 1000)
    ElMessage.success('验证码已发送至绑定手机')
  } catch (e) { ElMessage.error(e.message || '发送失败') }
}
async function sendPhoneSms() {
  try {
    await authApi.sendSmsCode(currentPhone.value)
    phoneSmsCountdown.value = 60
    if (phoneTimer) clearInterval(phoneTimer)
    phoneTimer = setInterval(() => { phoneSmsCountdown.value--; if (phoneSmsCountdown.value <= 0) { clearInterval(phoneTimer); phoneTimer = null } }, 1000)
    ElMessage.success('验证码已发送至绑定手机')
  } catch (e) { ElMessage.error(e.message || '发送失败') }
}
async function verifyPwd() {
  if (!pwdForm.value.current) { ElMessage.warning('请输入当前密码'); return }
  if (!pwdForm.value.code) { ElMessage.warning('请输入验证码'); return }
  pwdVerifying.value = true
  try {
    await authApi.verifyIdentity({ currentPassword: pwdForm.value.current, verifyCode: pwdForm.value.code })
    pwdStep.value = 2
  } catch (e) { ElMessage.error(e.message || '身份验证失败') }
  finally { pwdVerifying.value = false }
}
async function changePwd() {
  if (pwdForm.value.newPwd !== pwdForm.value.confirm) { ElMessage.warning('两次输入的密码不一致'); return }
  if (pwdForm.value.newPwd.length < 6) { ElMessage.warning('密码长度不能少于6位'); return }
  try {
    await authApi.changePassword({ currentPassword: pwdForm.value.current, newPassword: pwdForm.value.newPwd, verifyCode: pwdForm.value.code })
    ElMessage.success('密码修改成功'); showPwdDialog.value = false; pwdStep.value = 1; pwdForm.value = { current: '', code: '', newPwd: '', confirm: '' }
  } catch (e) { ElMessage.error(e.message || '密码修改失败') }
}
async function verifyPhone() {
  if (!phoneForm.value.code) { ElMessage.warning('请输入验证码'); return }
  phoneVerifying.value = true
  try {
    await authApi.verifySmsCode({ verifyCode: phoneForm.value.code })
    phoneStep.value = 2
  } catch (e) { ElMessage.error(e.message || '身份验证失败') }
  finally { phoneVerifying.value = false }
}
async function changePhone() {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.value.newPhone)) { ElMessage.warning('请输入正确的手机号'); return }
  try {
    await authApi.changePhone({ newPhone: phoneForm.value.newPhone, verifyCode: phoneForm.value.code })
    ElMessage.success(`手机号已修改为 ${phoneForm.value.newPhone}`)
    if (authStore.user) {
      authStore.user.phone = phoneForm.value.newPhone
      sessionStorage.setItem('user', JSON.stringify(authStore.user))
    }
    showPhoneDialog.value = false; phoneStep.value = 1; phoneForm.value = { newPhone: '', code: '' }
  } catch (e) { ElMessage.error(e.message || '修改失败') }
}

// ─── 数据加载 ───
onMounted(() => { fetchData() })
async function fetchData() {
  try {
    // 获取最新用户信息（含状态）
    try {
      const userRes = await authApi.getUserInfo()
      if (userRes.data) {
        authStore.user.dbId = userRes.data.id
        authStore.user.status = userRes.data.status === 1 ? 'active' : userRes.data.status === 3 ? 'disabled' : 'overdue_locked'
        authStore.user.phone = userRes.data.phone
        authStore.user.name = userRes.data.name
        sessionStorage.setItem('user', JSON.stringify(authStore.user))
      }
    } catch (e) {}
    const userId = authStore.user?.dbId
    if (!userId) return
    const [borrowRes, configRes, fineRes] = await Promise.all([
      borrowApi.listByUser(userId),
      configApi.getBorrowConfigs(),
      fineApi.list()
    ])
    const records = borrowRes.data || []
    myBorrows.value = records
    fullBorrowHistory.value = [...records].sort((a, b) => ((b.borrowDate||'')).localeCompare(a.borrowDate||''))
    // 滞纳金数据（关联借阅记录获取书名）
    const rawFines = fineRes.data || []
    finesList.value = rawFines.map(f => {
      const rec = records.find(r => r.id === f.recordId)
      return { id: f.id, bookTitle: rec?.bookTitle || '未知图书', fineAmount: f.fineAmount }
    })
    const configs = configRes.data || []
    const matched = configs.find(c => {
      if (authStore.user?.role === 'teacher') return c.role === 2
      const eduMap = { '本科': 2, '硕士': 3, '博士': 4, '高职': 1 }
      return c.role === 1 && c.educationLevel === eduMap[authStore.user?.educationLevel]
    })
    if (matched) borrowCfg.value = matched
  } catch (e) { ElMessage.error('数据加载失败') }
}
</script>
