<template>
  <div>
    <h3>检查报告管理</h3>
    <el-card style="margin:16px 0">
      <el-form :inline="true">
        <el-form-item label="状态">
          <el-select v-model="queryStatus" clearable style="width:130px">
            <el-option label="草稿" value="draft" />
            <el-option label="已提交" value="submitted" />
            <el-option label="已审核" value="reviewed" />
            <el-option label="已发布" value="published" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="结果ID" width="70" />
        <el-table-column prop="examRequestId" label="申请ID" width="70" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="examName" label="项目" min-width="150" show-overflow-tooltip />
        <el-table-column prop="resultDescription" label="结果描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="conclusion" label="结论" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="{draft:'info',submitted:'warning',reviewed:'primary',published:'success'}[row.status] || 'info'" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button size="small" v-if="row.status==='draft'" type="primary" @click="showEdit(row)">编辑</el-button>
            <el-button size="small" v-if="row.status==='draft'" type="warning" @click="handleSubmit(row.id)">提交</el-button>
            <el-button size="small" v-if="row.status==='submitted'" type="success" @click="showReview(row)">审核</el-button>
            <el-button size="small" v-if="row.status==='reviewed'" type="success" @click="handlePublish(row.id)">发布</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination layout="prev,pager,next" :total="total" :page-size="20" @current-change="p=>fetchData(p)" style="margin-top:16px;justify-content:center" />
    </el-card>

    <!-- 编辑结果弹窗 -->
    <el-dialog v-model="showEditDialog" title="录入检查结果" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="结果描述"><el-input v-model="editForm.resultDescription" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="检查结论"><el-input v-model="editForm.conclusion" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="异常标识"><el-input v-model="editForm.abnormalFlags" placeholder="无异常" /></el-form-item>
        <el-form-item label="参考范围"><el-input v-model="editForm.referenceRange" /></el-form-item>
        <el-form-item label="检查设备"><el-input v-model="editForm.equipment" placeholder="CT/MRI型号" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog=false">取消</el-button>
        <el-button type="primary" @click="handleSaveResult" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { techApi } from '../../api'

const queryStatus = ref('')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const showEditDialog = ref(false)
const saving = ref(false)
const currentRow = ref(null)
const editForm = reactive({
  resultDescription: '',
  conclusion: '',
  abnormalFlags: '',
  referenceRange: '',
  equipment: ''
})

function statusLabel(s) {
  return { draft:'草稿', submitted:'已提交', reviewed:'已审核', published:'已发布' }[s] || s
}

onMounted(() => fetchData())

async function fetchData(page = 1) {
  loading.value = true
  try {
    const params = { page, size: 20 }
    if (queryStatus.value) params.status = queryStatus.value
    const res = await techApi.getResults(params)
    list.value = res.data.items || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function showEdit(row) {
  currentRow.value = row
  editForm.resultDescription = row.resultDescription || ''
  editForm.conclusion = row.conclusion || ''
  editForm.abnormalFlags = row.abnormalFlags || ''
  editForm.referenceRange = row.referenceRange || ''
  editForm.equipment = row.equipment || ''
  showEditDialog.value = true
}

function showReview(row) {
  currentRow.value = row
  editForm.conclusion = row.conclusion || ''
  showEditDialog.value = true
}

async function handleSaveResult() {
  if (!currentRow.value) return
  saving.value = true
  try {
    await techApi.saveResult(
      currentRow.value.examRequestId,
      1, // technicianId
      { ...editForm }
    )
    ElMessage.success('保存成功')
    showEditDialog.value = false
    fetchData()
  } finally { saving.value = false }
}

async function handleSubmit(id) {
  await techApi.submitForReview(id)
  ElMessage.success('已提交审核')
  fetchData()
}

async function handlePublish(id) {
  await techApi.publishResult(id)
  ElMessage.success('报告已发布')
  fetchData()
}
</script>
