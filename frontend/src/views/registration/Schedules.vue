<template>
  <div>
    <h3>医生排班管理</h3>
    <el-card style="margin:16px 0">
      <el-form :inline="true" :model="query">
        <el-form-item label="科室">
          <el-select v-model="query.departmentId" placeholder="选择科室" clearable style="width:150px">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="query.date" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <div style="margin-bottom:12px">
        <el-button type="primary" @click="showDialog = true">新建排班</el-button>
      </div>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column label="时段" width="100">
          <template #default="{row}">{{ {morning:'上午',afternoon:'下午',evening:'晚间'}[row.timeSlot] || row.timeSlot }}</template>
        </el-table-column>
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="departmentName" label="科室" width="120" />
        <el-table-column prop="maxPatients" label="总号源" width="80" />
        <el-table-column prop="bookedCount" label="已预约" width="80" />
        <el-table-column label="余号" width="80">
          <template #default="{row}">{{ row.maxPatients - row.bookedCount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.isActive ? 'success' : 'info'">{{ row.isActive ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
      </el-table>
      <el-pagination layout="prev,pager,next" :total="total" :page-size="20" @current-change="p=>fetchData(p)" style="margin-top:16px;justify-content:center" />
    </el-card>

    <!-- 新建排班弹窗 -->
    <el-dialog v-model="showDialog" title="新建排班" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="医生">
          <el-select v-model="form.doctorId" filterable placeholder="选择医生" style="width:100%">
            <el-option v-for="d in doctors" :key="d.id" :label="d.realName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="form.departmentId" placeholder="选择科室" style="width:100%">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期"><el-date-picker v-model="form.scheduleDate" type="date" style="width:100%" /></el-form-item>
        <el-form-item label="时段">
          <el-select v-model="form.timeSlot" style="width:100%">
            <el-option label="上午" value="morning" />
            <el-option label="下午" value="afternoon" />
            <el-option label="晚间" value="evening" />
          </el-select>
        </el-form-item>
        <el-form-item label="号源数">
          <el-input-number v-model="form.maxPatients" :min="1" :max="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog=false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { registrationApi } from '../../api'

const query = ref({ departmentId: null, date: null })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const departments = ref([])
const doctors = ref([])
const showDialog = ref(false)
const form = ref({ doctorId: '', departmentId: '', scheduleDate: '', timeSlot: 'morning', maxPatients: 30 })

onMounted(async () => {
  const res = await registrationApi.getDepartments()
  departments.value = res.data || []
  const res2 = await registrationApi.getDepartments('临床')
  doctors.value = res2.data || []
  fetchData()
})

async function fetchData(page = 1) {
  loading.value = true
  try {
    const params = { page, size: 20 }
    if (query.value.departmentId) params.departmentId = query.value.departmentId
    if (query.value.date) params.date = query.value.date.toISOString().slice(0,10)
    const res = await registrationApi.getSchedules(params)
    list.value = res.data.items || []
    total.value = res.data.total || 0
  } catch(e){} finally { loading.value = false }
}

async function handleCreate() {
  await registrationApi.createSchedule(form.value)
  ElMessage.success('排班创建成功')
  showDialog.value = false
  fetchData()
}
</script>
