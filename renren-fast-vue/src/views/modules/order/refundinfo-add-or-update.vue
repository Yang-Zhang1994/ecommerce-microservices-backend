<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
    <el-form-item label="Return Order ID" prop="orderReturnId">
      <el-input v-model="dataForm.orderReturnId" placeholder="Return Order ID"></el-input>
    </el-form-item>
    <el-form-item label="Refund Amount" prop="refund">
      <el-input v-model="dataForm.refund" placeholder="Refund Amount"></el-input>
    </el-form-item>
    <el-form-item label="Refund Transaction No." prop="refundSn">
      <el-input v-model="dataForm.refundSn" placeholder="Refund Transaction No."></el-input>
    </el-form-item>
    <el-form-item label="Refund Status" prop="refundStatus">
      <el-input v-model="dataForm.refundStatus" placeholder="Refund Status"></el-input>
    </el-form-item>
    <el-form-item label="Refund Channel [1-Alipay 2-WeChat 3-UnionPay 4-Transfer]" prop="refundChannel">
      <el-input v-model="dataForm.refundChannel" placeholder="Refund Channel [1-Alipay 2-WeChat 3-UnionPay 4-Transfer]"></el-input>
    </el-form-item>
    <el-form-item label="" prop="refundContent">
      <el-input v-model="dataForm.refundContent" placeholder=""></el-input>
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
          orderReturnId: '',
          refund: '',
          refundSn: '',
          refundStatus: '',
          refundChannel: '',
          refundContent: ''
        },
        dataRule: {
          orderReturnId: [
            { required: true, message: 'Return Order ID is required', trigger: 'blur' }
          ],
          refund: [
            { required: true, message: 'Refund Amount is required', trigger: 'blur' }
          ],
          refundSn: [
            { required: true, message: 'Refund Transaction No. is required', trigger: 'blur' }
          ],
          refundStatus: [
            { required: true, message: 'Refund Status is required', trigger: 'blur' }
          ],
          refundChannel: [
            { required: true, message: 'Refund Channel [1-Alipay 2-WeChat 3-UnionPay 4-Transfer] is required', trigger: 'blur' }
          ],
          refundContent: [
            { required: true, message: ' is required', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/order/refundinfo/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.orderReturnId = data.refundInfo.orderReturnId
                this.dataForm.refund = data.refundInfo.refund
                this.dataForm.refundSn = data.refundInfo.refundSn
                this.dataForm.refundStatus = data.refundInfo.refundStatus
                this.dataForm.refundChannel = data.refundInfo.refundChannel
                this.dataForm.refundContent = data.refundInfo.refundContent
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
              url: this.$http.adornUrl(`/order/refundinfo/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'orderReturnId': this.dataForm.orderReturnId,
                'refund': this.dataForm.refund,
                'refundSn': this.dataForm.refundSn,
                'refundStatus': this.dataForm.refundStatus,
                'refundChannel': this.dataForm.refundChannel,
                'refundContent': this.dataForm.refundContent
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
