import request from './portal'

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
