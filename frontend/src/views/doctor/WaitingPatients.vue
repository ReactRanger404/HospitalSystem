<template>
  <div>
    <h3>待诊患者</h3>
    <el-card>
      <div style="margin-bottom:12px">
        <el-input v-model="doctorId" placeholder="医生ID" style="width:150px;margin-right:12px" />
        <el-button type="primary" @click="fetchData">查询</el-button>
      </div>
      <el-table :data="list" border stripe>
        <el-table-column prop="id" label="预约ID" width="70" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="queueNumber" label="排队号" width="80" />
        <el-table-column prop="timeSlot" label="时段" width="70" />
        <el-table-column prop="symptoms" label="症状" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status==='checked_in'?'warning':'primary'" size="small">{{ row.status === 'checked_in' ? '待就诊' : '就诊中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{row}">
            <el-button size="small" type="primary" @click="handleStart(row)">开始就诊</el-button>
            <el-button size="small" type="success" @click="handleComplete(row)">完成就诊</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { doctorApi } from '../../api'

const list = ref([])
const doctorId = ref(1)

async function fetchData() {
  if (!doctorId.value) return
  const res = await doctorApi.getWaitingPatients(doctorId.value)
  list.value = res.data || []
}

async function handleStart(row) {
  await doctorApi.startConsultation(row.id)
  ElMessage.success('开始就诊')
  fetchData()
}

async function handleComplete(row) {
  await doctorApi.completeConsultation(row.id)
  ElMessage.success('完成就诊')
  fetchData()
}
</script>
