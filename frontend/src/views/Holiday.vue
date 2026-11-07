<template>
  <div class="page" id="page-holiday">
    <div class="page-title">
      <h2><span class="icon"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zm0-12H5V6h14v2zM9 14H7v-2h2v2zm4 0h-2v-2h2v2zm4 0h-2v-2h2v2zm-8 4H7v-2h2v2zm4 0h-2v-2h2v2zm4 0h-2v-2h2v2z"/></svg></span> 寒暑假设置</h2>
      <div>
        <button class="btn btn-outline" @click="showHistoryModal = true"><span class="icon-btn"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M3 13h2v-2H3v2zm0 5h2v-2H3v2zm0-10h2V6H3v2zm4 4h14v-2H7v2zm0 5h14v-2H7v2zM7 6v2h14V6H7z"/></svg></span> 查看延期记录</button>
        <button class="btn btn-primary" style="margin-left:8px" @click="saveSettings"><span class="icon-btn"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z"/></svg></span> 保存设置</button>
      </div>
    </div>
    <div class="card" style="margin-bottom:16px">
      <div class="card-header"><h3>暑假延期设置</h3></div>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px">
        <div class="form-group"><label class="form-label">暑假起始日期</label><input class="form-input" type="date" v-model="summer.startDate"></div>
        <div class="form-group"><label class="form-label">暑假结束日期</label><input class="form-input" type="date" v-model="summer.endDate"></div>
        <div class="form-group"><label class="form-label">延期方式</label><select class="form-select" v-model="summer.extensionType"><option>统一延期至开学日（9月1日）</option><option>在到期日基础上 +40 天</option><option>在到期日基础上 +60 天</option></select></div>
        <div class="form-group"><label class="form-label">适用馆藏地</label><select class="form-select" v-model="summer.location"><option>全部馆藏地</option><option>嘉庚图书馆</option></select></div>
      </div>
    </div>
    <div class="card">
      <div class="card-header"><h3>寒假延期设置</h3></div>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px">
        <div class="form-group"><label class="form-label">寒假起始日期</label><input class="form-input" type="date" v-model="winter.startDate"></div>
        <div class="form-group"><label class="form-label">寒假结束日期</label><input class="form-input" type="date" v-model="winter.endDate"></div>
        <div class="form-group"><label class="form-label">延期方式</label><select class="form-select" v-model="winter.extensionType"><option>统一延期至开学日（2月22日）</option><option>在到期日基础上 +30 天</option></select></div>
        <div class="form-group"><label class="form-label">适用馆藏地</label><select class="form-select" v-model="winter.location"><option>全部馆藏地</option></select></div>
      </div>
      <div class="fine-card" style="margin-top:12px">
        <div><span class="fine-label">本次将影响</span><br><span style="font-size:14px;font-weight:600;color:var(--acad-navy)">约 124 册图书</span></div>
        <button class="btn btn-primary" @click="batchExtend"><span class="icon-btn"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg></span> 批量延期</button>
      </div>
    </div>

    <!-- Extension History Modal -->
    <div v-if="showHistoryModal" class="modal-overlay" :class="{ active: showHistoryModal }" @click.self="showHistoryModal = false">
      <div class="modal" style="width:700px;">
        <div class="modal-header">
          <h3>延期记录</h3>
          <button class="modal-close" @click="showHistoryModal = false"><svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg></button>
        </div>
        <div class="modal-body" style="max-height:400px;overflow-y:auto;">
          <div class="table-container">
            <table>
              <thead><tr><th>批次号</th><th>假期类型</th><th>日期范围</th><th>影响册数</th><th>操作人</th><th>操作时间</th></tr></thead>
              <tbody>
                <tr v-if="mockExtensionRecords.length === 0"><td colspan="6" style="text-align:center;padding:24px;color:#999;">暂无延期记录</td></tr>
                <tr v-for="record in mockExtensionRecords" :key="record.id">
                  <td><span class="badge badge-info">{{ record.batchNo }}</span></td>
                  <td>{{ record.type }}</td>
                  <td>{{ record.dateRange }}</td>
                  <td>{{ record.affectedBooks }} 册</td>
                  <td>{{ record.operator }}</td>
                  <td>{{ record.operateTime }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showHistoryModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const showHistoryModal = ref(false)

const summer = ref({
  startDate: '2026-07-04',
  endDate: '2026-08-31',
  extensionType: '统一延期至开学日（9月1日）',
  location: '全部馆藏地'
})

const winter = ref({
  startDate: '2027-01-11',
  endDate: '2027-02-20',
  extensionType: '统一延期至开学日（2月22日）',
  location: '全部馆藏地'
})

const mockExtensionRecords = [
  { id: 1, batchNo: 'EXT-2026-001', type: '暑假', dateRange: '2026-07-04 ~ 2026-08-31', affectedBooks: 87, operator: '系统管理员', operateTime: '2026-06-30 10:30' },
  { id: 2, batchNo: 'EXT-2026-002', type: '寒假', dateRange: '2026-01-11 ~ 2026-02-20', affectedBooks: 124, operator: '系统管理员', operateTime: '2026-01-05 09:15' },
  { id: 3, batchNo: 'EXT-2025-003', type: '暑假', dateRange: '2025-07-05 ~ 2025-08-31', affectedBooks: 72, operator: '李老师', operateTime: '2025-07-01 14:00' },
  { id: 4, batchNo: 'EXT-2025-004', type: '寒假', dateRange: '2025-01-12 ~ 2025-02-18', affectedBooks: 103, operator: '李老师', operateTime: '2025-01-06 11:20' },
]

function saveSettings() {
  ElMessage.success('寒暑假设置已保存成功！')
}

function batchExtend() {
  ElMessage.success('批量延期操作已提交，将影响约 124 册图书。')
}
</script>
