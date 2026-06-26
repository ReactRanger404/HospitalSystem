<template>
  <div>
    <h3>库存管理</h3>
    <el-card style="margin:16px 0">
      <el-form :inline="true">
        <el-form-item label="药品ID"><el-input v-model="drugId" style="width:120px" /></el-form-item>
        <el-form-item>
          <el-checkbox v-model="nearExpiry">仅看临期</el-checkbox>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
        <el-form-item><el-button type="success" @click="showReceive = true">药品入库</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="18">
        <el-card>
          <el-table :data="list" border stripe>
            <el-table-column prop="drugName" label="药品" width="150" />
            <el-table-column prop="batchNumber" label="批号" width="120" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="supplier" label="供应商" width="120" />
            <el-table-column prop="expiryDate" label="有效期" width="110" />
            <el-table-column label="预警" width="80">
              <template #default="{row}">
                <el-tag v-if="row.isNearExpiry" type="danger" size="small">临期</el-tag>
                <el-tag v-else-if="row.isExpired" type="warning" size="small">过期</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination layout="prev,pager,next" :total="total" :page-size="20" @current-change="p=>fetchData(p)" style="margin-top:16px;justify-content:center" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header><span style="color:#E6A23C">临期预警</span></template>
          <div v-for="w in warnings" :key="w.id" style="font-size:13px;padding:4px 0;border-bottom:1px solid #eee">
            {{ w.drugName }} - {{ w.expiryDate }} (余{{ w.quantity }})
          </div>
          <div v-if="!warnings.length" style="color:#909399">暂无</div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showReceive" title="药品入库" width="500px">
      <el-form :model="receiveForm" label-width="100px">
        <el-form-item label="药品ID"><el-input-number v-model="receiveForm.drugId" :min="1" /></el-form-item>
        <el-form-item label="批号"><el-input v-model="receiveForm.batchNumber" /></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="receiveForm.quantity" :min="1" /></el-form-item>
        <el-form-item label="采购价"><el-input-number v-model="receiveForm.purchasePrice" :precision="2" /></el-form-item>
        <el-form-item label="有效期"><el-date-picker v-model="receiveForm.expiryDate" type="date" style="width:100%" /></el-form-item>
        <el-form-item label="供应商"><el-input v-model="receiveForm.supplier" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReceive=false">取消</el-button>
        <el-button type="primary" @click="handleReceive">入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pharmacyApi } from '../../api'

const drugId = ref('')
const nearExpiry = ref(false)
const list = ref([])
const total = ref(0)
const warnings = ref([])
const showReceive = ref(false)
const receiveForm = ref({ drugId: 1, batchNumber: '', quantity: 1, purchasePrice: 0, expiryDate: '', supplier: '' })

onMounted(() => { fetchData(); loadWarnings() })

async function fetchData(page = 1) {
  const params = { page, size: 20 }
  if (drugId.value) params.drugId = drugId.value
  if (nearExpiry.value) params.nearExpiry = true
  const res = await pharmacyApi.getInventory(params)
  list.value = res.data.items || []
  total.value = res.data.total || 0
}

async function loadWarnings() {
  const res = await pharmacyApi.getExpiryWarnings()
  warnings.value = res.data || []
}

async function handleReceive() {
  const f = receiveForm.value
  await pharmacyApi.receiveDrug({ ...f, operatorId: 1, salePrice: f.purchasePrice, expiryDate: f.expiryDate.toISOString().slice(0,10) })
  ElMessage.success('入库成功')
  showReceive.value = false
  fetchData()
  loadWarnings()
}
</script>
