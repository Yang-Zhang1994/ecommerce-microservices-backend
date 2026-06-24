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
      <el-form-item label="spu_id" prop="skuId">
        <el-input v-model="dataForm.skuId" placeholder="spu_id"></el-input>
      </el-form-item>
      <el-form-item label="Min quantity" prop="fullCount">
        <el-input v-model="dataForm.fullCount" placeholder="Min quantity"></el-input>
      </el-form-item>
      <el-form-item label="Discount" prop="discount">
        <el-input v-model="dataForm.discount" placeholder="Discount"></el-input>
      </el-form-item>
      <el-form-item label="Discounted price" prop="price">
        <el-input v-model="dataForm.price" placeholder="Discounted price"></el-input>
      </el-form-item>
      <el-form-item label="Stackable with other offers" prop="addOther">
        <el-select v-model="dataForm.addOther" placeholder="Select">
          <el-option label="Not stackable" :value="0"></el-option>
          <el-option label="Stackable" :value="1"></el-option>
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
        skuId: "",
        fullCount: "",
        discount: "",
        price: "",
        addOther: ""
      },
      dataRule: {
        skuId: [{ required: true, message: 'Spu id is required', trigger: "blur" }],
        fullCount: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        discount: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        price: [{ required: true, message: 'This field is required', trigger: "blur" }],
        addOther: [
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
              `/coupon/skuladder/info/${this.dataForm.id}`
            ),
            method: "get",
            params: this.$http.adornParams()
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.dataForm.skuId = data.skuLadder.skuId;
              this.dataForm.fullCount = data.skuLadder.fullCount;
              this.dataForm.discount = data.skuLadder.discount;
              this.dataForm.price = data.skuLadder.price;
              this.dataForm.addOther = data.skuLadder.addOther;
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
              `/coupon/skuladder/${!this.dataForm.id ? "save" : "update"}`
            ),
            method: "post",
            data: this.$http.adornData({
              id: this.dataForm.id || undefined,
              skuId: this.dataForm.skuId,
              fullCount: this.dataForm.fullCount,
              discount: this.dataForm.discount,
              price: this.dataForm.price,
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
