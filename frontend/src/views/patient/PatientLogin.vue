<template>
  <div class="patient-layout">
    <header class="patient-header">
      <h2>🏥 健康之家 · 线上挂号</h2>
      <div>
        <el-button v-if="!authStore.isLoggedIn" type="primary" @click="showLogin=true">登录</el-button>
        <el-button v-else type="danger" @click="handleLogout">退出</el-button>
      </div>
    </header>

    <main class="patient-main" v-if="authStore.isLoggedIn">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card shadow="hover" class="menu-card" @click="currentView='departments'">
            <el-icon :size="30" color="#409EFF"><List /></el-icon>
            <p>科室列表</p>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="menu-card" @click="currentView='my-appointments'">
            <el-icon :size="30" color="#67C23A"><Calendar /></el-icon>
            <p>我的预约</p>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="menu-card" @click="currentView='my-records'">
            <el-icon :size="30" color="#E6A23C"><Document /></el-icon>
            <p>就诊记录</p>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="menu-card" @click="currentView='my-reports'">
            <el-icon :size="30" color="#F56C6C"><DataBoard /></el-icon>
            <p>检查报告</p>
          </el-card>
        </el-col>
      </el-row>

      <!-- 科室列表 -->
      <el-card v-if="currentView === 'departments'" style="margin-top:16px">
        <template #header><span>选择科室</span></template>
        <el-row :gutter="12">
          <el-col :span="6" v-for="dept in departments" :key="dept.id" style="margin-bottom:12px">
            <el-card shadow="hover" class="dept-card" @click="selectDepartment(dept)">
              <h4>{{ dept.name }}</h4>
              <p style="color:#909399;font-size:12px">{{ dept.location }}</p>
            </el-card>
          </el-col>
        </el-row>
      </el-card>

      <!-- 医生列表 -->
      <el-card v-if="selectedDept" style="margin-top:16px">
        <template #header>
          <span>{{ selectedDept.name }} — 选择医生</span>
          <el-button size="small" style="float:right" @click="selectedDept=null;doctors=[]">返回</el-button>
        </template>
        <el-table :data="doctors" border stripe @row-click="selectDoctor">
          <el-table-column prop="realName" label="姓名" width="100" />
          <el-table-column prop="title" label="职称" width="120" />
          <el-table-column prop="phone" label="电话" width="130" />
          <el-table-column label="操作" width="120">
            <template #default="{row}">
              <el-button size="small" type="primary" @click.stop="selectDoctor(row)">预约</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 预约弹窗 -->
      <el-dialog v-model="showAppointment" title="预约挂号" width="400px">
        <p><strong>医生：</strong>{{ selectedDoctor?.realName }}</p>
        <p><strong>科室：</strong>{{ selectedDept?.name }}</p>
        <el-form label-width="80px">
          <el-form-item label="日期">
            <el-date-picker v-model="appointDate" type="date" :disabled-date="d=>d<new Date()" style="width:100%" />
          </el-form-item>
          <el-form-item label="时段">
            <el-select v-model="appointSlot" style="width:100%">
              <el-option label="上午 08:00-12:00" value="morning" />
              <el-option label="下午 14:00-17:30" value="afternoon" />
            </el-select>
          </el-form-item>
          <el-form-item label="症状描述">
            <el-input v-model="symptoms" type="textarea" placeholder="请简要描述您的症状" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showAppointment=false">取消</el-button>
          <el-button type="primary" @click="handleBook" :loading="booking">确认预约</el-button>
        </template>
      </el-dialog>

      <!-- 我的预约 -->
      <el-card v-if="currentView === 'my-appointments'" style="margin-top:16px">
        <template #header><span>我的预约记录</span></template>
        <el-table :data="myAppointments" border stripe v-loading="loading">
          <el-table-column prop="doctorName" label="医生" width="100" />
          <el-table-column prop="departmentName" label="科室" width="100" />
          <el-table-column prop="appointmentDate" label="日期" width="110" />
          <el-table-column label="时段" width="70">
            <template #default="{row}">{{ {morning:'上午',afternoon:'下午'}[row.timeSlot] }}</template>
          </el-table-column>
          <el-table-column prop="queueNumber" label="排队号" width="70" />
          <el-table-column label="状态" width="90">
            <template #default="{row}">
              <el-tag :type="tagMap[row.status]" size="small">{{ statusMap[row.status] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{row}">
              <el-button size="small" type="danger" v-if="row.status==='pending'" @click="handleCancel(row.id)">取消</el-button>
              <el-button size="small" type="success" v-if="row.status==='checked_in'" disabled>已签到</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 就诊记录 -->
      <el-card v-if="currentView === 'my-records'" style="margin-top:16px">
        <template #header><span>就诊记录</span></template>
        <el-table :data="myRecords" border stripe v-loading="loading">
          <el-table-column prop="visitDate" label="日期" width="110" />
          <el-table-column prop="doctorName" label="医生" width="100" />
          <el-table-column prop="chiefComplaint" label="主诉" min-width="150" show-overflow-tooltip />
          <el-table-column prop="diagnosis" label="诊断" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="80">
            <template #default="{row}">
              <el-button size="small" @click="showRecordDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 检查报告 -->
      <el-card v-if="currentView === 'my-reports'" style="margin-top:16px">
        <template #header><span>检查报告</span></template>
        <el-table :data="myReports" border stripe v-loading="loading">
          <el-table-column prop="examName" label="项目" width="150" />
          <el-table-column prop="conclusion" label="结论" min-width="200" show-overflow-tooltip />
          <el-table-column prop="publishedAt" label="报告时间" width="150" />
        </el-table>
      </el-card>
    </main>

    <!-- 未登录展示 -->
    <main class="patient-main" v-else style="text-align:center;padding-top:80px">
      <h2 style="font-size:28px;color:#303133">欢迎使用线上挂号系统</h2>
      <p style="color:#909399;margin:20px 0">在线预约挂号，查看检查报告，管理就诊记录</p>
      <el-button type="primary" size="large" @click="showLogin=true">立即登录</el-button>
      <el-button size="large" style="margin-left:12px" @click="showRegister=true">注册账号</el-button>
    </main>

    <!-- 登录弹窗 -->
    <el-dialog v-model="showLogin" title="患者登录" width="380px">
      <el-form :model="loginForm" label-width="0">
        <el-input v-model="loginForm.username" placeholder="手机号/用户名" style="margin-bottom:16px" />
        <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password style="margin-bottom:16px" />
      </el-form>
      <template #footer>
        <el-button @click="showLogin=false">取消</el-button>
        <el-button type="primary" @click="handleLogin" :loading="logining">登录</el-button>
      </template>
    </el-dialog>

    <!-- 注册弹窗 -->
    <el-dialog v-model="showRegister" title="患者注册" width="400px">
      <el-form :model="regForm" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="regForm.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="regForm.phone" /></el-form-item>
        <el-form-item label="身份证"><el-input v-model="regForm.idCard" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="regForm.password" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister=false">取消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="registering">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { authApi, registrationApi, doctorApi, techApi } from '../../api'

const router = useRouter()
const authStore = useAuthStore()

const currentView = ref('')
const showLogin = ref(false)
const showRegister = ref(false)
const showAppointment = ref(false)
const logining = ref(false)
const registering = ref(false)
const booking = ref(false)
const loading = ref(false)

const departments = ref([])
const doctors = ref([])
const selectedDept = ref(null)
const selectedDoctor = ref(null)
const myAppointments = ref([])
const myRecords = ref([])
const myReports = ref([])
const appointDate = ref('')
const appointSlot = ref('morning')
const symptoms = ref('')

const loginForm = ref({ username: '', password: '' })
const regForm = ref({ realName: '', phone: '', idCard: '', password: '' })

const statusMap = { pending: '待就诊', checked_in: '已签到', in_consultation: '就诊中', completed: '已完成', cancelled: '已取消', refunded: '已退号' }
const tagMap = { pending: 'warning', checked_in: '', in_consultation: 'primary', completed: 'success', cancelled: 'info', refunded: 'danger' }

onMounted(() => {
  if (authStore.isLoggedIn) loadDepartments()
})

async function loadDepartments() {
  const res = await registrationApi.getDepartments('临床')
  departments.value = res.data || []
}

async function selectDepartment(dept) {
  selectedDept.value = dept
  selectedDoctor.value = null
  currentView.value = ''
  const res = await registrationApi.getDoctors(dept.id)
  doctors.value = res.data || []
}

function selectDoctor(doc) {
  selectedDoctor.value = doc
  appointDate.value = ''
  appointSlot.value = 'morning'
  symptoms.value = ''
  showAppointment.value = true
}

async function handleBook() {
  if (!appointDate.value || !appointSlot.value) {
    ElMessage.warning('请选择日期和时段')
    return
  }
  booking.value = true
  try {
    const schedRes = await registrationApi.getAvailableSchedules({
      departmentId: selectedDept.value.id,
      doctorId: selectedDoctor.value.id
    })
    const schedules = schedRes.data || []
    if (!schedules.length) {
      ElMessage.warning('该医生暂无可用排班')
      return
    }
    const schedule = schedules[0]
    await registrationApi.createAppointment({
      patientId: authStore.userId,
      doctorId: selectedDoctor.value.id,
      departmentId: selectedDept.value.id,
      scheduleId: schedule.id,
      appointmentDate: appointDate.value.toISOString().slice(0,10),
      timeSlot: appointSlot.value,
      source: 'wechat',
      symptoms: symptoms.value
    })
    ElMessage.success('预约成功！请按时到院就诊')
    showAppointment.value = false
    if (currentView.value === 'my-appointments') loadMyAppointments()
  } finally { booking.value = false }
}

async function loadMyAppointments() {
  loading.value = true
  try {
    const res = await registrationApi.getMyAppointments({ patientId: authStore.userId, page: 1, size: 20 })
    myAppointments.value = res.data.items || []
  } finally { loading.value = false }
}

async function handleCancel(id) {
  await ElMessageBox.confirm('确定要取消预约吗？')
  await registrationApi.cancel(id, '患者取消')
  ElMessage.success('已取消')
  loadMyAppointments()
}

async function loadMyRecords() {
  loading.value = true
  try {
    const res = await doctorApi.getPatientRecords({ patientId: authStore.userId, page: 1, size: 20 })
    myRecords.value = res.data.items || []
  } finally { loading.value = false }
}

async function loadMyReports() {
  loading.value = true
  try {
    const res = await techApi.getPatientResults(authStore.userId)
    myReports.value = res.data || []
  } finally { loading.value = false }
}

function showRecordDetail(row) {
  ElMessageBox.alert(
    `<b>主诉：</b>${row.chiefComplaint || '无'}<br>
     <b>诊断：</b>${row.diagnosis || '无'}<br>
     <b>治疗方案：</b>${row.treatmentPlan || '无'}<br>
     <b>医生建议：</b>${row.doctorAdvice || '无'}`,
    '就诊详情',
    { dangerouslyUseHTMLString: true }
  )
}

watch(currentView, (v) => {
  if (v === 'my-appointments') loadMyAppointments()
  if (v === 'my-records') loadMyRecords()
  if (v === 'my-reports') loadMyReports()
  if (v === 'departments') loadDepartments()
})

async function handleLogin() {
  logining.value = true
  try {
    const res = await authApi.login(loginForm.value)
    authStore.setToken(res.data.accessToken)
    authStore.setUser(res.data)
    showLogin.value = false
    ElMessage.success('登录成功')
    loadDepartments()
  } finally { logining.value = false }
}

async function handleRegister() {
  registering.value = true
  try {
    const data = { ...regForm.value, username: regForm.value.phone, role: 'patient' }
    await authApi.registerPatient(data)
    ElMessage.success('注册成功，请登录')
    showRegister.value = false
    loginForm.value.username = data.username
    loginForm.value.password = data.password
    showLogin.value = true
  } finally { registering.value = false }
}

function handleLogout() {
  authStore.logout()
  router.push('/patient')
}
</script>

<style scoped>
.patient-layout { min-height: 100vh; background: #f0f2f5; }
.patient-header {
  background: #fff; padding: 12px 40px;
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
  position: sticky; top: 0; z-index: 10;
}
.patient-header h2 { margin: 0; font-size: 20px; color: #409EFF; }
.patient-main { max-width: 1100px; margin: 20px auto; padding: 0 20px; }
.menu-card { text-align: center; padding: 24px; cursor: pointer; transition: .2s; }
.menu-card:hover { transform: translateY(-2px); }
.menu-card p { margin: 8px 0 0; font-size: 14px; color: #303133; }
.dept-card { cursor: pointer; text-align: center; }
.dept-card h4 { margin: 8px 0; }
</style>
