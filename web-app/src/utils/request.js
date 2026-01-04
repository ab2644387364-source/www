import request from "axios"
import {message} from 'ant-design-vue'
import router from '../router/index'

const service = request.create({
    baseURL: 'http://localhost:8084/api',
    timeout: 50000,
});

service.interceptors.request.use(
    config => {
        const token = localStorage.getItem("token")
        if (token) {
            config.headers.Authorization = token
        } else {
            delete config.headers.Authorization
        }
        return config
    },
    error => Promise.reject(error)
);

service.interceptors.response.use(
    response => {

        const res = response.data;

        //判断response状态
        if (!res.status) message.error('请求错误: ' + res.msg)

        if (res.code === 403) router.push("/403")

        return res
    },

    error => {
        message.error(error)
        router.push('/500')
        return Promise.reject(error)
    }
);

export default service
