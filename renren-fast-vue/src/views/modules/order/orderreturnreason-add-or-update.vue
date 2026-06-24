<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
    <el-form-item label="Return Reason" prop="name">
      <el-input v-model="dataForm.name" placeholder="Return Reason"></el-input>
    </el-form-item>
    <el-form-item label="Sort" prop="sort">
      <el-input v-model="dataForm.sort" placeholder="Sort"></el-input>
    </el-form-item>
    <el-form-item label="Status" prop="status">
      <el-input v-model="dataForm.status" placeholder="Status"></el-input>
    </el-form-item>
    <el-form-item label="create_time" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="create_time"></el-input>
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
          name: '',
          sort: '',
          status: '',
          createTime: ''
        },
        dataRule: {
          name: [
            { required: true, message: 'Return Reason is required', trigger: 'blur' }
          ],
          sort: [
            { required: true, message: 'Sort is required', trigger: 'blur' }
          ],
          status: [
            { required: true, message: 'Status is required', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: 'create_time is required', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/order/orderreturnreason/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.name = data.orderReturnReason.name
                this.dataForm.sort = data.orderReturnReason.sort
                this.dataForm.status = data.orderReturnReason.status
                this.dataForm.createTime = data.orderReturnReason.createTime
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
              url: this.$http.adornUrl(`/order/orderreturnreason/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'name': this.dataForm.name,
                'sort': this.dataForm.sort,
                'status': this.dataForm.status,
                'createTime': this.dataForm.createTime
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
