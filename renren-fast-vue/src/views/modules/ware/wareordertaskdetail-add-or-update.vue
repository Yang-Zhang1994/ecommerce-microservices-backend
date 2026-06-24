<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
    <el-form-item label="SKU ID" prop="skuId">
      <el-input v-model="dataForm.skuId" placeholder="SKU ID"></el-input>
    </el-form-item>
    <el-form-item label="SKU Name" prop="skuName">
      <el-input v-model="dataForm.skuName" placeholder="SKU Name"></el-input>
    </el-form-item>
    <el-form-item label="Quantity" prop="skuNum">
      <el-input v-model="dataForm.skuNum" placeholder="Quantity"></el-input>
    </el-form-item>
    <el-form-item label="Task ID" prop="taskId">
      <el-input v-model="dataForm.taskId" placeholder="Task ID"></el-input>
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
          skuId: '',
          skuName: '',
          skuNum: '',
          taskId: ''
        },
        dataRule: {
          skuId: [
            { required: true, message: 'SKU ID is required', trigger: 'blur' }
          ],
          skuName: [
            { required: true, message: 'SKU Name is required', trigger: 'blur' }
          ],
          skuNum: [
            { required: true, message: 'Quantity is required', trigger: 'blur' }
          ],
          taskId: [
            { required: true, message: 'Task ID is required', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/ware/wareordertaskdetail/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.skuId = data.wareOrderTaskDetail.skuId
                this.dataForm.skuName = data.wareOrderTaskDetail.skuName
                this.dataForm.skuNum = data.wareOrderTaskDetail.skuNum
                this.dataForm.taskId = data.wareOrderTaskDetail.taskId
              }
            })
          }
        })
      },
      // form submit
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/ware/wareordertaskdetail/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'skuId': this.dataForm.skuId,
                'skuName': this.dataForm.skuName,
                'skuNum': this.dataForm.skuNum,
                'taskId': this.dataForm.taskId
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
