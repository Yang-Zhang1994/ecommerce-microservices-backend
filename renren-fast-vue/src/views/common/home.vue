<template>
  <div class="mod-home">
    <!-- Welcome Section -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-content">
        <h2>Welcome to E-Commerce Control Center</h2>
        <p class="welcome-text">Manage your e-commerce platform efficiently with our comprehensive backend system</p>
        <p class="current-time">{{ currentTime }}</p>
      </div>
    </el-card>

    <!-- Statistics Cards -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF;">
              <i class="el-icon-goods"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.products || 0 }}</div>
              <div class="stat-label">Total Products</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A;">
              <i class="el-icon-shopping-cart-full"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.orders || 0 }}</div>
              <div class="stat-label">Total Orders</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.users || 0 }}</div>
              <div class="stat-label">Total Users</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C;">
              <i class="el-icon-money"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(stats.revenue || 0) }}</div>
              <div class="stat-label">Total Revenue</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Quick Actions -->
    <el-row :gutter="20" class="actions-row">
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover" class="action-card" @click.native="navigateTo('product')">
          <div class="action-content">
            <i class="el-icon-goods action-icon"></i>
            <h3>Product Management</h3>
            <p>Manage products, categories, and inventory</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover" class="action-card" @click.native="navigateTo('order')">
          <div class="action-content">
            <i class="el-icon-shopping-cart-full action-icon"></i>
            <h3>Order Management</h3>
            <p>View and process customer orders</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover" class="action-card" @click.native="navigateTo('member')">
          <div class="action-content">
            <i class="el-icon-user action-icon"></i>
            <h3>Member Management</h3>
            <p>Manage customer accounts and information</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- System Information -->
    <el-card class="info-card" shadow="never">
      <div slot="header" class="card-header">
        <span>System Information</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">System Version:</span>
            <span class="info-value">E-Commerce Control Center v1.0.0</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">Server Status:</span>
            <el-tag type="success" size="small">Running</el-tag>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">Last Update:</span>
            <span class="info-value">{{ lastUpdate }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">Database Status:</span>
            <el-tag type="success" size="small">Connected</el-tag>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      stats: {
        products: 0,
        orders: 0,
        users: 0,
        revenue: 0
      },
      currentTime: '',
      lastUpdate: ''
    }
  },
  mounted() {
    this.updateTime()
    this.loadStatistics()
    // Update time every minute
    setInterval(() => {
      this.updateTime()
    }, 60000)
  },
  methods: {
    updateTime() {
      const now = new Date()
      const options = { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric', 
        hour: '2-digit', 
        minute: '2-digit',
        weekday: 'long'
      }
      this.currentTime = now.toLocaleDateString('en-US', options)
      this.lastUpdate = now.toLocaleDateString('en-US', { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    loadStatistics() {
      // You can load real statistics from API here
      // Example:
      // this.$http.get('/api/dashboard/stats').then(res => {
      //   this.stats = res.data
      // })
    },
    formatMoney(amount) {
      return amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    },
    navigateTo(module) {
      // Navigate to different modules
      const routes = {
        product: '/product/spu',
        order: '/order/order',
        member: '/member/member'
      }
      if (routes[module]) {
        this.$router.push({ name: routes[module].split('/').pop() })
      }
    }
  }
}
</script>

<style scoped>
.mod-home {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.welcome-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.welcome-card >>> .el-card__body {
  padding: 30px;
}

.welcome-content {
  color: white;
}

.welcome-content h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-text {
  margin: 10px 0;
  font-size: 16px;
  opacity: 0.9;
}

.current-time {
  margin: 10px 0 0 0;
  font-size: 14px;
  opacity: 0.8;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  color: white;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.actions-row {
  margin-bottom: 20px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-content {
  text-align: center;
  padding: 20px;
}

.action-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 15px;
}

.action-content h3 {
  margin: 10px 0;
  color: #303133;
  font-size: 18px;
}

.action-content p {
  margin: 5px 0 0 0;
  color: #909399;
  font-size: 14px;
}

.info-card {
  margin-top: 20px;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.info-item {
  padding: 15px 0;
  border-bottom: 1px solid #EBEEF5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #909399;
  margin-right: 10px;
  font-size: 14px;
}

.info-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .mod-home {
    padding: 10px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 24px;
  }
}
</style>
