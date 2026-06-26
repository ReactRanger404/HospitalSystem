<template>
  <div>
    <h3>预约挂号查询</h3>
    <el-card style="margin:16px 0">
      <el-form :inline="true">
        <el-form-item label="患者ID"><el-input v-model="patientId" placeholder="患者ID" style="width:120px" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="status" placeholder="状态" clearable style="width:120px">
            <el-option label="待就诊" value="pending" />
            <el-option label="已签到" value="checked_in" />
            <el-option label="就诊中" value="in_consultation" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-button type="primary" style="margin-bottom:12px" @click="showAppoint = true">新建预约</el-button>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="departmentName" label="科室" width="100" />
        <el-table-column prop="appointmentDate" label="日期" width="110" />
        <el-table-column label="时段" width="70">
          <template #default="{row}">{{ {morning:'上午',afternoon:'下午',evening:'晚间'}[row.timeSlot] || row.timeSlot }}</template>
        </el-table-column>
        <el-table-column prop="queueNumber" label="排队号" width="70" />
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="statusMap[row.status] || 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <el-button size="small" v-if="row.status==='pending'" @click="handleCheckin(row.id)">签到</el-button>
            <el-button size="small" type="danger" v-if="['pending','checked_in'].includes(row.status)" @click="handleCancel(row.id)">退号</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination layout="prev,pager,next" :total="total" :page-size="20" @current-change="p=>fetchData(p)" style="margin-top:16px;justify-content:center" />
    </el-card>

    <el-dialog v-model="showAppoint" title="新建预约" width="500px">
      <el-form :model="appointForm" label-width="80px">
        <el-form-item label="患者ID"><el-input-number v-model="appointForm.patientId" :min="1" /></el-form-item>
        <el-form-item label="科室">
          <el-select v-model="appointForm.departmentId" @change="loadDoctors" filterable style="width:100%">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model="appointForm.doctorId" filterable style="width:100%">
            <el-option v-for="d in doctors" :key="d.id" :label="d.realName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期"><el-date-picker v-model="appointForm.appointmentDate" style="width:100%" /></el-form-item>
        <el-form-item label="时段">
          <el-select v-model="appointForm.timeSlot" style="width:100%">
            <el-option label="上午" value="morning" /><el-option label="下午" value="afternoon" /><el-option label="晚间" value="evening" />
          </el-select>
        </el-form-item>
        <el-form-item label="症状"><el-input v-model="appointForm.symptoms" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAppoint=false">取消</el-button>
        <el-button type="primary" @click="handleCreateAppoint">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { registrationApi } from '../../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const patientId = ref('')
const status = ref('')
const departments = ref([])
const doctors = ref([])
const showAppoint = ref(false)
const appointForm = ref({ patientId: 1, doctorId: '', departmentId: '', appointmentDate: '', timeSlot: 'morning', symptoms: '' })

const statusMap = { pending: 'warning', checked_in: '', in_consultation: 'primary', completed: 'success', cancelled: 'info', refunded: 'danger' }

onMounted(async () => {
  const res = await registrationApi.getDepartments()
  departments.value = res.data || []
  fetchData()
})

async function fetchData(page = 1) {
  loading.value = true
  try {
    const params = { page, size: 20 }
    if (patientId.value) params.patientId = patientId.value
    if (status.value) params.status = status.value
    const res = await registrationApi.getMyAppointments(params)
    list.value = res.data.items || []
    total.value = res.data.total || 0
  } catch(e){} finally { loading.value = false }
}

async function loadDoctors(deptId) {
  const res = await registrationApi.getDoctors(deptId)
  doctors.value = res.data || []
}

async function handleCheckin(id) {
  await registrationApi.checkin(id)
  ElMessage.success('签到成功')
  fetchData()
}

async function handleCancel(id) {
  await ElMessageBox.confirm('确定要退号吗？')
  await registrationApi.cancel(id, '患者退号')
  ElMessage.success('退号成功')
  fetchData()
}

async function handleCreateAppoint() {
  const f = appointForm.value
  const data = { ...f, appointmentDate: f.appointmentDate.toISOString().slice(0,10), scheduleId: 1, source: 'onsite' }
  await registrationApi.createAppointment(data)
  ElMessage.success('预约成功')
  showAppoint.value = false
  fetchData()
}
</script>
