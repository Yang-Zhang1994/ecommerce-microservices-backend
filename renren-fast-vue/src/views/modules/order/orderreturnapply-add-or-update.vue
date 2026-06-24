<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
    <el-form-item label="order_id" prop="orderId">
      <el-input v-model="dataForm.orderId" placeholder="order_id"></el-input>
    </el-form-item>
    <el-form-item label="Return SKU ID" prop="skuId">
      <el-input v-model="dataForm.skuId" placeholder="Return SKU ID"></el-input>
    </el-form-item>
    <el-form-item label="Order SN" prop="orderSn">
      <el-input v-model="dataForm.orderSn" placeholder="Order SN"></el-input>
    </el-form-item>
    <el-form-item label="Apply Time" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="Apply Time"></el-input>
    </el-form-item>
    <el-form-item label="Member Username" prop="memberUsername">
      <el-input v-model="dataForm.memberUsername" placeholder="Member Username"></el-input>
    </el-form-item>
    <el-form-item label="Refund Amount" prop="returnAmount">
      <el-input v-model="dataForm.returnAmount" placeholder="Refund Amount"></el-input>
    </el-form-item>
    <el-form-item label="Returner Name" prop="returnName">
      <el-input v-model="dataForm.returnName" placeholder="Returner Name"></el-input>
    </el-form-item>
    <el-form-item label="Returner Phone" prop="returnPhone">
      <el-input v-model="dataForm.returnPhone" placeholder="Returner Phone"></el-input>
    </el-form-item>
    <el-form-item label="Status [0-Pending 1-Returning 2-Done 3-Rejected]" prop="status">
      <el-input v-model="dataForm.status" placeholder="Status [0-Pending 1-Returning 2-Done 3-Rejected]"></el-input>
    </el-form-item>
    <el-form-item label="Handle Time" prop="handleTime">
      <el-input v-model="dataForm.handleTime" placeholder="Handle Time"></el-input>
    </el-form-item>
    <el-form-item label="Product Image" prop="skuImg">
      <el-input v-model="dataForm.skuImg" placeholder="Product Image"></el-input>
    </el-form-item>
    <el-form-item label="Product Name" prop="skuName">
      <el-input v-model="dataForm.skuName" placeholder="Product Name"></el-input>
    </el-form-item>
    <el-form-item label="Product Brand" prop="skuBrand">
      <el-input v-model="dataForm.skuBrand" placeholder="Product Brand"></el-input>
    </el-form-item>
    <el-form-item label="Sale Attributes (JSON)" prop="skuAttrsVals">
      <el-input v-model="dataForm.skuAttrsVals" placeholder="Sale Attributes (JSON)"></el-input>
    </el-form-item>
    <el-form-item label="Return Qty" prop="skuCount">
      <el-input v-model="dataForm.skuCount" placeholder="Return Qty"></el-input>
    </el-form-item>
    <el-form-item label="Unit Price" prop="skuPrice">
      <el-input v-model="dataForm.skuPrice" placeholder="Unit Price"></el-input>
    </el-form-item>
    <el-form-item label="Paid Unit Price" prop="skuRealPrice">
      <el-input v-model="dataForm.skuRealPrice" placeholder="Paid Unit Price"></el-input>
    </el-form-item>
    <el-form-item label="Reason" prop="reason">
      <el-input v-model="dataForm.reason" placeholder="Reason"></el-input>
    </el-form-item>
    <el-form-item label="Description" prop="description">
      <el-input v-model="dataForm.description" placeholder="Description"></el-input>
    </el-form-item>
    <el-form-item label="Proof Images (comma-separated)" prop="descPics">
      <el-input v-model="dataForm.descPics" placeholder="Proof Images (comma-separated)"></el-input>
    </el-form-item>
    <el-form-item label="Handle Note" prop="handleNote">
      <el-input v-model="dataForm.handleNote" placeholder="Handle Note"></el-input>
    </el-form-item>
    <el-form-item label="Handler" prop="handleMan">
      <el-input v-model="dataForm.handleMan" placeholder="Handler"></el-input>
    </el-form-item>
    <el-form-item label="Receiver" prop="receiveMan">
      <el-input v-model="dataForm.receiveMan" placeholder="Receiver"></el-input>
    </el-form-item>
    <el-form-item label="Receive Time" prop="receiveTime">
      <el-input v-model="dataForm.receiveTime" placeholder="Receive Time"></el-input>
    </el-form-item>
    <el-form-item label="Receive Note" prop="receiveNote">
      <el-input v-model="dataForm.receiveNote" placeholder="Receive Note"></el-input>
    </el-form-item>
    <el-form-item label="Receive Phone" prop="receivePhone">
      <el-input v-model="dataForm.receivePhone" placeholder="Receive Phone"></el-input>
    </el-form-item>
    <el-form-item label="Company Address" prop="companyAddress">
      <el-input v-model="dataForm.companyAddress" placeholder="Company Address"></el-input>
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
          skuId: '',
          orderSn: '',
          createTime: '',
          memberUsername: '',
          returnAmount: '',
          returnName: '',
          returnPhone: '',
          status: '',
          handleTime: '',
          skuImg: '',
          skuName: '',
          skuBrand: '',
          skuAttrsVals: '',
          skuCount: '',
          skuPrice: '',
          skuRealPrice: '',
          reason: '',
          description: '',
          descPics: '',
          handleNote: '',
          handleMan: '',
          receiveMan: '',
          receiveTime: '',
          receiveNote: '',
          receivePhone: '',
          companyAddress: ''
        },
        dataRule: {
          orderId: [
            { required: true, message: 'order_id is required', trigger: 'blur' }
          ],
          skuId: [
            { required: true, message: 'Return SKU ID is required', trigger: 'blur' }
          ],
          orderSn: [
            { required: true, message: 'Order SN is required', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: 'Apply Time is required', trigger: 'blur' }
          ],
          memberUsername: [
            { required: true, message: 'Member Username is required', trigger: 'blur' }
          ],
          returnAmount: [
            { required: true, message: 'Refund Amount is required', trigger: 'blur' }
          ],
          returnName: [
            { required: true, message: 'Returner Name is required', trigger: 'blur' }
          ],
          returnPhone: [
            { required: true, message: 'Returner Phone is required', trigger: 'blur' }
          ],
          status: [
            { required: true, message: 'Status [0-Pending 1-Returning 2-Done 3-Rejected] is required', trigger: 'blur' }
          ],
          handleTime: [
            { required: true, message: 'Handle Time is required', trigger: 'blur' }
          ],
          skuImg: [
            { required: true, message: 'Product Image is required', trigger: 'blur' }
          ],
          skuName: [
            { required: true, message: 'Product Name is required', trigger: 'blur' }
          ],
          skuBrand: [
            { required: true, message: 'Product Brand is required', trigger: 'blur' }
          ],
          skuAttrsVals: [
            { required: true, message: 'Sale Attributes (JSON) is required', trigger: 'blur' }
          ],
          skuCount: [
            { required: true, message: 'Return Qty is required', trigger: 'blur' }
          ],
          skuPrice: [
            { required: true, message: 'Unit Price is required', trigger: 'blur' }
          ],
          skuRealPrice: [
            { required: true, message: 'Paid Unit Price is required', trigger: 'blur' }
          ],
          reason: [
            { required: true, message: 'Reason is required', trigger: 'blur' }
          ],
          description: [
            { required: true, message: 'Description is required', trigger: 'blur' }
          ],
          descPics: [
            { required: true, message: 'Proof Images (comma-separated) is required', trigger: 'blur' }
          ],
          handleNote: [
            { required: true, message: 'Handle Note is required', trigger: 'blur' }
          ],
          handleMan: [
            { required: true, message: 'Handler is required', trigger: 'blur' }
          ],
          receiveMan: [
            { required: true, message: 'Receiver is required', trigger: 'blur' }
          ],
          receiveTime: [
            { required: true, message: 'Receive Time is required', trigger: 'blur' }
          ],
          receiveNote: [
            { required: true, message: 'Receive Note is required', trigger: 'blur' }
          ],
          receivePhone: [
            { required: true, message: 'Receive Phone is required', trigger: 'blur' }
          ],
          companyAddress: [
            { required: true, message: 'Company Address is required', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/order/orderreturnapply/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.orderId = data.orderReturnApply.orderId
                this.dataForm.skuId = data.orderReturnApply.skuId
                this.dataForm.orderSn = data.orderReturnApply.orderSn
                this.dataForm.createTime = data.orderReturnApply.createTime
                this.dataForm.memberUsername = data.orderReturnApply.memberUsername
                this.dataForm.returnAmount = data.orderReturnApply.returnAmount
                this.dataForm.returnName = data.orderReturnApply.returnName
                this.dataForm.returnPhone = data.orderReturnApply.returnPhone
                this.dataForm.status = data.orderReturnApply.status
                this.dataForm.handleTime = data.orderReturnApply.handleTime
                this.dataForm.skuImg = data.orderReturnApply.skuImg
                this.dataForm.skuName = data.orderReturnApply.skuName
                this.dataForm.skuBrand = data.orderReturnApply.skuBrand
                this.dataForm.skuAttrsVals = data.orderReturnApply.skuAttrsVals
                this.dataForm.skuCount = data.orderReturnApply.skuCount
                this.dataForm.skuPrice = data.orderReturnApply.skuPrice
                this.dataForm.skuRealPrice = data.orderReturnApply.skuRealPrice
                this.dataForm.reason = data.orderReturnApply.reason
                this.dataForm.description = data.orderReturnApply.description
                this.dataForm.descPics = data.orderReturnApply.descPics
                this.dataForm.handleNote = data.orderReturnApply.handleNote
                this.dataForm.handleMan = data.orderReturnApply.handleMan
                this.dataForm.receiveMan = data.orderReturnApply.receiveMan
                this.dataForm.receiveTime = data.orderReturnApply.receiveTime
                this.dataForm.receiveNote = data.orderReturnApply.receiveNote
                this.dataForm.receivePhone = data.orderReturnApply.receivePhone
                this.dataForm.companyAddress = data.orderReturnApply.companyAddress
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
              url: this.$http.adornUrl(`/order/orderreturnapply/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'orderId': this.dataForm.orderId,
                'skuId': this.dataForm.skuId,
                'orderSn': this.dataForm.orderSn,
                'createTime': this.dataForm.createTime,
                'memberUsername': this.dataForm.memberUsername,
                'returnAmount': this.dataForm.returnAmount,
                'returnName': this.dataForm.returnName,
                'returnPhone': this.dataForm.returnPhone,
                'status': this.dataForm.status,
                'handleTime': this.dataForm.handleTime,
                'skuImg': this.dataForm.skuImg,
                'skuName': this.dataForm.skuName,
                'skuBrand': this.dataForm.skuBrand,
                'skuAttrsVals': this.dataForm.skuAttrsVals,
                'skuCount': this.dataForm.skuCount,
                'skuPrice': this.dataForm.skuPrice,
                'skuRealPrice': this.dataForm.skuRealPrice,
                'reason': this.dataForm.reason,
                'description': this.dataForm.description,
                'descPics': this.dataForm.descPics,
                'handleNote': this.dataForm.handleNote,
                'handleMan': this.dataForm.handleMan,
                'receiveMan': this.dataForm.receiveMan,
                'receiveTime': this.dataForm.receiveTime,
                'receiveNote': this.dataForm.receiveNote,
                'receivePhone': this.dataForm.receivePhone,
                'companyAddress': this.dataForm.companyAddress
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
