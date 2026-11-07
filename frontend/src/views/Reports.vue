<template>
  <div class="page" id="page-reports">
    <div class="page-title">
      <h2><span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/></svg></span> 统计报表</h2>
      <div>
        <el-button @click="exportExcel"><el-icon><Download /></el-icon> 导出 Excel</el-button>
      </div>
    </div>

    <!-- 筛选 -->
    <el-card shadow="never" :body-style="{ padding: '16px 24px' }" style="margin-bottom:20px">
      <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
        <el-select v-model="selectedSemester" placeholder="选择学期" style="width:200px">
          <el-option v-for="s in semesterOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-button size="small" text @click="showFilters = !showFilters" style="flex-shrink:0">
          <el-icon><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z"/></svg></el-icon>
          {{ showFilters ? '收起' : '筛选' }}
        </el-button>
        <template v-if="showFilters">
          <el-select v-model="selectedDepartment" placeholder="全部学院" clearable style="width:150px" @change="selectedMajor=''; selectedClass=''">
            <el-option label="全部学院" value="" />
            <el-option v-for="d in departmentOptions" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
          <el-select v-model="selectedMajor" :disabled="!selectedDepartment" placeholder="全部专业" clearable style="width:150px" @change="selectedClass=''">
            <el-option label="全部专业" value="" />
            <el-option v-for="m in filteredMajorOptions" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
          <el-select v-model="selectedClass" :disabled="!selectedMajor" placeholder="全部班级" clearable style="width:150px">
            <el-option label="全部班级" value="" />
            <el-option v-for="cls in filteredClassOptions" :key="cls.id" :label="cls.name" :value="cls.name" />
          </el-select>
        </template>
        <span style="font-size:12px;color:var(--acad-stone);margin-left:auto">数据周期：{{ selectedSemester ? getSemesterRange(selectedSemester) : '—' }}</span>
      </div>
    </el-card>

    <!-- 图表区 -->
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span style="font-weight:600;font-size:15px"><el-icon style="vertical-align:-2px;margin-right:6px"><TrendCharts /></el-icon>月度借阅趋势</span></template>
          <div ref="barChartRef" style="width:100%;height:260px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span style="font-weight:600;font-size:15px"><el-icon style="vertical-align:-2px;margin-right:6px"><PieChart /></el-icon>图书分类分布</span></template>
          <div ref="pieChartRef" style="width:100%;height:280px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 概览 -->
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="6" v-for="stat in overviewStats" :key="stat.label">
        <el-card shadow="never" :body-style="{ padding: '18px 22px', cursor: 'default' }">
          <div style="display:flex;align-items:center;gap:14px">
            <div :style="{ width:'46px',height:'46px',borderRadius:'12px', display:'inline-flex',alignItems:'center',justifyContent:'center', background: stat.bg, color: stat.color, flexShrink:0 }">
              <svg v-if="stat.icon === 'bar-chart'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/></svg>
              <svg v-else-if="stat.icon === 'users'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>
              <svg v-else-if="stat.icon === 'book'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h10v2H4v-2z"/><path d="M4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2zm14 0v16H6V4h12z"/></svg>
              <svg v-else viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
            </div>
            <div>
              <div style="font-size:12px;color:var(--acad-stone);margin-bottom:2px">{{ stat.label }}</div>
              <div style="font-size:24px;font-weight:700;color:var(--acad-navy);line-height:1.2">{{ stat.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { reportApi } from '@/api/report'
import { dictApi } from '@/api/dictionary'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const selectedSemester = ref('2025-2')
const showFilters = ref(false)
const selectedDepartment = ref('')
const selectedMajor = ref('')
const selectedClass = ref('')

const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChartInstance = null
let pieChartInstance = null

const chartColors = ['#4E7A9E','#C49B5E','#3D7A56','#B04A46','#8B6FAF','#C99D41','#4A90D9','#D4836F']

const semesterOptions = [
  { value: '2025-2', label: '2025-2026 学年第二学期' },
  { value: '2025-1', label: '2025-2026 学年第一学期' },
  { value: '2024-2', label: '2024-2025 学年第二学期' },
]
const semesterDateRanges = {
  '2025-2': { start: '2026-03-01', end: '2026-07-15', label: '2026.03 — 2026.07' },
  '2025-1': { start: '2025-09-01', end: '2026-01-15', label: '2025.09 — 2026.01' },
  '2024-2': { start: '2025-03-01', end: '2025-07-15', label: '2025.03 — 2025.07' },
}
function getSemesterRange(value) { return semesterDateRanges[value]?.label || '—' }

// API data refs
const overviewStats = ref([])
const monthlyTrend = ref({ months: [], counts: [], overdueCounts: [] })
const categoryDist = ref([])
const departmentOptions = ref([])
const filteredMajorOptions = ref([])
const filteredClassOptions = ref([])

// Load major options when department changes
watch(selectedDepartment, async (dept) => {
  selectedMajor.value = ''
  selectedClass.value = ''
  filteredClassOptions.value = []
  if (dept) {
    try {
      const res = await dictApi.getMajors(dept)
      filteredMajorOptions.value = res.data || []
    } catch {
      filteredMajorOptions.value = []
    }
  } else {
    filteredMajorOptions.value = []
  }
})

// Load class options when major changes
watch(selectedMajor, async (major) => {
  selectedClass.value = ''
  if (major) {
    try {
      const res = await dictApi.getClasses(major)
      filteredClassOptions.value = res.data || []
    } catch {
      filteredClassOptions.value = []
    }
  } else {
    filteredClassOptions.value = []
  }
})

async function loadAllData() {
  try {
    const params = {}
    // 学期日期筛选
    const range = semesterDateRanges[selectedSemester.value]
    if (range) {
      params.startDate = range.start
      params.endDate = range.end
    }
    if (selectedDepartment.value) params.departmentId = selectedDepartment.value
    if (selectedMajor.value) params.majorId = selectedMajor.value
    if (selectedClass.value) params.className = selectedClass.value

    const [overviewRes, trendRes, catRes, deptRes] = await Promise.all([
      reportApi.getOverview(params),
      reportApi.getMonthlyTrend(params),
      reportApi.getCategoryDistribution(params),
      dictApi.getDepartments()
    ])
    // 转换概览数据为卡片格式
    const raw = overviewRes.data || {}
    const colors = ['#E8EDF5', '#F5EDE0', '#E6F0E8', '#FDF0EF']
    const icons = ['bar-chart', 'users', 'book', 'alert']
    overviewStats.value = [
      { label: '借阅总量', value: (raw.totalBorrows || 0) + ' 次', icon: icons[0], bg: colors[0], color: '#12203E' },
      { label: '借阅人数', value: (raw.totalUsers || 0) + ' 人', icon: icons[1], bg: colors[1], color: '#C49B5E' },
      { label: '涉及图书', value: (raw.totalBooks || 0) + ' 种', icon: icons[2], bg: colors[2], color: '#3D7A56' },
      { label: '超期率', value: raw.overdueRate || '0%', icon: icons[3], bg: colors[3], color: '#B04A46' },
    ]
    monthlyTrend.value = trendRes.data || { months: [], counts: [], overdueCounts: [] }
    categoryDist.value = catRes.data || []
    departmentOptions.value = deptRes.data || []
    updateCharts()
  } catch (e) {
    console.error('Failed to load report data', e)
    ElMessage.error('加载报表数据失败')
  }
}

function initCharts() {
  if (barChartRef.value) {
    barChartInstance = echarts.init(barChartRef.value)
  }
  if (pieChartRef.value) {
    pieChartInstance = echarts.init(pieChartRef.value)
  }
  updateCharts()
}

function updateCharts() {
  if (barChartInstance) {
    barChartInstance.setOption({
      legend: { data: ['借阅量', '超期量'], top: 0, left: 'center', textStyle: { color: '#6B7280', fontSize: 12, fontWeight: 500 } },
      tooltip: { trigger: 'axis' },
      grid: { left: '8%', right: '5%', bottom: '12%', top: '20%', containLabel: true },
      xAxis: { type: 'category', data: monthlyTrend.value.months, axisLine: { lineStyle: { color: '#E0D9CE' } }, axisLabel: { color: '#6B7280', fontWeight: 500 } },
      yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#EDE8E0', type: 'dashed' } }, axisLabel: { color: '#6B7280' } },
      series: [
        { type: 'bar', name: '借阅量', data: monthlyTrend.value.counts, itemStyle: { borderRadius: [6,6,0,0], color: new echarts.graphic.LinearGradient(0,0,0,1, [{offset:0,color:'#6B8FBF'},{offset:1,color:'#A8C5E0'}]) }, emphasis: { itemStyle: { color: '#233B6E' } }, label: { show: true, position: 'top', color: '#252A2E', fontWeight: 600, fontSize: 12 } },
        { type: 'line', name: '超期量', data: monthlyTrend.value.overdueCounts, smooth: true, symbol: 'circle', symbolSize: 8, lineStyle: { width: 2, color: '#B04A46' }, itemStyle: { color: '#B04A46' }, label: { show: true, position: 'top', color: '#B04A46', fontWeight: 600, fontSize: 11, formatter: '{c}' } }
      ]
    })
  }
  if (pieChartInstance) {
    pieChartInstance.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} 册 ({d}%)' },
      series: [{ type: 'pie', radius: ['45%','70%'], avoidLabelOverlap: true, label: { show: true, formatter: '{b}\n{d}%', color: '#252A2E', fontSize: 12, fontWeight: 500, lineHeight: 18 }, emphasis: { label: { fontSize: 14, fontWeight: 700 }, itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.15)' } }, labelLine: { lineStyle: { color: '#E0D9CE' } }, data: categoryDist.value.map(d => ({ name: d.name, value: d.count })) }]
    })
  }
}

function handleResize() { barChartInstance?.resize(); pieChartInstance?.resize() }

onMounted(() => {
  loadAllData()
  setTimeout(() => {
    initCharts()
    window.addEventListener('resize', handleResize)
  }, 100)
})

onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); barChartInstance?.dispose(); pieChartInstance?.dispose() })

watch([selectedSemester, selectedDepartment, selectedMajor, selectedClass], () => { loadAllData() })

async function exportExcel() {
  const params = {}
  const range = semesterDateRanges[selectedSemester.value]
  if (range) { params.startDate = range.start; params.endDate = range.end }
  if (selectedDepartment.value) params.departmentId = selectedDepartment.value
  if (selectedMajor.value) params.majorId = selectedMajor.value
  if (selectedClass.value) params.className = selectedClass.value
  try {
    const res = await reportApi.exportExcel(params)
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }))
    const a = document.createElement('a'); a.href = url; a.download = '图书管理报表.xlsx'; a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) { ElMessage.error('导出失败') }
}
</script>
