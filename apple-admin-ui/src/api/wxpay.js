import request from './request.js'

/**
 * 微信支付 Mock API —— 对应后端 com.apple.chain.trade.wxpay.controller.WxPayController
 *
 * 金额单位铁律：后端全部使用 amountCents / refundAmountCents（分），
 * 前端展示时除以 100，¥YY.XX。
 */

export const wxPayApi = {
  /**
   * 发起 Mock 微信支付
   * @param {{orderId:number, amountCents:number, description?:string}} data
   */
  create: (data) => request.post('/wxpay/payments', data),

  /**
   * 查询支付详情（后端按 90% 概率推进 PENDING → SUCCESS）
   */
  get: (id) => request.get(`/wxpay/payments/${id}`),

  /**
   * 分页列表
   * @param {{page?:number, size?:number, keyword?:string, status?:string}} params
   */
  list: (params) => request.get('/wxpay/payments', { params }),
}

export const wxRefundApi = {
  /**
   * 发起 Mock 微信退款
   * @param {{paymentId:number, refundAmountCents:number, reason:string}} data
   */
  create: (data) => request.post('/wxpay/refunds', data),

  /**
   * 按支付记录查询退款明细
   */
  listByPayment: (paymentId) => request.get('/wxpay/refunds', { params: { paymentId } }),

  /**
   * 退款详情
   */
  get: (id) => request.get(`/wxpay/refunds/${id}`),
}

export const wxReconcileApi = {
  /**
   * 按日查询 Mock 对账单
   * @param {string} date YYYY-MM-DD
   */
  byDate: (date) => request.get('/wxpay/reconcile', { params: { date } }),
}
