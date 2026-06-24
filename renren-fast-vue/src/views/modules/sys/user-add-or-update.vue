<template>
  <el-dialog
    :title="!dataForm.id ? 'Add New' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form
      class="user-add-form"
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      @keyup.enter.native="dataFormSubmit()"
      label-width="150px">
      <el-form-item label="Username" prop="userName">
        <el-input v-model="dataForm.userName" placeholder="Login Account"></el-input>
      </el-form-item>
      <el-form-item label="Password" prop="password" :class="{ 'is-required': !dataForm.id }">
        <el-input v-model="dataForm.password" type="password" placeholder="Password"></el-input>
      </el-form-item>
      <el-form-item label="Confirm Password" prop="comfirmPassword" :class="{ 'is-required': !dataForm.id }">
        <el-input v-model="dataForm.comfirmPassword" type="password" placeholder="Confirm Password"></el-input>
      </el-form-item>
      <el-form-item label="Email" prop="email">
        <el-input v-model="dataForm.email" placeholder="Email"></el-input>
      </el-form-item>
      <el-form-item label="Phone Number" prop="mobile">
        <el-input v-model="dataForm.mobile" placeholder="Phone Number"></el-input>
      </el-form-item>
      <el-form-item size="mini" prop="roleIdList" class="user-add-form__role-row">
        <template slot="label">
          <span class="role-status-labels">
            <span>Role</span>
            <span>Status</span>
          </span>
        </template>
        <div class="role-status-row">
          <el-checkbox-group v-model="dataForm.roleIdList" class="role-status-row__roles">
            <el-checkbox v-for="role in roleList" :key="role.roleId" :label="role.roleId">{{ role.roleName }}</el-checkbox>
          </el-checkbox-group>
          <el-radio-group v-model="dataForm.status" size="mini" class="role-status-row__radios">
            <el-radio :label="0">Disabled</el-radio>
            <el-radio :label="1">Normal</el-radio>
          </el-radio-group>
        </div>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">Cancel</el-button>
      <el-button type="primary" @click="dataFormSubmit()">Confirm</el-button>
    </span>
  </el-dialog>
</template>

<script>
  import { isEmail, isMobile } from '@/utils/validate'
  export default {
    data () {
      var validatePassword = (rule, value, callback) => {
        if (!this.dataForm.id && !/\S/.test(value)) {
          callback(new Error('Password cannot be empty'))
        } else {
          callback()
        }
      }
      var validateComfirmPassword = (rule, value, callback) => {
        if (!this.dataForm.id && !/\S/.test(value)) {
          callback(new Error('Confirm password cannot be empty'))
        } else if (this.dataForm.password !== value) {
          callback(new Error('Confirm password does not match password'))
        } else {
          callback()
        }
      }
      var validateEmail = (rule, value, callback) => {
        if (!isEmail(value)) {
          callback(new Error('Invalid email format'))
        } else {
          callback()
        }
      }
      var validateMobile = (rule, value, callback) => {
        if (!isMobile(value)) {
          callback(new Error('Invalid phone number format'))
        } else {
          callback()
        }
      }
      return {
        visible: false,
        roleList: [],
        dataForm: {
          id: 0,
          userName: '',
          password: '',
          comfirmPassword: '',
          salt: '',
          email: '',
          mobile: '',
          roleIdList: [],
          status: 1
        },
        dataRule: {
          userName: [
            { required: true, message: 'Username cannot be empty', trigger: 'blur' }
          ],
          password: [
            { validator: validatePassword, trigger: 'blur' }
          ],
          comfirmPassword: [
            { validator: validateComfirmPassword, trigger: 'blur' }
          ],
          email: [
            { required: true, message: 'Email cannot be empty', trigger: 'blur' },
            { validator: validateEmail, trigger: 'blur' }
          ],
          mobile: [
            { required: true, message: 'Phone number cannot be empty', trigger: 'blur' },
            { validator: validateMobile, trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.$http({
          url: this.$http.adornUrl('/sys/role/select'),
          method: 'get',
          params: this.$http.adornParams()
        }).then(({data}) => {
          this.roleList = data && data.code === 0 ? data.list : []
        }).then(() => {
          this.visible = true
          this.$nextTick(() => {
            this.$refs['dataForm'].resetFields()
          })
        }).then(() => {
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/sys/user/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.userName = data.user.username
                this.dataForm.salt = data.user.salt
                this.dataForm.email = data.user.email
                this.dataForm.mobile = data.user.mobile
                this.dataForm.roleIdList = data.user.roleIdList
                this.dataForm.status = data.user.status
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
              url: this.$http.adornUrl(`/sys/user/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'userId': this.dataForm.id || undefined,
                'username': this.dataForm.userName,
                'password': this.dataForm.password,
                'salt': this.dataForm.salt,
                'email': this.dataForm.email,
                'mobile': this.dataForm.mobile,
                'status': this.dataForm.status,
                'roleIdList': this.dataForm.roleIdList
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

<style scoped>
.user-add-form >>> .el-form-item__label {
  white-space: nowrap;
}
.user-add-form__role-row >>> .el-form-item__label {
  line-height: 28px;
}
.user-add-form__role-row >>> .el-form-item__content {
  flex: 1;
  line-height: 28px;
}
.role-status-labels {
  display: inline-flex;
  align-items: center;
  gap: 20px;
  white-space: nowrap;
}
.role-status-row {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 20px;
}
.role-status-row__roles {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 12px;
}
.role-status-row__radios {
  white-space: nowrap;
}
</style>
