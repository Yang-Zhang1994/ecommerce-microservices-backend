'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  OPEN_PROXY: false, // 是否开启代理, 重置后需重启vue-cli
  MALL_PUBLIC_ORIGIN: '"http://localhost:3001"',
  // kind 网关 NodePort；Docker Compose 本地网关一般为 88
  GATEWAY_PROXY_TARGET: '"https://www.yangzhangtech.online"'
})
