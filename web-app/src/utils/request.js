import request from "axios"
import { message } from 'ant-design-vue'
import router from '../router/index'
import { getToken, isTokenExpired, clearAuth, refreshTokenExpiry } from './auth'

const service = request.create({
    baseURL: 'http://localhost:8084/api',
    timeout: 50000,
});

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = getToken()

        // 检查 Token 是否过期
        if (token && isTokenExpired()) {
            clearAuth()
            message.warning('登录已过期，请重新登录')
            router.push('/login')
            return Promise.reject(new Error('Token expired'))
        }

        if (token) {
            config.headers.Authorization = token
            // 用户有活动，刷新 Token 有效期
            refreshTokenExpiry()
        } else {
            delete config.headers.Authorization
        }
        return config
    },
    error => Promise.reject(error)
);

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data;

        // 判断 response 状态
        if (!res.status) {
            message.error('请求错误: ' + res.msg)
        }

        // 权限不足
        if (res.code === 403) {
            router.push("/403")
        }

        // Token 无效或过期（后端返回 401）
        if (res.code === 401) {
            clearAuth()
            message.warning('登录已过期，请重新登录')
            router.push('/login')
        }

        return res
    },

    error => {
        // 处理网络错误或服务器错误
        if (error.response) {
            const status = error.response.status

            if (status === 401) {
                clearAuth()
                message.warning('登录已过期，请重新登录')
                router.push('/login')
            } else if (status === 403) {
                message.error('您没有权限执行此操作')
                router.push('/403')
            } else if (status === 404) {
                message.error('请求的资源不存在')
            } else if (status >= 500) {
                message.error('服务器错误，请稍后重试')
                router.push('/500')
            } else {
                const errMsg = error.response.data && error.response.data.msg ? error.response.data.msg : error.message
                message.error('请求失败: ' + errMsg)
            }
        } else if (error.message === 'Token expired') {
            // Token 过期已处理，不需要额外提示
        } else {
            message.error('网络错误，请检查网络连接')
        }

        return Promise.reject(error)
    }
);

export default service
