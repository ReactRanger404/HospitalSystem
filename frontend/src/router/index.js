import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },

  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },

      // 门诊挂号与预约
      { path: 'registration/schedules', name: 'Schedules', component: () => import('../views/registration/Schedules.vue') },
      { path: 'registration/appointments', name: 'Appointments', component: () => import('../views/registration/Appointments.vue') },

      // 门诊医生站
      { path: 'doctor/patients', name: 'WaitingPatients', component: () => import('../views/doctor/WaitingPatients.vue') },
      { path: 'doctor/medical-records', name: 'MedicalRecords', component: () => import('../views/doctor/MedicalRecords.vue') },
      { path: 'doctor/prescriptions', name: 'Prescriptions', component: () => import('../views/doctor/Prescriptions.vue') },
      { path: 'doctor/exam-requests', name: 'ExamRequests', component: () => import('../views/doctor/ExamRequests.vue') },

      // 医技工作站
      { path: 'tech/pending', name: 'PendingExams', component: () => import('../views/tech/PendingExams.vue') },
      { path: 'tech/results', name: 'TechResults', component: () => import('../views/tech/TechResults.vue') },

      // 药房药库
      { path: 'pharmacy/drugs', name: 'Drugs', component: () => import('../views/pharmacy/Drugs.vue') },
      { path: 'pharmacy/inventory', name: 'Inventory', component: () => import('../views/pharmacy/Inventory.vue') },
      { path: 'pharmacy/dispense', name: 'Dispense', component: () => import('../views/pharmacy/Dispense.vue') },

      // 收费财务
      { path: 'billing/charges', name: 'BillingCharges', component: () => import('../views/billing/BillingCharges.vue') },
      { path: 'billing/inpatient', name: 'Inpatient', component: () => import('../views/billing/Inpatient.vue') },
      { path: 'billing/reports', name: 'Reports', component: () => import('../views/billing/Reports.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
