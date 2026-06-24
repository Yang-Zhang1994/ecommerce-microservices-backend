<template>
  <div class="mod-config">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.key" placeholder="Parameter Name" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">Query</el-button>
        <el-button v-if="isAuth('order:order:save')" type="primary" @click="addOrUpdateHandle()">Add</el-button>
        <el-button v-if="isAuth('order:order:delete')" type="danger" @click="deleteHandle()" :disabled="dataListSelections.length <= 0">Batch Delete</el-button>
      </el-form-item>
    </el-form>
    <el-table
      class="admin-table-wide admin-table-ellipsis"
      :fit="false"
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
        prop="memberId"
        header-align="center"
        align="center"
        label="member_id">
      </el-table-column>
      <el-table-column
        prop="orderSn"
        header-align="center"
        align="center"
        min-width="150"
        show-overflow-tooltip
        label="Order SN">
      </el-table-column>
      <el-table-column
        prop="couponId"
        header-align="center"
        align="center"
        min-width="90"
        label="Coupon">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.couponId" type="success" size="small">Used</el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="createTime"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Created">
      </el-table-column>
      <el-table-column
        prop="memberUsername"
        header-align="center"
        align="center"
        min-width="120"
        show-overflow-tooltip
        label="Username">
      </el-table-column>
      <el-table-column
        prop="totalAmount"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Total">
      </el-table-column>
      <el-table-column
        prop="payAmount"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Payable">
      </el-table-column>
      <el-table-column
        prop="freightAmount"
        header-align="center"
        align="center"
        min-width="90"
        show-overflow-tooltip
        label="Freight">
      </el-table-column>
      <el-table-column
        prop="promotionAmount"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Promotion">
      </el-table-column>
      <el-table-column
        prop="integrationAmount"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Points Ded.">
      </el-table-column>
      <el-table-column
        prop="couponAmount"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Coupon Ded.">
      </el-table-column>
      <el-table-column
        prop="discountAmount"
        header-align="center"
        align="center"
        min-width="110"
        show-overflow-tooltip
        label="Admin Disc.">
      </el-table-column>
      <el-table-column
        prop="payType"
        header-align="center"
        align="center"
        min-width="130"
        label="Pay Type">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.payType != null && scope.row.payType !== ''" :type="payTypeTagType(scope.row.payType)" size="small">
            {{ formatPayType(scope.row.payType) }}
          </el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="sourceType"
        header-align="center"
        align="center"
        min-width="80"
        label="Source">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.sourceType != null && scope.row.sourceType !== ''" :type="sourceTypeTagType(scope.row.sourceType)" size="small">
            {{ formatSourceType(scope.row.sourceType) }}
          </el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="status"
        header-align="center"
        align="center"
        min-width="130"
        label="Status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status != null && scope.row.status !== ''" :type="orderStatusTagType(scope.row.status)" size="small">
            {{ formatOrderStatus(scope.row.status) }}
          </el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="deliveryCompany"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Carrier">
      </el-table-column>
      <el-table-column
        prop="deliverySn"
        header-align="center"
        align="center"
        min-width="110"
        show-overflow-tooltip
        label="Tracking">
      </el-table-column>
      <el-table-column
        prop="autoConfirmDay"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Auto Confirm (d)">
      </el-table-column>
      <el-table-column
        prop="integration"
        header-align="center"
        align="center"
        min-width="80"
        show-overflow-tooltip
        label="Points">
      </el-table-column>
      <el-table-column
        prop="growth"
        header-align="center"
        align="center"
        min-width="90"
        show-overflow-tooltip
        label="Growth">
      </el-table-column>
      <el-table-column
        prop="billType"
        header-align="center"
        align="center"
        min-width="110"
        label="Invoice Type">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.billType != null && scope.row.billType !== ''" :type="billTypeTagType(scope.row.billType)" size="small">
            {{ formatBillType(scope.row.billType) }}
          </el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="billHeader"
        header-align="center"
        align="center"
        label="Invoice Header">
      </el-table-column>
      <el-table-column
        prop="billContent"
        header-align="center"
        align="center"
        label="Invoice Content">
      </el-table-column>
      <el-table-column
        prop="billReceiverPhone"
        header-align="center"
        align="center"
        min-width="110"
        show-overflow-tooltip
        label="Invoice Phone">
      </el-table-column>
      <el-table-column
        prop="billReceiverEmail"
        header-align="center"
        align="center"
        min-width="110"
        show-overflow-tooltip
        label="Invoice Email">
      </el-table-column>
      <el-table-column
        prop="receiverName"
        header-align="center"
        align="center"
        label="Receiver Name">
      </el-table-column>
      <el-table-column
        prop="receiverPhone"
        header-align="center"
        align="center"
        label="Receiver Phone">
      </el-table-column>
      <el-table-column
        prop="receiverPostCode"
        header-align="center"
        align="center"
        min-width="90"
        show-overflow-tooltip
        label="Postal Code">
      </el-table-column>
      <el-table-column
        prop="receiverProvince"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Province">
      </el-table-column>
      <el-table-column
        prop="receiverCity"
        header-align="center"
        align="center"
        label="City">
      </el-table-column>
      <el-table-column
        prop="receiverRegion"
        header-align="center"
        align="center"
        label="Region">
      </el-table-column>
      <el-table-column
        prop="receiverDetailAddress"
        header-align="center"
        align="center"
        min-width="120"
        show-overflow-tooltip
        label="Address">
      </el-table-column>
      <el-table-column
        prop="note"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Remarks">
      </el-table-column>
      <el-table-column
        prop="confirmStatus"
        header-align="center"
        align="center"
        min-width="120"
        label="Receipt">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.confirmStatus != null && scope.row.confirmStatus !== ''" :type="confirmStatusTagType(scope.row.confirmStatus)" size="small">
            {{ formatConfirmStatus(scope.row.confirmStatus) }}
          </el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="deleteStatus"
        header-align="center"
        align="center"
        min-width="90"
        label="Record">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.deleteStatus != null && scope.row.deleteStatus !== ''" :type="deleteStatusTagType(scope.row.deleteStatus)" size="small">
            {{ formatDeleteStatus(scope.row.deleteStatus) }}
          </el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="useIntegration"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Points Used">
      </el-table-column>
      <el-table-column
        prop="paymentTime"
        header-align="center"
        align="center"
        label="Payment Time">
      </el-table-column>
      <el-table-column
        prop="deliveryTime"
        header-align="center"
        align="center"
        label="Delivery Time">
      </el-table-column>
      <el-table-column
        prop="receiveTime"
        header-align="center"
        align="center"
        min-width="100"
        show-overflow-tooltip
        label="Received At">
      </el-table-column>
      <el-table-column
        prop="commentTime"
        header-align="center"
        align="center"
        label="Comment Time">
      </el-table-column>
      <el-table-column
        prop="modifyTime"
        header-align="center"
        align="center"
        label="Modify Time">
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
  import AddOrUpdate from './order-add-or-update'
  import {
    formatOrderStatus,
    orderStatusTagType,
    formatPayType,
    payTypeTagType,
    formatSourceType,
    sourceTypeTagType,
    formatBillType,
    billTypeTagType,
    formatConfirmStatus,
    confirmStatusTagType,
    formatDeleteStatus,
    deleteStatusTagType
  } from './order-admin-meta'

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
      formatOrderStatus,
      orderStatusTagType,
      formatPayType,
      payTypeTagType,
      formatSourceType,
      sourceTypeTagType,
      formatBillType,
      billTypeTagType,
      formatConfirmStatus,
      confirmStatusTagType,
      formatDeleteStatus,
      deleteStatusTagType,
      // Get data list
      getDataList () {
        this.dataListLoading = true
        this.$http({
          url: this.$http.adornUrl('/order/order/list'),
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
            url: this.$http.adornUrl('/order/order/delete'),
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
