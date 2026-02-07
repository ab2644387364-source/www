import request from 'axios'

/**
 * 创建轨迹记录
 */
export function CreateTrack(data) {
    return request({
        url: '/api/gps/track/create',
        method: 'post',
        data
    })
}

/**
 * 上报GPS位置
 */
export function ReportLocation(data) {
    return request({
        url: '/api/gps/track/report',
        method: 'post',
        data
    })
}

/**
 * 获取配送单的完整轨迹
 */
export function GetTrack(distributionId) {
    return request({
        url: `/api/gps/track/${distributionId}`,
        method: 'get'
    })
}

/**
 * 获取最新位置
 */
export function GetLatestLocation(distributionId) {
    return request({
        url: `/api/gps/latest/${distributionId}`,
        method: 'get'
    })
}
