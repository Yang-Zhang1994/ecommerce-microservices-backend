<template>
  <div>
    <el-upload
      action=""
      :data="dataObj"
      :list-type="listType"
      :file-list="fileList"
      :before-upload="beforeUpload"
      :on-remove="handleRemove"
      :on-success="handleUploadSuccess"
      :on-preview="handlePreview"
      :limit="maxCount"
      :on-exceed="handleExceed"
      :show-file-list="showFile"
      :http-request="httpRequest"
    >
      <i class="el-icon-plus"></i>
    </el-upload>
    <el-dialog :visible.sync="dialogVisible" width="720px">
      <div class="admin-image-preview-wrap">
        <img :src="dialogImageUrl" alt="" />
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getS3PresignedUrl } from './s3policy'

export default {
  name: "multiUpload",
  props: {
    // 图片 URL 数组
    value: Array,
    maxCount: {
      type: Number,
      default: 30
    },
    listType: {
      type: String,
      default: "picture-card"
    },
    showFile: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      dataObj: {},
      dialogVisible: false,
      dialogImageUrl: null
    };
  },
  computed: {
    fileList() {
      const list = this.value || [];
      return list.map(url => ({ url }));
    }
  },
  mounted() {},
  methods: {
    emitInput(fileList) {
      let value = [];
      for (let i = 0; i < fileList.length; i++) {
        value.push(fileList[i].url);
      }
      this.$emit("input", value);
    },
    handleRemove(file, fileList) {
      this.emitInput(fileList);
    },
    handlePreview(file) {
      this.dialogVisible = true;
      this.dialogImageUrl = file.url;
    },
    beforeUpload(file) {
      const isJpgPng = file.type === 'image/jpeg' || file.type === 'image/png';
      if (!isJpgPng) {
        this.$message.error('Only JPG and PNG images are supported');
        return Promise.reject();
      }
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        this.$message.error('Each image must be 10MB or smaller');
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
          return fetch(uploadUrl, {
            method: 'PUT',
            body: file
          }).then(async (res) => {
            if (!res.ok) {
              const text = await res.text();
              const msg = text ? `${res.status} ${res.statusText}: ${text.substring(0, 150)}` : res.statusText;
              throw new Error(msg);
            }
            this.$emit('input', [...(this.value || []), fileUrl]);
            onSuccess({ fileUrl });
          });
        })
        .catch(err => {
          this.$message.error(err.message || 'Upload failed');
          onError(err);
        });
    },
    handleUploadSuccess(res, file) {
      // S3 上传结果在 uploadToS3 中已通过 emit input 更新
    },
    handleExceed(files, fileList) {
      this.$message({
        message: "You can upload at most " + this.maxCount + " image(s)",
        type: "warning",
        duration: 1000
      });
    }
  }
};
</script>
<style>
</style>


