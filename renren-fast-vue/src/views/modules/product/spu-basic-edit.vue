<template>
  <el-dialog
    title="Edit Product"
    :visible.sync="innerVisible"
    width="560px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      v-loading="loading"
      ref="dataForm"
      :model="dataForm"
      :rules="dataRule"
      label-width="120px"
    >
      <el-form-item label="Name" prop="spuName">
        <el-input v-model="dataForm.spuName" clearable></el-input>
      </el-form-item>
      <el-form-item label="Description" prop="spuDescription">
        <el-input type="textarea" :rows="3" v-model="dataForm.spuDescription"></el-input>
      </el-form-item>
      <el-form-item label="Category" prop="catalogId">
        <el-cascader
          v-model="catelogPath"
          :options="categoryTree"
          :props="cascaderProps"
          filterable
          clearable
          style="width: 100%;"
          @change="onCategoryChange"
        ></el-cascader>
      </el-form-item>
      <el-form-item label="Brand" prop="brandId">
        <el-select
          v-model="dataForm.brandId"
          filterable
          clearable
          placeholder="Select brand"
          style="width: 100%;"
        >
          <el-option
            v-for="b in brands"
            :key="b.brandId"
            :label="b.brandName"
            :value="b.brandId"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="Weight (kg)" prop="weight">
        <el-input-number
          v-model="dataForm.weight"
          :min="0"
          :precision="3"
          :step="0.01"
          style="width: 100%;"
        ></el-input-number>
      </el-form-item>
    </el-form>
    <span slot="footer">
      <el-button @click="innerVisible = false">Cancel</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">Save</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { formatSaveWithSearchSync } from "@/utils/searchSyncMessage";

export default {
  name: "SpuBasicEdit",
  props: {
    visible: { type: Boolean, default: false },
    spuId: { type: [Number, String], default: null }
  },
  data() {
    return {
      innerVisible: false,
      loading: false,
      submitting: false,
      categoryTree: [],
      catelogPath: [],
      brands: [],
      cascaderProps: { value: "catId", label: "name", children: "children" },
      dataForm: {
        id: null,
        spuName: "",
        spuDescription: "",
        catalogId: null,
        brandId: null,
        weight: 0,
        publishStatus: null
      },
      dataRule: {
        spuName: [{ required: true, message: "Name is required", trigger: "blur" }],
        catalogId: [{ required: true, message: "Category is required", trigger: "change" }],
        brandId: [{ required: true, message: "Brand is required", trigger: "change" }]
      }
    };
  },
  watch: {
    visible(val) {
      this.innerVisible = val;
      if (val && this.spuId) {
        this.loadSpu();
      }
    },
    innerVisible(val) {
      this.$emit("update:visible", val);
    }
  },
  created() {
    this.loadCategoryTree();
  },
  methods: {
    handleClose() {
      this.$refs.dataForm && this.$refs.dataForm.resetFields();
      this.catelogPath = [];
      this.brands = [];
    },
    loadCategoryTree() {
      this.$http({
        url: this.$http.adornUrl("/product/category/list/tree"),
        method: "get"
      }).then(({ data }) => {
        if (data && data.code === 0) {
          this.categoryTree = data.data || [];
        }
      });
    },
    loadSpu() {
      this.loading = true;
      this.$http({
        url: this.$http.adornUrl(`/product/spuinfo/info/${this.spuId}`),
        method: "get"
      })
        .then(({ data }) => {
          if (!data || data.code !== 0 || !data.spuInfo) {
            this.$message.error("Failed to load product");
            return;
          }
          const spu = data.spuInfo;
          this.dataForm = {
            id: spu.id,
            spuName: spu.spuName,
            spuDescription: spu.spuDescription || "",
            catalogId: spu.catalogId,
            brandId: spu.brandId,
            weight: spu.weight != null ? Number(spu.weight) : 0,
            publishStatus: spu.publishStatus
          };
          return this.loadCategoryPath(spu.catalogId).then(() => {
            this.loadBrands(spu.catalogId);
          });
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadCategoryPath(catalogId) {
      if (!catalogId) {
        this.catelogPath = [];
        return Promise.resolve();
      }
      return this.$http({
        url: this.$http.adornUrl(`/product/category/info/${catalogId}`),
        method: "get"
      }).then(({ data }) => {
        if (data && data.code === 0 && data.data && data.data.catelogPath) {
          this.catelogPath = data.data.catelogPath.map(id => Number(id));
        } else {
          this.catelogPath = [Number(catalogId)];
        }
      });
    },
    onCategoryChange(path) {
      const catalogId = path && path.length ? path[path.length - 1] : null;
      this.dataForm.catalogId = catalogId;
      this.dataForm.brandId = null;
      if (catalogId) {
        this.loadBrands(catalogId);
      } else {
        this.brands = [];
      }
    },
    loadBrands(catId) {
      this.$http({
        url: this.$http.adornUrl("/product/categorybrandrelation/brands/list"),
        method: "get",
        params: this.$http.adornParams({ catId })
      }).then(({ data }) => {
        this.brands = (data && data.data) || [];
      });
    },
    submit() {
      this.$refs.dataForm.validate(valid => {
        if (!valid) return;
        this.submitting = true;
        this.$http({
          url: this.$http.adornUrl("/product/spuinfo/update"),
          method: "post",
          data: this.dataForm
        })
          .then(({ data }) => {
            if (data && data.code === 0) {
              const msg = formatSaveWithSearchSync(data, "Saved");
              this.$message.success(msg || "Saved");
              this.innerVisible = false;
              this.$emit("refresh");
            } else {
              this.$message.error((data && data.msg) || "Save failed");
            }
          })
          .finally(() => {
            this.submitting = false;
          });
      });
    }
  }
};
</script>
