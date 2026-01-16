import request from './portal'

// 获取所有仪表盘数据
export function GetDashboard() {
    return request({
        url: '/statistics/dashboard',
        method: 'get'
    })
}

// 获取总览统计
export function GetOverview() {
    return request({
        url: '/statistics/overview',
        method: 'get'
    })
}

// 获取运输状态统计
export function GetTransportStats() {
    return request({
        url: '/statistics/transport',
        method: 'get'
    })
}

// 获取趋势数据
export function GetTrend() {
    return request({
        url: '/statistics/trend',
        method: 'get'
    })
}

// 获取最近动态
export function GetActivities() {
    return request({
        url: '/statistics/activities',
        method: 'get'
    })
}
