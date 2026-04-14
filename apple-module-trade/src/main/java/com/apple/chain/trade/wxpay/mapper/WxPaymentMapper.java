package com.apple.chain.trade.wxpay.mapper;

import com.apple.chain.trade.wxpay.entity.WxPayment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WxPaymentMapper extends BaseMapper<WxPayment> {
}
