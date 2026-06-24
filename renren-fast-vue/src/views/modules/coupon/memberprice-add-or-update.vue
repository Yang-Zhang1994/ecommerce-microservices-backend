<template>
  <el-dialog
    class="coupon-marketing-dialog"
    width="600px"
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible"
  >
    <el-form
      class="coupon-marketing-form"
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      @keyup.enter.native="dataFormSubmit()"
      label-width="220px"
    >
      <el-form-item label="SKU ID" prop="skuId">
        <el-input v-model="dataForm.skuId" placeholder="SKU ID"></el-input>
      </el-form-item>
      <el-form-item label="Member Level ID" prop="memberLevelId">
        <el-input v-model="dataForm.memberLevelId" placeholder="Member Level ID"></el-input>
      </el-form-item>
      <el-form-item label="Member Level Name" prop="memberLevelName">
        <el-input v-model="dataForm.memberLevelName" placeholder="Member Level Name"></el-input>
      </el-form-item>
      <el-form-item label="Member Price" prop="memberPrice">
        <el-input v-model="dataForm.memberPrice" placeholder="Member Price"></el-input>
      </el-form-item>
      <el-form-item label="Stack Other Promotions" prop="addOther">
        <el-switch
          v-model="dataForm.addOther"
          :active-value="1"
          inactive-value="0"
          active-text="Can Stack"
          inactive-text="Cannot Stack"
        ></el-switch>
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
        skuId: "",
        memberLevelId: "",
        memberLevelName: "",
        memberPrice: "",
        addOther: ""
      },
      dataRule: {
        skuId: [{ required: true, message: 'SKU ID is required', trigger: "blur" }],
        memberLevelId: [
          { required: true, message: 'Member Level ID is required', trigger: "blur" }
        ],
        memberLevelName: [
          { required: true, message: 'Member Level Name is required', trigger: "blur" }
        ],
        memberPrice: [
          { required: true, message: 'Member Price is required', trigger: "blur" }
        ],
        addOther: [
          {
            required: true,
            message: 'Stack Other Promotions is required',
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
              `/coupon/memberprice/info/${this.dataForm.id}`
            ),
            method: "get",
            params: this.$http.adornParams()
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.dataForm.skuId = data.memberPrice.skuId;
              this.dataForm.memberLevelId = data.memberPrice.memberLevelId;
              this.dataForm.memberLevelName = data.memberPrice.memberLevelName;
              this.dataForm.memberPrice = data.memberPrice.memberPrice;
              this.dataForm.addOther = data.memberPrice.addOther;
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
              `/coupon/memberprice/${!this.dataForm.id ? "save" : "update"}`
            ),
            method: "post",
            data: this.$http.adornData({
              id: this.dataForm.id || undefined,
              skuId: this.dataForm.skuId,
              memberLevelId: this.dataForm.memberLevelId,
              memberLevelName: this.dataForm.memberLevelName,
              memberPrice: this.dataForm.memberPrice,
              addOther: this.dataForm.addOther
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
