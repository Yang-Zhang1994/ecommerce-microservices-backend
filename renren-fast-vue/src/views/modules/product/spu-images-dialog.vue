<template>
  <el-dialog
    title="Detail gallery"
    :visible.sync="innerVisible"
    width="680px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <p v-if="spuName" class="spu-gallery-hint">
      <strong>{{ spuName }}</strong>
      <span v-if="spuId"> (SPU ID: {{ spuId }})</span>
    </p>
    <p class="spu-gallery-note">
      Both sections appear under <strong>Product gallery</strong> on the mall detail page (after SKU thumbnails).
      Per-color images are managed in <strong>Product Manager → SKU Images</strong>.
    </p>

    <div v-loading="loading">
      <h4 class="spu-gallery-section-title">1. Product images</h4>
      <p class="spu-gallery-section-note">SPU shared gallery; first block on the detail tab.</p>
      <el-form label-width="110px">
        <el-form-item label="Gallery">
          <multi-upload v-model="spuImageUrls" :max-count="10"></multi-upload>
        </el-form-item>
        <el-form-item label="Default" v-if="spuImageUrls.length > 0">
          <el-radio-group v-model="defaultImgIndex">
            <el-radio
              v-for="(url, idx) in spuImageUrls"
              :key="'spu-' + idx"
              :label="idx"
              class="spu-img-radio"
            >
              <span class="admin-img admin-img--sm"><img :src="url" alt="" /></span>
              Image {{ idx + 1 }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <el-divider></el-divider>

      <h4 class="spu-gallery-section-title">2. Product introduction</h4>
      <p class="spu-gallery-section-note">Long intro images; appended after product images in the same gallery.</p>
      <el-form label-width="110px">
        <el-form-item label="Intro images">
          <multi-upload v-model="introImageUrls" :max-count="10"></multi-upload>
        </el-form-item>
      </el-form>
    </div>

    <span slot="footer">
      <el-button @click="innerVisible = false">Cancel</el-button>
      <el-button type="primary" :loading="saving" @click="save">Save</el-button>
    </span>
  </el-dialog>
</template>

<script>
import MultiUpload from "@/components/upload/multiUpload";
import { formatSaveWithSearchSync } from "@/utils/searchSyncMessage";

export default {
  name: "SpuImagesDialog",
  components: { MultiUpload },
  props: {
    visible: { type: Boolean, default: false },
    spuId: { type: [Number, String], default: null },
    spuName: { type: String, default: "" }
  },
  data() {
    return {
      innerVisible: false,
      loading: false,
      saving: false,
      spuImageUrls: [],
      introImageUrls: [],
      defaultImgIndex: 0,
      introDescExists: false
    };
  },
  watch: {
    visible(val) {
      this.innerVisible = val;
      if (val && this.spuId) {
        this.loadAll();
      }
    },
    innerVisible(val) {
      this.$emit("update:visible", val);
    }
  },
  methods: {
    handleClose() {
      this.spuImageUrls = [];
      this.introImageUrls = [];
      this.defaultImgIndex = 0;
      this.introDescExists = false;
    },
    parseDecript(decript) {
      if (!decript || typeof decript !== "string") {
        return [];
      }
      return decript
        .split(",")
        .map((s) => s.trim())
        .filter((s) => /^https?:\/\//i.test(s));
    },
    loadAll() {
      this.loading = true;
      const spuId = this.spuId;
      Promise.all([
        this.$http({
          url: this.$http.adornUrl(`/product/spuimages/byspu/${spuId}`),
          method: "get"
        }),
        this.$http({
          url: this.$http.adornUrl(`/product/spuinfodesc/info/${spuId}`),
          method: "get"
        })
      ])
        .then(([spuRes, introRes]) => {
          const spuData = spuRes.data;
          if (spuData && spuData.code === 0 && spuData.list && spuData.list.length) {
            const sorted = spuData.list.slice().sort((a, b) => (a.imgSort || 0) - (b.imgSort || 0));
            this.spuImageUrls = sorted.map((item) => item.imgUrl);
            const defIdx = sorted.findIndex((item) => item.defaultImg === 1);
            this.defaultImgIndex = defIdx >= 0 ? defIdx : 0;
          } else {
            this.spuImageUrls = [];
            this.defaultImgIndex = 0;
          }

          const introData = introRes.data;
          if (introData && introData.code === 0 && introData.spuInfoDesc) {
            this.introDescExists = true;
            this.introImageUrls = this.parseDecript(introData.spuInfoDesc.decript);
          } else {
            this.introDescExists = false;
            this.introImageUrls = [];
          }
        })
        .catch(() => {
          this.spuImageUrls = [];
          this.introImageUrls = [];
        })
        .finally(() => {
          this.loading = false;
        });
    },
    saveSpuImages() {
      const images = this.spuImageUrls.map((imgUrl, i) => ({
        imgUrl,
        imgSort: i,
        defaultImg: i === this.defaultImgIndex ? 1 : 0
      }));
      return this.$http({
        url: this.$http.adornUrl("/product/spuimages/saveBatch"),
        method: "post",
        data: { spuId: Number(this.spuId), images }
      });
    },
    saveIntroduction() {
      const body = { spuId: Number(this.spuId), decript: this.introImageUrls.join(",") };
      const url = this.introDescExists
        ? this.$http.adornUrl("/product/spuinfodesc/update")
        : this.$http.adornUrl("/product/spuinfodesc/save");
      return this.$http({ url, method: "post", data: body });
    },
    save() {
      if (!this.spuId) return;
      this.saving = true;
      Promise.all([this.saveSpuImages(), this.saveIntroduction()])
        .then(([spuRes, introRes]) => {
          const spuData = spuRes.data;
          const introData = introRes.data;
          if (
            (!spuData || spuData.code !== 0) ||
            (!introData || introData.code !== 0)
          ) {
            const msg =
              (spuData && spuData.msg) ||
              (introData && introData.msg) ||
              "Save failed";
            this.$message.error(msg);
            return;
          }
          this.introDescExists = true;
          const toast = formatSaveWithSearchSync(spuData, "Detail gallery saved");
          this.$message.success(toast || "Detail gallery saved");
          this.innerVisible = false;
          this.$emit("refresh");
        })
        .catch(() => {
          this.$message.error("Save failed");
        })
        .finally(() => {
          this.saving = false;
        });
    }
  }
};
</script>

<style scoped>
.spu-gallery-hint {
  margin: 0 0 8px;
  color: #606266;
}
.spu-gallery-note {
  margin: 0 0 16px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
.spu-gallery-section-title {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.spu-gallery-section-note {
  margin: 0 0 10px;
  font-size: 12px;
  color: #909399;
  line-height: 1.45;
}
.spu-img-radio {
  display: block;
  margin-bottom: 8px;
}
.spu-img-radio .admin-img {
  margin-right: 8px;
}
</style>
