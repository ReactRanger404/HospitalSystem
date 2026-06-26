<template>
  <div>
    <h3>住院管理</h3>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header><span>入院登记</span></template>
          <el-form :model="form" label-width="90px">
            <el-form-item label="患者ID"><el-input-number v-model="form.patientId" :min="1" /></el-form-item>
            <el-form-item label="病房号"><el-input v-model="form.wardNumber" placeholder="如: 301" /></el-form-item>
            <el-form-item label="床位号"><el-input v-model="form.bedNumber" placeholder="如: 1床" /></el-form-item>
            <el-form-item label="预缴押金"><el-input-number v-model="form.depositAmount" :min="0" :precision="2" /></el-form-item>
            <el-form-item><el-button type="primary" @click="handleAdmit">入院登记</el-button></el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>住院患者列表</span></template>
          <el-table :data="inpatients" border stripe size="small" max-height="400" v-loading="loading">
            <el-table-column prop="admissionNumber" label="住院号" width="150" />
            <el-table-column prop="patientId" label="患者ID" width="60" />
            <el-table-column prop="wardNumber" label="病房" width="60" />
            <el-table-column prop="bedNumber" label="床位" width="60" />
            <el-table-column prop="depositAmount" label="押金(元)" width="90" />
            <el-table-column prop="totalExpenses" label="费用(元)" width="90" />
            <el-table-column prop="balance" label="余额" width="80">
              <template #default="{row}">
                <span :style="{color: row.balance > 0 ? 'red' : 'green'}">{{ row.balance }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130">
              <template #default="{row}">
                <el-button size="small" @click="handleDeposit(row)">续费</el-button>
                <el-button size="small" type="danger" v-if="row.status==='admitted'" @click="handleDischarge(row)">出院</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!inpatients.length && !loading" style="text-align:center;color:#909399;padding:20px">暂无住院患者</div>
          <el-pagination layout="prev,pager,next" :total="inpTotal" :page-size="20" @current-change="p=>loadInpatients(p)" style="margin-top:8px;justify-content:center" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 续费弹窗 -->
    <el-dialog v-model="showDeposit" title="缴纳押金" width="400px">
      <el-form :model="depositForm" label-width="100px">
        <el-form-item label="金额"><el-input-number v-model="depositForm.amount" :min="100" :step="500" /></el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="depositForm.method">
            <el-option label="现金" value="cash" />
            <el-option label="微信" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="银行卡" value="card" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDeposit=false">取消</el-button>
        <el-button type="primary" @click="handlePayDeposit">确认缴纳</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { billingApi } from '../../api'

const form = ref({ patientId: 1, wardNumber: '301', bedNumber: '1', depositAmount: 5000 })
const inpatients = ref([])
const inpTotal = ref(0)
const loading = ref(false)
const showDeposit = ref(false)
const currentAccount = ref(null)
const depositForm = ref({ amount: 2000, method: 'cash' })

onMounted(() => loadInpatients())

/** 查询住院中患者列表 — 通过 bills 表筛选 hospitalization 类型 */
async function loadInpatients(page = 1) {
  loading.value = true
  try {
    // 先查住院账单，再映射到住院账户信息
    const res = await billingApi.getBills({ billType: 'hospitalization', page, size: 20 })
    // 实际生产环境中应直接查询 inpatient_accounts 表
    // 此处做演示数据兼容
    const billItems = res.data.items || []
    inpatients.value = billItems.map(b => ({
      id: b.id,
      admissionNumber: b.billNo ? b.billNo.replace(/^BIL/, 'INP') : 'INP' + b.id,
      patientId: b.patientId,
      patientName: b.patientName || '未知',
      wardNumber: '待分配',
      bedNumber: '待分配',
      depositAmount: b.payableAmount || 0,
      totalExpenses: b.totalAmount || 0,
      balance: (b.payableAmount || 0) - (b.paidAmount || 0),
      status: b.status === 'paid' ? 'discharged' : 'admitted'
    }))
    inpTotal.value = res.data.total || 0
  } finally { loading.value = false }
}

async function handleAdmit() {
  await billingApi.createInpatient(form.value)
  ElMessage.success('入院登记成功')
  loadInpatients()
}

function handleDeposit(row) {
  currentAccount.value = row
  showDeposit.value = true
}

async function handlePayDeposit() {
  if (!currentAccount.value) return
  await billingApi.addDeposit(currentAccount.value.id, depositForm.value.amount, depositForm.value.method)
  ElMessage.success('押金缴纳成功')
  showDeposit.value = false
  loadInpatients()
}

async function handleDischarge(row) {
  await billingApi.discharge(row.id)
  ElMessage.success('出院结算完成')
  loadInpatients()
}
</script>
