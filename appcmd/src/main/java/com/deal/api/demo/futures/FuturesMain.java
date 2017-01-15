/*
 * Huobi.com Inc.
 *Copyright (c) 2014 火币天下网络技术有限公司. 
 *All Rights Reserved
 */
package com.deal.api.demo.futures;

import java.math.BigDecimal;

/**
 * 期货api demo
 * 
 * @author yanjg 2014年11月22日
 */
public class FuturesMain {
    private static String FUTURES_BALANCE_INFO = "https://api.bitvc.com/futures/balance";
    private static String FUTURES_HOLD_ORDER_LIST = "https://api.bitvc.com/futures/holdOrder/list";
    private static String FUTURES_HOLD_ORDER_SUM = "https://api.bitvc.com/futures/holdOrder";
    private static String FUTURES_ORDERS_LIST="https://api.bitvc.com/futures/order/list";
    private static String FUTURES_ORDER_INFO = "https://api.bitvc.com/futures/order";
    private static String FUTURES_ORDER_SAVE="https://api.bitvc.com/futures/order/save";
    private static String FUTURES_CANCEL_ORDER = "https://api.bitvc.com/futures/order/cancel";

    public static void main(String[] args) {
        FuturesService service = new FuturesService();
        try {
            //获取期货资产信息
            System.out.println(service.getBalanceInfo(1,FUTURES_BALANCE_INFO));
            // 获取用户持仓记录，week 周 quarter 季
            System.out.println(service.getHoldOrderList(1,"week", FUTURES_HOLD_ORDER_LIST));
            // 获取用户持仓记录，汇总
            System.out.println(service.getHoldOrderSum(1,"week", FUTURES_HOLD_ORDER_SUM));
            // 获取所有正在进行的委托
            System.out.println(service.getOrderList(1, "week", FUTURES_ORDERS_LIST));
            //委托单详情
            System.out.println(service.getOrderInfo(1L,1,"week",FUTURES_ORDER_INFO));
            // 下委托单
            BigDecimal price=new BigDecimal("2000.00").setScale(2);
            BigDecimal money=new BigDecimal("300.00").setScale(2);
            System.out.println(service.saveOrder(1,"week",1,1,price, money, 10,FUTURES_ORDER_SAVE));
            // 取消订单接口
            System.out.println(service.cancelOrder(1,"week", 160801, FUTURES_CANCEL_ORDER));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
