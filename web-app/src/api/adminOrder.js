import request from "@/utils/request";

/**
 * 管理端订单 API
 */

// 获取订单列表（分页）
export function getOrderList(params) {
    return request({
        url: '/admin/orders',
        method: 'get',
        params
    })
}

// 获取订单详情
export function getOrderDetail(id) {
    return request({
        url: `/admin/orders/${id}`,
        method: 'get'
    })
}

// 审核通过订单
export function approveOrder(id) {
    return request({
        url: `/admin/orders/${id}/approve`,
        method: 'put'
    })
}

// 驳回订单
export function rejectOrder(id, reason) {
    return request({
        url: `/admin/orders/${id}/reject`,
        method: 'put',
        data: { reason }
    })
}

// 更新订单状态
export function updateOrderStatus(id, status, remark) {
    return request({
        url: `/admin/orders/${id}/status`,
        method: 'put',
        data: { status, remark }
    })
}

// 获取订单状态统计
export function getOrderStats() {
    return request({
        url: '/admin/orders/stats',
        method: 'get'
    })
}
