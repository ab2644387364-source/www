/**
 * Token 管理工具类
 * 负责 Token 的存储、读取、验证和过期处理
 */

const TOKEN_KEY = 'token'
const USER_KEY = 'user'
const ROLE_KEY = 'role'
const TOKEN_EXPIRE_KEY = 'token_expire'

// Token 有效期（毫秒）- 默认 24 小时
const TOKEN_EXPIRE_TIME = 24 * 60 * 60 * 1000

/**
 * 保存 Token 及过期时间
 */
export function setToken(token) {
    if (!token) return
    localStorage.setItem(TOKEN_KEY, token)
    // 记录 Token 保存时间，用于计算过期
    localStorage.setItem(TOKEN_EXPIRE_KEY, Date.now().toString())
}

/**
 * 获取 Token
 */
export function getToken() {
    return localStorage.getItem(TOKEN_KEY)
}

/**
 * 移除 Token
 */
export function removeToken() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(TOKEN_EXPIRE_KEY)
}

/**
 * 检查 Token 是否过期
 * @returns {boolean} true = 已过期, false = 未过期
 */
export function isTokenExpired() {
    const token = getToken()
    if (!token) return true

    const expireTime = localStorage.getItem(TOKEN_EXPIRE_KEY)
    if (!expireTime) return true

    const savedTime = parseInt(expireTime, 10)
    const now = Date.now()

    // 检查是否超过有效期
    return (now - savedTime) > TOKEN_EXPIRE_TIME
}

/**
 * 获取 Token 剩余有效时间（毫秒）
 * @returns {number} 剩余时间，-1 表示已过期
 */
export function getTokenRemainingTime() {
    const expireTime = localStorage.getItem(TOKEN_EXPIRE_KEY)
    if (!expireTime) return -1

    const savedTime = parseInt(expireTime, 10)
    const now = Date.now()
    const remaining = TOKEN_EXPIRE_TIME - (now - savedTime)

    return remaining > 0 ? remaining : -1
}

/**
 * 刷新 Token 有效期（重置过期时间）
 * 当用户有活动时调用此方法延长 Token 有效期
 */
export function refreshTokenExpiry() {
    const token = getToken()
    if (token) {
        localStorage.setItem(TOKEN_EXPIRE_KEY, Date.now().toString())
    }
}

/**
 * 保存用户信息
 */
export function setUser(user) {
    if (!user) return
    localStorage.setItem(USER_KEY, JSON.stringify(user))
}

/**
 * 获取用户信息
 */
export function getUser() {
    try {
        const user = localStorage.getItem(USER_KEY)
        return user ? JSON.parse(user) : null
    } catch (e) {
        return null
    }
}

/**
 * 移除用户信息
 */
export function removeUser() {
    localStorage.removeItem(USER_KEY)
}

/**
 * 保存角色
 */
export function setRole(role) {
    if (!role) return
    localStorage.setItem(ROLE_KEY, role)
}

/**
 * 获取角色
 */
export function getRole() {
    return localStorage.getItem(ROLE_KEY)
}

/**
 * 移除角色
 */
export function removeRole() {
    localStorage.removeItem(ROLE_KEY)
}

/**
 * 清除所有认证信息（登出时调用）
 */
export function clearAuth() {
    removeToken()
    removeUser()
    removeRole()
}

/**
 * 检查是否已登录（Token 存在且未过期）
 */
export function isAuthenticated() {
    return getToken() && !isTokenExpired()
}

export default {
    setToken,
    getToken,
    removeToken,
    isTokenExpired,
    getTokenRemainingTime,
    refreshTokenExpiry,
    setUser,
    getUser,
    removeUser,
    setRole,
    getRole,
    removeRole,
    clearAuth,
    isAuthenticated
}
