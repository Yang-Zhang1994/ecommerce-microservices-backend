<template>
  <div>
    <el-cascader
      filterable
      clearable
      placeholder="Try searching: Mobile phone"
      v-model="paths"
      :options="categorys"
      :props="setting"
    ></el-cascader>
  </div>
</template>

<script>
export default {
  props: {
    catelogPath: {
      type: Array,
      default () {
        return []
      }
    }
  },
  data () {
    return {
      setting: {
        value: 'catId',
        label: 'name',
        children: 'children'
      },
      categorys: [],
      paths: []
    }
  },
  watch: {
    catelogPath: {
      handler () {
        this.syncPathsFromProp()
      },
      deep: true,
      immediate: true
    },
    categorys () {
      this.syncPathsFromProp()
    },
    paths (v) {
      const normalized = this.normalizePath(v)
      if (JSON.stringify(normalized) !== JSON.stringify(this.normalizePath(this.catelogPath))) {
        this.$emit('update:catelogPath', normalized)
        this.PubSub.publish('catPath', normalized)
      }
    }
  },
  created () {
    this.getCategorys()
  },
  methods: {
    normalizePath (path) {
      if (!path || !path.length) return []
      return path.map(id => Number(id)).filter(id => !Number.isNaN(id))
    },
    syncPathsFromProp () {
      const normalized = this.normalizePath(this.catelogPath)
      if (!normalized.length) {
        this.paths = []
        return
      }
      if (!this.categorys || !this.categorys.length) {
        return
      }
      this.paths = normalized
    },
    getCategorys () {
      this.$http({
        url: this.$http.adornUrl('/product/category/list/tree'),
        method: 'get'
      }).then(({ data }) => {
        if (data && data.code === 0) {
          this.categorys = data.data || []
        } else {
          this.categorys = data.data || []
        }
        this.$nextTick(() => this.syncPathsFromProp())
      })
    }
  }
}
</script>

<style scoped>
</style>
