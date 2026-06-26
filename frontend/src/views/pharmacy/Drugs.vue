<template>
  <div>
    <h3>药品管理</h3>
    <el-card style="margin:16px 0">
      <el-form :inline="true">
        <el-form-item label="搜索"><el-input v-model="keyword" placeholder="药品名称/编码" style="width:200px" clearable /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="category" clearable style="width:120px">
            <el-option label="西药" value="western" />
            <el-option label="中药" value="chinese" />
            <el-option label="中成药" value="chinese_patent" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
        <el-form-item><el-button type="success" @click="showCreate = true">新增药品</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="list" border stripe>
        <el-table-column prop="code" label="编码" width="100" />
        <el-table-column prop="name" label="药品名称" width="150" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="manufacturer" label="生产厂家" min-width="180" show-overflow-tooltip />
        <el-table-column prop="salePrice" label="零售价" width="80" />
        <el-table-column prop="totalStock" label="库存量" width="80" />
        <el-table-column prop="minStock" label="预警线" width="80" />
        <el-table-column prop="isPrescription" label="处方药" width="70">
          <template #default="{row}">{{ row.isPrescription ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{row}"><el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination layout="prev,pager,next" :total="total" :page-size="20" @current-change="p=>fetchData(p)" style="margin-top:16px;justify-content:center" />
    </el-card>

    <el-dialog v-model="showCreate" title="新增药品" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.drugCategory">
            <el-option label="西药" value="western" />
            <el-option label="中药" value="chinese" />
            <el-option label="中成药" value="chinese_patent" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格"><el-input v-model="form.specification" /></el-form-item>
        <el-form-item label="零售价"><el-input-number v-model="form.salePrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="最低库存"><el-input-number v-model="form.minStock" :min="0" /></el-form-item>
        <el-form-item label="处方药"><el-switch v-model="form.isPrescription" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate=false">取消</el-button>
        <el-button type="primary" @click="handleCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑药品弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑药品" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="规格"><el-input v-model="editForm.specification" /></el-form-item>
        <el-form-item label="生产厂家"><el-input v-model="editForm.manufacturer" /></el-form-item>
        <el-form-item label="零售价"><el-input-number v-model="editForm.salePrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="最低库存"><el-input-number v-model="editForm.minStock" :min="0" /></el-form-item>
        <el-form-item label="处方药"><el-switch v-model="editForm.isPrescription" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="editForm.isActive" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog=false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pharmacyApi } from '../../api'

const keyword = ref('')
const category = ref('')
const list = ref([])
const total = ref(0)
const showCreate = ref(false)
const showEditDialog = ref(false)
const form = ref({ code: '', name: '', drugCategory: 'western', specification: '', salePrice: 0, minStock: 10, isPrescription: true })
const editForm = ref({ id: null, name: '', specification: '', manufacturer: '', salePrice: 0, minStock: 10, isPrescription: true, isActive: true })

onMounted(() => fetchData())

async function fetchData(page = 1) {
  const params = { page, size: 20 }
  if (keyword.value) params.keyword = keyword.value
  if (category.value) params.category = category.value
  const res = await pharmacyApi.getDrugs(params)
  list.value = res.data.items || []
  total.value = res.data.total || 0
}

async function handleCreate() {
  await pharmacyApi.createDrug(form.value)
  ElMessage.success('新增成功')
  showCreate.value = false
  fetchData()
}

function handleEdit(row) {
  editForm.value = {
    id: row.id,
    name: row.name || '',
    specification: row.specification || '',
    manufacturer: row.manufacturer || '',
    salePrice: row.salePrice || 0,
    minStock: row.minStock || 10,
    isPrescription: row.isPrescription !== false,
    isActive: true
  }
  showEditDialog.value = true
}

async function handleUpdate() {
  const { id, ...data } = editForm.value
  await pharmacyApi.updateDrug(id, data)
  ElMessage.success('更新成功')
  showEditDialog.value = false
  fetchData()
}
</script>
