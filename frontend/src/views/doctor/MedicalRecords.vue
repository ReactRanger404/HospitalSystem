<template>
  <div>
    <h3>电子病历</h3>
    <el-card style="margin:16px 0">
      <el-form :inline="true">
        <el-form-item label="患者ID"><el-input v-model="patientId" style="width:120px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="fetchRecords">查询病历</el-button></el-form-item>
        <el-form-item><el-button type="success" @click="showCreate = true;newRecord.patientId=Number(patientId)">新建病历</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="visitDate" label="就诊日期" width="110" />
        <el-table-column prop="chiefComplaint" label="主诉" min-width="200" show-overflow-tooltip />
        <el-table-column prop="diagnosis" label="诊断" min-width="200" show-overflow-tooltip />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column label="归档" width="70">
          <template #default="{row}">{{ row.isFinalized ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{row}"><el-button size="small" @click="viewDetail(row)">详情</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination layout="prev,pager,next" :total="recordTotal" :page-size="20" @current-change="p=>fetchRecords(p)" style="margin-top:16px;justify-content:center" />
    </el-card>

    <el-dialog v-model="showCreate" title="新建病历" width="700px">
      <el-form :model="newRecord" label-width="100px">
        <el-form-item label="患者ID"><el-input-number v-model="newRecord.patientId" :min="1" /></el-form-item>
        <el-form-item label="主诉"><el-input v-model="newRecord.chiefComplaint" type="textarea" /></el-form-item>
        <el-form-item label="现病史"><el-input v-model="newRecord.presentIllness" type="textarea" /></el-form-item>
        <el-form-item label="既往史"><el-input v-model="newRecord.pastHistory" type="textarea" /></el-form-item>
        <el-form-item label="诊断"><el-input v-model="newRecord.diagnosis" type="textarea" /></el-form-item>
        <el-form-item label="治疗方案"><el-input v-model="newRecord.treatmentPlan" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate=false">取消</el-button>
        <el-button type="primary" @click="handleCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetail" title="病历详情" width="700px">
      <div v-if="detail">
        <p><strong>主诉：</strong>{{ detail.chiefComplaint }}</p>
        <p><strong>现病史：</strong>{{ detail.presentIllness }}</p>
        <p><strong>既往史：</strong>{{ detail.pastHistory }}</p>
        <p><strong>诊断：</strong>{{ detail.diagnosis }}</p>
        <p><strong>治疗方案：</strong>{{ detail.treatmentPlan }}</p>
        <p><strong>医生建议：</strong>{{ detail.doctorAdvice }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { doctorApi } from '../../api'

const patientId = ref('')
const records = ref([])
const recordTotal = ref(0)
const showCreate = ref(false)
const showDetail = ref(false)
const detail = ref(null)
const newRecord = ref({ patientId: 1, chiefComplaint: '', presentIllness: '', pastHistory: '', diagnosis: '', treatmentPlan: '' })

async function fetchRecords(page = 1) {
  if (!patientId.value) return
  const res = await doctorApi.getPatientRecords({ patientId: patientId.value, page, size: 20 })
  records.value = res.data.items || []
  recordTotal.value = res.data.total || 0
}

async function handleCreate() {
  await doctorApi.createMedicalRecord(newRecord.value)
  ElMessage.success('病历创建成功')
  showCreate.value = false
  fetchRecords()
}

async function viewDetail(row) {
  const res = await doctorApi.getMedicalRecord(row.id)
  detail.value = res.data
  showDetail.value = true
}
</script>
