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

/**
 * 分页获取订单列表
 * @param {Object} params - {status, keyword, page, size}
 */
export function FetchOrdersPage(params) {
    return request({
        url: '/order/page',
        method: 'get',
        params
    })
}

/**
 * 获取订单详情
 */
export function FetchOrderDetail(id) {
    return request({
        url: `/order/${id}/detail`,
        method: 'get'
    })
}

/**
 * 获取订单轨迹
 */
export function FetchOrderTracking(id) {
    return request({
        url: `/order/${id}/tracking`,
        method: 'get'
    })
}

/**
 * 取消订单（带原因）
 */
export function CancelOrderWithReason(id, reason) {
    return request({
        url: `/user/orders/${id}/cancel`,
        method: 'post',
        data: { reason }
    })
}

/**
 * 改约提货时间
 */
export function RescheduleOrder(id, expectedPickupTime, remark) {
    return request({
        url: `/user/orders/${id}/reschedule`,
        method: 'post',
        data: { expectedPickupTime, remark }
    })
}

/**
 * 获取消费分析数据
 */
export function FetchExpenseAnalysis() {
    return request({
        url: '/order/expense-analysis',
        method: 'get'
    })
}

/**
 * 获取最近账单
 */
export function FetchRecentBills() {
    return request({
        url: '/order/recent-bills',
        method: 'get'
    })
}

/**
 * 获取运输报告统计数据
 */
export function FetchTransportReport() {
    return request({
        url: '/order/transport-report',
        method: 'get'
    })
}
