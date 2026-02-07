import request from '../utils/request'

// 获取所有通知
export function GetNotifications() {
    return request({
        url: '/notification',
        method: 'get'
    })
}

// 获取未读数量
export function GetUnreadCount() {
    return request({
        url: '/notification/unread-count',
        method: 'get'
    })
}

// 标记为已读
export function MarkAsRead(id) {
    return request({
        url: `/notification/${id}/read`,
        method: 'put'
    })
}

// 标记全部已读
export function MarkAllAsRead() {
    return request({
        url: '/notification/read-all',
        method: 'put'
    })
}

// 获取配送单追踪记录
export function GetTrackRecords(distributionId) {
    return request({
        url: `/distribution/${distributionId}/track`,
        method: 'get'
    })
}

// 添加追踪记录
export function AddTrackRecord(distributionId, record) {
    return request({
        url: `/distribution/${distributionId}/track`,
        method: 'post',
        data: record
    })
}

// ===== 用户端通知API =====

// 获取用户的通知
export function GetUserNotifications(userId) {
    return request({
        url: `/notification/user/${userId}`,
        method: 'get'
    })
}

// 获取用户未读数量
export function GetUserUnreadCount(userId) {
    return request({
        url: `/notification/user/${userId}/unread-count`,
        method: 'get'
    })
}

// 标记用户所有通知为已读
export function MarkAllAsReadByUser(userId) {
    return request({
        url: `/notification/user/${userId}/read-all`,
        method: 'put'
    })
}
