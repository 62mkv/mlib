/*
 * Huobi.com Inc.
 *Copyright (c) 2014 火币天下网络技术有限公司.
 *All Rights Reserved
 */
package com.deal.api.demo.futures;

import java.math.BigDecimal;
import java.util.TreeMap;

import com.deal.api.demo.huobi.Base;

/**
 * @author yanjg 2014年11月22日
 */
public class FuturesService extends Base {

    /**
     * 获取所有正在进行的委托
     * 
     * @param coinType
     * @param contractType
     * @param url
     * @return
     * @throws Exception
     */
    public String getOrderList(int coinType, String contractType, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("accessKey", BITVC_ACCESS_KEY);
        paraMap.put("secretKey", BITVC_SECRET_KEY);
        paraMap.put("coinType", coinType);
        paraMap.put("created", getTimestamp());
        String md5 = sign(paraMap);
        paraMap.remove("secretKey");
        paraMap.put("sign", md5);
        paraMap.put("contractType", contractType);
        return post(paraMap, url);
    }

    /**
     * 撤销订单
     * 
     * @param coinType
     * @param contractType
     * @param id
     * @param url
     * @return
     * @throws Exception
     */
    public String cancelOrder(int coinType, String contractType, long id, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("accessKey", BITVC_ACCESS_KEY);
        paraMap.put("secretKey", BITVC_SECRET_KEY);
        paraMap.put("coinType", coinType);
        paraMap.put("contractType", contractType);
        paraMap.put("created", getTimestamp());
        paraMap.put("id", id);
        String md5 = sign(paraMap);
        paraMap.remove("secretKey");
        paraMap.put("sign", md5);
        return post(paraMap, url);
    }

    /**
     * 获取期货资产详情
     * 
     * @param coinType
     * @param url
     * @return
     * @throws Exception
     */
    public String getBalanceInfo(int coinType, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("accessKey", BITVC_ACCESS_KEY);
        paraMap.put("secretKey", BITVC_SECRET_KEY);
        paraMap.put("coinType", coinType);
        paraMap.put("created", getTimestamp());
        String md5 = sign(paraMap);
        paraMap.remove("secretKey");
        paraMap.put("sign", md5);
        return post(paraMap, url);
    }

    /**
     * 获取用户持仓记录
     * 
     * @param coinType
     * @param contractType
     * @param url
     * @return
     * @throws Exception
     */
    public String getHoldOrderList(int coinType, String contractType, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("accessKey", BITVC_ACCESS_KEY);
        paraMap.put("secretKey", BITVC_SECRET_KEY);
        paraMap.put("coinType", coinType);
        paraMap.put("created", getTimestamp());
        String md5 = sign(paraMap);
        paraMap.remove("secretKey");
        paraMap.put("sign", md5);
        paraMap.put("contractType", contractType);
        return post(paraMap, url);
    }

    /**
     * 获取用户持仓记录（汇总）
     * 
     * @param coinType
     * @param contractType
     * @param url
     * @return
     * @throws Exception
     */
    public String getHoldOrderSum(int coinType, String contractType, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("accessKey", BITVC_ACCESS_KEY);
        paraMap.put("secretKey", BITVC_SECRET_KEY);
        paraMap.put("coinType", coinType);
        paraMap.put("created", getTimestamp());
        String md5 = sign(paraMap);
        paraMap.remove("secretKey");
        paraMap.put("sign", md5);
        paraMap.put("contractType", contractType);
        return post(paraMap, url);
    }

    /**
     * 下委托单
     * 
     * @param coinType
     * @param contractType
     * @param orderType
     * @param tradeType
     * @param price
     * @param money
     * @param leverage
     * @param url
     * @return
     * @throws Exception
     */
    public String saveOrder(int coinType, String contractType, int orderType, int tradeType, BigDecimal price,
            BigDecimal money, int leverage, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("accessKey", BITVC_ACCESS_KEY);
        paraMap.put("secretKey", BITVC_SECRET_KEY);
        paraMap.put("coinType", coinType);
        paraMap.put("contractType", contractType);
        paraMap.put("created", getTimestamp());
        paraMap.put("orderType", orderType);
        paraMap.put("tradeType", tradeType);
        paraMap.put("price", price);
        paraMap.put("money", money);
        String md5 = sign(paraMap);
        paraMap.put("leverage", leverage);
        paraMap.remove("secretKey");
        paraMap.put("sign", md5);
        return post(paraMap, url);
    }

    /**
     * 委托单详情
     * 
     * @param id
     * @param coinType
     * @param contractType
     * @param url
     * @return
     * @throws Exception
     */
    public String getOrderInfo(long id, int coinType, String contractType, String url) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("accessKey", BITVC_ACCESS_KEY);
        paraMap.put("secretKey", BITVC_SECRET_KEY);
        paraMap.put("coinType", coinType);
        paraMap.put("contractType", contractType);
        paraMap.put("created", getTimestamp());
        paraMap.put("id", id);
        String md5 = sign(paraMap);
        paraMap.remove("secretKey");
        paraMap.put("sign", md5);

        return post(paraMap, url);
    }

}
