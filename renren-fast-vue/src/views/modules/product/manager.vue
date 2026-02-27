<template>
  <div class="mod-config">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form :inline="true" :model="dataForm">
        <el-form-item label="Category">
          <category-cascader :catelogPath.sync="catelogPath"></category-cascader>
        </el-form-item>
        <el-form-item label="Brand">
          <brand-select style="width:160px"></brand-select>
        </el-form-item>
        <el-form-item label="Price">
          <el-input-number style="width:160px" v-model="dataForm.price.min" :min="0"></el-input-number>-
          <el-input-number style="width:160px" v-model="dataForm.price.max" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="Keyword">
          <el-input style="width:160px" v-model="dataForm.key" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchSkuInfo">Query</el-button>
        </el-form-item>
      </el-form>
    </el-form>
    <el-table
      :data="dataList"
      border
      v-loading="dataListLoading"
      @selection-change="selectionChangeHandle"
      style="width: 100%;"
      @expand-change="getSkuDetails"
    >
      <el-table-column type="expand">
        <template slot-scope="scope">
          Product Title: {{scope.row.skuTitle}}
          <br />
          Product Subtitle: {{scope.row.skuSubtitle}}
          <br />
          Product Description: {{scope.row.skuDesc}}
          <br />
          Category ID: {{scope.row.catalogId}}
          <br />
          SpuID: {{scope.row.spuId}}
          <br />
          Brand ID: {{scope.row.brandId}}
          <br />
        </template>
      </el-table-column>
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="skuId" header-align="center" align="center" label="SKU ID"></el-table-column>
      <el-table-column prop="skuName" header-align="center" align="center" label="Name"></el-table-column>
      <el-table-column prop="skuDefaultImg" header-align="center" align="center" label="Default Image">
        <template slot-scope="scope">
          <img :src="scope.row.skuDefaultImg" style="width:80px;height:80px;" />
        </template>
      </el-table-column>
      <el-table-column prop="price" header-align="center" align="center" label="Price"></el-table-column>
      <el-table-column prop="saleCount" header-align="center" align="center" label="Sales"></el-table-column>
    </el-table>
    <!-- Upload Images Dialog -->
    <el-dialog
      title="Upload SKU Images"
      :visible.sync="uploadDialogVisible"
      width="600px"
      @close="closeUploadDialog"
    >
      <div v-if="uploadDialogSku">
        <p><strong>{{ uploadDialogSku.skuName }}</strong> (SKU ID: {{ uploadDialogSku.skuId }})</p>
        <el-form label-width="100px">
          <el-form-item label="Images">
            <multi-upload v-model="skuImageUrls" :max-count="10"></multi-upload>
          </el-form-item>
          <el-form-item label="Set Default" v-if="skuImageUrls.length > 0">
            <el-radio-group v-model="defaultImgIndex">
              <el-radio
                v-for="(url, idx) in skuImageUrls"
                :key="idx"
                :label="idx"
                style="display: block; margin-bottom: 8px;"
              >
                <img :src="url" style="width: 60px; height: 60px; object-fit: cover; vertical-align: middle; margin-right: 8px;" />
                Image {{ idx + 1 }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="uploadDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="uploadSaveLoading" @click="saveSkuImages">Save</el-button>
      </span>
    </el-dialog>

    <el-pagination
      @size-change="sizeChangeHandle"
      @current-change="currentChangeHandle"
      :current-page="pageIndex"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      :total="totalPage"
      layout="total, sizes, prev, pager, next, jumper"
    ></el-pagination>
  </div>
</template>

<script>
import CategoryCascader from "../common/category-cascader";
import BrandSelect from "../common/brand-select";
import MultiUpload from "@/components/upload/multiUpload";

export default {
  data() {
    return {
      uploadDialogVisible: false,
      uploadDialogSku: null,
      skuImageUrls: [],
      defaultImgIndex: 0,
      uploadSaveLoading: false,
      catPathSub: null,
      brandIdSub: null,
      dataForm: {
        key: "",
        brandId: 0,
        catelogId: 0,
        price: {
          min: 0,
          max: 0
        }
      },
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      addOrUpdateVisible: false,
      catelogPath: []
    };
  },
  components: {
    CategoryCascader,
    BrandSelect,
    MultiUpload
  },
  activated() {
    this.getDataList();
  },
  methods: {
    getSkuDetails(row, expand) {
      //sku detail query
      console.log("Expand row...", row, expand);
    },
    uploadImagesHandle(row) {
      this.uploadDialogSku = row;
      this.uploadDialogVisible = true;
      this.loadSkuImages(row.skuId);
    },
    loadSkuImages(skuId) {
      this.$http({
        url: this.$http.adornUrl(`/product/skuimages/bysku/${skuId}`),
        method: "get"
      }).then(({ data }) => {
        if (data && data.code === 0 && data.list) {
          this.skuImageUrls = data.list
            .sort((a, b) => (a.imgSort || 0) - (b.imgSort || 0))
            .map((item) => item.imgUrl);
          const defaultIdx = data.list.findIndex((item) => item.defaultImg === 1);
          this.defaultImgIndex = defaultIdx >= 0 ? defaultIdx : 0;
        } else {
          this.skuImageUrls = [];
          this.defaultImgIndex = 0;
        }
      }).catch(() => {
        this.skuImageUrls = [];
        this.defaultImgIndex = 0;
      });
    },
    closeUploadDialog() {
      this.uploadDialogSku = null;
      this.skuImageUrls = [];
      this.defaultImgIndex = 0;
    },
    saveSkuImages() {
      if (!this.uploadDialogSku || this.skuImageUrls.length === 0) {
        this.$message.warning("Please upload at least one image");
        return;
      }
      this.uploadSaveLoading = true;
      const images = this.skuImageUrls.map((imgUrl, i) => ({
        imgUrl,
        defaultImg: i === this.defaultImgIndex ? 1 : 0,
        imgSort: i
      }));
      this.$http({
        url: this.$http.adornUrl("/product/skuimages/saveBatch"),
        method: "post",
        data: this.$http.adornData({
          skuId: this.uploadDialogSku.skuId,
          images
        })
      })
        .then(({ data }) => {
          if (data && data.code === 0) {
            this.$message.success("Saved successfully");
            this.uploadDialogVisible = false;
            this.getDataList();
          } else {
            this.$message.error(data.msg || "Save failed");
          }
        })
        .catch((err) => {
          this.$message.error(err.message || "Save failed");
        })
        .finally(() => {
          this.uploadSaveLoading = false;
        });
    },
    stockSettingsHandle(row) {
      this.$router.push({ path: "/ware-sku", query: { skuId: row.skuId } });
    },
    searchSkuInfo() {
      this.getDataList();
    },
    // get data list
    getDataList() {
      this.dataListLoading = true;
      this.$http({
        url: this.$http.adornUrl("/product/skuinfo/list"),
        method: "get",
        params: this.$http.adornParams({
          page: this.pageIndex,
          limit: this.pageSize,
          key: this.dataForm.key,
          catelogId: this.dataForm.catelogId,
          brandId: this.dataForm.brandId,
          min: this.dataForm.price.min,
          max: this.dataForm.price.max
        })
      }).then(({ data }) => {
        if (data && data.code === 0) {
          this.dataList = data.page.list;
          this.totalPage = data.page.totalCount;
        } else {
          this.dataList = [];
          this.totalPage = 0;
        }
        this.dataListLoading = false;
      });
    },
    // page size
    sizeChangeHandle(val) {
      this.pageSize = val;
      this.pageIndex = 1;
      this.getDataList();
    },
    // current page
    currentChangeHandle(val) {
      this.pageIndex = val;
      this.getDataList();
    },
    // multi-select
    selectionChangeHandle(val) {
      this.dataListSelections = val;
    }
  },
  mounted() {
    this.catPathSub = PubSub.subscribe("catPath", (msg, val) => {
      this.dataForm.catelogId = val[val.length - 1];
    });
    this.brandIdSub = PubSub.subscribe("brandId", (msg, val) => {
      this.dataForm.brandId = val;
    });
  },
  beforeDestroy() {
    PubSub.unsubscribe(this.catPathSub);
    PubSub.unsubscribe(this.brandIdSub);
  } //生命周期 - 销毁之前
};
</script>
