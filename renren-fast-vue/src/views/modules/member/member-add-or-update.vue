<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
    <el-form-item label="Member level ID" prop="levelId">
      <el-input v-model="dataForm.levelId" placeholder="Member level ID"></el-input>
    </el-form-item>
    <el-form-item label="Username" prop="username">
      <el-input v-model="dataForm.username" placeholder="Username"></el-input>
    </el-form-item>
    <el-form-item label="Password" prop="password">
      <el-input v-model="dataForm.password" placeholder="Password"></el-input>
    </el-form-item>
    <el-form-item label="Nickname" prop="nickname">
      <el-input v-model="dataForm.nickname" placeholder="Nickname"></el-input>
    </el-form-item>
    <el-form-item label="Mobile" prop="mobile">
      <el-input v-model="dataForm.mobile" placeholder="Mobile"></el-input>
    </el-form-item>
    <el-form-item label="Email" prop="email">
      <el-input v-model="dataForm.email" placeholder="Email"></el-input>
    </el-form-item>
    <el-form-item label="Avatar" prop="header">
      <el-input v-model="dataForm.header" placeholder="Avatar"></el-input>
    </el-form-item>
    <el-form-item label="Gender" prop="gender">
      <el-input v-model="dataForm.gender" placeholder="Gender"></el-input>
    </el-form-item>
    <el-form-item label="Birthday" prop="birth">
      <el-input v-model="dataForm.birth" placeholder="Birthday"></el-input>
    </el-form-item>
    <el-form-item label="City" prop="city">
      <el-input v-model="dataForm.city" placeholder="City"></el-input>
    </el-form-item>
    <el-form-item label="Occupation" prop="job">
      <el-input v-model="dataForm.job" placeholder="Occupation"></el-input>
    </el-form-item>
    <el-form-item label="Signature" prop="sign">
      <el-input v-model="dataForm.sign" placeholder="Signature"></el-input>
    </el-form-item>
    <el-form-item label="User Source" prop="sourceType">
      <el-input v-model="dataForm.sourceType" placeholder="User Source"></el-input>
    </el-form-item>
    <el-form-item label="Points" prop="integration">
      <el-input v-model="dataForm.integration" placeholder="Points"></el-input>
    </el-form-item>
    <el-form-item label="Growth Value" prop="growth">
      <el-input v-model="dataForm.growth" placeholder="Growth Value"></el-input>
    </el-form-item>
    <el-form-item label="EnableStatus" prop="status">
      <el-input v-model="dataForm.status" placeholder="EnableStatus"></el-input>
    </el-form-item>
    <el-form-item label="Registration Time" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="Registration Time"></el-input>
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
          levelId: '',
          username: '',
          password: '',
          nickname: '',
          mobile: '',
          email: '',
          header: '',
          gender: '',
          birth: '',
          city: '',
          job: '',
          sign: '',
          sourceType: '',
          integration: '',
          growth: '',
          status: '',
          createTime: ''
        },
        dataRule: {
          levelId: [
            { required: true, message: 'Member level ID is required', trigger: 'blur' }
          ],
          username: [
            { required: true, message: 'Username is required', trigger: 'blur' }
          ],
          password: [
            { required: true, message: 'Password is required', trigger: 'blur' }
          ],
          nickname: [
            { required: true, message: 'Nickname is required', trigger: 'blur' }
          ],
          mobile: [
            { required: true, message: 'This field is required', trigger: 'blur' }
          ],
          email: [
            { required: true, message: 'Email is required', trigger: 'blur' }
          ],
          header: [
            { required: true, message: 'Avatar is required', trigger: 'blur' }
          ],
          gender: [
            { required: true, message: 'Gender is required', trigger: 'blur' }
          ],
          birth: [
            { required: true, message: 'Birthday is required', trigger: 'blur' }
          ],
          city: [
            { required: true, message: 'City is required', trigger: 'blur' }
          ],
          job: [
            { required: true, message: 'Occupation is required', trigger: 'blur' }
          ],
          sign: [
            { required: true, message: 'Signature is required', trigger: 'blur' }
          ],
          sourceType: [
            { required: true, message: 'User Source is required', trigger: 'blur' }
          ],
          integration: [
            { required: true, message: 'Points is required', trigger: 'blur' }
          ],
          growth: [
            { required: true, message: 'Growth Value is required', trigger: 'blur' }
          ],
          status: [
            { required: true, message: 'EnableStatus is required', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: 'Registration Time is required', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/member/member/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.levelId = data.member.levelId
                this.dataForm.username = data.member.username
                this.dataForm.password = data.member.password
                this.dataForm.nickname = data.member.nickname
                this.dataForm.mobile = data.member.mobile
                this.dataForm.email = data.member.email
                this.dataForm.header = data.member.header
                this.dataForm.gender = data.member.gender
                this.dataForm.birth = data.member.birth
                this.dataForm.city = data.member.city
                this.dataForm.job = data.member.job
                this.dataForm.sign = data.member.sign
                this.dataForm.sourceType = data.member.sourceType
                this.dataForm.integration = data.member.integration
                this.dataForm.growth = data.member.growth
                this.dataForm.status = data.member.status
                this.dataForm.createTime = data.member.createTime
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
              url: this.$http.adornUrl(`/member/member/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'levelId': this.dataForm.levelId,
                'username': this.dataForm.username,
                'password': this.dataForm.password,
                'nickname': this.dataForm.nickname,
                'mobile': this.dataForm.mobile,
                'email': this.dataForm.email,
                'header': this.dataForm.header,
                'gender': this.dataForm.gender,
                'birth': this.dataForm.birth,
                'city': this.dataForm.city,
                'job': this.dataForm.job,
                'sign': this.dataForm.sign,
                'sourceType': this.dataForm.sourceType,
                'integration': this.dataForm.integration,
                'growth': this.dataForm.growth,
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
