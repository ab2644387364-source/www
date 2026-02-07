import request from '@/utils/request'

/**
 * 提交订单评价
 */
export function submitReview(data) {
    return request({
        url: '/review',
        method: 'post',
        data
    })
}

/**
 * 获取订单评价
 */
export function getOrderReview(orderId) {
    return request({
        url: '/review/order/' + orderId,
        method: 'get'
    })
}

/**
 * 检查是否已评价
 */
export function checkReviewed(orderId) {
    return request({
        url: '/review/check/' + orderId,
        method: 'get'
    })
}
