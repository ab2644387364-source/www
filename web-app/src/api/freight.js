import request from '@/utils/request'

/**
 * 估算运费
 */
export function EstimateFreight(data) {
    return request({
        url: '/user/freight/estimate',
        method: 'post',
        data
    })
}

/**
 * 获取价格配置
 */
export function GetFreightPrices() {
    return request({
        url: '/user/freight/prices',
        method: 'get'
    })
}
