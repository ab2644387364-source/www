const storedUser = JSON.parse(localStorage.getItem("user"))
const storedRole = localStorage.getItem("role")

const state = {
    token: localStorage.getItem("token"),
    details: storedUser,
    role: storedRole || (storedUser && storedUser.roles ? "admin" : null)
}

const getters = {}

const mutations = {

    saveToken(state, token) {
        state.token = token
        localStorage.setItem("token", token)
    },

    saveLoginUser(state, user) {
        state.details = user
        localStorage.setItem("user", JSON.stringify(user))
    },

    saveLoginRole(state, role) {
        state.role = role
        localStorage.setItem("role", role)
    },

    userLogout(state) {
        state.details = null
        state.token = null
        state.role = null
        localStorage.removeItem("token")
        localStorage.removeItem("user")
        localStorage.removeItem("role")
    }

}

const actions = {}

export default {
    namespaced: true,
    state,
    getters,
    mutations,
    actions
}
