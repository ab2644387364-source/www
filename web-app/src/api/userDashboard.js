import request from '@/utils/request'

/**
 * 获取用户订单统计
 */
export function getUserStats() {
    return request({
        url: '/order/user-stats',
        method: 'get'
    })
}

/**
 * 获取运输中的订单
 */
export function getInTransitOrders() {
    return request({
        url: '/order/in-transit',
        method: 'get'
    })
}
