<template>
  <el-dialog
    class="coupon-marketing-dialog"
    width="560px"
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form
      class="coupon-marketing-form"
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      @keyup.enter.native="dataFormSubmit()"
      label-width="200px">
    <el-form-item label="spuId" prop="spuId">
      <el-input v-model="dataForm.spuId" placeholder=""></el-input>
    </el-form-item>
    <el-form-item label="Growth Points" prop="growBounds">
      <el-input v-model="dataForm.growBounds" placeholder="Growth Points"></el-input>
    </el-form-item>
    <el-form-item label="Shopping Points" prop="buyBounds">
      <el-input v-model="dataForm.buyBounds" placeholder="Shopping Points"></el-input>
    </el-form-item>
    <!-- [1111: four status bits, right to left; 0/1 no promo grow/shop points; 2/3 with promo; bit 0=off 1=on] -->
    <el-form-item label="Promotion Status" prop="work">
      <el-input v-model="dataForm.work" placeholder="Promotion Status"></el-input>
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
          spuId: '',
          growBounds: '',
          buyBounds: '',
          work: ''
        },
        dataRule: {
          spuId: [
            { required: true, message: 'SPU ID is required', trigger: 'blur' }
          ],
          growBounds: [
            { required: true, message: 'Growth Points is required', trigger: 'blur' }
          ],
          buyBounds: [
            { required: true, message: 'Shopping Points is required', trigger: 'blur' }
          ],
          work: [
            { required: true, message: 'Promotion Status is required', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/coupon/spubounds/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.spuId = data.spuBounds.spuId
                this.dataForm.growBounds = data.spuBounds.growBounds
                this.dataForm.buyBounds = data.spuBounds.buyBounds
                this.dataForm.work = data.spuBounds.work
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
              url: this.$http.adornUrl(`/coupon/spubounds/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'spuId': this.dataForm.spuId,
                'growBounds': this.dataForm.growBounds,
                'buyBounds': this.dataForm.buyBounds,
                'work': this.dataForm.work
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
