import { getToken, getUser, getRole, setToken, setUser, setRole, clearAuth, isAuthenticated } from '@/utils/auth'

const storedUser = getUser()
const storedRole = getRole()

const state = {
    token: getToken(),
    details: storedUser,
    role: storedRole || (storedUser && storedUser.roles ? "admin" : null)
}

const getters = {
    isLoggedIn: state => !!state.token && isAuthenticated(),
    userInfo: state => state.details,
    userRole: state => state.role
}

const mutations = {

    saveToken(state, token) {
        state.token = token
        setToken(token)
    },

    saveLoginUser(state, user) {
        state.details = user
        setUser(user)
    },

    saveLoginRole(state, role) {
        state.role = role
        setRole(role)
    },

    userLogout(state) {
        state.details = null
        state.token = null
        state.role = null
        clearAuth()
    }

}

const actions = {
    // 检查登录状态
    checkAuth({ commit }) {
        if (!isAuthenticated()) {
            commit('userLogout')
            return false
        }
        return true
    }
}

export default {
    namespaced: true,
    state,
    getters,
    mutations,
    actions
}
