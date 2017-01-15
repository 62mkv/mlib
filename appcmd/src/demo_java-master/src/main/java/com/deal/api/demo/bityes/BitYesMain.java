/*
 * BitYes.com Inc.
 *Copyright (c) 2014 火币天下网络技术有限公司. 
 *All Rights Reserved
 */
package com.deal.api.demo.bityes;

/**
 * @author yanjg 2014年11月22日
 */
public class BitYesMain {
    private static String BUY = "buy";
    private static String BUY_MARKET = "buy_market";
    private static String CANCEL_ORDER = "cancel_order";
    private static String ACCOUNT_INFO = "get_account_info";
    private static String GET_ORDERS = "get_orders";
    private static String ORDER_INFO = "order_info";
    private static String SELL = "sell";
    private static String SELL_MARKET = "sell_market";

    public static void main(String[] args) {
        BitYesService service = new BitYesService();
        try {
            // 提交限价单接口 1btc 2ltc
            System.out.println(service.buy(1, "2281.52", "0.001", null, BUY));
            // 提交市价单接口
            System.out.println(service.buyMarket(1, "2", null, BUY_MARKET));
            // 取消订单接口
            System.out.println(service.cancelOrder(1, 160801, CANCEL_ORDER));
            // 获取账号详情
            System.out.println(service.getAccountInfo(ACCOUNT_INFO));
            // 获取所有正在进行的委托
            System.out.println(service.getOrders(1, GET_ORDERS));
            // 获取订单详情
            System.out.println(service.getOrderInfo(1, 160802, ORDER_INFO));
            // 市价卖出
            System.out.println(service.sell(1, "2555.52", "0.1", null, SELL));
            // 市价卖出
            System.out.println(service.sellMarket(1, "2", null, SELL_MARKET));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
