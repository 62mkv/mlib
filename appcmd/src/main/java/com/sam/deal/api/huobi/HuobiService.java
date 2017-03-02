/*
 * Huobi.com Inc.
 *Copyright (c) 2014 火币天下网络技术有限公司.
 *All Rights Reserved
 */
package com.sam.deal.api.huobi;

import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.deal.api.huobi.Base;
import com.sam.deal.api.okcoin.rest.stock.impl.StockRestApi;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.crud.CrudManager;
import com.sam.moca.util.MocaUtils;

/**
 * @author yanjg 2014年11月22日
 */
public class HuobiService extends Base {

    private MocaContext _moca = null;
    private CrudManager _manager = null;
    private Logger _logger = LogManager.getLogger(HuobiService.class);
    
    public HuobiService() {
        HUOBI_ACCESS_KEY = "a4379746-a122fb6b-0a8eba26-63f47";
        HUOBI_SECRET_KEY = "d85c11ec-856a9e77-15061294-c63bb";
    }
    
    public HuobiService(MocaContext mocaCtx) {
        
        if (mocaCtx != null) {
            _moca = mocaCtx;
            _manager = MocaUtils.crudManager(_moca);
            
            if (HUOBI_ACCESS_KEY.isEmpty()) {
                MocaResults rs = null;
                try {
                    rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = 'TRADE' and polval ='ACCESS-SECRET' and grp_id = '----'");
                    rs.next();
                    HUOBI_ACCESS_KEY = rs.getString("rtstr1");
                    HUOBI_SECRET_KEY = rs.getString("rtstr2");
                    _logger.info("got policy ACCESS-SECRET, HUOBI_ACCESS_KEY:" + HUOBI_ACCESS_KEY + ", HUOBI_SECRET_KEY:" + HUOBI_SECRET_KEY);
                } catch (MocaException e) {
                    // TODO Auto-generated catch block
                    _logger.error(e.getMessage());
                    HUOBI_ACCESS_KEY = "a4379746-a122fb6b-0a8eba26-63f47";
                    HUOBI_SECRET_KEY = "d85c11ec-856a9e77-15061294-c63bb";
                    _logger.info("Get policy ACCESS-SECRET error, use default value, HUOBI_ACCESS_KEY:" + HUOBI_ACCESS_KEY + ", HUOBI_SECRET_KEY:" + HUOBI_SECRET_KEY);
                }
            }
            else {
                _logger.info("Using existing HUOBI_ACCESS_KEY:" + HUOBI_ACCESS_KEY + ", HUOBI_SECRET_KEY:" + HUOBI_SECRET_KEY);
            }
        }
        else {
            HUOBI_ACCESS_KEY = "a4379746-a122fb6b-0a8eba26-63f47";
            HUOBI_SECRET_KEY = "d85c11ec-856a9e77-15061294-c63bb";
        }
    }
    /**
     * 下单接口
     * 
     * @param coinType
     * @param price
     * @param amount
     * @param tradePassword
     * @param tradeid
     * @param method
     * @return
     * @throws Exception
     */
    public String buy(int coinType, String price, String amount, String tradePassword, Integer tradeid, String method)
            throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", HUOBI_ACCESS_KEY);
        paraMap.put("secret_key", HUOBI_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("price", price);
        paraMap.put("amount", amount);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }
        if (null != tradeid) {
            paraMap.put("trade_id", tradeid);
        }
        return post(paraMap, HUOBI_API_URL);
    }

    /**
     * 提交市价单接口
     * 
     * @param coinType
     * @param amount
     * @param tradePassword
     * @param tradeid
     * @param method
     * @return
     * @throws Exception
     */
    public String buyMarket(int coinType, String amount, String tradePassword, Integer tradeid, String method)
            throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", HUOBI_ACCESS_KEY);
        System.out.println("HUOBI_ACCESS_KEY:" + HUOBI_ACCESS_KEY);
        paraMap.put("secret_key", HUOBI_SECRET_KEY);
        System.out.println("HUOBI_SECRET_KEY:" + HUOBI_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("amount", amount);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }
        if (null != tradeid) {
            paraMap.put("trade_id", tradeid);
        }
        return post(paraMap, HUOBI_API_URL);
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
        paraMap.put("access_key", HUOBI_ACCESS_KEY);
        paraMap.put("secret_key", HUOBI_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("id", id);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, HUOBI_API_URL);
    }

    /**
     * 获取账号详情
     * 
     * @param method
     * @return
     * @throws Exception
     */
    public String getAccountInfo(String method, String HAK, String HSK) throws Exception {
        String k1 = (HAK == null || HAK.isEmpty()) ? HUOBI_ACCESS_KEY : HAK;
        String k2 = (HSK == null || HSK.isEmpty()) ? HUOBI_SECRET_KEY : HAK;
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", k1);
        paraMap.put("secret_key", k2);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, HUOBI_API_URL);
    }

    /**
     * 查询个人最新10条成交订单
     * 
     * @param coinType
     * @param method
     * @return
     * @throws Exception
     */
    public String getNewDealOrders(int coinType, String method) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", HUOBI_ACCESS_KEY);
        paraMap.put("secret_key", HUOBI_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, HUOBI_API_URL);
    }

    /**
     * 根据trade_id查询oder_id
     * 
     * @param coinType
     * @param tradeid
     * @param method
     * @return
     * @throws Exception
     */
    public String getOrderIdByTradeId(int coinType, long tradeid, String method) throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", HUOBI_ACCESS_KEY);
        paraMap.put("secret_key", HUOBI_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("trade_id", tradeid);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, HUOBI_API_URL);
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
        paraMap.put("access_key", HUOBI_ACCESS_KEY);
        paraMap.put("secret_key", HUOBI_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, HUOBI_API_URL);
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
        paraMap.put("access_key", HUOBI_ACCESS_KEY);
        paraMap.put("secret_key", HUOBI_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("id", id);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        return post(paraMap, HUOBI_API_URL);
    }

    /**
     * 限价卖出
     * 
     * @param coinType
     * @param price
     * @param amount
     * @param tradePassword
     * @param tradeid
     * @param method
     * @return
     * @throws Exception
     */
    public String sell(int coinType, String price, String amount, String tradePassword, Integer tradeid, String method)
            throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", HUOBI_ACCESS_KEY);
        paraMap.put("secret_key", HUOBI_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("price", price);
        paraMap.put("amount", amount);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }
        if (null != tradeid) {
            paraMap.put("trade_id", tradeid);
        }
        return post(paraMap, HUOBI_API_URL);
    }

    /**
     * 市价卖出
     * 
     * @param coinType
     * @param amount
     * @param tradePassword
     * @param tradeid
     * @param method
     * @return
     * @throws Exception
     */
    public String sellMarket(int coinType, String amount, String tradePassword, Integer tradeid, String method)
            throws Exception {
        TreeMap<String, Object> paraMap = new TreeMap<String, Object>();
        paraMap.put("method", method);
        paraMap.put("created", getTimestamp());
        paraMap.put("access_key", HUOBI_ACCESS_KEY);
        paraMap.put("secret_key", HUOBI_SECRET_KEY);
        paraMap.put("coin_type", coinType);
        paraMap.put("amount", amount);
        String md5 = sign(paraMap);
        paraMap.remove("secret_key");
        paraMap.put("sign", md5);
        if (StringUtils.isNotEmpty(tradePassword)) {
            paraMap.put("trade_password", tradePassword);
        }
        if (null != tradeid) {
            paraMap.put("trade_id", tradeid);
        }
        return post(paraMap, HUOBI_API_URL);
    }

}
