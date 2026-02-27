/**
 * Shim for Node 12+ so old graceful-fs (used by gulp/vinyl-fs) does not throw "primordials is not defined".
 * Use: node -r ./primordials-shim.js node_modules/.bin/gulp
 */
const vm = require('vm');
global.primordials = vm.runInNewContext('this', vm.createContext({}));
