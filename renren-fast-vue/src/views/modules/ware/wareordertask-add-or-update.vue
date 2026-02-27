<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="180px">
    <el-form-item label="Order ID" prop="orderId">
      <el-input v-model="dataForm.orderId" placeholder="Order ID"></el-input>
    </el-form-item>
    <el-form-item label="Order SN" prop="orderSn">
      <el-input v-model="dataForm.orderSn" placeholder="Order SN"></el-input>
    </el-form-item>
    <el-form-item label="Consignee" prop="consignee">
      <el-input v-model="dataForm.consignee" placeholder="Consignee"></el-input>
    </el-form-item>
    <el-form-item label="Consignee Phone" prop="consigneeTel">
      <el-input v-model="dataForm.consigneeTel" placeholder="Consignee Phone"></el-input>
    </el-form-item>
    <el-form-item label="Delivery Address" prop="deliveryAddress">
      <el-input v-model="dataForm.deliveryAddress" placeholder="Delivery Address"></el-input>
    </el-form-item>
    <el-form-item label="Order Comment" prop="orderComment">
      <el-input v-model="dataForm.orderComment" placeholder="Order Comment"></el-input>
    </el-form-item>
    <el-form-item label="Payment Method [1:Online 2:COD]" prop="paymentWay">
      <el-input v-model="dataForm.paymentWay" placeholder="Payment Method [1:Online Payment 2:Cash on Delivery]"></el-input>
    </el-form-item>
    <el-form-item label="Task Status" prop="taskStatus">
      <el-input v-model="dataForm.taskStatus" placeholder="Task Status"></el-input>
    </el-form-item>
    <el-form-item label="Order Description" prop="orderBody">
      <el-input v-model="dataForm.orderBody" placeholder="Order Description"></el-input>
    </el-form-item>
    <el-form-item label="Tracking Number" prop="trackingNo">
      <el-input v-model="dataForm.trackingNo" placeholder="Tracking Number"></el-input>
    </el-form-item>
    <el-form-item label="Create Time" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="Create Time"></el-input>
    </el-form-item>
    <el-form-item label="Warehouse ID" prop="wareId">
      <el-input v-model="dataForm.wareId" placeholder="Warehouse ID"></el-input>
    </el-form-item>
    <el-form-item label="Task Comment" prop="taskComment">
      <el-input v-model="dataForm.taskComment" placeholder="Task Comment"></el-input>
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
          orderSn: '',
          consignee: '',
          consigneeTel: '',
          deliveryAddress: '',
          orderComment: '',
          paymentWay: '',
          taskStatus: '',
          orderBody: '',
          trackingNo: '',
          createTime: '',
          wareId: '',
          taskComment: ''
        },
        dataRule: {
          orderId: [
            { required: true, message: 'Order ID is required', trigger: 'blur' }
          ],
          orderSn: [
            { required: true, message: 'Order SN is required', trigger: 'blur' }
          ],
          consignee: [
            { required: true, message: 'Consignee is required', trigger: 'blur' }
          ],
          consigneeTel: [
            { required: true, message: 'Consignee phone is required', trigger: 'blur' }
          ],
          deliveryAddress: [
            { required: true, message: 'Delivery address is required', trigger: 'blur' }
          ],
          orderComment: [
            { required: true, message: 'Order comment is required', trigger: 'blur' }
          ],
          paymentWay: [
            { required: true, message: 'Payment method is required', trigger: 'blur' }
          ],
          taskStatus: [
            { required: true, message: 'Task status is required', trigger: 'blur' }
          ],
          orderBody: [
            { required: true, message: 'Order description is required', trigger: 'blur' }
          ],
          trackingNo: [
            { required: true, message: 'Tracking number is required', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: 'Create time is required', trigger: 'blur' }
          ],
          wareId: [
            { required: true, message: 'Warehouse ID is required', trigger: 'blur' }
          ],
          taskComment: [
            { required: true, message: 'Task comment is required', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/ware/wareordertask/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.orderId = data.wareOrderTask.orderId
                this.dataForm.orderSn = data.wareOrderTask.orderSn
                this.dataForm.consignee = data.wareOrderTask.consignee
                this.dataForm.consigneeTel = data.wareOrderTask.consigneeTel
                this.dataForm.deliveryAddress = data.wareOrderTask.deliveryAddress
                this.dataForm.orderComment = data.wareOrderTask.orderComment
                this.dataForm.paymentWay = data.wareOrderTask.paymentWay
                this.dataForm.taskStatus = data.wareOrderTask.taskStatus
                this.dataForm.orderBody = data.wareOrderTask.orderBody
                this.dataForm.trackingNo = data.wareOrderTask.trackingNo
                this.dataForm.createTime = data.wareOrderTask.createTime
                this.dataForm.wareId = data.wareOrderTask.wareId
                this.dataForm.taskComment = data.wareOrderTask.taskComment
              }
            })
          }
        })
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/ware/wareordertask/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'orderId': this.dataForm.orderId,
                'orderSn': this.dataForm.orderSn,
                'consignee': this.dataForm.consignee,
                'consigneeTel': this.dataForm.consigneeTel,
                'deliveryAddress': this.dataForm.deliveryAddress,
                'orderComment': this.dataForm.orderComment,
                'paymentWay': this.dataForm.paymentWay,
                'taskStatus': this.dataForm.taskStatus,
                'orderBody': this.dataForm.orderBody,
                'trackingNo': this.dataForm.trackingNo,
                'createTime': this.dataForm.createTime,
                'wareId': this.dataForm.wareId,
                'taskComment': this.dataForm.taskComment
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
