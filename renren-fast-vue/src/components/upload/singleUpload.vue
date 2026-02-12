<template>
  <div>
    <el-upload
      :action="uploadAction"
      :data="dataObj"
      list-type="picture"
      :multiple="false"
      :show-file-list="showFileList"
      :file-list="fileList"
      :before-upload="beforeUpload"
      :on-remove="handleRemove"
      :on-success="handleUploadSuccess"
      :on-preview="handlePreview"
      :http-request="httpRequest">
      <el-button size="small" type="primary">Click to upload</el-button>
      <div slot="tip" class="el-upload__tip">Only jpg/png files can be uploaded, and the size should not exceed 10MB</div>
    </el-upload>
    <el-dialog :visible.sync="dialogVisible">
      <img width="100%" :src="fileList[0].url" alt="">
    </el-dialog>
  </div>
</template>
<script>
   import { getS3PresignedUrl } from './s3policy'

  export default {
    name: 'singleUpload',
    props: {
      value: String
    },
    computed: {
      uploadAction() {
        return '';
      },
      imageUrl() {
        return this.value;
      },
      imageName() {
        if (this.value != null && this.value !== '') {
          return this.value.substr(this.value.lastIndexOf("/") + 1);
        } else {
          return null;
        }
      },
      fileList() {
        return [{
          name: this.imageName,
          url: this.imageUrl
        }]
      },
      showFileList: {
        get: function () {
          return this.value !== null && this.value !== ''&& this.value!==undefined;
        },
        set: function (newValue) {
        }
      }
    },
    data() {
      return {
        dataObj: {},
        dialogVisible: false
      };
    },
    methods: {
      emitInput(val) {
        this.$emit('input', val)
      },
      handleRemove(file, fileList) {
        this.emitInput('');
      },
      handlePreview(file) {
        this.dialogVisible = true;
      },
      beforeUpload(file) {
        const isJpgPng = file.type === 'image/jpeg' || file.type === 'image/png';
        if (!isJpgPng) {
          this.$message.error('Only jpg/png files can be uploaded');
          return Promise.reject();
        }
        const isLt10M = file.size / 1024 / 1024 < 10;
        if (!isLt10M) {
          this.$message.error('File size must be less than 10MB');
          return Promise.reject();
        }
        return Promise.resolve(true);
      },
      httpRequest(options) {
        this.uploadToS3(options);
      },
      uploadToS3(options) {
        const { file, onSuccess, onError } = options;
        getS3PresignedUrl(file.name)
          .then(({ uploadUrl, fileUrl }) => {
            // 后端未将 Content-Type 加入签名，此处不传 header 避免 SignatureDoesNotMatch
            return fetch(uploadUrl, {
              method: 'PUT',
              body: file
            }).then(async (res) => {
              if (!res.ok) {
                const text = await res.text();
                const msg = text ? `${res.status} ${res.statusText}: ${text.substring(0, 200)}` : res.statusText;
                throw new Error(msg);
              }
              this.emitInput(fileUrl);
              onSuccess({ fileUrl });
            });
          })
          .catch(err => {
            this.$message.error(err.message || 'Upload failed');
            onError(err);
          });
      },
      handleUploadSuccess(res, file) {
        // S3 上传结果在 uploadToS3 中已处理
      }
    }
  }
</script>
<style>

</style>


