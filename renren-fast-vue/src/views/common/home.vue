<template>
  <div class="mod-home">
    <!-- Welcome Section -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-content">
        <h2>Welcome to E-Commerce Control Center</h2>
        <p class="welcome-text">Manage your e-commerce platform efficiently with our comprehensive backend system</p>
        <p class="current-time">
          <i class="el-icon-time"></i> {{ currentTime }}
        </p>
      </div>
    </el-card>

    <!-- Empty State Tip (when all zeros) -->
    <el-alert
      v-if="isStatsEmpty && !statsLoading"
      class="empty-tip"
      type="info"
      title="Getting Started"
      :closable="false"
      show-icon
    >
      No data yet. Add admins in
      <el-button type="text" size="small" @click="navigateTo('admin')">Admin List</el-button>,
      create products in
      <el-button type="text" size="small" @click="navigateTo('product')">Product Management</el-button>,
      or set up inventory in
      <el-button type="text" size="small" @click="navigateTo('ware')">Inventory</el-button>.
    </el-alert>

    <!-- Statistics Cards (System Management, Product Management, Inventory) -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card" shadow="hover" @click.native="navigateTo('admin')">
          <div v-loading="statsLoading" class="stat-content">
            <div class="stat-icon stat-icon-purple">
              <svg class="stat-icon-inline" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M512 512a192 192 0 1 0 0-384 192 192 0 0 0 0 384zm0 64a256 256 0 0 0-256 256v160h512V832a256 256 0 0 0-256-256z"/></svg>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.admins }}</div>
              <div class="stat-label">Admins</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card" shadow="hover" @click.native="navigateTo('product')">
          <div v-loading="statsLoading" class="stat-content">
            <div class="stat-icon stat-icon-blue">
              <i class="el-icon-goods"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.products }}</div>
              <div class="stat-label">Products (SPU)</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card" shadow="hover" @click.native="navigateTo('productManager')">
          <div v-loading="statsLoading" class="stat-content">
            <div class="stat-icon stat-icon-green">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.skus }}</div>
              <div class="stat-label">SKUs</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card" shadow="hover" @click.native="navigateTo('ware')">
          <div v-loading="statsLoading" class="stat-content">
            <div class="stat-icon stat-icon-orange">
              <svg class="stat-icon-inline" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M832 320H692V192c0-35.2-28.8-64-64-64H396c-35.2 0-64 28.8-64 64v128H192c-35.2 0-64 28.8-64 64v448c0 35.2 28.8 64 64 64h640c35.2 0 64-28.8 64-64V384c0-35.2-28.8-64-64-64zM396 192h232v128H396V192zm396 704H232V384h160v64h64v-64h232v64h64v-64h160v512z"/></svg>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.purchases }}</div>
              <div class="stat-label">Purchase Orders</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Quick Actions -->
    <el-row :gutter="20" class="actions-row">
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover" class="action-card" @click.native="navigateTo('admin')">
          <div class="action-content">
            <div class="action-icon-wrap stat-icon-purple">
              <svg class="action-icon-inline" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M512 512a192 192 0 1 0 0-384 192 192 0 0 0 0 384zm0 64a256 256 0 0 0-256 256v160h512V832a256 256 0 0 0-256-256z"/></svg>
            </div>
            <h3>System Management</h3>
            <p>Admin list and permissions</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover" class="action-card" @click.native="navigateTo('product')">
          <div class="action-content">
            <div class="action-icon-wrap stat-icon-blue">
              <i class="el-icon-goods"></i>
            </div>
            <h3>Product Management</h3>
            <p>Categories, brands, SPU, SKU, specifications</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover" class="action-card" @click.native="navigateTo('ware')">
          <div class="action-content">
            <div class="action-icon-wrap stat-icon-orange">
              <svg class="action-icon-inline" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M832 384H192c-35.2 0-64 28.8-64 64v384c0 35.2 28.8 64 64 64h640c35.2 0 64-28.8 64-64V448c0-35.2-28.8-64-64-64zm0 64v128H192V448h640zm0 384H192V640h640v192z"/></svg>
            </div>
            <h3>Inventory</h3>
            <p>Purchase orders, warehouse stock, tasks</p>
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
        admins: 0,
        products: 0,
        skus: 0,
        purchases: 0
      },
      statsLoading: false,
      currentTime: '',
      lastUpdate: '',
      timeTimer: null
    }
  },
  computed: {
    isStatsEmpty() {
      return this.stats.admins === 0 && this.stats.products === 0 &&
        this.stats.skus === 0 && this.stats.purchases === 0
    }
  },
  mounted() {
    this.updateTime()
    this.loadStatistics()
    // Real-time clock: update every second
    this.timeTimer = setInterval(() => {
      this.updateTime()
    }, 1000)
  },
  beforeDestroy() {
    if (this.timeTimer) clearInterval(this.timeTimer)
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
        second: '2-digit',
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
      this.statsLoading = true
      const base = { page: 1, limit: 1 }
      const reqs = [
        this.$http.get(this.$http.adornUrl('/sys/user/list'), { params: this.$http.adornParams(base) }),
        this.$http.get(this.$http.adornUrl('/product/spuinfo/list'), { params: this.$http.adornParams(base) }),
        this.$http.get(this.$http.adornUrl('/product/skuinfo/list'), { params: this.$http.adornParams(base) }),
        this.$http.get(this.$http.adornUrl('/ware/purchase/list'), { params: this.$http.adornParams(base) })
      ]
      Promise.all(reqs)
        .then(([aRes, pRes, sRes, wRes]) => {
          const getTotal = (res) => (res && res.data && res.data.code === 0 && res.data.page) ? (res.data.page.totalCount || 0) : 0
          this.stats = {
            admins: getTotal(aRes),
            products: getTotal(pRes),
            skus: getTotal(sRes),
            purchases: getTotal(wRes)
          }
          this.statsLoading = false
        })
        .catch(() => {
          this.statsLoading = false
        })
    },
    navigateTo(module) {
      const routes = {
        admin: '/sys-user',
        product: '/product-spu',
        productManager: '/product-manager',
        ware: '/ware-purchase'
      }
      const path = routes[module]
      if (path) this.$router.push(path)
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
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
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
.stat-icon-inline { width: 32px; height: 32px; flex-shrink: 0; }
.action-icon-inline { width: 36px; height: 36px; flex-shrink: 0; }
.stat-icon-purple { background: linear-gradient(135deg, #6c5ce7 0%, #a29bfe 100%); }
.stat-icon-blue { background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%); }
.stat-icon-green { background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%); }
.stat-icon-orange { background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%); }

.empty-tip {
  margin-bottom: 20px;
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
  padding: 24px 20px;
}

.action-icon-wrap {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
  margin-bottom: 16px;
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
  margin-top: 24px;
  background: #fafbfc;
  border: 1px solid #ebeef5;
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
