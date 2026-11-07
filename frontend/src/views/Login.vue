<template>
  <div class="login-page active">
    <el-card shadow="always" style="width:420px;border-radius:16px" :body-style="{ padding: '48px 40px' }">
      <div class="login-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
            <path d="M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h10v2H4v-2z"/>
            <path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2zm14 0v16H6V4h12z"/>
          </svg>
        </div>
        <h1>图书管理系统</h1>
        <p>集美大学 · 计算机工程学院</p>
      </div>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="密码登录" name="password">
          <el-form>
            <el-form-item label="手机号">
              <el-input v-model="loginPhone" placeholder="请输入绑定的手机号" maxlength="11" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input type="password" v-model="password" placeholder="请输入密码" show-password @keyup.enter="doPasswordLogin" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doPasswordLogin" :loading="loading" style="width:100%">
                <span class="icon">
                  <svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                  </svg>
                </span>
                登 录
              </el-button>
            </el-form-item>
          </el-form>
          <div v-if="passwordLoginError" style="margin-top:10px;font-size:12px;color:var(--acad-danger);text-align:center">{{ passwordLoginError }}</div>
        </el-tab-pane>
        <el-tab-pane label="短信验证" name="sms">
          <el-form>
            <el-form-item label="手机号">
              <el-input v-model="smsPhone" placeholder="请输入绑定的手机号" maxlength="11" />
            </el-form-item>
            <el-form-item label="验证码">
              <el-row :gutter="8" style="width:100%">
                <el-col :span="16">
                  <el-input v-model="smsCode" placeholder="请输入验证码" @keyup.enter="doSmsLogin" />
                </el-col>
                <el-col :span="8">
                  <el-button :disabled="smsCountdown > 0" @click="sendSmsCode" style="width:100%">
                    {{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}
                  </el-button>
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doSmsLogin" :loading="loading" style="width:100%">
                <span class="icon">
                  <svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                  </svg>
                </span>
                登 录
              </el-button>
            </el-form-item>
          </el-form>
          <div v-if="smsLoginError" style="margin-top:10px;font-size:12px;color:var(--acad-danger);text-align:center">{{ smsLoginError }}</div>
        </el-tab-pane>
      </el-tabs>

    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('password')
const loading = ref(false)

// 密码登录
const loginPhone = ref('')
const password = ref('')
const passwordLoginError = ref('')

// 短信登录
const smsPhone = ref('')
const smsCode = ref('')
const smsCountdown = ref(0)
const smsLoginError = ref('')
let smsTimer = null

async function doPasswordLogin() {
  passwordLoginError.value = ''
  if (!loginPhone.value.trim()) {
    passwordLoginError.value = '请输入手机号'
    return
  }
  if (!/^1[3-9]\d{9}$/.test(loginPhone.value.trim())) {
    passwordLoginError.value = '手机号格式不正确'
    return
  }
  if (!password.value) {
    passwordLoginError.value = '请输入密码'
    return
  }

  loading.value = true
  try {
    await authStore.login(loginPhone.value, password.value)
    redirectAfterLogin()
  } catch (e) {
    passwordLoginError.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}

async function doSmsLogin() {
  smsLoginError.value = ''
  if (!smsPhone.value.trim()) {
    smsLoginError.value = '请输入手机号'
    return
  }
  if (!/^1[3-9]\d{9}$/.test(smsPhone.value.trim())) {
    smsLoginError.value = '手机号格式不正确'
    return
  }
  if (!smsCode.value) {
    smsLoginError.value = '请输入验证码'
    return
  }

  loading.value = true
  try {
    await authStore.smsLogin(smsPhone.value, smsCode.value)
    redirectAfterLogin()
  } catch (e) {
    smsLoginError.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}

function redirectAfterLogin() {
  if (authStore.isAdmin) {
    router.push('/dashboard')
  } else {
    router.push('/catalog')
  }
}

async function sendSmsCode() {
  if (!smsPhone.value.trim()) return
  smsLoginError.value = ''
  try {
    await authApi.sendSmsCode(smsPhone.value)
    // 开始倒计时
    smsCountdown.value = 60
    if (smsTimer) clearInterval(smsTimer)
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
    ElMessage.success(`验证码已发送至 ${smsPhone.value}`)
  } catch (e) {
    smsLoginError.value = e.message || '发送失败'
  }
}
</script>
