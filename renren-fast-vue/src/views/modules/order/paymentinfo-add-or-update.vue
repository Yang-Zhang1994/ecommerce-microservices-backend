<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
    <el-form-item label="Order SN" prop="orderSn">
      <el-input v-model="dataForm.orderSn" placeholder="Order SN"></el-input>
    </el-form-item>
    <el-form-item label="Order ID" prop="orderId">
      <el-input v-model="dataForm.orderId" placeholder="Order ID"></el-input>
    </el-form-item>
    <el-form-item label="Alipay Trade No." prop="alipayTradeNo">
      <el-input v-model="dataForm.alipayTradeNo" placeholder="Alipay Trade No."></el-input>
    </el-form-item>
    <el-form-item label="Total Paid" prop="totalAmount">
      <el-input v-model="dataForm.totalAmount" placeholder="Total Paid"></el-input>
    </el-form-item>
    <el-form-item label="Subject" prop="subject">
      <el-input v-model="dataForm.subject" placeholder="Subject"></el-input>
    </el-form-item>
    <el-form-item label="Payment Status" prop="paymentStatus">
      <el-input v-model="dataForm.paymentStatus" placeholder="Payment Status"></el-input>
    </el-form-item>
    <el-form-item label="Create Time" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="Create Time"></el-input>
    </el-form-item>
    <el-form-item label="Confirm Time" prop="confirmTime">
      <el-input v-model="dataForm.confirmTime" placeholder="Confirm Time"></el-input>
    </el-form-item>
    <el-form-item label="Callback Content" prop="callbackContent">
      <el-input v-model="dataForm.callbackContent" placeholder="Callback Content"></el-input>
    </el-form-item>
    <el-form-item label="Callback Time" prop="callbackTime">
      <el-input v-model="dataForm.callbackTime" placeholder="Callback Time"></el-input>
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
          orderSn: '',
          orderId: '',
          alipayTradeNo: '',
          totalAmount: '',
          subject: '',
          paymentStatus: '',
          createTime: '',
          confirmTime: '',
          callbackContent: '',
          callbackTime: ''
        },
        dataRule: {
          orderSn: [
            { required: true, message: 'Order SN is required', trigger: 'blur' }
          ],
          orderId: [
            { required: true, message: 'Order ID is required', trigger: 'blur' }
          ],
          alipayTradeNo: [
            { required: true, message: 'Alipay Trade No. is required', trigger: 'blur' }
          ],
          totalAmount: [
            { required: true, message: 'Total Paid is required', trigger: 'blur' }
          ],
          subject: [
            { required: true, message: 'Subject is required', trigger: 'blur' }
          ],
          paymentStatus: [
            { required: true, message: 'Payment Status is required', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: 'Create Time is required', trigger: 'blur' }
          ],
          confirmTime: [
            { required: true, message: 'Confirm Time is required', trigger: 'blur' }
          ],
          callbackContent: [
            { required: true, message: 'Callback Content is required', trigger: 'blur' }
          ],
          callbackTime: [
            { required: true, message: 'Callback Time is required', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/order/paymentinfo/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.orderSn = data.paymentInfo.orderSn
                this.dataForm.orderId = data.paymentInfo.orderId
                this.dataForm.alipayTradeNo = data.paymentInfo.alipayTradeNo
                this.dataForm.totalAmount = data.paymentInfo.totalAmount
                this.dataForm.subject = data.paymentInfo.subject
                this.dataForm.paymentStatus = data.paymentInfo.paymentStatus
                this.dataForm.createTime = data.paymentInfo.createTime
                this.dataForm.confirmTime = data.paymentInfo.confirmTime
                this.dataForm.callbackContent = data.paymentInfo.callbackContent
                this.dataForm.callbackTime = data.paymentInfo.callbackTime
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
              url: this.$http.adornUrl(`/order/paymentinfo/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'orderSn': this.dataForm.orderSn,
                'orderId': this.dataForm.orderId,
                'alipayTradeNo': this.dataForm.alipayTradeNo,
                'totalAmount': this.dataForm.totalAmount,
                'subject': this.dataForm.subject,
                'paymentStatus': this.dataForm.paymentStatus,
                'createTime': this.dataForm.createTime,
                'confirmTime': this.dataForm.confirmTime,
                'callbackContent': this.dataForm.callbackContent,
                'callbackTime': this.dataForm.callbackTime
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
