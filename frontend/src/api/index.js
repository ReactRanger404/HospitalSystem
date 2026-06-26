import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：在请求头添加 JWT Token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) return res
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/#/login'
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request

// ====== 认证 API ======
export const authApi = {
  login: data => request.post('/auth/login', data),
  register: data => request.post('/auth/register', data),
  registerPatient: data => request.post('/auth/register/patient', data),
}

// ====== 预约挂号 API ======
export const registrationApi = {
  getDepartments: (category) => request.get('/registration/departments', { params: { category } }),
  getDoctors: (deptId) => request.get(`/registration/departments/${deptId}/doctors`),
  createSchedule: data => request.post('/registration/schedules', data),
  getSchedules: params => request.get('/registration/schedules', { params }),
  getAvailableSchedules: params => request.get('/registration/schedules/available', { params }),
  createAppointment: data => request.post('/registration/appointments', data),
  getMyAppointments: params => request.get('/registration/appointments', { params }),
  checkin: id => request.put(`/registration/appointments/${id}/checkin`),
  cancel: (id, reason) => request.put(`/registration/appointments/${id}/cancel`, null, { params: { reason } }),
  getTodayAppointments: params => request.get('/registration/appointments/doctor/today', { params }),
}

// ====== 医生站 API ======
export const doctorApi = {
  getWaitingPatients: doctorId => request.get('/doctor/patients/waiting', { params: { doctorId } }),
  startConsultation: id => request.put(`/doctor/appointments/${id}/start`),
  completeConsultation: id => request.put(`/doctor/appointments/${id}/complete`),
  createMedicalRecord: data => request.post('/doctor/medical-records', data),
  updateMedicalRecord: (id, data) => request.put(`/doctor/medical-records/${id}`, data),
  getMedicalRecord: id => request.get(`/doctor/medical-records/${id}`),
  getPatientRecords: params => request.get('/doctor/medical-records', { params }),
  createPrescription: data => request.post('/doctor/prescriptions', data),
  getPrescriptions: params => request.get('/doctor/prescriptions', { params }),
  createExamRequest: data => request.post('/doctor/exam-requests', data),
  getExamRequests: params => request.get('/doctor/exam-requests', { params }),
  getPatientHistory: patientId => request.get(`/doctor/patients/${patientId}/history`),
}

// ====== 医技工作站 API ======
export const techApi = {
  getPendingExams: params => request.get('/tech/pending', { params }),
  startExam: (id, technicianId) => request.put(`/tech/exams/${id}/start`, null, { params: { technicianId } }),
  // examRequestId和technicianId作为query参数, result数据作为request body
  saveResult: (examRequestId, technicianId, data) =>
    request.post('/tech/results', data, { params: { examRequestId, technicianId } }),
  submitForReview: id => request.put(`/tech/results/${id}/submit`),
  reviewResult: (id, reviewerId, decision, comment) =>
    request.put(`/tech/results/${id}/review`, null, { params: { reviewerId, decision, comment } }),
  publishResult: id => request.put(`/tech/results/${id}/publish`),
  getResults: params => request.get('/tech/results', { params }),
  getPatientResults: patientId => request.get(`/tech/patients/${patientId}/results`),
}

// ====== 药房药库 API ======
export const pharmacyApi = {
  createDrug: data => request.post('/pharmacy/drugs', data),
  updateDrug: (id, data) => request.put(`/pharmacy/drugs/${id}`, data),
  getDrugs: params => request.get('/pharmacy/drugs', { params }),
  receiveDrug: params => request.post('/pharmacy/inventory/receive', null, { params }),
  getInventory: params => request.get('/pharmacy/inventory', { params }),
  getExpiryWarnings: () => request.get('/pharmacy/inventory/expiry-warnings'),
  getPendingDispense: params => request.get('/pharmacy/dispense/pending', { params }),
  dispense: (prescriptionId, pharmacistId) =>
    request.put(`/pharmacy/dispense/${prescriptionId}`, null, { params: { pharmacistId } }),
  auditItem: (itemId, status, note) =>
    request.put(`/pharmacy/audit/${itemId}`, null, { params: { status, note } }),
  getTransactions: params => request.get('/pharmacy/transactions', { params }),
}

// ====== 收费财务 API ======
export const billingApi = {
  createBill: data => request.post('/billing/bills', data),
  pay: params => request.post('/billing/pay', null, { params }),
  refundBill: (id, operatorId, reason) =>
    request.put(`/billing/bills/${id}/refund`, null, { params: { operatorId, reason } }),
  getBills: params => request.get('/billing/bills', { params }),
  createInpatient: data => request.post('/billing/inpatient', data),
  addDeposit: (id, amount, method) =>
    request.put(`/billing/inpatient/${id}/deposit`, null, { params: { amount, method } }),
  discharge: id => request.put(`/billing/inpatient/${id}/discharge`),
  getRevenueReport: params => request.get('/billing/reports/revenue', { params }),
  getDepartmentRevenue: params => request.get('/billing/reports/department', { params }),
  getDoctorWorkload: params => request.get('/billing/reports/doctor-workload', { params }),
}
