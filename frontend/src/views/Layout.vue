<template>
  <el-container style="height:100vh">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" style="background:#304156;transition:width 0.3s">
      <div class="logo">{{ isCollapse ? '医院' : '医院综合管理系统' }}</div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :collapse="isCollapse"
        :collapse-transition="false"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon><span>工作台</span>
        </el-menu-item>

        <!-- 门诊挂号 -->
        <el-sub-menu index="registration">
          <template #title><el-icon><Document /></el-icon><span>门诊挂号预约</span></template>
          <el-menu-item index="/registration/schedules">医生排班管理</el-menu-item>
          <el-menu-item index="/registration/appointments">预约挂号查询</el-menu-item>
        </el-sub-menu>

        <!-- 医生站 -->
        <el-sub-menu index="doctor">
          <template #title><el-icon><UserFilled /></el-icon><span>门诊医生站</span></template>
          <el-menu-item index="/doctor/patients">待诊患者</el-menu-item>
          <el-menu-item index="/doctor/medical-records">电子病历</el-menu-item>
          <el-menu-item index="/doctor/prescriptions">处方管理</el-menu-item>
          <el-menu-item index="/doctor/exam-requests">检查申请</el-menu-item>
        </el-sub-menu>

        <!-- 医技站 -->
        <el-sub-menu index="tech">
          <template #title><el-icon><Monitor /></el-icon><span>医技工作站</span></template>
          <el-menu-item index="/tech/pending">接诊登记</el-menu-item>
          <el-menu-item index="/tech/results">检查报告</el-menu-item>
        </el-sub-menu>

        <!-- 药房 -->
        <el-sub-menu index="pharmacy">
          <template #title><el-icon><Goods /></el-icon><span>药房药库</span></template>
          <el-menu-item index="/pharmacy/drugs">药品管理</el-menu-item>
          <el-menu-item index="/pharmacy/inventory">库存管理</el-menu-item>
          <el-menu-item index="/pharmacy/dispense">配药发药</el-menu-item>
        </el-sub-menu>

        <!-- 收费财务 -->
        <el-sub-menu index="billing">
          <template #title><el-icon><Money /></el-icon><span>收费财务</span></template>
          <el-menu-item index="/billing/charges">门诊收费</el-menu-item>
          <el-menu-item index="/billing/inpatient">住院管理</el-menu-item>
          <el-menu-item index="/billing/reports">财务报表</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 主区域 -->
    <el-container>
      <el-header style="background:#fff;border-bottom:1px solid #e6e6e6;display:flex;align-items:center;justify-content:space-between;height:50px;padding:0 20px">
        <el-button link @click="isCollapse = !isCollapse">
          <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
        </el-button>
        <div>
          <el-tag type="info" size="small" style="margin-right:12px">{{ authStore.realName }} ({{ authStore.role }})</el-tag>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main style="background:#f0f2f5;padding:16px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isCollapse = ref(false)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 50px;
  line-height: 50px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
  overflow: hidden;
  white-space: nowrap;
}
.el-aside { overflow: hidden; }
.el-menu { border-right: none; }
</style>
