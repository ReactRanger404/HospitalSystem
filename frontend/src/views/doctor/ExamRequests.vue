<template>
  <div>
    <h3>检查检验申请</h3>
    <el-button type="success" style="margin-bottom:12px" @click="showCreate=true">新建申请</el-button>
    <el-card>
      <el-table :data="list" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="examCategory" label="类别" width="80" />
        <el-table-column prop="examName" label="项目" min-width="150" />
        <el-table-column prop="urgency" label="紧急" width="60" />
        <el-table-column prop="status" label="状态" width="80" />
        <el-table-column prop="createdAt" label="申请时间" width="160" />
      </el-table>
    </el-card>

    <el-dialog v-model="showCreate" title="新建检查检验申请" width="500px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="患者ID"><el-input-number v-model="form.patientId" :min="1" /></el-form-item>
        <el-form-item label="申请类型">
          <el-select v-model="form.requestType">
            <el-option label="检查" value="examination" />
            <el-option label="检验" value="lab_test" />
          </el-select>
        </el-form-item>
        <el-form-item label="检查类别">
          <el-select v-model="form.examCategory">
            <el-option label="CT" value="CT" />
            <el-option label="核磁共振" value="MRI" />
            <el-option label="X光" value="X-ray" />
            <el-option label="B超" value="Ultrasound" />
            <el-option label="血常规" value="Blood" />
            <el-option label="尿常规" value="Urine" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称"><el-input v-model="form.examName" /></el-form-item>
        <el-form-item label="临床诊断"><el-input v-model="form.clinicalDiagnosis" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate=false">取消</el-button>
        <el-button type="primary" @click="handleCreate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { doctorApi } from '../../api'

const list = ref([])
const showCreate = ref(false)
const form = ref({ patientId: 1, requestType: 'examination', examCategory: 'CT', examName: '', clinicalDiagnosis: '' })

onMounted(() => fetchData())

async function fetchData(page = 1) {
  const res = await doctorApi.getExamRequests({ page, size: 20 })
  list.value = res.data.items || []
}

async function handleCreate() {
  await doctorApi.createExamRequest(form.value)
  ElMessage.success('申请提交成功')
  showCreate.value = false
  fetchData()
}
</script>
