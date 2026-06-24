<template>
  <div class="mod-schedule">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.beanName" placeholder="beanName" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">Query</el-button>
        <el-button v-if="isAuth('sys:schedule:save')" type="primary" @click="addOrUpdateHandle()">Add</el-button>
        <el-button v-if="isAuth('sys:schedule:delete')" type="danger" @click="deleteHandle()" :disabled="dataListSelections.length <= 0">Batch Delete</el-button>
        <el-button v-if="isAuth('sys:schedule:pause')" type="danger" @click="pauseHandle()" :disabled="dataListSelections.length <= 0">Batch Pause</el-button>
        <el-button v-if="isAuth('sys:schedule:resume')" type="danger" @click="resumeHandle()" :disabled="dataListSelections.length <= 0">Batch Resume</el-button>
        <el-button v-if="isAuth('sys:schedule:run')" type="danger" @click="runHandle()" :disabled="dataListSelections.length <= 0">Batch Run Now</el-button>
        <el-button v-if="isAuth('sys:schedule:log')" type="success" @click="logHandle()">Log List</el-button>
      </el-form-item>
    </el-form>
    <el-table
      :data="dataList"
      border
      v-loading="dataListLoading"
      @selection-change="selectionChangeHandle"
      style="width: 100%;">
      <el-table-column
        type="selection"
        header-align="center"
        align="center"
        width="50">
      </el-table-column>
      <el-table-column
        prop="jobId"
        header-align="center"
        align="center"
        width="80"
        label="ID">
      </el-table-column>
      <el-table-column
        prop="beanName"
        header-align="center"
        align="center"
        label="beanName">
      </el-table-column>
      <el-table-column
        prop="params"
        header-align="center"
        align="center"
        label="Parameters">
      </el-table-column>
      <el-table-column
        prop="cronExpression"
        header-align="center"
        align="center"
        label="Cron expression">
      </el-table-column>
      <el-table-column
        prop="remark"
        header-align="center"
        align="center"
        label="Remark">
      </el-table-column>
      <el-table-column
        prop="status"
        header-align="center"
        align="center"
        label="Status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 0" size="small">Normal</el-tag>
          <el-tag v-else size="small" type="danger">Pause</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        fixed="right"
        header-align="center"
        align="center"
        width="150"
        label="Actions">
        <template slot-scope="scope">
          <el-button v-if="isAuth('sys:schedule:update')" type="text" size="small" @click="addOrUpdateHandle(scope.row.jobId)">Edit</el-button>
          <el-button v-if="isAuth('sys:schedule:delete')" type="text" size="small" @click="deleteHandle(scope.row.jobId)">Delete</el-button>
          <el-button v-if="isAuth('sys:schedule:pause')" type="text" size="small" @click="pauseHandle(scope.row.jobId)">Pause</el-button>
          <el-button v-if="isAuth('sys:schedule:resume')" type="text" size="small" @click="resumeHandle(scope.row.jobId)">Resume</el-button>
          <el-button v-if="isAuth('sys:schedule:run')" type="text" size="small" @click="runHandle(scope.row.jobId)">Run Now</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="sizeChangeHandle"
      @current-change="currentChangeHandle"
      :current-page="pageIndex"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      :total="totalPage"
      layout="total, sizes, prev, pager, next, jumper">
    </el-pagination>
    <!-- 弹窗, Add / Edit -->
    <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>
    <!-- 弹窗, Log List -->
    <log v-if="logVisible" ref="log"></log>
  </div>
</template>

