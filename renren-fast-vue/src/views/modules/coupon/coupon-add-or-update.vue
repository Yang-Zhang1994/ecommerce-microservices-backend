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
      <el-form-item label="Coupon type" prop="couponType">
        <el-select v-model="dataForm.couponType" placeholder="Select">
          <el-option label="Site-wide gift" :value="0"></el-option>
          <el-option label="Member gift" :value="1"></el-option>
          <el-option label="Purchase gift" :value="2"></el-option>
          <el-option label="Registration gift" :value="3"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="Coupon image" prop="couponImg">
        <single-upload v-model="dataForm.couponImg"></single-upload>
      </el-form-item>
      <el-form-item label="Coupon name" prop="couponName">
        <el-input v-model="dataForm.couponName" placeholder="Coupon name"></el-input>
      </el-form-item>
      <el-form-item label="Quantity" prop="num">
        <el-input-number :min="0" v-model="dataForm.num"></el-input-number>
      </el-form-item>
      <el-form-item label="Amount" prop="amount">
        <el-input-number :min="0" v-model="dataForm.amount" :precision="2"></el-input-number>
      </el-form-item>
      <el-form-item label="Limit per person" prop="perLimit">
        <el-input-number :min="0" v-model="dataForm.perLimit"></el-input-number>
      </el-form-item>
      <el-form-item label="Min points to use" prop="minPoint">
        <el-input-number :min="0" v-model="dataForm.minPoint"></el-input-number>
      </el-form-item>
      <el-form-item label="Valid period" prop="useTimeRange">
        <el-date-picker
          v-model="dataForm.useTimeRange"
          type="daterange"
          range-separator="至"
          start-placeholder="Start time"
          end-placeholder="End time"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="Usage type" prop="useType">
        <el-select v-model="dataForm.useType" placeholder="Select">
          <el-option :value="0" label="Site-wide"></el-option>
          <el-option :value="1" label="Specific category"></el-option>
          <el-option :value="2" label="Specific product"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="Remark" prop="note">
        <el-input v-model="dataForm.note" placeholder="Remark"></el-input>
      </el-form-item>
      <el-form-item label="Publish quantity" prop="publishCount">
        <el-input-number v-model="dataForm.publishCount" :min="0"></el-input-number>
      </el-form-item>
      <el-form-item label="Claim period" prop="enableStartTime">
        <el-date-picker
          v-model="dataForm.timeRange"
          type="daterange"
          range-separator="至"
          start-placeholder="Start date"
          end-placeholder="End date"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="Promo code" prop="code">
        <el-input v-model="dataForm.code" placeholder="Promo code"></el-input>
      </el-form-item>
      <el-form-item label="Required member level" prop="memberLevel">
        <el-select v-model="dataForm.memberLevel" placeholder="Select">
          <el-option :value="0" label="No limit"></el-option>
          <el-option
            v-for="item in memberLevels"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          ></el-option>
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
import SingleUpload from "@/components/upload/singleUpload";
export default {
  components: { SingleUpload },
  data() {
    return {
      visible: false,
      memberLevels: [],
      dataForm: {
        id: 0,
        couponType: "",
        couponImg: "",
        couponName: "",
        num: "",
        amount: "",
        perLimit: "",
        minPoint: "",
        startTime: "",
        endTime: "",
        useType: "",
        note: "",
        publishCount: "",
        useCount: "",
        receiveCount: "",
        enableStartTime: "",
        enableEndTime: "",
        code: "",
        memberLevel: "",
        publish: 0,
        timeRange: [],
        useTimeRange:[]
      },
      dataRule: {
        couponType: [
          {
            required: true,
            message: 'This field is required',
            trigger: "blur"
          }
        ],
        couponImg: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        couponName: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        num: [{ required: true, message: 'This field is required', trigger: "blur" }],
        amount: [{ required: true, message: 'This field is required', trigger: "blur" }],
        perLimit: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        minPoint: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        useType: [
          {
            required: true,
            message: 'This field is required',
            trigger: "blur"
          }
        ],
        note: [{ required: true, message: 'Remark is required', trigger: "blur" }],
        publishCount: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        enableStartTime: [
          {
            required: true,
            message: 'This field is required',
            trigger: "blur"
          }
        ],
        enableEndTime: [
          {
            required: true,
            message: 'This field is required',
            trigger: "blur"
          }
        ],
        code: [{ required: true, message: 'This field is required', trigger: "blur" }],
        memberLevel: [
          {
            required: true,
            message: 'This field is required',
            trigger: "blur"
          }
        ]
      }
    };
  },
  created() {
    this.getMemberLevels();
  },
  methods: {
    getMemberLevels() {
      //获取所有的Member Level
      this.$http({
        url: this.$http.adornUrl("/member/memberlevel/list"),
        method: "get",
        params: this.$http.adornParams({
          page: 1,
          limit: 500
        })
      }).then(({ data }) => {
        this.memberLevels = data.page.list;
      });
    },
    init(id) {
      this.dataForm.id = id || 0;
      this.visible = true;
      this.$nextTick(() => {
        this.$refs["dataForm"].resetFields();
        if (this.dataForm.id) {
          this.$http({
            url: this.$http.adornUrl(`/coupon/coupon/info/${this.dataForm.id}`),
            method: "get",
            params: this.$http.adornParams()
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.dataForm.couponType = data.coupon.couponType;
              this.dataForm.couponImg = data.coupon.couponImg;
              this.dataForm.couponName = data.coupon.couponName;
              this.dataForm.num = data.coupon.num;
              this.dataForm.amount = data.coupon.amount;
              this.dataForm.perLimit = data.coupon.perLimit;
              this.dataForm.minPoint = data.coupon.minPoint;
              this.dataForm.startTime = data.coupon.startTime;
              this.dataForm.endTime = data.coupon.endTime;
              this.dataForm.useType = data.coupon.useType;
              this.dataForm.note = data.coupon.note;
              this.dataForm.publishCount = data.coupon.publishCount;
              this.dataForm.useCount = data.coupon.useCount;
              this.dataForm.receiveCount = data.coupon.receiveCount;
              this.dataForm.enableStartTime = data.coupon.enableStartTime;
              this.dataForm.enableEndTime = data.coupon.enableEndTime;
              this.dataForm.code = data.coupon.code;
              this.dataForm.memberLevel = data.coupon.memberLevel;
              this.dataForm.publish = data.coupon.publish;
              this.dataForm.timeRange = [
                this.dataForm.startTime,
                this.dataForm.endTime
              ];
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
              `/coupon/coupon/${!this.dataForm.id ? "save" : "update"}`
            ),
            method: "post",
            data: this.$http.adornData({
              id: this.dataForm.id || undefined,
              couponType: this.dataForm.couponType,
              couponImg: this.dataForm.couponImg,
              couponName: this.dataForm.couponName,
              num: this.dataForm.num,
              amount: this.dataForm.amount,
              perLimit: this.dataForm.perLimit,
              minPoint: this.dataForm.minPoint,
              startTime: this.dataForm.useTimeRange[0],
              endTime: this.dataForm.useTimeRange[1],
              useType: this.dataForm.useType,
              note: this.dataForm.note,
              publishCount: this.dataForm.publishCount,
              useCount: this.dataForm.useCount,
              receiveCount: this.dataForm.receiveCount,
              enableStartTime: this.dataForm.timeRange[0],
              enableEndTime: this.dataForm.timeRange[1],
              code: this.dataForm.code,
              memberLevel: this.dataForm.memberLevel,
              publish: this.dataForm.publish
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
