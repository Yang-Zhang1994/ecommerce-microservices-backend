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
      <el-form-item label="Session name" prop="name">
        <el-input v-model="dataForm.name" placeholder="Session name"></el-input>
      </el-form-item>
      <el-form-item label="Session start" prop="startTime">
        <el-date-picker
          v-model="dataForm.startTime"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
          placeholder="Session start"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="Session end" prop="endTime">
        <el-date-picker
          v-model="dataForm.endTime"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
          placeholder="Session end"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="Enabled" prop="status">
        <el-select v-model="dataForm.status" placeholder="Enabled">
          <el-option :value="1" label="Enabled (1)"></el-option>
          <el-option :value="0" label="Disabled (0)"></el-option>
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
        name: "",
        startTime: "",
        endTime: "",
        status: 1,
        createTime: ""
      },
      dataRule: {
        name: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        startTime: [
          { required: true, message: 'Session start is required', trigger: 'change' }
        ],
        endTime: [
          { required: true, message: 'Session end is required', trigger: 'change' }
        ],
        status: [
          { required: true, message: 'Enabled status is required', trigger: 'change' }
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
              `/coupon/seckillsession/info/${this.dataForm.id}`
            ),
            method: "get",
            params: this.$http.adornParams()
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.dataForm.name = data.seckillSession.name;
              this.dataForm.startTime = data.seckillSession.startTime;
              this.dataForm.endTime = data.seckillSession.endTime;
              this.dataForm.status = data.seckillSession.status;
              this.dataForm.createTime = data.seckillSession.createTime;
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
              `/coupon/seckillsession/${!this.dataForm.id ? "save" : "update"}`
            ),
            method: "post",
            data: this.$http.adornData({
              id: this.dataForm.id || undefined,
              name: this.dataForm.name,
              startTime: this.dataForm.startTime,
              endTime: this.dataForm.endTime,
              status: Number(this.dataForm.status)
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
              this.$message.error((data && data.msg) || 'Save failed');
            }
          }).catch((err) => {
            const msg =
              (err.response && err.response.data && err.response.data.msg) ||
              (err.response && err.response.data && err.response.data.error) ||
              err.message ||
              'Network error';
            this.$message.error(msg);
          });
        }
      });
    }
  }
};
</script>
