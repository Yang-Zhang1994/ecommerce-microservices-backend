// Mock for proxy-agent (Node.js-only package used by ueditor)
// This is a simple mock that returns a basic agent
module.exports = function () {
  return {
    addRequest: function () {},
    removeRequest: function () {}
  }
}
module.exports.ProxyAgent = module.exports
