const mockResponse = (data) => Promise.resolve({status: true, data})

export function FetchUserProfile() {
  return mockResponse({
    id: 1,
    name: "示例用户",
    email: "user@example.com",
    phone: "13800000000",
    address: "上海市浦东新区世纪大道 100 号",
    company: "示例物流有限公司"
  })
}

export function UpdateUserProfile(data) {
  return mockResponse(data)
}

export function FetchRecentOrders() {
  return mockResponse([
    {orderNo: "OD20250101001", origin: "上海", destination: "苏州", status: "待支付", amount: 1200},
    {orderNo: "OD20241230007", origin: "南京", destination: "无锡", status: "运输中", amount: 980},
    {orderNo: "OD20241228002", origin: "合肥", destination: "杭州", status: "已完成", amount: 1680},
  ])
}

export function CreateOrder(data) {
  return mockResponse({
    orderNo: `OD${Date.now()}`,
    amount: 1280,
    status: "待支付",
    payload: data
  })
}

export function QueryRoutes(data) {
  return mockResponse([
    {node: "起点站", city: data.origin || "上海", time: "08:30", status: "已发车"},
    {node: "中转站", city: "苏州", time: "10:00", status: "待到达"},
    {node: "终点站", city: data.destination || "无锡", time: "12:30", status: "待到达"},
  ])
}

export function FetchPayableOrders() {
  return mockResponse([
    {orderNo: "OD20250101001", amount: 1200, status: "待支付"},
    {orderNo: "OD20241230007", amount: 980, status: "待支付"},
  ])
}

export function SubmitPayment(data) {
  return mockResponse({
    paidAt: "2025-01-01 10:30",
    method: data.method,
    orderNo: data.orderNo
  })
}
