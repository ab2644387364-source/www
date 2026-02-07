import service from "../utils/request";

export function AnalyzeCommodity(type) {
    return service({
        url: '/inventory/analyze?type=' + type,
        method: 'get'
    })
}

export function FindAllInventory(id) {
    return service({
        url: '/inventory/warehouse/' + id,
        method: 'get'
    })
}

export function InAndOut(type, data) {
    return service({
        url: '/inventory/' + type,
        method: 'post',
        data: data
    })
}

export function FindRecordByWarehouse(id) {
    return service({
        url: '/inventory/record/warehouse/' + id,
        method: 'get'
    })
}

export function DeleteInventoryById(id) {
    return service({
        url: '/inventory?id=' + id,
        method: 'delete'
    })
}

// ========== 新增 API ==========

// 分页查询库存
export function getInventoryPage(params) {
    return service({
        url: '/inventory/page',
        method: 'get',
        params
    })
}

// 获取库存预警列表
export function getWarningList(wid) {
    return service({
        url: '/inventory/warning',
        method: 'get',
        params: { wid }
    })
}

// 设置库存预警阈值
export function setMinCount(id, minCount) {
    return service({
        url: '/inventory/' + id + '/min-count',
        method: 'put',
        data: { minCount }
    })
}

// 库存盘点
export function stockCheck(data) {
    return service({
        url: '/inventory/check',
        method: 'post',
        data
    })
}

// 获取库存统计
export function getInventoryStats() {
    return service({
        url: '/inventory/stats',
        method: 'get'
    })
}

// 获取库存详情
export function getInventoryById(id) {
    return service({
        url: '/inventory/' + id,
        method: 'get'
    })
}