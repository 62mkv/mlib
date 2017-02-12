/*
 * Huobi.com Inc.
 *Copyright (c) 2014 火币天下网络技术有限公司.
 *All Rights Reserved
 */
package com.sam.deal.api.huobi.bitvc;

import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.sam.deal.api.huobi.Base;

/**
 * @author yanjg 2014年11月22日
 */
public class BitvcService extends Base {
    /**
     * 下单接口
     * 
     * @param coinType
     * @param price
     * @param amount
     * @param tradePassword
     * @param url
     * @return
     * @throws Exception
     */
    public String buy(int coinType, String price, String amount, String tradePassword, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("access_key", BITVC_ACCESS_KEY);
        paraMap.put("secret_key", BITVC_SECRET_KEY);
        paraMap.put("amount", amount);
        paraMap.put("coin_type", coinType);
        paraMap.put("created", getTimestamp());
        paraMap.put("price", price);
        String md5 = sign(paraMap);
        paraMap.put("sign", md5);
        paraMap.remove("secret_key");
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }
        return post(paraMap, url);
    }

    /**
     * 提交市价单接口
     * 
     * @param coinType
     * @param amount
     * @param tradePassword
     * @param url
     * @return
     * @throws Exception
     */
    public String buyMarket(int coinType, String amount, String tradePassword, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("access_key", BITVC_ACCESS_KEY);
        paraMap.put("secret_key", BITVC_SECRET_KEY);
        paraMap.put("amount", amount);
        paraMap.put("coin_type", coinType);
        paraMap.put("created", getTimestamp());
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }
        return post(paraMap, url);
    }

    /**
     * 撤销订单
     * 
     * @param coinType
     * @param id
     * @param url
     * @return
     * @throws Exception
     */
    public String cancelOrder(int coinType, long id, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        url = url + id;
        paraMap.put("access_key", BITVC_ACCESS_KEY);
        paraMap.put("secret_key", BITVC_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("created", getTimestamp());
        paraMap.put("id", id);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, url);
    }

    /**
     * 获取账号详情
     * 
     * @param url
     * @return
     * @throws Exception
     */
    public String getAccountInfo(String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", BITVC_ACCESS_KEY);
        paraMap.put("secret_key", BITVC_SECRET_KEY);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, url);
    }

    /**
     * 获取所有正在进行的委托
     * 
     * @param coinType
     * @param url
     * @return
     * @throws Exception
     */
    public String getOrders(int coinType, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("access_key", BITVC_ACCESS_KEY);
        paraMap.put("secret_key", BITVC_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("created", getTimestamp());
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, url);
    }

    /**
     * 获取订单详情
     * 
     * @param coinType
     * @param id
     * @param url
     * @return
     * @throws Exception
     */
    public String getOrderInfo(int coinType, long id, String url) throws Exception {
        url = url + id;
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("access_key", BITVC_ACCESS_KEY);
        paraMap.put("secret_key", BITVC_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("created", getTimestamp());
        paraMap.put("id", id);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, url);
    }

    /**
     * 限价卖出
     * 
     * @param coinType
     * @param price
     * @param amount
     * @param tradePassword
     * @param url
     * @return
     * @throws Exception
     */
    public String sell(int coinType, String price, String amount, String tradePassword, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("access_key", BITVC_ACCESS_KEY);
        paraMap.put("secret_key", BITVC_SECRET_KEY);
        paraMap.put("amount", amount);
        paraMap.put("coin_type", coinType);
        paraMap.put("created", getTimestamp());
        paraMap.put("price", price);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }
        return post(paraMap, url);
    }

    /**
     * 市价卖出
     * 
     * @param coinType
     * @param amount
     * @param tradePassword
     * @param url
     * @return
     * @throws Exception
     */
    public String sellMarket(int coinType, String amount, String tradePassword, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("access_key", BITVC_ACCESS_KEY);
        paraMap.put("secret_key", BITVC_SECRET_KEY);
        paraMap.put("amount", amount);
        paraMap.put("coin_type", coinType);
        paraMap.put("created", getTimestamp());
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }

        return post(paraMap, url);
    }

}
