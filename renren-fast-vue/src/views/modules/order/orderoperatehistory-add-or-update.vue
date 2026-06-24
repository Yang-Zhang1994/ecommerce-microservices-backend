<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
    <el-form-item label="Order ID" prop="orderId">
      <el-input v-model="dataForm.orderId" placeholder="Order ID"></el-input>
    </el-form-item>
    <el-form-item label="Operator" prop="operateMan">
      <el-input v-model="dataForm.operateMan" placeholder="Operator"></el-input>
    </el-form-item>
    <el-form-item label="Operate Time" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="Operate Time"></el-input>
    </el-form-item>
    <el-form-item label="Order Status [0-Pending 1-To ship 2-Shipped 3-Done 4-Closed 5-Invalid]" prop="orderStatus">
      <el-input v-model="dataForm.orderStatus" placeholder="Order Status [0-Pending 1-To ship 2-Shipped 3-Done 4-Closed 5-Invalid]"></el-input>
    </el-form-item>
    <el-form-item label="Note" prop="note">
      <el-input v-model="dataForm.note" placeholder="Note"></el-input>
    </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">Cancel</el-button>
      <el-button type="primary" @click="dataFormSubmit()">Confirm</el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        dataForm: {
          id: 0,
          orderId: '',
          operateMan: '',
          createTime: '',
          orderStatus: '',
          note: ''
        },
        dataRule: {
          orderId: [
            { required: true, message: 'Order ID is required', trigger: 'blur' }
          ],
          operateMan: [
            { required: true, message: 'Operator is required', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: 'Operate Time is required', trigger: 'blur' }
          ],
          orderStatus: [
            { required: true, message: 'Order Status [0-Pending 1-To ship 2-Shipped 3-Done 4-Closed 5-Invalid] is required', trigger: 'blur' }
          ],
          note: [
            { required: true, message: 'Note is required', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/order/orderoperatehistory/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.orderId = data.orderOperateHistory.orderId
                this.dataForm.operateMan = data.orderOperateHistory.operateMan
                this.dataForm.createTime = data.orderOperateHistory.createTime
                this.dataForm.orderStatus = data.orderOperateHistory.orderStatus
                this.dataForm.note = data.orderOperateHistory.note
              }
            })
          }
        })
      },
      // Form submit
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/order/orderoperatehistory/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'orderId': this.dataForm.orderId,
                'operateMan': this.dataForm.operateMan,
                'createTime': this.dataForm.createTime,
                'orderStatus': this.dataForm.orderStatus,
                'note': this.dataForm.note
              })
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: 'Operation successful',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>
