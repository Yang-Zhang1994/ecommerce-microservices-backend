<template>
  <div class="mod-config">
    <el-dialog
      class="coupon-marketing-dialog"
      width="600px"
      :title="!dataForm.id ? 'Add' : 'Edit'"
      :close-on-click-modal="false"
      :visible.sync="visible"
      :append-to-body="true"
    >
      <el-form
        class="coupon-marketing-form"
        :model="dataForm"
        :rules="dataRule"
        ref="dataForm"
        @keyup.enter.native="dataFormSubmit()"
        label-width="260px"
      >
        <el-form-item label="spu_id" prop="skuId">
          <el-input v-model="dataForm.skuId" placeholder="spu_id"></el-input>
        </el-form-item>
        <el-form-item label="Full Amount" prop="fullPrice">
          <el-input v-model="dataForm.fullPrice" placeholder="Full Amount"></el-input>
        </el-form-item>
        <el-form-item label="Reduce Amount" prop="reducePrice">
          <el-input v-model="dataForm.reducePrice" placeholder="Reduce Amount"></el-input>
        </el-form-item>
        <el-form-item label="Other Promotions" prop="addOther">
          <el-input v-model="dataForm.addOther" placeholder="0 = no, 1 = yes"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="visible = false">Cancel</el-button>
        <el-button type="primary" @click="dataFormSubmit()">Confirm</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      visible: false,
      dataForm: {
        id: 0,
        skuId: "",
        fullPrice: "",
        reducePrice: "",
        addOther: ""
      },
      dataRule: {
        skuId: [{ required: true, message: 'SPU ID is required', trigger: "blur" }],
        fullPrice: [
          { required: true, message: 'Full Amount is required', trigger: "blur" }
        ],
        reducePrice: [
          { required: true, message: 'Reduce Amount is required', trigger: "blur" }
        ],
        addOther: [
          {
            required: true,
            message: 'Other Promotions is required',
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
              `/coupon/skufullreduction/info/${this.dataForm.id}`
            ),
            method: "get",
            params: this.$http.adornParams()
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.dataForm.skuId = data.skuFullReduction.skuId;
              this.dataForm.fullPrice = data.skuFullReduction.fullPrice;
              this.dataForm.reducePrice = data.skuFullReduction.reducePrice;
              this.dataForm.addOther = data.skuFullReduction.addOther;
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
              `/coupon/skufullreduction/${
                !this.dataForm.id ? "save" : "update"
              }`
            ),
            method: "post",
            data: this.$http.adornData({
              id: this.dataForm.id || undefined,
              skuId: this.dataForm.skuId,
              fullPrice: this.dataForm.fullPrice,
              reducePrice: this.dataForm.reducePrice,
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
