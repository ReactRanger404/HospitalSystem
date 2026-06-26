<template>
  <div>
    <h3>检查检验接诊登记</h3>
    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="doctorName" label="开单医生" width="100" />
        <el-table-column prop="examCategory" label="类别" width="80" />
        <el-table-column prop="examName" label="项目" min-width="150" />
        <el-table-column label="紧急" width="60">
          <template #default="{row}"><el-tag :type="row.urgency==='emergency'?'danger':'warning'" size="small">{{ {emergency:'紧急',urgent:'急',routine:'普通'}[row.urgency] }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="150" />
        <el-table-column label="操作" width="120">
          <template #default="{row}">
            <el-button size="small" type="primary" @click="handleStart(row)">接诊</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination layout="prev,pager,next" :total="total" :page-size="20" @current-change="p=>fetchData(p)" style="margin-top:16px;justify-content:center" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { techApi } from '../../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)

onMounted(() => fetchData())

async function fetchData(page = 1) {
  loading.value = true
  try {
    const res = await techApi.getPendingExams({ page, size: 20 })
    list.value = res.data.items || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

async function handleStart(row) {
  await techApi.startExam(row.id, 1)
  ElMessage.success('已接诊')
  fetchData()
}
</script>
