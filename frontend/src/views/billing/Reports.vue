<template>
  <div>
    <h3>财务报表</h3>
    <el-card>
      <el-form :inline="true">
        <el-form-item label="开始">
          <el-date-picker v-model="startDate" type="date" />
        </el-form-item>
        <el-form-item label="结束">
          <el-date-picker v-model="endDate" type="date" />
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadReports">查询</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="8">
        <el-card>
          <template #header><span>收入概览</span></template>
          <div style="text-align:center;padding:20px">
            <div style="font-size:32px;font-weight:bold;color:#409EFF">{{ revenue }} 元</div>
            <div style="color:#909399;margin-top:8px">总收入 / {{ billCount }} 笔</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <template #header><span>科室收入统计</span></template>
          <el-table :data="deptRevenue" border stripe size="small">
            <el-table-column label="类型" prop="type" />
            <el-table-column label="收入" prop="revenue" />
            <el-table-column label="笔数" prop="count" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:16px">
      <template #header><span>医生工作量统计</span></template>
      <el-table :data="workload" border stripe size="small">
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="recordCount" label="就诊人数" width="100" />
        <el-table-column prop="prescriptionCount" label="处方数" width="100" />
        <el-table-column prop="prescriptionAmount" label="处方金额" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { billingApi } from '../../api'

const startDate = ref(new Date(Date.now() - 30*24*60*60*1000))
const endDate = ref(new Date())
const revenue = ref(0)
const billCount = ref(0)
const deptRevenue = ref([])
const workload = ref([])

onMounted(() => loadReports())

async function loadReports() {
  const s = startDate.value.toISOString().slice(0,10)
  const e = endDate.value.toISOString().slice(0,10)

  const rev = await billingApi.getRevenueReport({ startDate: s, endDate: e })
  revenue.value = rev.data?.totalRevenue || 0
  billCount.value = rev.data?.totalBills || 0

  const dept = await billingApi.getDepartmentRevenue({ startDate: s, endDate: e })
  deptRevenue.value = dept.data || []

  const wl = await billingApi.getDoctorWorkload({ startDate: s, endDate: e })
  workload.value = wl.data || []
}
</script>
