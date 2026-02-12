<template>
  <div class="mod-config">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.key" placeholder="Parameter Name" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">Query</el-button>
        <el-button v-if="isAuth('order:orderreturnapply:save')" type="primary" @click="addOrUpdateHandle()">Add</el-button>
        <el-button v-if="isAuth('order:orderreturnapply:delete')" type="danger" @click="deleteHandle()" :disabled="dataListSelections.length <= 0">Batch Delete</el-button>
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
        prop="id"
        header-align="center"
        align="center"
        label="id">
      </el-table-column>
      <el-table-column
        prop="orderId"
        header-align="center"
        align="center"
        label="order_id">
      </el-table-column>
      <el-table-column
        prop="skuId"
        header-align="center"
        align="center"
        label="Return Product ID">
      </el-table-column>
      <el-table-column
        prop="orderSn"
        header-align="center"
        align="center"
        label="Order Number">
      </el-table-column>
      <el-table-column
        prop="createTime"
        header-align="center"
        align="center"
        label="Application Time">
      </el-table-column>
      <el-table-column
        prop="memberUsername"
        header-align="center"
        align="center"
        label="Member Username">
      </el-table-column>
      <el-table-column
        prop="returnAmount"
        header-align="center"
        align="center"
        label="Refund Amount">
      </el-table-column>
      <el-table-column
        prop="returnName"
        header-align="center"
        align="center"
        label="Returner Name">
      </el-table-column>
      <el-table-column
        prop="returnPhone"
        header-align="center"
        align="center"
        label="Returner Phone">
      </el-table-column>
      <el-table-column
        prop="status"
        header-align="center"
        align="center"
        label="Application Status [0->Pending; 1->Returning; 2->Completed; 3->Rejected]">
      </el-table-column>
      <el-table-column
        prop="handleTime"
        header-align="center"
        align="center"
        label="Processing Time">
      </el-table-column>
      <el-table-column
        prop="skuImg"
        header-align="center"
        align="center"
        label="Product Image">
      </el-table-column>
      <el-table-column
        prop="skuName"
        header-align="center"
        align="center"
        label="Product Name">
      </el-table-column>
      <el-table-column
        prop="skuBrand"
        header-align="center"
        align="center"
        label="Product Brand">
      </el-table-column>
      <el-table-column
        prop="skuAttrsVals"
        header-align="center"
        align="center"
        label="Product Sales Attributes (JSON)">
      </el-table-column>
      <el-table-column
        prop="skuCount"
        header-align="center"
        align="center"
        label="Return Quantity">
      </el-table-column>
      <el-table-column
        prop="skuPrice"
        header-align="center"
        align="center"
        label="Product Unit Price">
      </el-table-column>
      <el-table-column
        prop="skuRealPrice"
        header-align="center"
        align="center"
        label="Product Actual Paid Unit Price">
      </el-table-column>
      <el-table-column
        prop="reason"
        header-align="center"
        align="center"
        label="Reason">
      </el-table-column>
      <el-table-column
        prop="description述"
        header-align="center"
        align="center"
        label="Description">
      </el-table-column>
      <el-table-column
        prop="descPics"
        header-align="center"
        align="center"
        label="Evidence Images (comma separated)">
      </el-table-column>
      <el-table-column
        prop="handleNote"
        header-align="center"
        align="center"
        label="Processing Note">
      </el-table-column>
      <el-table-column
        prop="handleMan"
        header-align="center"
        align="center"
        label="Processor">
      </el-table-column>
      <el-table-column
        prop="receiveMan"
        header-align="center"
        align="center"
        label="Receiver">
      </el-table-column>
      <el-table-column
        prop="receiveTime"
        header-align="center"
        align="center"
        label="Receive Time">
      </el-table-column>
      <el-table-column
        prop="receiveNote"
        header-align="center"
        align="center"
        label="Receive Note">
      </el-table-column>
      <el-table-column
        prop="receivePhone"
        header-align="center"
        align="center"
        label="Receive Phone">
      </el-table-column>
      <el-table-column
        prop="companyAddress"
        header-align="center"
        align="center"
        label="Company Receive Address">
      </el-table-column>
      <el-table-column
        fixed="right"
        header-align="center"
        align="center"
        width="150"
        label="Actions">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="addOrUpdateHandle(scope.row.id)">Edit</el-button>
          <el-button type="text" size="small" @click="deleteHandle(scope.row.id)">Delete</el-button>
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
    <!-- Dialog, Add / Edit -->
    <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>
  </div>
</template>

<script>
  import AddOrUpdate from './orderreturnapply-add-or-update'
  export default {
    data () {
      return {
        dataForm: {
          key: ''
        },
        dataList: [],
        pageIndex: 1,
        pageSize: 10,
        totalPage: 0,
        dataListLoading: false,
        dataListSelections: [],
        addOrUpdateVisible: false
      }
    },
    components: {
      AddOrUpdate
    },
    activated () {
      this.getDataList()
    },
    methods: {
      // Get data list
      getDataList () {
        this.dataListLoading = true
        this.$http({
          url: this.$http.adornUrl('/order/orderreturnapply/list'),
          method: 'get',
          params: this.$http.adornParams({
            'page': this.pageIndex,
            'limit': this.pageSize,
            'key': this.dataForm.key
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
      // Multiple selection
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
          return item.id
        })
        this.$confirm(`Are you sure you want to [${id ? 'delete' : 'batch delete'}] [id=${ids.join(',')}]?`, 'Tip', {
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: this.$http.adornUrl('/order/orderreturnapply/delete'),
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
        })
      }
    }
  }
</script>
