import request from "@/utils/request";

/**
 * 创建订单
 */
export function CreateOrder(data) {
    return request({
        url: '/order/create',
        method: 'post',
        data
    })
}

/**
 * 获取用户订单列表
 */
export function FetchOrders() {
    return request({
        url: '/order/list',
        method: 'get'
    })
}

/**
 * 获取待支付订单
 */
export function FetchPayableOrders() {
    return request({
        url: '/order/payable',
        method: 'get'
    })
}

/**
 * 获取最近订单
 */
export function FetchRecentOrders() {
    return request({
        url: '/order/recent',
        method: 'get'
    })
}

/**
 * 支付订单
 */
export function PayOrder(orderId, method) {
    return request({
        url: '/order/pay',
        method: 'post',
        data: { orderId, method }
    })
}

/**
 * 取消订单
 */
export function CancelOrder(orderId) {
    return request({
        url: '/order/cancel',
        method: 'post',
        data: { orderId }
    })
}
