/*
 * BitYes.com Inc.
 *Copyright (c) 2014 火币天下网络技术有限公司.
 *All Rights Reserved
 */
package com.deal.api.demo.bityes;

import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

/**
 * @author yanjg 2014年11月22日
 */
public class BitYesService extends Base {

    /**
     * 下单接口
     * 
     * @param coinType
     * @param price
     * @param amount
     * @param tradePassword
     * @param method
     * @return
     * @throws Exception
     */
    public String buy(int coinType, String price, String amount, String tradePassword, String method) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", BITYES_ACCESS_KEY);
        paraMap.put("secret_key", BITYES_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("price", price);
        paraMap.put("amount", amount);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }
        return post(paraMap, BITYES_API_URL);
    }

    /**
     * 提交市价单接口
     * 
     * @param coinType
     * @param amount
     * @param tradePassword
     * @param method
     * @return
     * @throws Exception
     */
    public String buyMarket(int coinType, String amount, String tradePassword, String method) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", BITYES_ACCESS_KEY);
        paraMap.put("secret_key", BITYES_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("amount", amount);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }
        return post(paraMap, BITYES_API_URL);
    }

    /**
     * 撤销订单
     * 
     * @param coinType
     * @param id
     * @param method
     * @return
     * @throws Exception
     */
    public String cancelOrder(int coinType, long id, String method) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", BITYES_ACCESS_KEY);
        paraMap.put("secret_key", BITYES_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("id", id);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, BITYES_API_URL);
    }

    /**
     * 获取账号详情
     * 
     * @param method
     * @return
     * @throws Exception
     */
    public String getAccountInfo(String method) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", BITYES_ACCESS_KEY);
        paraMap.put("secret_key", BITYES_SECRET_KEY);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, BITYES_API_URL);
    }

    /**
     * 获取所有正在进行的委托
     * 
     * @param coinType
     * @param method
     * @return
     * @throws Exception
     */
    public String getOrders(int coinType, String method) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", BITYES_ACCESS_KEY);
        paraMap.put("secret_key", BITYES_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, BITYES_API_URL);
    }

    /**
     * 获取订单详情
     * 
     * @param coinType
     * @param id
     * @param method
     * @return
     * @throws Exception
     */
    public String getOrderInfo(int coinType, long id, String method) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", BITYES_ACCESS_KEY);
        paraMap.put("secret_key", BITYES_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("id", id);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, BITYES_API_URL);
    }

    /**
     * 限价卖出
     * 
     * @param coinType
     * @param price
     * @param amount
     * @param tradePassword
     * @param method
     * @return
     * @throws Exception
     */
    public String sell(int coinType, String price, String amount, String tradePassword, String method) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", BITYES_ACCESS_KEY);
        paraMap.put("secret_key", BITYES_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("price", price);
        paraMap.put("amount", amount);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }
        return post(paraMap, BITYES_API_URL);
    }

    /**
     * 市价卖出
     * 
     * @param coinType
     * @param amount
     * @param tradePassword
     * @param method
     * @return
     * @throws Exception
     */
    public String sellMarket(int coinType, String amount, String tradePassword, String method) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", BITYES_ACCESS_KEY);
        paraMap.put("secret_key", BITYES_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("amount", amount);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }
        return post(paraMap, BITYES_API_URL);
    }

}
