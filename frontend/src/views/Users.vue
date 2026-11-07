<template>
  <div class="page" id="page-users">
    <div class="page-title">
      <h2>
        <span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg></span>
        用户管理
      </h2>
      <div>
        <el-button type="primary" @click="openAddModal">
          <el-icon style="vertical-align:middle;margin-right:4px"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg></el-icon> 新增用户
        </el-button>
      </div>
    </div>

    <div class="card">
      <el-tabs v-model="activeTab" class="custom-tabs" @tab-change="currentPage = 1">
        <el-tab-pane label="学生管理" name="student" />
        <el-tab-pane label="教师管理" name="teacher" />
      </el-tabs>

      <div class="toolbar">
        <el-input
          v-model="searchQuery"
          :placeholder="activeTab === 'student' ? '搜索学号 / 姓名...' : '搜索工号 / 姓名...'"
          clearable
          style="width:280px"
          @input="currentPage = 1"
        >
          <template #prefix>
            <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg></el-icon>
          </template>
        </el-input>

        <div class="filter-group">
          <el-select v-if="activeTab === 'student'" v-model="selectedEducation" placeholder="全部学历" clearable style="width:120px" @change="currentPage = 1">
            <el-option v-for="level in educationOptions" :key="level" :label="level" :value="level" />
          </el-select>
          <el-select v-model="selectedDepartment" placeholder="全部学院" clearable style="width:150px" @change="selectedMajor = ''; selectedClass = ''; currentPage = 1">
            <el-option v-for="dept in departmentOptions" :key="dept" :label="dept" :value="dept" />
          </el-select>
          <el-select v-if="activeTab === 'student'" v-model="selectedMajor" :disabled="!selectedDepartment" :placeholder="selectedDepartment ? '全部专业' : '请先选择学院'" clearable style="width:150px" @change="selectedClass = ''; currentPage = 1">
            <el-option v-for="m in filteredMajorOptions" :key="m" :label="m" :value="m" />
          </el-select>
          <el-select v-if="activeTab === 'student'" v-model="selectedClass" :disabled="!selectedMajor" :placeholder="selectedMajor ? '全部班级' : (selectedDepartment ? '请先选择专业' : '请先选择学院')" clearable style="width:150px" @change="currentPage = 1">
            <el-option v-for="cls in filteredClassOptions" :key="cls.classId" :label="cls.className" :value="cls.className" />
          </el-select>
          <el-button v-if="hasAnyFilter" size="small" text @click="clearAllFilters" :icon="ClearIcon" style="margin-left:4px">清空</el-button>
        </div>
      </div>

      <!-- 学生列表 -->
      <el-table v-if="activeTab === 'student'" :data="paginatedStudents" stripe style="width:100%" empty-text="暂无匹配的学生记录">
        <el-table-column prop="userId" label="学号" width="150" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="学历" width="100">
          <template #default="{ row }">
            <el-tag :type="eduTagType(row.educationLevelName)" size="small">{{ row.educationLevelName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="140" />
        <el-table-column prop="departmentName" label="学院" width="180" />
        <el-table-column prop="currentBorrows" label="借阅中" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="plain">
              {{ row.status === 1 ? '正常' : '超期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right">
          <template #default="{ row }">
            <div style="display:flex;gap:4px;justify-content:center">
              <el-button size="small" circle text @click="openEditModal(row)">
                <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg></el-icon>
              </el-button>
              <el-button size="small" circle text type="danger" @click="deleteUser(row)">
                <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 教师列表 -->
      <el-table v-if="activeTab === 'teacher'" :data="paginatedTeachers" stripe style="width:100%" empty-text="暂无匹配的教师记录">
        <el-table-column prop="userId" label="工号" width="130" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="学院" width="160">
          <template #default="{ row }">{{ row.departmentName || '—' }}</template>
        </el-table-column>
        <el-table-column label="专业" width="160">
          <template #default="{ row }">{{ row.majorName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="currentBorrows" label="借阅中" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="plain">
              {{ row.status === 1 ? '正常' : '超期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right">
          <template #default="{ row }">
            <div style="display:flex;gap:4px;justify-content:center">
              <el-button size="small" circle text @click="openEditModal(row)">
                <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg></el-icon>
              </el-button>
              <el-button size="small" circle text type="danger" @click="deleteUser(row)">
                <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <el-pagination
          v-if="totalPages > 0"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="activeTab === 'student' ? filteredStudents.length : filteredTeachers.length"
          layout="total, prev, pager, next"
          @current-change="currentPage = $event"
        />
      </div>
    </div>

    <!-- 新增 / 编辑 用户弹窗 -->
    <el-dialog v-model="showModal" :title="isEditing ? '编辑用户' : '新增用户'" width="560px" :close-on-click-modal="false" @close="closeModal">
      <el-form :model="form" label-position="top" style="padding:0 8px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="activeTab === 'student' ? '学号' : '工号'" required>
              <el-input v-model="form.userId" :placeholder="activeTab === 'student' ? '请输入学号' : '请输入工号'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" required>
              <el-input v-model="form.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="activeTab === 'student'">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="学历" required>
                <el-select v-model="form.educationLevel" placeholder="请选择学历" style="width:100%">
                  <el-option v-for="level in educationOptions" :key="level" :label="level" :value="level" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="学院" required>
                <el-select v-model="form.department" placeholder="请选择学院" style="width:100%" @change="onDeptChange">
                  <el-option v-for="dept in departmentOptions" :key="dept" :label="dept" :value="dept" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="专业" required>
                <el-select v-model="form.major" :disabled="!form.department" :placeholder="form.department ? '请选择专业' : '请先选择学院'" style="width:100%" @change="onMajorChange">
                  <el-option v-for="m in formMajorOptions" :key="m" :label="m" :value="m" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="班级" required>
                <el-select v-model="form.classInfo" :disabled="!form.major" :placeholder="form.major ? '请选择班级' : (form.department ? '请先选择专业' : '请先选择学院')" style="width:100%">
                  <el-option v-for="cls in formClassOptions" :key="cls.classId" :label="cls.className" :value="cls.className" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <template v-if="activeTab === 'teacher'">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="学院" required>
                <el-select v-model="form.tempDept" placeholder="请选择学院" style="width:100%" @change="onTeacherDeptChange">
                  <el-option v-for="d in deptList" :key="d.id" :label="d.name" :value="d.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="专业" required>
                <el-select v-model="form.majorId" :disabled="!form.tempDept" placeholder="请先选择学院" style="width:100%">
                  <el-option v-for="m in filteredMajors" :key="m.id" :label="m.name" :value="m.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-row v-if="isEditing" :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号" required>
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户状态">
              <el-select v-model="form.status" style="width:100%">
                <el-option label="正常" :value="1" />
                <el-option label="超期锁定" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-else label="手机号" required>
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="closeModal">取消</el-button>
        <el-button type="primary" @click="saveUser" :disabled="!isFormValid">
          {{ isEditing ? '保存修改' : '确认新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, h, onMounted } from 'vue'
import { studentApi } from '@/api/student'
import { teacherApi } from '@/api/teacher'
import { dictApi } from '@/api/dictionary'
import 'element-plus/dist/index.css'

// Element Plus 组件
import {
  ElButton, ElTabs, ElTabPane, ElInput, ElSelect, ElOption,
  ElTable, ElTableColumn, ElPagination, ElDialog, ElForm, ElFormItem,
  ElTag, ElIcon, ElRow, ElCol, ElMessageBox, ElMessage
} from 'element-plus'

const ClearIcon = {
  render() {
    return h('svg', { viewBox: '0 0 24 24', width: '1em', height: '1em', fill: 'currentColor' }, [
      h('path', { d: 'M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z' })
    ])
  }
}

const activeTab = ref('student')
const searchQuery = ref('')
const selectedEducation = ref('')
const selectedDepartment = ref('')
const selectedMajor = ref('')
const selectedClass = ref('')
const currentPage = ref(1)
const pageSize = 10

const allUsers = ref([])
const teacherData = ref([])
const allClasses = ref([])
const allMajors = ref([])

/** 教师编辑表单用的学院列表（从 allMajors 推导） */
const deptList = computed(() => {
  const deptMap = {}
  allMajors.value.forEach(m => {
    if (m.departmentId && !deptMap[m.departmentId]) {
      const names = { 1: '计算机工程学院', 2: '理学院', 3: '经济管理学院', 4: '外国语学院' }
      deptMap[m.departmentId] = { id: m.departmentId, name: names[m.departmentId] || '未知' }
    }
  })
  return Object.values(deptMap)
})

/** 根据选中的学院过滤专业 */
const filteredMajors = computed(() => {
  if (!form.value.tempDept) return []
  return allMajors.value.filter(m => m.departmentId === form.value.tempDept)
})

function onTeacherDeptChange() {
  form.value.majorId = null
}

const educationOptions = ['本科', '硕士', '博士', '高职']

const departmentOptions = computed(() => {
  return [...new Set(allClasses.value.map(c => c.department))].sort()
})

const filteredMajorOptions = computed(() => {
  if (!selectedDepartment.value) return []
  return [...new Set(allClasses.value
    .filter(c => c.department === selectedDepartment.value)
    .map(c => c.major)
  )].sort()
})

const filteredClassOptions = computed(() => {
  let classes = [...allClasses.value]
  if (selectedDepartment.value) {
    classes = classes.filter(c => c.department === selectedDepartment.value)
  }
  if (selectedMajor.value) {
    classes = classes.filter(c => c.major === selectedMajor.value)
  }
  return classes
})

const hasAnyFilter = computed(() =>
  selectedEducation.value || selectedDepartment.value || selectedMajor.value || selectedClass.value
)

function clearAllFilters() {
  selectedEducation.value = ''
  selectedDepartment.value = ''
  selectedMajor.value = ''
  selectedClass.value = ''
}

function filterBySearch(users) {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return users
  return users.filter(u =>
    u.name.toLowerCase().includes(q) ||
    u.userId.toLowerCase().includes(q)
  )
}

function filterByEducation(users) {
  if (!selectedEducation.value) return users
  return users.filter(u => u.educationLevelName === selectedEducation.value)
}

function filterByDepartment(users) {
  if (!selectedDepartment.value) return users
  return users.filter(u => u.departmentName === selectedDepartment.value)
}

function filterByMajor(users) {
  if (!selectedMajor.value) return users
  return users.filter(u => u.majorName === selectedMajor.value)
}

function filterByClass(users) {
  if (!selectedClass.value) return users
  return users.filter(u => u.className === selectedClass.value)
}

const studentList = computed(() => allUsers.value)

const filteredStudents = computed(() => {
  let result = studentList.value
  result = filterBySearch(result)
  result = filterByEducation(result)
  result = filterByDepartment(result)
  result = filterByMajor(result)
  result = filterByClass(result)
  return result
})

const teacherList = computed(() => teacherData.value)

const filteredTeachers = computed(() => {
  let result = teacherList.value
  result = filterBySearch(result)
  result = filterByDepartment(result)
  return result
})

const totalPages = computed(() => {
  const total = activeTab.value === 'student' ? filteredStudents.value.length : filteredTeachers.value.length
  return Math.ceil(total / pageSize) || 1
})

const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredStudents.value.slice(start, start + pageSize)
})

const paginatedTeachers = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredTeachers.value.slice(start, start + pageSize)
})

function eduTagType(level) {
  const map = { '本科': '', '硕士': 'info', '博士': 'warning', '高职': 'danger' }
  return map[level] || 'info'
}

// ─── Modal State ───
const showModal = ref(false)
const isEditing = ref(false)
const editingUserId = ref('')
const editingDbId = ref(null)

const defaultForm = () => ({
  userId: '',
  realName: '',
  educationLevel: '',
  department: '',
  major: '',
  classInfo: '',
  majorId: null,
  tempDept: null,
  phone: '',
  status: 1,
})

const form = ref(defaultForm())

const formMajorOptions = computed(() => {
  if (!form.value.department) return []
  return [...new Set(allClasses.value
    .filter(c => c.department === form.value.department)
    .map(c => c.major)
  )].sort()
})

const formClassOptions = computed(() => {
  if (!form.value.department) return []
  let classes = allClasses.value.filter(c => c.department === form.value.department)
  if (form.value.major) {
    classes = classes.filter(c => c.major === form.value.major)
  }
  return classes
})

const isFormValid = computed(() => {
  if (!form.value.userId || !form.value.realName) return false
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) return false
  if (activeTab.value === 'student') {
    if (!form.value.educationLevel || !form.value.department || !form.value.major || !form.value.classInfo) return false
  } else {
    if (!form.value.tempDept || !form.value.majorId) return false
  }
  return true
})

function onDeptChange() {
  form.value.major = ''
  form.value.classInfo = ''
}

function onMajorChange() {
  form.value.classInfo = ''
}

function openAddModal() {
  isEditing.value = false
  editingUserId.value = ''
  editingDbId.value = null
  form.value = defaultForm()
  showModal.value = true
}

function openEditModal(user) {
  isEditing.value = true
  editingUserId.value = user.userId
  editingDbId.value = user.id
  form.value = {
    userId: user.userId,
    realName: user.name,
    educationLevel: user.educationLevelName || '',
    department: user.departmentName,
    major: user.majorName || '',
    classInfo: user.className || '',
    majorId: user.majorId || null,
    tempDept: user.majorId ? (allMajors.value.find(m => m.id === user.majorId)?.departmentId || null) : null,
    phone: user.phone || '',
    status: user.status || 1,
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  form.value = defaultForm()
}

async function saveUser() {
  if (!isFormValid.value) return

  const role = activeTab.value === 'student' ? 1 : 2
  const eduMap = { '高职': 1, '本科': 2, '硕士': 3, '博士': 4 }
  const isStudent = activeTab.value === 'student'

  try {
    if (isEditing.value) {
      if (isStudent) {
        const matchedClass = allClasses.value.find(c => c.className === form.value.classInfo)
        await studentApi.update(editingDbId.value, {
          userId: form.value.userId, name: form.value.realName,
          educationLevel: eduMap[form.value.educationLevel] || null,
          classId: matchedClass ? matchedClass.classId : null,
          phone: form.value.phone, status: form.value.status,
        })
      } else {
        await teacherApi.update(editingDbId.value, {
          name: form.value.realName,
          majorId: form.value.majorId || null,
          phone: form.value.phone, status: form.value.status,
        })
      }
      ElMessage.success('用户信息已更新')
    } else {
      if (isStudent) {
        const matchedClass = allClasses.value.find(c => c.className === form.value.classInfo)
        await studentApi.add({
          userId: form.value.userId, name: form.value.realName,
          educationLevel: eduMap[form.value.educationLevel] || null,
          classId: matchedClass ? matchedClass.classId : null,
          phone: form.value.phone, status: form.value.status,
          password: '123456',
        })
      } else {
        await teacherApi.add({
          userId: form.value.userId, name: form.value.realName,
          majorId: form.value.majorId || null,
          phone: form.value.phone, status: form.value.status,
          password: '123456',
        })
      }
      ElMessage.success('用户已添加')
    }
    closeModal()
    if (isStudent) {
      await fetchUsers()
    } else {
      await fetchTeachers()
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function deleteUser(user) {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${user.name}（${user.userId}）"吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  try {
    if (activeTab.value === 'teacher') {
      await teacherApi.delete(user.id)
    } else {
      await studentApi.delete(user.id)
    }
    ElMessage.success('用户已删除')
    if (activeTab.value === 'student') {
      await fetchUsers()
    } else {
      await fetchTeachers()
    }
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

// ─── API 数据获取 ───

async function fetchUsers() {
  try {
    const res = await studentApi.list({ page: 1, size: 999 })
    allUsers.value = res.data?.records || []
  } catch (e) {
    console.error('获取学生列表失败', e)
  }
}

async function fetchTeachers() {
  try {
    const res = await teacherApi.list({ page: 1, size: 999 })
    teacherData.value = res.data?.records || []
  } catch (e) {
    console.error('获取教师列表失败', e)
  }
}

async function fetchClasses() {
  try {
    const [clsRes, majRes] = await Promise.all([
      dictApi.getAllClasses(),
      dictApi.getMajors()
    ])
    allClasses.value = clsRes.data || clsRes.records || clsRes || []
    allMajors.value = majRes.data || majRes.records || majRes || []
  } catch (e) {
    console.error('获取班级/专业列表失败', e)
  }
}

onMounted(() => { fetchUsers(); fetchTeachers(); fetchClasses() })

watch([activeTab, searchQuery, selectedEducation, selectedDepartment, selectedMajor, selectedClass], () => {
  currentPage.value = 1
})
</script>

<style scoped>
/* 让 Element Plus 的标签页视觉上接近原来的风格 */
.custom-tabs {
  margin-bottom: 20px;
}
.custom-tabs :deep(.el-tabs__header) {
  margin: 0 0 20px 0;
  border-bottom: 2px solid var(--acad-ivory);
}
.custom-tabs :deep(.el-tabs__item) {
  font-size: 13px;
  font-weight: 500;
  color: var(--acad-stone);
  padding: 10px 20px;
  height: auto;
  line-height: 1.5;
}
.custom-tabs :deep(.el-tabs__item.is-active) {
  color: var(--acad-navy);
  font-weight: 600;
}
.custom-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--acad-gold);
  height: 2px;
}
.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}
/* 搜索框和筛选行布局 */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.filter-group {
  display: flex;
  gap: 8px;
  align-items: center;
  flex: 1;
}
/* 弹窗内表单项间距 */
:deep(.el-form-item) {
  margin-bottom: 18px;
}
:deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: var(--acad-charcoal);
  padding-bottom: 6px;
  line-height: 1.4;
}
</style>
