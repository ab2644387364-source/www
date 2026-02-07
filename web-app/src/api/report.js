import service from "../utils/request";

// 获取订单报表数据
export function getOrderReport(params) {
    return service({
        url: '/report/orders',
        method: 'get',
        params
    })
}

// 获取配送报表数据
export function getDistributionReport(params) {
    return service({
        url: '/report/distributions',
        method: 'get',
        params
    })
}

// 获取库存报表数据
export function getInventoryReport(params) {
    return service({
        url: '/report/inventory',
        method: 'get',
        params
    })
}

// 获取报表统计概览
export function getReportSummary() {
    return service({
        url: '/report/summary',
        method: 'get'
    })
}
