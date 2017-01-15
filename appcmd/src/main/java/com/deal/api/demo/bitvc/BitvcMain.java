/*
 * Huobi.com Inc.
 *Copyright (c) 2014 火币天下网络技术有限公司. 
 *All Rights Reserved
 */
package com.deal.api.demo.bitvc;

/**
 * bitvc现货
 * 
 * @author yanjg 2014年11月22日
 */
public class BitvcMain {
    private static String BITVC_ACCOUNT_INFO = "https://api.bitvc.com/api/accountInfo/get";
    private static String BITVC_GET_ORDERS = "https://api.bitvc.com/api/order/list";
    private static String BITVC_ORDER_INFO = "https://api.bitvc.com/api/order/";
    private static String BITVC_BUY_MARKET = "https://api.bitvc.com/api/order/buy_market";
    private static String BITVC_BUY = "https://api.bitvc.com/api/order/buy";
    private static String BITVC_SELL_MARKET = "https://api.bitvc.com/api/order/sell_market";
    private static String BITVC_SELL = "https://api.bitvc.com/api/order/sell";
    private static String BITVC_CANCEL_ORDER = "https://api.bitvc.com/api/order/cancel/";

    public static void main(String[] args) {
        BitvcService service = new BitvcService();
        try {
            // 获取账号详情
            System.out.println(service.getAccountInfo(BITVC_ACCOUNT_INFO));
            // 获取所有正在进行的委托
            System.out.println(service.getOrders(1, BITVC_GET_ORDERS));
            // 获取委托单详情
            System.out.println(service.getOrderInfo(1, 160802, BITVC_ORDER_INFO));
            // 市价买入接口
            System.out.println(service.buyMarket(1, "500", "", BITVC_BUY_MARKET));
            // 限价买入接口
            System.out.println(service.buy(1, "2555.52", "0.1", null, BITVC_BUY));
            // 市价卖出
            System.out.println(service.sellMarket(1, "2", null, BITVC_SELL_MARKET));
            // 市价卖出
            System.out.println(service.sell(1, "2555.52", "0.1", null, BITVC_SELL));

            // 取消订单接口
            System.out.println(service.cancelOrder(1, 160801, BITVC_CANCEL_ORDER));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
