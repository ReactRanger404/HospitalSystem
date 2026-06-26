<template>
  <div>
    <h3>配药发药</h3>
    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="处方ID" width="70" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column label="类型" width="70">
          <template #default="{row}">{{ {western:'西药',chinese:'中药'}[row.prescriptionType] }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{row}"><el-tag type="warning" size="small">待发药</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="开方时间" width="150" />
        <el-table-column label="操作" width="120">
          <template #default="{row}">
            <el-button size="small" type="primary" @click="handleDispense(row.id)">发药</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination layout="prev,pager,next" :total="total" :page-size="20" @current-change="p=>fetchData(p)" style="margin-top:16px;justify-content:center" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pharmacyApi } from '../../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)

onMounted(() => fetchData())

async function fetchData(page = 1) {
  loading.value = true
  try {
    const res = await pharmacyApi.getPendingDispense({ page, size: 20 })
    list.value = res.data.items || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

async function handleDispense(prescriptionId) {
  await ElMessageBox.confirm('确认发药吗？')
  await pharmacyApi.dispense(prescriptionId, 1)
  ElMessage.success('发药完成')
  fetchData()
}
</script>
