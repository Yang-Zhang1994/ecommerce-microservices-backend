<template>
  <div>
    <el-row>
      <el-col :span="24">
        <el-steps :active="step" finish-status="success">
          <el-step title="Basic Info"></el-step>
          <el-step title="Specifications"></el-step>
          <el-step title="Sale Attributes"></el-step>
          <el-step title="SKU Info"></el-step>
          <el-step title="Save Complete"></el-step>
        </el-steps>
      </el-col>
      <el-col :span="24" v-show="step==0">
        <el-card class="box-card" style="width:80%;margin:20px auto">
          <el-form ref="spuBaseForm" :model="spu" label-width="120px" :rules="spuBaseInfoRules">
            <el-form-item label="Product Name" prop="spuName">
              <el-input v-model="spu.spuName"></el-input>
            </el-form-item>
            <el-form-item label="Product Description" prop="spuDescription">
              <el-input v-model="spu.spuDescription"></el-input>
            </el-form-item>
            <el-form-item label="Select Category" prop="catalogId">
              <category-cascader></category-cascader>
            </el-form-item>
            <el-form-item label="Select Brand" prop="brandId">
              <brand-select></brand-select>
            </el-form-item>
            <el-form-item label="Product Weight (Kg)" prop="weight">
              <el-input-number v-model.number="spu.weight" :min="0" :precision="3" :step="0.1"></el-input-number>
            </el-form-item>
            <el-form-item label="Set Points" prop="bounds">
              <label>Points</label>
              <el-input-number
                style="width:130px"
                placeholder="Points"
                v-model="spu.bounds.buyBounds"
                :min="0"
                controls-position="right"
              ></el-input-number>
              <label style="margin-left:15px">Growth Value</label>
              <el-input-number
                style="width:130px"
                placeholder="Growth Value"
                v-model="spu.bounds.growBounds"
                :min="0"
                controls-position="right"
              >
                <template slot="prepend">Growth Value</template>
              </el-input-number>
            </el-form-item>
            <el-form-item label="Product Introduction" prop="decript">
              <multi-upload v-model="spu.decript"></multi-upload>
            </el-form-item>

            <el-form-item label="Product Images" prop="images">
              <multi-upload v-model="spu.images"></multi-upload>
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="collectSpuBaseInfo">Next: Set Base Parameters</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="24" v-show="step==1">
        <el-card class="box-card" style="width:80%;margin:20px auto">
          <el-tabs tab-position="left" style="width:98%">
            <el-tab-pane
              :label="group.attrGroupName"
              v-for="(group,gidx) in dataResp.attrGroups"
              :key="group.attrGroupId"
            >
              <!-- iterate attributes, each tab-pane corresponds to a form, each attribute is a form item  spu.baseAttrs[0] = [{attrId:xx,val:}]-->
              <el-form ref="form" :model="spu">
                <el-form-item
                  :label="attr.attrName"
                  v-for="(attr,aidx) in group.attrs"
                  :key="attr.attrId"
                >
                  <el-input
                    v-model="dataResp.baseAttrs[gidx][aidx].attrId"
                    type="hidden"
                    v-show="false"
                  ></el-input>
                  <el-select
                    v-model="dataResp.baseAttrs[gidx][aidx].attrValues"
                    :multiple="attr.valueType == 1"
                    filterable
                    allow-create
                    default-first-option
                    placeholder="Please Select or Enter Value"
                  >
                    <el-option
                      v-for="(val,vidx) in attr.valueSelect.split(';')"
                      :key="vidx"
                      :label="val"
                      :value="val"
                    ></el-option>
                  </el-select>
                  <el-checkbox
                    v-model="dataResp.baseAttrs[gidx][aidx].showDesc"
                    :true-label="1"
                    :false-label="0"
                  >Quick Display</el-checkbox>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
          <div style="margin:auto">
            <el-button type="primary" @click="step = 0">Previous</el-button>
            <el-button type="success" @click="generateSaleAttrs">Next: Set Sale Attributes</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="24" v-show="step==2">
        <el-card class="box-card" style="width:80%;margin:20px auto">
          <el-card class="box-card">
            <div slot="header" class="clearfix">
              <span>Select Sale Attributes</span>
              <el-form ref="saleform" :model="spu">
                <el-form-item
                  :label="attr.attrName"
                  v-for="(attr,aidx) in dataResp.saleAttrs"
                  :key="attr.attrId"
                >
                  <el-input
                    v-model="dataResp.tempSaleAttrs[aidx].attrId"
                    type="hidden"
                    v-show="false"
                  ></el-input>
                  <el-checkbox-group v-model="dataResp.tempSaleAttrs[aidx].attrValues">
                    <el-checkbox
                      v-if="dataResp.saleAttrs[aidx].valueSelect != ''"
                      :label="val"
                      v-for="val in dataResp.saleAttrs[aidx].valueSelect.split(';')"
                      :key="val"
                    ></el-checkbox>
                    <div style="margin-left:20px;display:inline">
                      <el-button
                        v-show="!inputVisible[aidx].view"
                        class="button-new-tag"
                        size="mini"
                        @click="showInput(aidx)"
                      >+Custom</el-button>
                      <el-input
                        v-show="inputVisible[aidx].view"
                        v-model="inputValue[aidx].val"
                        :ref="'saveTagInput'+aidx"
                        size="mini"
                        style="width:150px"
                        @keyup.enter.native="handleInputConfirm(aidx)"
                        @blur="handleInputConfirm(aidx)"
                      ></el-input>
                    </div>
                  </el-checkbox-group>
                </el-form-item>
              </el-form>
            </div>
            <el-button type="primary" @click="step = 1">Previous</el-button>
            <el-button type="success" @click="generateSkus">Next: Set SKU Info</el-button>
          </el-card>
        </el-card>
      </el-col>
      <el-col :span="24" v-show="step==3">
        <el-card class="box-card" style="width:80%;margin:20px auto">
          <el-table :data="spu.skus" style="width: 100%">
            <el-table-column label="Attribute Combination">
              <el-table-column
                :label="item.attrName"
                v-for="(item,index) in dataResp.tableAttrColumn"
                :key="item.attrId"
              >
                <template slot-scope="scope">
                  <span style="margin-left: 10px">{{ scope.row.attr[index].attrValue }}</span>
                </template>
              </el-table-column>
            </el-table-column>
            <el-table-column label="Product Name" prop="skuName">
              <template slot-scope="scope">
                <el-input v-model="scope.row.skuName"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="Title" prop="skuTitle">
              <template slot-scope="scope">
                <el-input v-model="scope.row.skuTitle"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="Subtitle" prop="skuSubtitle">
              <template slot-scope="scope">
                <el-input v-model="scope.row.skuSubtitle"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="Price" prop="price">
              <template slot-scope="scope">
                <el-input v-model="scope.row.price"></el-input>
              </template>
            </el-table-column>
            <el-table-column type="expand">
              <template slot-scope="scope">
                <el-row>
                  <el-col :span="24">
                    <label style="display:block;float:left">Select Image Set or</label>
                    <multi-upload
                      style="float:left;margin-left:10px;"
                      :showFile="false"
                      :listType="'text'"
                      v-model="uploadImages"
                    ></multi-upload>
                  </el-col>
                  <el-col :span="24">
                    <el-divider></el-divider>
                  </el-col>
                  <el-col :span="24">
                    <el-card
                      style="width:170px;float:left;margin-left:15px;margin-top:15px;"
                      :body-style="{ padding: '0px' }"
                      v-for="(img,index) in spu.images"
                      :key="index"
                    >
                      <img :src="img" style="width:160px;height:120px" />
                      <div style="padding: 14px;">
                        <el-row>
                          <el-col :span="12">
                            <el-checkbox
                              v-model="scope.row.images[index].imgUrl"
                              :true-label="img"
                              false-label
                            ></el-checkbox>
                          </el-col>
                          <el-col :span="12">
                            <el-tag v-if="scope.row.images[index].defaultImg == 1">
                              <input
                                type="radio"
                                checked
                                :name="scope.row.skuName"
                                @change="checkDefaultImg(scope.row,index,img)"
                              />Set Default
                            </el-tag>
                            <el-tag v-else>
                              <input
                                type="radio"
                                :name="scope.row.skuName"
                                @change="checkDefaultImg(scope.row,index,img)"
                              />Set Default
                            </el-tag>
                          </el-col>
                        </el-row>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
                <!-- Discount, Full Reduction, Member Price -->
                <el-form :model="scope.row">
                  <el-row>
                    <el-col :span="24">
                      <el-form-item label="Set Discount">
                        <label>Full</label>
                        <el-input-number
                          style="width:160px"
                          :min="0"
                          controls-position="right"
                          v-model="scope.row.fullCount"
                        ></el-input-number>
                        <label>Items</label>

                        <label style="margin-left:15px;">Discount</label>
                        <el-input-number
                          style="width:160px"
                          v-model="scope.row.discount"
                          :precision="2"
                          :max="1"
                          :min="0"
                          :step="0.01"
                          controls-position="right"
                        ></el-input-number>
                        <label>Off</label>
                        <el-checkbox
                          v-model="scope.row.countStatus"
                          :true-label="1"
                          :false-label="0"
                        >Stackable</el-checkbox>
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="Set Full Reduction">
                        <label>Full</label>
                        <el-input-number
                          style="width:160px"
                          v-model="scope.row.fullPrice"
                          :step="100"
                          :min="0"
                          controls-position="right"
                        ></el-input-number>
                        <label>Yuan</label>
                        <label style="margin-left:15px;">Reduce</label>
                        <el-input-number
                          style="width:160px"
                          v-model="scope.row.reducePrice"
                          :step="10"
                          :min="0"
                          controls-position="right"
                        ></el-input-number>
                        <label>Yuan</label>
                        <el-checkbox
                          v-model="scope.row.priceStatus"
                          :true-label="1"
                          :false-label="0"
                        >Stackable</el-checkbox>
                      </el-form-item>
                    </el-col>

                    <el-col :span="24">
                      <el-form-item label="Set Member Price" v-if="scope.row.memberPrice.length>0">
                        <br />
                        <!--   @change="handlePriceChange(scope,mpidx,$event)" -->
                        <el-form-item v-for="(mp,mpidx) in scope.row.memberPrice" :key="mp.id">
                          {{mp.name}}
                          <el-input-number
                            style="width:160px"
                            v-model="scope.row.memberPrice[mpidx].price"
                            :precision="2"
                            :min="0"
                            controls-position="right"
                          ></el-input-number>
                        </el-form-item>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" @click="step = 2">Previous</el-button>
          <el-button type="success" @click="submitSkus">Next: Save Product Info</el-button>
        </el-card>
      </el-col>
      <el-col :span="24" v-show="step==4">
        <el-card class="box-card" style="width:80%;margin:20px auto">
          <h1>Save Successful</h1>
          <el-button type="primary" @click="addAgian">Continue Adding</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
