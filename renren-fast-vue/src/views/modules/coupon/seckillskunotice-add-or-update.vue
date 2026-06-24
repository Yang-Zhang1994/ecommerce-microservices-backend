<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible"
  >
    <el-form
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      @keyup.enter.native="dataFormSubmit()"
      label-width="120px"
    >
      <el-form-item label="member_id" prop="memberId">
        <el-input v-model="dataForm.memberId" placeholder="member_id"></el-input>
      </el-form-item>
      <el-form-item label="SKU ID" prop="skuId">
        <el-input v-model="dataForm.skuId" placeholder="SKU ID"></el-input>
      </el-form-item>
      <el-form-item label="Promotion session ID" prop="sessionId">
        <el-input v-model="dataForm.sessionId" placeholder="Promotion session ID"></el-input>
      </el-form-item>
      <el-form-item label="Subscribed at" prop="subcribeTime">
        <el-input v-model="dataForm.subcribeTime" placeholder="Subscribed at"></el-input>
      </el-form-item>
      <el-form-item label="Sent at" prop="sendTime">
        <el-input v-model="dataForm.sendTime" placeholder="Sent at"></el-input>
      </el-form-item>
      <el-form-item label="Notification method" prop="noticeType">
        <el-select v-model="dataForm.noticeType" placeholder="Select">
          <el-option  label="SMS" :value="0"></el-option>
          <el-option  label="Email" :value="1"></el-option>
        </el-select>
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
  data() {
    return {
      visible: false,
      dataForm: {
        id: 0,
        memberId: "",
        skuId: "",
        sessionId: "",
        subcribeTime: "",
        sendTime: "",
        noticeType: ""
      },
      dataRule: {
        memberId: [
          { required: true, message: 'Member id is required', trigger: "blur" }
        ],
        skuId: [{ required: true, message: 'SKU ID is required', trigger: "blur" }],
        sessionId: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        subcribeTime: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        sendTime: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        noticeType: [
          {
            required: true,
            message: 'This field is required',
            trigger: "blur"
          }
        ]
      }
    };
  },
  methods: {
    init(id) {
      this.dataForm.id = id || 0;
      this.visible = true;
      this.$nextTick(() => {
        this.$refs["dataForm"].resetFields();
        if (this.dataForm.id) {
          this.$http({
            url: this.$http.adornUrl(
              `/coupon/seckillskunotice/info/${this.dataForm.id}`
            ),
            method: "get",
            params: this.$http.adornParams()
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.dataForm.memberId = data.seckillSkuNotice.memberId;
              this.dataForm.skuId = data.seckillSkuNotice.skuId;
              this.dataForm.sessionId = data.seckillSkuNotice.sessionId;
              this.dataForm.subcribeTime = data.seckillSkuNotice.subcribeTime;
              this.dataForm.sendTime = data.seckillSkuNotice.sendTime;
              this.dataForm.noticeType = data.seckillSkuNotice.noticeType;
            }
          });
        }
      });
    },
    // form submit
    dataFormSubmit() {
      this.$refs["dataForm"].validate(valid => {
        if (valid) {
          this.$http({
            url: this.$http.adornUrl(
              `/coupon/seckillskunotice/${
                !this.dataForm.id ? "save" : "update"
              }`
            ),
            method: "post",
            data: this.$http.adornData({
              id: this.dataForm.id || undefined,
              memberId: this.dataForm.memberId,
              skuId: this.dataForm.skuId,
              sessionId: this.dataForm.sessionId,
              subcribeTime: this.dataForm.subcribeTime,
              sendTime: this.dataForm.sendTime,
              noticeType: this.dataForm.noticeType
            })
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.$message({
                message: "Operation successful",
                type: "success",
                duration: 1500,
                onClose: () => {
                  this.visible = false;
                  this.$emit("refreshDataList");
                }
              });
            } else {
              this.$message.error(data.msg);
            }
          });
        }
      });
    }
  }
};
</script>
