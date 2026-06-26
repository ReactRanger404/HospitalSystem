<template>
  <div>
    <h3 style="margin-bottom:16px">工作台</h3>
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card shadow="hover" style="margin-bottom:16px;cursor:pointer" @click="$router.push(card.path)">
          <div style="display:flex;align-items:center;justify-content:space-between">
            <div>
              <div style="font-size:28px;font-weight:bold;color:#409EFF">{{ card.count }}</div>
              <div style="color:#909399;margin-top:8px">{{ card.title }}</div>
            </div>
            <el-icon :size="40" :color="card.color"><component :is="card.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header><span>快捷操作</span></template>
          <el-space wrap>
            <el-button type="primary" @click="$router.push('/registration/appointments')">预约挂号</el-button>
            <el-button type="success" @click="$router.push('/doctor/patients')">接诊患者</el-button>
            <el-button type="warning" @click="$router.push('/pharmacy/dispense')">配药发药</el-button>
            <el-button type="info" @click="$router.push('/billing/charges')">门诊收费</el-button>
          </el-space>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>系统信息</span></template>
          <p>技术栈: Spring Boot 3.2 + MyBatis-Plus + Redis + Kafka + MySQL</p>
          <p>前端: Vue 3 + Element Plus + Axios</p>
          <p>欢迎使用医院综合信息管理系统</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const cards = [
  { title: '待诊患者', count: 12, icon: 'User', color: '#409EFF', path: '/doctor/patients' },
  { title: '待发药方', count: 8, icon: 'Goods', color: '#67C23A', path: '/pharmacy/dispense' },
  { title: '待接诊检查', count: 5, icon: 'Monitor', color: '#E6A23C', path: '/tech/pending' },
  { title: '待缴费账单', count: 15, icon: 'Money', color: '#F56C6C', path: '/billing/charges' },
]
</script>
