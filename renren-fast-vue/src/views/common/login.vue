<template>
  <div class="site-wrapper site-page--login">
    <div class="site-content__wrapper">
      <div class="site-content">
        <div class="brand-info">
          <h2 class="brand-info__text">{{ adminBrand }} Admin</h2>
          <p class="brand-info__intro">Manage products, orders, members, and inventory for the GrainMart storefront.</p>
        </div>
        <div class="login-main">
          <h3 class="login-title">Admin Login</h3>
          <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" status-icon>
            <el-form-item prop="userName">
              <el-input v-model="dataForm.userName" placeholder="Username"></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="dataForm.password" type="password" placeholder="Password"></el-input>
            </el-form-item>
            <el-form-item prop="captcha">
              <el-row :gutter="20">
                <el-col :span="14">
                  <el-input v-model="dataForm.captcha" placeholder="Verification Code">
                  </el-input>
                </el-col>
                <el-col :span="10" class="login-captcha">
                  <img :key="captchaPath" :src="captchaPath" @click="getCaptcha()" alt="" title="Click to refresh">
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item>
              <el-button class="login-btn-submit" type="primary" @click="dataFormSubmit()">Login</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import { getUUID } from '@/utils'
  import { setToken } from '@/utils/authToken'
  import { ADMIN_BRAND } from '@/constants/brand'
  export default {
    data () {
      return {
        adminBrand: ADMIN_BRAND,
        dataForm: {
          userName: '',
          password: '',
          uuid: '',
          captcha: ''
        },
        dataRule: {
          userName: [
            { required: true, message: 'Username cannot be empty', trigger: 'blur' }
          ],
          password: [
            { required: true, message: 'Password cannot be empty', trigger: 'blur' }
          ],
          captcha: [
            { required: true, message: 'Verification code cannot be empty', trigger: 'blur' }
          ]
        },
        captchaPath: ''
      }
    },
    created () {
      this.getCaptcha()
    },
    methods: {
      // submit form
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl('/sys/login'),
              method: 'post',
              data: this.$http.adornData({
                'username': this.dataForm.userName,
                'password': this.dataForm.password,
                'uuid': this.dataForm.uuid,
                'captcha': this.dataForm.captcha
              })
            }).then(({ data }) => {
              const ok = data && Number(data.code) === 0
              const token = (data && (data.token || (data.data && data.data.token))) || ''
              if (ok && token) {
                setToken(token)
                // 等存储写入后再进需鉴权路由，避免守卫里读不到 token 被立即打回登录页
                this.$nextTick(() => {
                  this.$router.replace({ name: 'home' }).catch(() => {})
                })
              } else {
                this.getCaptcha()
                this.$message.error((data && data.msg) || (ok ? 'Login succeeded but no token returned; check gateway/backend' : 'Login failed'))
              }
            }).catch(() => {
              this.getCaptcha()
              this.$message.error('Network error. Please try again later.')
            })
          }
        })
      },
      // 与登录等接口一致走 /api（devServer 代理到网关）；勿硬编码 88，kind 本地网关为 3088
      getCaptcha () {
        this.dataForm.uuid = getUUID()
        const q = `uuid=${this.dataForm.uuid}&_t=${Date.now()}`
        this.captchaPath = this.$http.adornUrl(`/captcha.jpg?${q}`)
      }
    }
  }
</script>

<style lang="scss">
  .site-wrapper.site-page--login {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    background-color: rgba(38, 50, 56, .6);
    overflow: hidden;
    &:before {
      position: fixed;
      top: 0;
      left: 0;
      z-index: -1;
      width: 100%;
      height: 100%;
      content: "";
      /* 使用绝对路径，避免懒加载 chunk 内相对路径解析错误导致背景图请求未发出 */
      background-image: url(/static/img/login_bg.jpg);
      background-size: cover;
    }
    .site-content__wrapper {
      position: absolute;
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
      padding: 0;
      margin: 0;
      overflow-x: hidden;
      overflow-y: auto;
      background-color: transparent;
    }
    .site-content {
      min-height: 100%;
      padding: 30px 500px 30px 30px;
    }
    .brand-info {
      margin: 220px 100px 0 90px;
      color: #fff;
    }
    .brand-info__text {
      margin:  0 0 22px 0;
      font-size: 48px;
      font-weight: 400;
      text-transform : uppercase;
    }
    .brand-info__intro {
      margin: 10px 0;
      font-size: 16px;
      line-height: 1.58;
      opacity: .6;
    }
    .login-main {
      position: absolute;
      top: 0;
      right: 0;
      padding: 150px 60px 180px;
      width: 470px;
      min-height: 100%;
      background-color: #fff;
    }
    .login-title {
      font-size: 16px;
    }
    .login-captcha {
      overflow: hidden;
      > img {
        width: 100%;
        cursor: pointer;
      }
    }
    .login-btn-submit {
      width: 100%;
      margin-top: 38px;
    }
  }
</style>
