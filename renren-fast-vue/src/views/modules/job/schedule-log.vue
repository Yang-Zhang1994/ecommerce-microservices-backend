<template>
  <el-dialog
    title="Log List"
    :close-on-click-modal="false"
    :visible.sync="visible"
    width="75%">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.id" placeholder="Job ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">Query</el-button>
      </el-form-item>
    </el-form>
    <el-table
      :data="dataList"
      border
      v-loading="dataListLoading"
      height="460"
      style="width: 100%;">
      <el-table-column
        prop="logId"
        header-align="center"
        align="center"
        width="80"
        label="Log ID">
      </el-table-column>
      <el-table-column
        prop="jobId"
        header-align="center"
        align="center"
        width="80"
        label="Job ID">
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
        prop="status"
        header-align="center"
        align="center"
        label="Status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 0" size="small">Success</el-tag>
          <el-tag v-else @click.native="showErrorInfo(scope.row.logId)" size="small" type="danger" style="cursor: pointer;">Failed</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="times"
        header-align="center"
        align="center"
        label="Duration (ms)">
      </el-table-column>
      <el-table-column
        prop="createTime"
        header-align="center"
        align="center"
        width="180"
        label="Executed at">
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
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        dataForm: {
          id: ''
        },
        dataList: [],
        pageIndex: 1,
        pageSize: 10,
        totalPage: 0,
        dataListLoading: false
      }
    },
    methods: {
      init () {
        this.visible = true
        this.getDataList()
      },
      // Load data list
      getDataList () {
        this.dataListLoading = true
        this.$http({
          url: this.$http.adornUrl('/sys/scheduleLog/list'),
          method: 'get',
          params: this.$http.adornParams({
            'page': this.pageIndex,
            'limit': this.pageSize,
            'jobId': this.dataForm.id
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
      // Failed信息
      showErrorInfo (id) {
        this.$http({
          url: this.$http.adornUrl(`/sys/scheduleLog/info/${id}`),
          method: 'get',
          params: this.$http.adornParams()
        }).then(({data}) => {
          if (data && data.code === 0) {
            this.$alert(data.log.error)
          } else {
            this.$message.error(data.msg)
          }
        })
      }
    }
  }
</script>
