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
      label-width="140px"
    >
      <el-form-item label="SKU ID" prop="skuId">
        <el-input v-model="dataForm.skuId" placeholder="SKU ID"></el-input>
      </el-form-item>
      <el-form-item label="Warehouse" prop="wareId">
        <el-select v-model="dataForm.wareId" placeholder="Select warehouse" clearable>
          <el-option :label="w.name" :value="w.id" v-for="w in wareList" :key="w.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="Stock Quantity" prop="stock">
        <el-input v-model="dataForm.stock" placeholder="Stock Quantity"></el-input>
      </el-form-item>
      <el-form-item label="SKU Name" prop="skuName">
        <el-input v-model="dataForm.skuName" placeholder="SKU Name"></el-input>
      </el-form-item>
      <el-form-item label="Locked Stock" prop="stockLocked">
        <el-input v-model="dataForm.stockLocked" placeholder="Locked Stock"></el-input>
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
      wareList: [],
      dataForm: {
        id: 0,
        skuId: "",
        wareId: "",
        stock: 0,
        skuName: "",
        stockLocked: 0
      },
      dataRule: {
        skuId: [{ required: true, message: "SKU ID is required", trigger: "blur" }],
        wareId: [
          { required: true, message: "Warehouse is required", trigger: "blur" }
        ],
        stock: [{ required: true, message: "Stock quantity is required", trigger: "blur" }],
        skuName: [
          { required: true, message: "SKU Name is required", trigger: "blur" }
        ]
      }
    };
  },
  created(){
    this.getWares();
  },
  methods: {
    getWares() {
      this.$http({
        url: this.$http.adornUrl("/ware/wareinfo/list"),
        method: "get",
        params: this.$http.adornParams({
          page: 1,
          limit: 500
        })
      }).then(({ data }) => {
        this.wareList = data.page.list;
      });
    },
    init(id) {
      this.dataForm.id = id || 0;
      this.visible = true;
      this.$nextTick(() => {
        this.$refs["dataForm"].resetFields();
        if (this.dataForm.id) {
          this.$http({
            url: this.$http.adornUrl(`/ware/waresku/info/${this.dataForm.id}`),
            method: "get",
            params: this.$http.adornParams()
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.dataForm.skuId = data.wareSku.skuId;
              this.dataForm.wareId = data.wareSku.wareId;
              this.dataForm.stock = data.wareSku.stock;
              this.dataForm.skuName = data.wareSku.skuName;
              this.dataForm.stockLocked = data.wareSku.stockLocked;
            }
          });
        }
      });
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs["dataForm"].validate(valid => {
        if (valid) {
          this.$http({
            url: this.$http.adornUrl(
              `/ware/waresku/${!this.dataForm.id ? "save" : "update"}`
            ),
            method: "post",
            data: this.$http.adornData({
              id: this.dataForm.id || undefined,
              skuId: this.dataForm.skuId,
              wareId: this.dataForm.wareId,
              stock: this.dataForm.stock,
              skuName: this.dataForm.skuName,
              stockLocked: this.dataForm.stockLocked
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
