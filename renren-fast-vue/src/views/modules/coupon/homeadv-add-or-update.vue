<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
    <el-form-item label="Name" prop="name">
      <el-input v-model="dataForm.name" placeholder="Name"></el-input>
    </el-form-item>
    <el-form-item label="Image URL" prop="pic">
      <el-input v-model="dataForm.pic" placeholder="Image URL"></el-input>
    </el-form-item>
    <el-form-item label="Start time" prop="startTime">
      <el-input v-model="dataForm.startTime" placeholder="Start time"></el-input>
    </el-form-item>
    <el-form-item label="End time" prop="endTime">
      <el-input v-model="dataForm.endTime" placeholder="End time"></el-input>
    </el-form-item>
    <el-form-item label="Status" prop="status">
      <el-input v-model="dataForm.status" placeholder="Status"></el-input>
    </el-form-item>
    <el-form-item label="Click count" prop="clickCount">
      <el-input v-model="dataForm.clickCount" placeholder="Click count"></el-input>
    </el-form-item>
    <el-form-item label="Ad detail URL" prop="url">
      <el-input v-model="dataForm.url" placeholder="Ad detail URL"></el-input>
    </el-form-item>
    <el-form-item label="Remark" prop="note">
      <el-input v-model="dataForm.note" placeholder="Remark"></el-input>
    </el-form-item>
    <el-form-item label="Sort" prop="sort">
      <el-input v-model="dataForm.sort" placeholder="Sort"></el-input>
    </el-form-item>
    <el-form-item label="Publisher" prop="publisherId">
      <el-input v-model="dataForm.publisherId" placeholder="Publisher"></el-input>
    </el-form-item>
    <el-form-item label="Reviewer" prop="authId">
      <el-input v-model="dataForm.authId" placeholder="Reviewer"></el-input>
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
          pic: '',
          startTime: '',
          endTime: '',
          status: '',
          clickCount: '',
          url: '',
          note: '',
          sort: '',
          publisherId: '',
          authId: ''
        },
        dataRule: {
          name: [
            { required: true, message: 'This field is required', trigger: 'blur' }
          ],
          pic: [
            { required: true, message: 'This field is required', trigger: 'blur' }
          ],
          startTime: [
            { required: true, message: 'This field is required', trigger: 'blur' }
          ],
          endTime: [
            { required: true, message: 'This field is required', trigger: 'blur' }
          ],
          status: [
            { required: true, message: 'Status is required', trigger: 'blur' }
          ],
          clickCount: [
            { required: true, message: 'This field is required', trigger: 'blur' }
          ],
          url: [
            { required: true, message: 'This field is required', trigger: 'blur' }
          ],
          note: [
            { required: true, message: 'Remark is required', trigger: 'blur' }
          ],
          sort: [
            { required: true, message: 'Sort is required', trigger: 'blur' }
          ],
          publisherId: [
            { required: true, message: 'This field is required', trigger: 'blur' }
          ],
          authId: [
            { required: true, message: 'This field is required', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/coupon/homeadv/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.name = data.homeAdv.name
                this.dataForm.pic = data.homeAdv.pic
                this.dataForm.startTime = data.homeAdv.startTime
                this.dataForm.endTime = data.homeAdv.endTime
                this.dataForm.status = data.homeAdv.status
                this.dataForm.clickCount = data.homeAdv.clickCount
                this.dataForm.url = data.homeAdv.url
                this.dataForm.note = data.homeAdv.note
                this.dataForm.sort = data.homeAdv.sort
                this.dataForm.publisherId = data.homeAdv.publisherId
                this.dataForm.authId = data.homeAdv.authId
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
              url: this.$http.adornUrl(`/coupon/homeadv/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'name': this.dataForm.name,
                'pic': this.dataForm.pic,
                'startTime': this.dataForm.startTime,
                'endTime': this.dataForm.endTime,
                'status': this.dataForm.status,
                'clickCount': this.dataForm.clickCount,
                'url': this.dataForm.url,
                'note': this.dataForm.note,
                'sort': this.dataForm.sort,
                'publisherId': this.dataForm.publisherId,
                'authId': this.dataForm.authId
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