<script>
  import AddOrUpdate from './schedule-add-or-update'
  import Log from './schedule-log'
  export default {
    data () {
      return {
        dataForm: {
          beanName: ''
        },
        dataList: [],
        pageIndex: 1,
        pageSize: 10,
        totalPage: 0,
        dataListLoading: false,
        dataListSelections: [],
        addOrUpdateVisible: false,
        logVisible: false
      }
    },
    components: {
      AddOrUpdate,
      Log
    },
    activated () {
      this.getDataList()
    },
    methods: {
      // Load data list
      getDataList () {
        this.dataListLoading = true
        this.$http({
          url: this.$http.adornUrl('/sys/schedule/list'),
          method: 'get',
          params: this.$http.adornParams({
            'page': this.pageIndex,
            'limit': this.pageSize,
            'beanName': this.dataForm.beanName
          })
        }).then(({data}) => {
          if (data && data.code === 0) {
            this.dataList = data.page.list
            this.totalPage = data.page.totalCount
          } else {
            this.dataList = []
            this.totalPage = 0
          }
          this.dataListLoading = false
        })
      },
      // Page size
      sizeChangeHandle (val) {
        this.pageSize = val
        this.pageIndex = 1
        this.getDataList()
      },
      // Current page
      currentChangeHandle (val) {
        this.pageIndex = val
        this.getDataList()
      },
      // Multi-select
      selectionChangeHandle (val) {
        this.dataListSelections = val
      },
      // Add / Edit
      addOrUpdateHandle (id) {
        this.addOrUpdateVisible = true
        this.$nextTick(() => {
          this.$refs.addOrUpdate.init(id)
        })
      },
      // Delete
      deleteHandle (id) {
        var ids = id ? [id] : this.dataListSelections.map(item => {
          return item.jobId
        })
        this.$confirm(`Are you sure you want to [${id ? 'delete' : 'batch delete'}] [id=${ids.join(',')}]?`, 'Tip', {
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: this.$http.adornUrl('/sys/schedule/delete'),
            method: 'post',
            data: this.$http.adornData(ids, false)
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.$message({
                message: 'Operation successful',
                type: 'success',
                duration: 1500,
                onClose: () => {
                  this.getDataList()
                }
              })
            } else {
              this.$message.error(data.msg)
            }
          })
        }).catch(() => {})
      },
      // Pause
      pauseHandle (id) {
        var ids = id ? [id] : this.dataListSelections.map(item => {
          return item.jobId
        })
        this.$confirm(`Are you sure you want to [${id ? 'pause' : 'batch pause'}] [id=${ids.join(',')}]?`, 'Tip', {
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: this.$http.adornUrl('/sys/schedule/pause'),
            method: 'post',
            data: this.$http.adornData(ids, false)
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.$message({
                message: 'Operation successful',
                type: 'success',
                duration: 1500,
                onClose: () => {
                  this.getDataList()
                }
              })
            } else {
              this.$message.error(data.msg)
            }
          })
        }).catch(() => {})
      },
      // Resume
      resumeHandle (id) {
        var ids = id ? [id] : this.dataListSelections.map(item => {
          return item.jobId
        })
        this.$confirm(`Are you sure you want to [${id ? 'resume' : 'batch resume'}] [id=${ids.join(',')}]?`, 'Tip', {
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: this.$http.adornUrl('/sys/schedule/resume'),
            method: 'post',
            data: this.$http.adornData(ids, false)
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.$message({
                message: 'Operation successful',
                type: 'success',
                duration: 1500,
                onClose: () => {
                  this.getDataList()
                }
              })
            } else {
              this.$message.error(data.msg)
            }
          })
        }).catch(() => {})
      },
      // Run Now
      runHandle (id) {
        var ids = id ? [id] : this.dataListSelections.map(item => {
          return item.jobId
        })
        this.$confirm(`Are you sure you want to [${id ? 'run now' : 'batch run now'}] [id=${ids.join(',')}]?`, 'Tip', {
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: this.$http.adornUrl('/sys/schedule/run'),
            method: 'post',
            data: this.$http.adornData(ids, false)
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.$message({
                message: 'Operation successful',
                type: 'success',
                duration: 1500,
                onClose: () => {
                  this.getDataList()
                }
              })
            } else {
              this.$message.error(data.msg)
            }
          })
        }).catch(() => {})
      },
      // Log List
      logHandle () {
        this.logVisible = true
        this.$nextTick(() => {
          this.$refs.log.init()
        })
      }
    }
  }
</script>
