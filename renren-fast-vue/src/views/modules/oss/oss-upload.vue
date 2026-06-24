<template>
  <el-dialog
    title="Upload File"
    :close-on-click-modal="false"
    @close="closeHandle"
    :visible.sync="visible">
    <el-upload
      drag
      :action="url"
      :before-upload="beforeUploadHandle"
      :on-success="successHandle"
      multiple
      :file-list="fileList"
      style="text-align: center;">
      <i class="el-icon-upload"></i>
      <div class="el-upload__text">Drop file here, or <em>click to upload</em></div>
      <div class="el-upload__tip" slot="tip">JPG, PNG and GIF only.</div>
    </el-upload>
  </el-dialog>
</template>

<script>
  import { getToken } from '@/utils/authToken'
  export default {
    data () {
      return {
        visible: false,
        url: '',
        num: 0,
        successNum: 0,
        fileList: []
      }
    },
    methods: {
      init (id) {
        this.url = this.$http.adornUrl(`/sys/oss/upload?token=${encodeURIComponent(getToken())}`)
        this.visible = true
      },
      // Upload之前
      beforeUploadHandle (file) {
        if (file.type !== 'image/jpg' && file.type !== 'image/jpeg' && file.type !== 'image/png' && file.type !== 'image/gif') {
          this.$message.error('Only JPG, PNG, and GIF images are supported')
          return false
        }
        this.num++
      },
      // UploadSuccess
      successHandle (response, file, fileList) {
        this.fileList = fileList
        this.successNum++
        if (response && response.code === 0) {
          if (this.num === this.successNum) {
            this.$confirm('Operation successful. Continue uploading?', 'Tip', {
              confirmButtonText: 'Confirm',
              cancelButtonText: 'Cancel',
              type: 'warning'
            }).catch(() => {
              this.visible = false
            })
          }
        } else {
          this.$message.error(response.msg)
        }
      },
      // 弹窗Close时
      closeHandle () {
        this.fileList = []
        this.$emit('refreshDataList')
      }
    }
  }
</script>
