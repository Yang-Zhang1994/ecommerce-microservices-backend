<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
    <el-form-item label="order_id" prop="orderId">
      <el-input v-model="dataForm.orderId" placeholder="order_id"></el-input>
    </el-form-item>
    <el-form-item label="order_sn" prop="orderSn">
      <el-input v-model="dataForm.orderSn" placeholder="order_sn"></el-input>
    </el-form-item>
    <el-form-item label="spu_id" prop="spuId">
      <el-input v-model="dataForm.spuId" placeholder="spu_id"></el-input>
    </el-form-item>
    <el-form-item label="spu_name" prop="spuName">
      <el-input v-model="dataForm.spuName" placeholder="spu_name"></el-input>
    </el-form-item>
    <el-form-item label="spu_pic" prop="spuPic">
      <el-input v-model="dataForm.spuPic" placeholder="spu_pic"></el-input>
    </el-form-item>
    <el-form-item label="Brand" prop="spuBrand">
      <el-input v-model="dataForm.spuBrand" placeholder="Brand"></el-input>
    </el-form-item>
    <el-form-item label="Category ID" prop="categoryId">
      <el-input v-model="dataForm.categoryId" placeholder="Category ID"></el-input>
    </el-form-item>
    <el-form-item label="SKU ID" prop="skuId">
      <el-input v-model="dataForm.skuId" placeholder="SKU ID"></el-input>
    </el-form-item>
    <el-form-item label="SKU Name" prop="skuName">
      <el-input v-model="dataForm.skuName" placeholder="SKU Name"></el-input>
    </el-form-item>
    <el-form-item label="SKU Image" prop="skuPic">
      <el-input v-model="dataForm.skuPic" placeholder="SKU Image"></el-input>
    </el-form-item>
    <el-form-item label="SKU Price" prop="skuPrice">
      <el-input v-model="dataForm.skuPrice" placeholder="SKU Price"></el-input>
    </el-form-item>
    <el-form-item label="Quantity" prop="skuQuantity">
      <el-input v-model="dataForm.skuQuantity" placeholder="Quantity"></el-input>
    </el-form-item>
    <el-form-item label="Sale Attributes (JSON)" prop="skuAttrsVals">
      <el-input v-model="dataForm.skuAttrsVals" placeholder="Sale Attributes (JSON)"></el-input>
    </el-form-item>
    <el-form-item label="Promotion Amount" prop="promotionAmount">
      <el-input v-model="dataForm.promotionAmount" placeholder="Promotion Amount"></el-input>
    </el-form-item>
    <el-form-item label="Coupon Amount" prop="couponAmount">
      <el-input v-model="dataForm.couponAmount" placeholder="Coupon Amount"></el-input>
    </el-form-item>
    <el-form-item label="Points Amount" prop="integrationAmount">
      <el-input v-model="dataForm.integrationAmount" placeholder="Points Amount"></el-input>
    </el-form-item>
    <el-form-item label="Real Amount" prop="realAmount">
      <el-input v-model="dataForm.realAmount" placeholder="Real Amount"></el-input>
    </el-form-item>
    <el-form-item label="Gift Points" prop="giftIntegration">
      <el-input v-model="dataForm.giftIntegration" placeholder="Gift Points"></el-input>
    </el-form-item>
    <el-form-item label="Gift Growth" prop="giftGrowth">
      <el-input v-model="dataForm.giftGrowth" placeholder="Gift Growth"></el-input>
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
    data () {
      return {
        visible: false,
        dataForm: {
          id: 0,
          orderId: '',
          orderSn: '',
          spuId: '',
          spuName: '',
          spuPic: '',
          spuBrand: '',
          categoryId: '',
          skuId: '',
          skuName: '',
          skuPic: '',
          skuPrice: '',
          skuQuantity: '',
          skuAttrsVals: '',
          promotionAmount: '',
          couponAmount: '',
          integrationAmount: '',
          realAmount: '',
          giftIntegration: '',
          giftGrowth: ''
        },
        dataRule: {
          orderId: [
            { required: true, message: 'order_id is required', trigger: 'blur' }
          ],
          orderSn: [
            { required: true, message: 'order_sn is required', trigger: 'blur' }
          ],
          spuId: [
            { required: true, message: 'spu_id is required', trigger: 'blur' }
          ],
          spuName: [
            { required: true, message: 'spu_name is required', trigger: 'blur' }
          ],
          spuPic: [
            { required: true, message: 'spu_pic is required', trigger: 'blur' }
          ],
          spuBrand: [
            { required: true, message: 'Brand is required', trigger: 'blur' }
          ],
          categoryId: [
            { required: true, message: 'Category ID is required', trigger: 'blur' }
          ],
          skuId: [
            { required: true, message: 'SKU ID is required', trigger: 'blur' }
          ],
          skuName: [
            { required: true, message: 'SKU Name is required', trigger: 'blur' }
          ],
          skuPic: [
            { required: true, message: 'SKU Image is required', trigger: 'blur' }
          ],
          skuPrice: [
            { required: true, message: 'SKU Price is required', trigger: 'blur' }
          ],
          skuQuantity: [
            { required: true, message: 'Quantity is required', trigger: 'blur' }
          ],
          skuAttrsVals: [
            { required: true, message: 'Sale Attributes (JSON) is required', trigger: 'blur' }
          ],
          promotionAmount: [
            { required: true, message: 'Promotion Amount is required', trigger: 'blur' }
          ],
          couponAmount: [
            { required: true, message: 'Coupon Amount is required', trigger: 'blur' }
          ],
          integrationAmount: [
            { required: true, message: 'Points Amount is required', trigger: 'blur' }
          ],
          realAmount: [
            { required: true, message: 'Real Amount is required', trigger: 'blur' }
          ],
          giftIntegration: [
            { required: true, message: 'Gift Points is required', trigger: 'blur' }
          ],
          giftGrowth: [
            { required: true, message: 'Gift Growth is required', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/order/orderitem/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.orderId = data.orderItem.orderId
                this.dataForm.orderSn = data.orderItem.orderSn
                this.dataForm.spuId = data.orderItem.spuId
                this.dataForm.spuName = data.orderItem.spuName
                this.dataForm.spuPic = data.orderItem.spuPic
                this.dataForm.spuBrand = data.orderItem.spuBrand
                this.dataForm.categoryId = data.orderItem.categoryId
                this.dataForm.skuId = data.orderItem.skuId
                this.dataForm.skuName = data.orderItem.skuName
                this.dataForm.skuPic = data.orderItem.skuPic
                this.dataForm.skuPrice = data.orderItem.skuPrice
                this.dataForm.skuQuantity = data.orderItem.skuQuantity
                this.dataForm.skuAttrsVals = data.orderItem.skuAttrsVals
                this.dataForm.promotionAmount = data.orderItem.promotionAmount
                this.dataForm.couponAmount = data.orderItem.couponAmount
                this.dataForm.integrationAmount = data.orderItem.integrationAmount
                this.dataForm.realAmount = data.orderItem.realAmount
                this.dataForm.giftIntegration = data.orderItem.giftIntegration
                this.dataForm.giftGrowth = data.orderItem.giftGrowth
              }
            })
          }
        })
      },
      // Form submit
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/order/orderitem/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'orderId': this.dataForm.orderId,
                'orderSn': this.dataForm.orderSn,
                'spuId': this.dataForm.spuId,
                'spuName': this.dataForm.spuName,
                'spuPic': this.dataForm.spuPic,
                'spuBrand': this.dataForm.spuBrand,
                'categoryId': this.dataForm.categoryId,
                'skuId': this.dataForm.skuId,
                'skuName': this.dataForm.skuName,
                'skuPic': this.dataForm.skuPic,
                'skuPrice': this.dataForm.skuPrice,
                'skuQuantity': this.dataForm.skuQuantity,
                'skuAttrsVals': this.dataForm.skuAttrsVals,
                'promotionAmount': this.dataForm.promotionAmount,
                'couponAmount': this.dataForm.couponAmount,
                'integrationAmount': this.dataForm.integrationAmount,
                'realAmount': this.dataForm.realAmount,
                'giftIntegration': this.dataForm.giftIntegration,
                'giftGrowth': this.dataForm.giftGrowth
              })
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: 'Operation successful',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>
