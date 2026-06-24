<template>
  <el-dialog
    title="Cloud Storage Config"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
      <el-form-item size="mini" label="Storage Type">
        <el-radio-group v-model="dataForm.type">
          <el-radio :label="1">Qiniu</el-radio>
          <el-radio :label="2">Aliyun</el-radio>
          <el-radio :label="3">Tencent Cloud</el-radio>
        </el-radio-group>
      </el-form-item>
      <template v-if="dataForm.type === 1">
        <el-form-item size="mini">
          <a href="http://www.renren.io/open/qiniu.html" target="_blank">Free Qiniu 10GB storage</a>
        </el-form-item>
        <el-form-item label="Domain">
          <el-input v-model="dataForm.qiniuDomain" placeholder="Qiniubound domain"></el-input>
        </el-form-item>
        <el-form-item label="Path prefix">
          <el-input v-model="dataForm.qiniuPrefix" placeholder="Leave blank for default"></el-input>
        </el-form-item>
        <el-form-item label="AccessKey">
          <el-input v-model="dataForm.qiniuAccessKey" placeholder="QiniuAccessKey"></el-input>
        </el-form-item>
        <el-form-item label="SecretKey">
          <el-input v-model="dataForm.qiniuSecretKey" placeholder="QiniuSecretKey"></el-input>
        </el-form-item>
        <el-form-item label="Bucket name">
          <el-input v-model="dataForm.qiniuBucketName" placeholder="Qiniubucket name"></el-input>
        </el-form-item>
      </template>
      <template v-else-if="dataForm.type === 2">
        <el-form-item label="Domain">
          <el-input v-model="dataForm.aliyunDomain" placeholder="Aliyunbound domain"></el-input>
        </el-form-item>
        <el-form-item label="Path prefix">
          <el-input v-model="dataForm.aliyunPrefix" placeholder="Leave blank for default"></el-input>
        </el-form-item>
        <el-form-item label="EndPoint">
          <el-input v-model="dataForm.aliyunEndPoint" placeholder="AliyunEndPoint"></el-input>
        </el-form-item>
        <el-form-item label="AccessKeyId">
          <el-input v-model="dataForm.aliyunAccessKeyId" placeholder="AliyunAccessKeyId"></el-input>
        </el-form-item>
        <el-form-item label="AccessKeySecret">
          <el-input v-model="dataForm.aliyunAccessKeySecret" placeholder="AliyunAccessKeySecret"></el-input>
        </el-form-item>
        <el-form-item label="BucketName">
          <el-input v-model="dataForm.aliyunBucketName" placeholder="AliyunBucketName"></el-input>
        </el-form-item>
      </template>
      <template v-else-if="dataForm.type === 3">
        <el-form-item label="Domain">
          <el-input v-model="dataForm.qcloudDomain" placeholder="Tencent Cloudbound domain"></el-input>
        </el-form-item>
        <el-form-item label="Path prefix">
          <el-input v-model="dataForm.qcloudPrefix" placeholder="Leave blank for default"></el-input>
        </el-form-item>
        <el-form-item label="AppId">
          <el-input v-model="dataForm.qcloudAppId" placeholder="Tencent CloudAppId"></el-input>
        </el-form-item>
        <el-form-item label="SecretId">
          <el-input v-model="dataForm.qcloudSecretId" placeholder="Tencent CloudSecretId"></el-input>
        </el-form-item>
        <el-form-item label="SecretKey">
          <el-input v-model="dataForm.qcloudSecretKey" placeholder="Tencent CloudSecretKey"></el-input>
        </el-form-item>
        <el-form-item label="BucketName">
          <el-input v-model="dataForm.qcloudBucketName" placeholder="Tencent CloudBucketName"></el-input>
        </el-form-item>
        <el-form-item label="Bucket region">
          <el-input v-model="dataForm.qcloudRegion" placeholder="e.g. sh (gz/tj/sh)"></el-input>
        </el-form-item>
      </template>
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
        dataForm: {},
        dataRule: {}
      }
    },
    methods: {
      init (id) {
        this.visible = true
        this.$http({
          url: this.$http.adornUrl('/sys/oss/config'),
          method: 'get',
          params: this.$http.adornParams()
        }).then(({data}) => {
          this.dataForm = data && data.code === 0 ? data.config : []
        })
      },
      // form submit
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl('/sys/oss/saveConfig'),
              method: 'post',
              data: this.$http.adornData(this.dataForm)
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: 'Operation successful',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
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

