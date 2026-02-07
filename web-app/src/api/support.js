import request from 'axios'

/**
 * 提交反馈
 */
export function CreateFeedback(data) {
    return request({
        url: '/api/user/feedback',
        method: 'post',
        data
    })
}

/**
 * 获取用户的反馈列表
 */
export function GetUserFeedback(userId) {
    return request({
        url: '/api/user/feedback',
        method: 'get',
        params: { userId }
    })
}

/**
 * 获取反馈详情
 */
export function GetFeedbackDetail(id) {
    return request({
        url: `/api/user/feedback/${id}`,
        method: 'get'
    })
}

/**
 * 管理员获取所有反馈
 */
export function GetAllFeedback(status) {
    return request({
        url: '/api/admin/feedback',
        method: 'get',
        params: status ? { status } : {}
    })
}

/**
 * 管理员回复反馈
 */
export function ReplyFeedback(id, reply) {
    return request({
        url: `/api/admin/feedback/${id}/reply`,
        method: 'put',
        data: { reply }
    })
}

/**
 * 管理员更新反馈状态
 */
export function UpdateFeedbackStatus(id, status) {
    return request({
        url: `/api/admin/feedback/${id}/status`,
        method: 'put',
        data: { status }
    })
}
