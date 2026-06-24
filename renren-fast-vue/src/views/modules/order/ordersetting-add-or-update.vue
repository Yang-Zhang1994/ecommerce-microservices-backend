<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
    <el-form-item label="Flash Sale Timeout (min)" prop="flashOrderOvertime">
      <el-input v-model="dataForm.flashOrderOvertime" placeholder="Flash Sale Timeout (min)"></el-input>
    </el-form-item>
    <el-form-item label="Normal Order Timeout (min)" prop="normalOrderOvertime">
      <el-input v-model="dataForm.normalOrderOvertime" placeholder="Normal Order Timeout (min)"></el-input>
    </el-form-item>
    <el-form-item label="Auto Confirm After Ship (days)" prop="confirmOvertime">
      <el-input v-model="dataForm.confirmOvertime" placeholder="Auto Confirm After Ship (days)"></el-input>
    </el-form-item>
    <el-form-item label="Auto Finish / No Returns (days)" prop="finishOvertime">
      <el-input v-model="dataForm.finishOvertime" placeholder="Auto Finish / No Returns (days)"></el-input>
    </el-form-item>
    <el-form-item label="Auto Review After Complete (days)" prop="commentOvertime">
      <el-input v-model="dataForm.commentOvertime" placeholder="Auto Review After Complete (days)"></el-input>
    </el-form-item>
    <el-form-item label="Member Level [0-All]" prop="memberLevel">
      <el-input v-model="dataForm.memberLevel" placeholder="Member Level [0-All]"></el-input>
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
          flashOrderOvertime: '',
          normalOrderOvertime: '',
          confirmOvertime: '',
          finishOvertime: '',
          commentOvertime: '',
          memberLevel: ''
        },
        dataRule: {
          flashOrderOvertime: [
            { required: true, message: 'Flash Sale Timeout (min) is required', trigger: 'blur' }
          ],
          normalOrderOvertime: [
            { required: true, message: 'Normal Order Timeout (min) is required', trigger: 'blur' }
          ],
          confirmOvertime: [
            { required: true, message: 'Auto Confirm After Ship (days) is required', trigger: 'blur' }
          ],
          finishOvertime: [
            { required: true, message: 'Auto Finish / No Returns (days) is required', trigger: 'blur' }
          ],
          commentOvertime: [
            { required: true, message: 'Auto Review After Complete (days) is required', trigger: 'blur' }
          ],
          memberLevel: [
            { required: true, message: 'Member Level [0-All] is required', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/order/ordersetting/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.flashOrderOvertime = data.orderSetting.flashOrderOvertime
                this.dataForm.normalOrderOvertime = data.orderSetting.normalOrderOvertime
                this.dataForm.confirmOvertime = data.orderSetting.confirmOvertime
                this.dataForm.finishOvertime = data.orderSetting.finishOvertime
                this.dataForm.commentOvertime = data.orderSetting.commentOvertime
                this.dataForm.memberLevel = data.orderSetting.memberLevel
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
              url: this.$http.adornUrl(`/order/ordersetting/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'flashOrderOvertime': this.dataForm.flashOrderOvertime,
                'normalOrderOvertime': this.dataForm.normalOrderOvertime,
                'confirmOvertime': this.dataForm.confirmOvertime,
                'finishOvertime': this.dataForm.finishOvertime,
                'commentOvertime': this.dataForm.commentOvertime,
                'memberLevel': this.dataForm.memberLevel
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
