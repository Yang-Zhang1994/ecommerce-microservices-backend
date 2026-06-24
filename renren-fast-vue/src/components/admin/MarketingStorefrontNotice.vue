<template>
  <el-alert
    class="marketing-storefront-notice"
    type="info"
    :closable="false"
    show-icon
    title="Not applied on the storefront yet"
  >
    <template slot="default">
      Stored for future use; not applied on the storefront yet. GrainMart checkout and product
      pages still use base SKU prices only.
    </template>
  </el-alert>
</template>

<script>
export default {
  name: 'MarketingStorefrontNotice'
}
</script>

<style scoped>
.marketing-storefront-notice {
  margin-bottom: 12px;
}
</style>
