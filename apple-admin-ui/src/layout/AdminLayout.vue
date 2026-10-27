<template>
  <el-container class="admin-layout">
    <Sidebar />
    <el-container class="main-container">
      <Navbar />
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}
.main-container {
  flex-direction: column;
  overflow: hidden;
}
.main-content {
  background: #F5F5F0;
  padding: 20px;
  overflow-y: auto;
}
</style>
