<template>
  <div>
    <h3>处方管理</h3>
    <el-card style="margin:16px 0">
      <el-form :inline="true">
        <el-form-item label="患者ID"><el-input v-model="patientId" style="width:120px" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="status" clearable style="width:120px">
            <el-option label="待缴费" value="pending" />
            <el-option label="已缴费" value="paid" />
            <el-option label="已发药" value="dispensed" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
        <el-form-item><el-button type="success" @click="showCreate = true">开具处方</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="list" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column label="类型" width="80">
          <template #default="{row}">{{ row.prescriptionType === 'western' ? '西药' : '中药' }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{row}">{{ {pending:'待缴费',paid:'已缴费',dispensed:'已发药',cancelled:'已取消'}[row.status] || row.status }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="开单时间" width="160" />
      </el-table>
      <el-pagination layout="prev,pager,next" :total="total" :page-size="20" @current-change="p=>fetchData(p)" style="margin-top:16px;justify-content:center" />
    </el-card>

    <el-dialog v-model="showCreate" title="开具处方" width="650px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="患者ID"><el-input-number v-model="form.patientId" :min="1" /></el-form-item>
        <el-form-item label="处方类型">
          <el-radio-group v-model="form.prescriptionType">
            <el-radio value="western">西药</el-radio>
            <el-radio value="chinese">中药</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="药品明细">
          <div v-for="(item,idx) in form.items" :key="idx" style="display:flex;gap:8px;margin-bottom:8px">
            <el-input v-model="item.drugName" placeholder="药品名" style="width:120px" />
            <el-input v-model="item.dosage" placeholder="用量" style="width:80px" />
            <el-input v-model="item.frequency" placeholder="频次" style="width:80px" />
            <el-input-number v-model="item.days" :min="1" :max="30" style="width:100px" />
            <el-input-number v-model="item.quantity" :min="1" style="width:100px" />
            <el-button type="danger" @click="form.items.splice(idx,1)">删除</el-button>
          </div>
          <el-button @click="form.items.push({drugId:1,drugName:'',dosage:'',frequency:'',days:1,quantity:1,unit:'盒',usageMethod:'oral'})">添加药品</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate=false">取消</el-button>
        <el-button type="primary" @click="handleCreate">开具</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { doctorApi } from '../../api'

const patientId = ref('')
const status = ref('')
const list = ref([])
const total = ref(0)
const showCreate = ref(false)
const form = ref({ patientId: 1, prescriptionType: 'western', medicalRecordId: null, items: [] })

async function fetchData(page = 1) {
  const params = { page, size: 20 }
  if (patientId.value) params.patientId = patientId.value
  if (status.value) params.status = status.value
  const res = await doctorApi.getPrescriptions(params)
  list.value = res.data.items || []
  total.value = res.data.total || 0
}

async function handleCreate() {
  await doctorApi.createPrescription(form.value)
  ElMessage.success('处方开具成功')
  showCreate.value = false
  fetchData()
}
</script>
