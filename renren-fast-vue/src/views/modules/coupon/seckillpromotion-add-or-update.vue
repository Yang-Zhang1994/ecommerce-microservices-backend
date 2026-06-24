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
      <el-form-item label="Promotion title" prop="title">
        <el-input v-model="dataForm.title" placeholder="Promotion title"></el-input>
      </el-form-item>
      <el-form-item label="Effective dates" prop="enableStartTime">
        <el-date-picker
          v-model="dataForm.timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="Start date"
          end-placeholder="End date"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="Online status" prop="status">
        <el-select v-model="dataForm.status" placeholder="Online status">
          <el-option :value="1" label="Online"></el-option>
          <el-option :value="0" label="Offline"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="Created by" prop="userId">
        <el-input v-model="dataForm.userId" placeholder="Created by"></el-input>
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
        title: "",
        startTime: "",
        endTime: "",
        status: "",
        createTime: "",
        userId: "",
        timeRange: []
      },
      dataRule: {
        title: [
          { required: true, message: 'This field is required', trigger: "blur" }
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
              `/coupon/seckillpromotion/info/${this.dataForm.id}`
            ),
            method: "get",
            params: this.$http.adornParams()
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.dataForm.title = data.seckillPromotion.title;
              this.dataForm.startTime = data.seckillPromotion.startTime;
              this.dataForm.endTime = data.seckillPromotion.endTime;
              this.dataForm.status = data.seckillPromotion.status;
              this.dataForm.createTime = data.seckillPromotion.createTime;
              this.dataForm.userId = data.seckillPromotion.userId;
              this.dataForm.timeRange.push(this.dataForm.startTime);
              this.dataForm.timeRange.push(this.dataForm.endTime);
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
              `/coupon/seckillpromotion/${
                !this.dataForm.id ? "save" : "update"
              }`
            ),
            method: "post",
            data: this.$http.adornData({
              id: this.dataForm.id || undefined,
              title: this.dataForm.title,
              startTime: this.dataForm.timeRange[0],
              endTime: this.dataForm.timeRange[1],
              status: this.dataForm.status
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
