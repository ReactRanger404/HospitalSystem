<template>
  <div>
    <h3>门诊收费</h3>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header><span>创建账单</span></template>
          <el-form :model="billForm" label-width="90px">
            <el-form-item label="患者ID"><el-input-number v-model="billForm.patientId" :min="1" /></el-form-item>
            <el-form-item label="账单类型">
              <el-select v-model="billForm.billType" style="width:100%">
                <el-option label="挂号费" value="registration" />
                <el-option label="处方费" value="prescription" />
                <el-option label="检查费" value="examination" />
              </el-select>
            </el-form-item>
            <el-form-item label="关联ID"><el-input-number v-model="billForm.referenceId" :min="0" /></el-form-item>
            <el-form-item label="收费项目">
              <div v-for="(item,idx) in billForm.items" :key="idx" style="display:flex;gap:8px;margin-bottom:8px">
                <el-input v-model="item.itemName" placeholder="项目名" style="width:130px" />
                <el-input-number v-model="item.amount" :min="0" :precision="2" style="width:120px" />
                <el-button type="danger" size="small" @click="billForm.items.splice(idx,1)">删</el-button>
              </div>
              <el-button size="small" @click="billForm.items.push({itemType:'prescription',itemName:'',quantity:1,unitPrice:0,amount:0})">添加项目</el-button>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleCreateBill">创建账单</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>待缴费账单</span></template>
          <el-table :data="pendingBills" border stripe size="small" max-height="400">
            <el-table-column prop="billNo" label="编号" width="140" />
            <el-table-column prop="payableAmount" label="应付" width="80" />
            <el-table-column label="类型" width="60">
              <template #default="{row}">{{ {registration:'挂号',prescription:'处方',examination:'检查'}[row.billType] }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{row}">
                <el-button size="small" type="success" @click="handlePay(row.id, row.payableAmount)">收费</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { billingApi } from '../../api'

const billForm = ref({ patientId: 1, billType: 'prescription', referenceId: null, referenceType: 'prescription', items: [] })
const pendingBills = ref([])

onMounted(() => loadPending())

async function loadPending() {
  const res = await billingApi.getBills({ status: 'pending', page: 1, size: 50 })
  pendingBills.value = res.data.items || []
}

async function handleCreateBill() {
  await billingApi.createBill(billForm.value)
  ElMessage.success('账单创建成功')
  loadPending()
}

async function handlePay(billId, amount) {
  await billingApi.pay({ billId, amount, method: 'wechat', operatorId: 1 })
  ElMessage.success('收费成功')
  loadPending()
}
</script>
