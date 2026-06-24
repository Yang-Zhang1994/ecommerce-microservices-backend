import Vue from 'vue'

const KEY = 'token'

/** 与登录、路由守卫、axios 拦截器共用，避免仅 cookie 在部分环境下不可用 */
export function getToken () {
  try {
    const s = sessionStorage.getItem(KEY)
    if (s != null && /\S/.test(String(s))) return String(s).trim()
  } catch (e) { /* ignore */ }
  const c = Vue.cookie.get(KEY)
  return c != null && /\S/.test(String(c)) ? String(c).trim() : ''
}

export function setToken (value) {
  if (value == null || !String(value).trim()) return
  const v = String(value).trim()
  try {
    sessionStorage.setItem(KEY, v)
  } catch (e) { /* ignore */ }
  Vue.cookie.set(KEY, v, { expires: 7, path: '/' })
}

export function removeToken () {
  try {
    sessionStorage.removeItem(KEY)
  } catch (e) { /* ignore */ }
  Vue.cookie.delete(KEY, { path: '/' })
}
