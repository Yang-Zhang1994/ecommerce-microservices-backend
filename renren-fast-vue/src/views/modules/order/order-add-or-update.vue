<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
    <el-form-item label="member_id" prop="memberId">
      <el-input v-model="dataForm.memberId" placeholder="member_id"></el-input>
    </el-form-item>
    <el-form-item label="Order Number" prop="orderSn">
      <el-input v-model="dataForm.orderSn" placeholder="Order Number"></el-input>
    </el-form-item>
    <el-form-item label="Coupon Used" prop="couponId">
      <el-input v-model="dataForm.couponId" placeholder="Coupon Used"></el-input>
    </el-form-item>
    <el-form-item label="create_time" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="create_time"></el-input>
    </el-form-item>
    <el-form-item label="Username" prop="memberUsername">
      <el-input v-model="dataForm.memberUsername" placeholder="Username"></el-input>
    </el-form-item>
    <el-form-item label="Total Amount" prop="totalAmount">
      <el-input v-model="dataForm.totalAmount" placeholder="Total Amount"></el-input>
    </el-form-item>
    <el-form-item label="Pay Amount" prop="payAmount">
      <el-input v-model="dataForm.payAmount" placeholder="Pay Amount"></el-input>
    </el-form-item>
    <el-form-item label="Freight Amount" prop="freightAmount">
      <el-input v-model="dataForm.freightAmount" placeholder="Freight Amount"></el-input>
    </el-form-item>
    <el-form-item label="Promotion Amount" prop="promotionAmount">
      <el-input v-model="dataForm.promotionAmount" placeholder="Promotion Amount"></el-input>
    </el-form-item>
    <el-form-item label="Points Deduction" prop="integrationAmount">
      <el-input v-model="dataForm.integrationAmount" placeholder="Points Deduction"></el-input>
    </el-form-item>
    <el-form-item label="Coupon Deduction" prop="couponAmount">
      <el-input v-model="dataForm.couponAmount" placeholder="Coupon Deduction"></el-input>
    </el-form-item>
    <el-form-item label="Admin Discount" prop="discountAmount">
      <el-input v-model="dataForm.discountAmount" placeholder="Admin Discount"></el-input>
    </el-form-item>
    <el-form-item label="Payment Method [1-Alipay 2-WeChat 3-UnionPay 4-COD]" prop="payType">
      <el-input v-model="dataForm.payType" placeholder="Payment Method [1-Alipay 2-WeChat 3-UnionPay 4-COD]"></el-input>
    </el-form-item>
    <el-form-item label="Source [0-PC 1-App]" prop="sourceType">
      <el-input v-model="dataForm.sourceType" placeholder="Source [0-PC 1-App]"></el-input>
    </el-form-item>
    <el-form-item label="Order Status [0-Pending 1-To ship 2-Shipped 3-Done 4-Closed 5-Invalid]" prop="status">
      <el-input v-model="dataForm.status" placeholder="Order Status [0-Pending 1-To ship 2-Shipped 3-Done 4-Closed 5-Invalid]"></el-input>
    </el-form-item>
    <el-form-item label="Delivery Company" prop="deliveryCompany">
      <el-input v-model="dataForm.deliveryCompany" placeholder="Delivery Company"></el-input>
    </el-form-item>
    <el-form-item label="Tracking Number" prop="deliverySn">
      <el-input v-model="dataForm.deliverySn" placeholder="Tracking Number"></el-input>
    </el-form-item>
    <el-form-item label="Auto Confirm Time (Days)" prop="autoConfirmDay">
      <el-input v-model="dataForm.autoConfirmDay" placeholder="Auto Confirm Time (Days)"></el-input>
    </el-form-item>
    <el-form-item label="Points Earned" prop="integration">
      <el-input v-model="dataForm.integration" placeholder="Points Earned"></el-input>
    </el-form-item>
    <el-form-item label="Growth Value Earned" prop="growth">
      <el-input v-model="dataForm.growth" placeholder="Growth Value Earned"></el-input>
    </el-form-item>
    <el-form-item label="Invoice Type [0->No Invoice; 1->Electronic; 2->Paper]" prop="billType">
      <el-input v-model="dataForm.billType" placeholder="Invoice Type [0->No Invoice; 1->Electronic; 2->Paper]"></el-input>
    </el-form-item>
    <el-form-item label="Invoice Header" prop="billHeader">
      <el-input v-model="dataForm.billHeader" placeholder="Invoice Header"></el-input>
    </el-form-item>
    <el-form-item label="Invoice Content" prop="billContent">
      <el-input v-model="dataForm.billContent" placeholder="Invoice Content"></el-input>
    </el-form-item>
    <el-form-item label="Invoice Receiver Phone" prop="billReceiverPhone">
      <el-input v-model="dataForm.billReceiverPhone" placeholder="Invoice Receiver Phone"></el-input>
    </el-form-item>
    <el-form-item label="Invoice Receiver Email" prop="billReceiverEmail">
      <el-input v-model="dataForm.billReceiverEmail" placeholder="Invoice Receiver Email"></el-input>
    </el-form-item>
    <el-form-item label="Receiver Name" prop="receiverName">
      <el-input v-model="dataForm.receiverName" placeholder="Receiver Name"></el-input>
    </el-form-item>
    <el-form-item label="Receiver Phone" prop="receiverPhone">
      <el-input v-model="dataForm.receiverPhone" placeholder="Receiver Phone"></el-input>
    </el-form-item>
    <el-form-item label="Receiver Postal Code" prop="receiverPostCode">
      <el-input v-model="dataForm.receiverPostCode" placeholder="Receiver Postal Code"></el-input>
    </el-form-item>
    <el-form-item label="Province/Municipality" prop="receiverProvince">
      <el-input v-model="dataForm.receiverProvince" placeholder="Province/Municipality"></el-input>
    </el-form-item>
    <el-form-item label="City" prop="receiverCity">
      <el-input v-model="dataForm.receiverCity" placeholder="City"></el-input>
    </el-form-item>
    <el-form-item label="Region" prop="receiverRegion">
      <el-input v-model="dataForm.receiverRegion" placeholder="Region"></el-input>
    </el-form-item>
    <el-form-item label="Detail Address" prop="receiverDetailAddress">
      <el-input v-model="dataForm.receiverDetailAddress" placeholder="Detail Address"></el-input>
    </el-form-item>
    <el-form-item label="Order Remarks" prop="note">
      <el-input v-model="dataForm.note" placeholder="Order Remarks"></el-input>
    </el-form-item>
    <el-form-item label="Confirm Receipt Status [0->Unconfirmed; 1->Confirmed]" prop="confirmStatus">
      <el-input v-model="dataForm.confirmStatus" placeholder="Confirm Receipt Status [0->Unconfirmed; 1->Confirmed]"></el-input>
    </el-form-item>
    <el-form-item label="Delete Status [0->Not Deleted; 1->Deleted]" prop="deleteStatus">
      <el-input v-model="dataForm.deleteStatus" placeholder="Delete Status [0->Not Deleted; 1->Deleted]"></el-input>
    </el-form-item>
    <el-form-item label="Points Used When Ordering" prop="useIntegration">
      <el-input v-model="dataForm.useIntegration" placeholder="Points Used When Ordering"></el-input>
    </el-form-item>
    <el-form-item label="Payment Time" prop="paymentTime">
      <el-input v-model="dataForm.paymentTime" placeholder="Payment Time"></el-input>
    </el-form-item>
    <el-form-item label="Delivery Time" prop="deliveryTime">
      <el-input v-model="dataForm.deliveryTime" placeholder="Delivery Time"></el-input>
    </el-form-item>
    <el-form-item label="Confirm Receipt Time" prop="receiveTime">
      <el-input v-model="dataForm.receiveTime" placeholder="Confirm Receipt Time"></el-input>
    </el-form-item>
    <el-form-item label="Comment Time" prop="commentTime">
      <el-input v-model="dataForm.commentTime" placeholder="Comment Time"></el-input>
    </el-form-item>
    <el-form-item label="Modify Time" prop="modifyTime">
      <el-input v-model="dataForm.modifyTime" placeholder="Modify Time"></el-input>
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
          memberId: '',
          orderSn: '',
          couponId: '',
          createTime: '',
          memberUsername: '',
          totalAmount: '',
          payAmount: '',
          freightAmount: '',
          promotionAmount: '',
          integrationAmount: '',
          couponAmount: '',
          discountAmount: '',
          payType: '',
          sourceType: '',
          status: '',
          deliveryCompany: '',
          deliverySn: '',
          autoConfirmDay: '',
          integration: '',
          growth: '',
          billType: '',
          billHeader: '',
          billContent: '',
          billReceiverPhone: '',
          billReceiverEmail: '',
          receiverName: '',
          receiverPhone: '',
          receiverPostCode: '',
          receiverProvince: '',
          receiverCity: '',
          receiverRegion: '',
          receiverDetailAddress: '',
          note: '',
          confirmStatus: '',
          deleteStatus: '',
          useIntegration: '',
          paymentTime: '',
          deliveryTime: '',
          receiveTime: '',
          commentTime: '',
          modifyTime: ''
        },
        dataRule: {
          memberId: [
            { required: true, message: 'member_id is required', trigger: 'blur' }
          ],
          orderSn: [
            { required: true, message: 'Order Number is required', trigger: 'blur' }
          ],
          couponId: [
            { required: true, message: 'Coupon Used is required', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: 'create_time is required', trigger: 'blur' }
          ],
          memberUsername: [
            { required: true, message: 'Username is required', trigger: 'blur' }
          ],
          totalAmount: [
            { required: true, message: 'Total Amount is required', trigger: 'blur' }
          ],
          payAmount: [
            { required: true, message: 'Pay Amount is required', trigger: 'blur' }
          ],
          freightAmount: [
            { required: true, message: 'Freight Amount is required', trigger: 'blur' }
          ],
          promotionAmount: [
            { required: true, message: 'Promotion Amount is required', trigger: 'blur' }
          ],
          integrationAmount: [
            { required: true, message: 'Points Deduction is required', trigger: 'blur' }
          ],
          couponAmount: [
            { required: true, message: 'Coupon Deduction is required', trigger: 'blur' }
          ],
          discountAmount: [
            { required: true, message: 'Admin Discount is required', trigger: 'blur' }
          ],
          payType: [
            { required: true, message: 'Payment Method [1-Alipay 2-WeChat 3-UnionPay 4-COD] is required', trigger: 'blur' }
          ],
          sourceType: [
            { required: true, message: 'Source [0-PC 1-App] is required', trigger: 'blur' }
          ],
          status: [
            { required: true, message: 'Order Status [0-Pending 1-To ship 2-Shipped 3-Done 4-Closed 5-Invalid] is required', trigger: 'blur' }
          ],
          deliveryCompany: [
            { required: true, message: 'Delivery Company is required', trigger: 'blur' }
          ],
          deliverySn: [
            { required: true, message: 'Tracking Number is required', trigger: 'blur' }
          ],
          autoConfirmDay: [
            { required: true, message: 'Auto Confirm Time (Days) is required', trigger: 'blur' }
          ],
          integration: [
            { required: true, message: 'Points Earned is required', trigger: 'blur' }
          ],
          growth: [
            { required: true, message: 'Growth Value Earned is required', trigger: 'blur' }
          ],
          billType: [
            { required: true, message: 'Invoice Type [0->No Invoice; 1->Electronic; 2->Paper] is required', trigger: 'blur' }
          ],
          billHeader: [
            { required: true, message: 'Invoice Header is required', trigger: 'blur' }
          ],
          billContent: [
            { required: true, message: 'Invoice Content is required', trigger: 'blur' }
          ],
          billReceiverPhone: [
            { required: true, message: 'Invoice Receiver Phone is required', trigger: 'blur' }
          ],
          billReceiverEmail: [
            { required: true, message: 'Invoice Receiver Email is required', trigger: 'blur' }
          ],
          receiverName: [
            { required: true, message: 'Receiver Name is required', trigger: 'blur' }
          ],
          receiverPhone: [
            { required: true, message: 'Receiver Phone is required', trigger: 'blur' }
          ],
          receiverPostCode: [
            { required: true, message: 'Receiver Postal Code is required', trigger: 'blur' }
          ],
          receiverProvince: [
            { required: true, message: 'Province/Municipality is required', trigger: 'blur' }
          ],
          receiverCity: [
            { required: true, message: 'City is required', trigger: 'blur' }
          ],
          receiverRegion: [
            { required: true, message: 'Region is required', trigger: 'blur' }
          ],
          receiverDetailAddress: [
            { required: true, message: 'Detail Address is required', trigger: 'blur' }
          ],
          note: [
            { required: true, message: 'Order Remarks is required', trigger: 'blur' }
          ],
          confirmStatus: [
            { required: true, message: 'Confirm Receipt Status [0->Unconfirmed; 1->Confirmed] is required', trigger: 'blur' }
          ],
          deleteStatus: [
            { required: true, message: 'Delete Status [0->Not Deleted; 1->Deleted] is required', trigger: 'blur' }
          ],
          useIntegration: [
            { required: true, message: 'Points Used When Ordering is required', trigger: 'blur' }
          ],
          paymentTime: [
            { required: true, message: 'Payment Time is required', trigger: 'blur' }
          ],
          deliveryTime: [
            { required: true, message: 'Delivery Time is required', trigger: 'blur' }
          ],
          receiveTime: [
            { required: true, message: 'Confirm Receipt Time is required', trigger: 'blur' }
          ],
          commentTime: [
            { required: true, message: 'Comment Time is required', trigger: 'blur' }
          ],
          modifyTime: [
            { required: true, message: 'Modify Time is required', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/order/order/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.memberId = data.order.memberId
                this.dataForm.orderSn = data.order.orderSn
                this.dataForm.couponId = data.order.couponId
                this.dataForm.createTime = data.order.createTime
                this.dataForm.memberUsername = data.order.memberUsername
                this.dataForm.totalAmount = data.order.totalAmount
                this.dataForm.payAmount = data.order.payAmount
                this.dataForm.freightAmount = data.order.freightAmount
                this.dataForm.promotionAmount = data.order.promotionAmount
                this.dataForm.integrationAmount = data.order.integrationAmount
                this.dataForm.couponAmount = data.order.couponAmount
                this.dataForm.discountAmount = data.order.discountAmount
                this.dataForm.payType = data.order.payType
                this.dataForm.sourceType = data.order.sourceType
                this.dataForm.status = data.order.status
                this.dataForm.deliveryCompany = data.order.deliveryCompany
                this.dataForm.deliverySn = data.order.deliverySn
                this.dataForm.autoConfirmDay = data.order.autoConfirmDay
                this.dataForm.integration = data.order.integration
                this.dataForm.growth = data.order.growth
                this.dataForm.billType = data.order.billType
                this.dataForm.billHeader = data.order.billHeader
                this.dataForm.billContent = data.order.billContent
                this.dataForm.billReceiverPhone = data.order.billReceiverPhone
                this.dataForm.billReceiverEmail = data.order.billReceiverEmail
                this.dataForm.receiverName = data.order.receiverName
                this.dataForm.receiverPhone = data.order.receiverPhone
                this.dataForm.receiverPostCode = data.order.receiverPostCode
                this.dataForm.receiverProvince = data.order.receiverProvince
                this.dataForm.receiverCity = data.order.receiverCity
                this.dataForm.receiverRegion = data.order.receiverRegion
                this.dataForm.receiverDetailAddress = data.order.receiverDetailAddress
                this.dataForm.note = data.order.note
                this.dataForm.confirmStatus = data.order.confirmStatus
                this.dataForm.deleteStatus = data.order.deleteStatus
                this.dataForm.useIntegration = data.order.useIntegration
                this.dataForm.paymentTime = data.order.paymentTime
                this.dataForm.deliveryTime = data.order.deliveryTime
                this.dataForm.receiveTime = data.order.receiveTime
                this.dataForm.commentTime = data.order.commentTime
                this.dataForm.modifyTime = data.order.modifyTime
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
              url: this.$http.adornUrl(`/order/order/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'memberId': this.dataForm.memberId,
                'orderSn': this.dataForm.orderSn,
                'couponId': this.dataForm.couponId,
                'createTime': this.dataForm.createTime,
                'memberUsername': this.dataForm.memberUsername,
                'totalAmount': this.dataForm.totalAmount,
                'payAmount': this.dataForm.payAmount,
                'freightAmount': this.dataForm.freightAmount,
                'promotionAmount': this.dataForm.promotionAmount,
                'integrationAmount': this.dataForm.integrationAmount,
                'couponAmount': this.dataForm.couponAmount,
                'discountAmount': this.dataForm.discountAmount,
                'payType': this.dataForm.payType,
                'sourceType': this.dataForm.sourceType,
                'status': this.dataForm.status,
                'deliveryCompany': this.dataForm.deliveryCompany,
                'deliverySn': this.dataForm.deliverySn,
                'autoConfirmDay': this.dataForm.autoConfirmDay,
                'integration': this.dataForm.integration,
                'growth': this.dataForm.growth,
                'billType': this.dataForm.billType,
                'billHeader': this.dataForm.billHeader,
                'billContent': this.dataForm.billContent,
                'billReceiverPhone': this.dataForm.billReceiverPhone,
                'billReceiverEmail': this.dataForm.billReceiverEmail,
                'receiverName': this.dataForm.receiverName,
                'receiverPhone': this.dataForm.receiverPhone,
                'receiverPostCode': this.dataForm.receiverPostCode,
                'receiverProvince': this.dataForm.receiverProvince,
                'receiverCity': this.dataForm.receiverCity,
                'receiverRegion': this.dataForm.receiverRegion,
                'receiverDetailAddress': this.dataForm.receiverDetailAddress,
                'note': this.dataForm.note,
                'confirmStatus': this.dataForm.confirmStatus,
                'deleteStatus': this.dataForm.deleteStatus,
                'useIntegration': this.dataForm.useIntegration,
                'paymentTime': this.dataForm.paymentTime,
                'deliveryTime': this.dataForm.deliveryTime,
                'receiveTime': this.dataForm.receiveTime,
                'commentTime': this.dataForm.commentTime,
                'modifyTime': this.dataForm.modifyTime
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