//这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）
//例如：import 《组件名称》 from '《组件路径》';
import CategoryCascader from "../common/category-cascader";
import BrandSelect from "../common/brand-select";
import MultiUpload from "@/components/upload/multiUpload";
export default {
  //import引入的组件需要注入到对象中才能使用
  components: { CategoryCascader, BrandSelect, MultiUpload },
  props: {},
  data() {
    return {
      catPathSub: null,
      brandIdSub: null,
      uploadDialogVisible: false,
      uploadImages: [],
      step: 0,
      //spu_name  spu_description  catalog_id  brand_id  weight  publish_status
      spu: {
        //data to submit
        spuName: "",
        spuDescription: "",
        catalogId: 0,
        brandId: "",
        weight: "",
        publishStatus: 0,
        decript: [], //product details
        images: [], //product images, sku can also add new
        bounds: {
          //points
          buyBounds: 0,
          growBounds: 0
        },
        baseAttrs: [], //base attributes
        skus: [] //all sku info
      },
      spuBaseInfoRules: {
        spuName: [
          { required: true, message: "Please enter product name", trigger: "blur" }
        ],
        spuDescription: [
          { required: true, message: "Please write a simple description", trigger: "blur" }
        ],
        catalogId: [
          { required: true, message: "Please select a category", trigger: "blur" }
        ],
        brandId: [
          { required: true, message: "Please select a brand", trigger: "blur" }
        ],
        decript: [
          { required: true, message: "Please upload product detail images", trigger: "blur" }
        ],
        images: [
          { required: true, message: "Please upload product images", trigger: "blur" }
        ],
        weight: [
          {
            type: "number",
            required: true,
            message: "Please enter correct weight value",
            trigger: "blur"
          }
        ]
      },
      dataResp: {
        //all data returned from backend
        attrGroups: [],
        baseAttrs: [],
        saleAttrs: [],
        tempSaleAttrs: [],
        tableAttrColumn: [],
        memberLevels: [],
        steped: [false, false, false, false, false]
      },
      inputVisible: [],
      inputValue: []
    };
  },
  //computed properties similar to data concept
  computed: {},
  //watch data changes
  watch: {
    uploadImages(val) {
      //extend images option for each sku
      let imgArr = Array.from(new Set(this.spu.images.concat(val)));

      //{imgUrl:"",defaultImg:0} since concat iterates previous, there are many duplicates. so we must get total length of previous + current

      this.spu.skus.forEach((item, index) => {
        let len = imgArr.length - this.spu.skus[index].images.length; //still need this many
        if (len > 0) {
          let imgs = new Array(len);
          imgs = imgs.fill({ imgUrl: "", defaultImg: 0 });
          this.spu.skus[index].images = item.images.concat(imgs);
        }
      });

      this.spu.images = imgArr; //deduplicate
      console.log("this.spu.skus", this.spu.skus);
    }
  },
  //methods
  methods: {
    addAgian() {
      this.step = 0;
      this.resetSpuData();
    },
    resetSpuData() {
      this.spu = {
        spuName: "",
        spuDescription: "",
        catalogId: 0,
        brandId: "",
        weight: "",
        publishStatus: 0,
        decript: [], 
        images: [], 
        bounds: {
          buyBounds: 0,
          growBounds: 0
        },
        baseAttrs: [], 
        skus: []
      };
    },
    handlePriceChange(scope, mpidx, e) {
      this.spu.skus[scope.$index].memberPrice[mpidx].price = e;
    },
    getMemberLevels() {
      this.$http({
        url: this.$http.adornUrl("/member/memberlevel/list"),
        method: "get",
        params: this.$http.adornParams({
          page: 1,
          limit: 500
        })
      })
        .then(({ data }) => {
          this.dataResp.memberLevels = data.page.list;
        })
        .catch(e => {
          console.log(e);
        });
    },
    showInput(idx) {
      console.log("``````", this.view);
      this.inputVisible[idx].view = true;
      // this.$refs['saveTagInput'+idx].$refs.input.focus();
    },
    checkDefaultImg(row, index, img) {
      console.log("Default image", row, index);
      //this image is selected
      row.images[index].imgUrl = img; //default selected
      row.images[index].defaultImg = 1; //modify flag
      //modify other flags
      row.images.forEach((item, idx) => {
        if (idx != index) {
          row.images[idx].defaultImg = 0;
        }
      });
    },
    handleInputConfirm(idx) {
      let inputValue = this.inputValue[idx].val;
      if (inputValue) {
        // this.dynamicTags.push(inputValue);
        if (this.dataResp.saleAttrs[idx].valueSelect == "") {
          this.dataResp.saleAttrs[idx].valueSelect = inputValue;
        } else {
          this.dataResp.saleAttrs[idx].valueSelect += ";" + inputValue;
        }
      }
      this.inputVisible[idx].view = false;
      this.inputValue[idx].val = "";
    },
    collectSpuBaseInfo() {
      //spuBaseForm
      this.$refs.spuBaseForm.validate(valid => {
        if (valid) {
          this.step = 1;
          this.showBaseAttrs();
        } else {
          return false;
        }
      });
    },
    generateSaleAttrs() {
      //process all attrs bound on page into spu, this step must be done
      this.spu.baseAttrs = [];
      this.dataResp.baseAttrs.forEach(item => {
        item.forEach(attr => {
          let { attrId, attrValues, showDesc } = attr;
          //skip attributes without values
          if (attrValues != "") {
            if (attrValues instanceof Array) {
              //multiple values separated by ;
              attrValues = attrValues.join(";");
            }
            this.spu.baseAttrs.push({ attrId, attrValues, showDesc });
          }
        });
      });
      console.log("baseAttrs", this.spu.baseAttrs);
      this.step = 2;
      this.getShowSaleAttr();
    },
    generateSkus() {
      this.step = 3;

      //generate sku based on cartesian product calculation
      let selectValues = [];
      this.dataResp.tableAttrColumn = [];
      this.dataResp.tempSaleAttrs.forEach(item => {
        if (item.attrValues.length > 0) {
          selectValues.push(item.attrValues);
          this.dataResp.tableAttrColumn.push(item);
        }
      });

      let descartes = this.descartes(selectValues);
      //[["黑色","6GB","移动"],["黑色","6GB","联通"],["黑色","8GB","移动"],["黑色","8GB","联通"],
      //["白色","6GB","移动"],["白色","6GB","联通"],["白色","8GB","移动"],["白色","8GB","联通"],
      //["蓝色","6GB","移动"],["蓝色","6GB","联通"],["蓝色","8GB","移动"],["蓝色","8GB","联通"]]
      console.log("Generated combinations", JSON.stringify(descartes));
      //有多少descartes就有多少sku
      let skus = [];

      descartes.forEach((descar, descaridx) => {
        let attrArray = []; //sku属性组
        descar.forEach((de, index) => {
          //构造saleAttr信息
          let saleAttrItem = {
            attrId: this.dataResp.tableAttrColumn[index].attrId,
            attrName: this.dataResp.tableAttrColumn[index].attrName,
            attrValue: de
          };
          attrArray.push(saleAttrItem);
        });
        //先初始化几个images，后面的上传还要加
        let imgs = [];
        this.spu.images.forEach((img, idx) => {
          imgs.push({ imgUrl: "", defaultImg: 0 });
        });

        //会员价，也必须在循环里面生成，否则会导致数据绑定问题
        let memberPrices = [];
        if (this.dataResp.memberLevels.length > 0) {
          for (let i = 0; i < this.dataResp.memberLevels.length; i++) {
            if (this.dataResp.memberLevels[i].priviledgeMemberPrice == 1) {
              memberPrices.push({
                id: this.dataResp.memberLevels[i].id,
                name: this.dataResp.memberLevels[i].name,
                price: 0
              });
            }
          }
        }
        //;descaridx，判断如果之前有就用之前的值;
        let res = this.hasAndReturnSku(this.spu.skus, descar);
        if (res === null) {
          skus.push({
            attr: attrArray,
            skuName: this.spu.spuName + " " + descar.join(" "),
            price: 0,
            skuTitle: this.spu.spuName + " " + descar.join(" "),
            skuSubtitle: "",
            images: imgs,
            descar: descar,
            fullCount: 0,
            discount: 0,
            countStatus: 0,
            fullPrice: 0.0,
            reducePrice: 0.0,
            priceStatus: 0,
            memberPrice: new Array().concat(memberPrices)
          });
        } else {
          skus.push(res);
        }
      });
      this.spu.skus = skus;
      console.log("结果!!!", this.spu.skus, this.dataResp.tableAttrColumn);
    },
    //判断如果包含之前的sku的descar组合，就返回这个sku的详细信息；
    hasAndReturnSku(skus, descar) {
      let res = null;
      if (skus.length > 0) {
        for (let i = 0; i < skus.length; i++) {
          if (skus[i].descar.join(" ") == descar.join(" ")) {
            res = skus[i];
          }
        }
      }
      return res;
    },
    getShowSaleAttr() {
      //获取当前分类可以使用的销售属性
      if (!this.dataResp.steped[1]) {
        this.$http({
          url: this.$http.adornUrl(
            `/product/attr/sale/list/${this.spu.catalogId}`
          ),
          method: "get",
          params: this.$http.adornParams({
            page: 1,
            limit: 500
          })
        }).then(({ data }) => {
          this.dataResp.saleAttrs = data.page.list;
          data.page.list.forEach(item => {
            this.dataResp.tempSaleAttrs.push({
              attrId: item.attrId,
              attrValues: [],
              attrName: item.attrName
            });
            this.inputVisible.push({ view: false });
            this.inputValue.push({ val: "" });
          });
          this.dataResp.steped[1] = true;
        });
      }
    },
    showBaseAttrs() {
      if (!this.dataResp.steped[0]) {
        this.$http({
          url: this.$http.adornUrl(
            `/product/attrgroup/${this.spu.catalogId}/withattr`
          ),
          method: "get",
          params: this.$http.adornParams({})
        }).then(({ data }) => {
          //先对表单的baseAttrs进行初始化
          data.data.forEach(item => {
            let attrArray = [];
            item.attrs.forEach(attr => {
              attrArray.push({
                attrId: attr.attrId,
                attrValues: "",
                showDesc: attr.showDesc
              });
            });
            this.dataResp.baseAttrs.push(attrArray);
          });
          this.dataResp.steped[0] = 0;
          this.dataResp.attrGroups = data.data;
        });
      }
    },

    submitSkus() {
      console.log("~~~~~", JSON.stringify(this.spu));
      this.$confirm("将要提交商品数据，需要一小段时间，是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          this.$http({
            url: this.$http.adornUrl("/product/spuinfo/save"),
            method: "post",
            data: this.$http.adornData(this.spu, false)
          }).then(({ data }) => {
            if (data.code == 0) {
              this.$message({
                type: "success",
                message: "新增商品成功!"
              });
              this.step = 4;
            } else {
              this.$message({
                type: "error",
                message: "保存失败，原因【" + data.msg + "】"
              });
            }
          });
        })
        .catch(e => {
          console.log(e);
          this.$message({
            type: "info",
            message: "已取消"
          });
        });
    },
    //笛卡尔积运算
    descartes(list) {
      //parent上一级索引;count指针计数
      var point = {};

      var result = [];
      var pIndex = null;
      var tempCount = 0;
      var temp = [];

      //根据参数列生成指针对象
      for (var index in list) {
        if (typeof list[index] == "object") {
          point[index] = { parent: pIndex, count: 0 };
          pIndex = index;
        }
      }

      //单维度数据结构直接返回
      if (pIndex == null) {
        return list;
      }

      //动态生成笛卡尔积
      while (true) {
        for (var index in list) {
          tempCount = point[index]["count"];
          temp.push(list[index][tempCount]);
        }

        //压入结果数组
        result.push(temp);
        temp = [];

        //检查指针最大值问题
        while (true) {
          if (point[index]["count"] + 1 >= list[index].length) {
            point[index]["count"] = 0;
            pIndex = point[index]["parent"];
            if (pIndex == null) {
              return result;
            }

            //赋值parent进行再次检查
            index = pIndex;
          } else {
            point[index]["count"]++;
            break;
          }
        }
      }
    }
  },
  //生命周期 - 创建完成（可以访问当前this实例）
  created() {},
  //生命周期 - 挂载完成（可以访问DOM元素）
  mounted() {
    this.catPathSub = PubSub.subscribe("catPath", (msg, val) => {
      this.spu.catalogId = val[val.length - 1];
    });
    this.brandIdSub = PubSub.subscribe("brandId", (msg, val) => {
      this.spu.brandId = val;
    });
    this.getMemberLevels();
  },
  beforeCreate() {}, //生命周期 - 创建之前
  beforeMount() {}, //生命周期 - 挂载之前
  beforeUpdate() {}, //生命周期 - 更新之前
  updated() {}, //生命周期 - 更新之后
  beforeDestroy() {
    PubSub.unsubscribe(this.catPathSub);
    PubSub.unsubscribe(this.brandIdSub);
  }, //生命周期 - 销毁之前
  destroyed() {}, //生命周期 - 销毁完成
  activated() {} //如果页面有keep-alive缓存功能，这个函数会触发
};
</script>
<style scoped>
</style>