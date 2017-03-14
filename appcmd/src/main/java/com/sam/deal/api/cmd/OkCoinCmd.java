/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 */

package com.sam.deal.api.cmd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.Highlighter.Highlight;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.SimpleResults;
import com.sam.deal.api.cmd.HuobiCmd;
import com.sam.deal.api.huobi.HuobiService;
import com.sam.deal.api.okcoin.rest.stock.IStockRestApi;
import com.sam.deal.api.okcoin.rest.stock.impl.StockRestApi;
import com.sam.moca.EditableResults;
import com.sam.moca.MocaArgument;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.NotFoundException;
import com.sam.moca.crud.CodeInvalidException;
import com.sam.moca.crud.CrudManager;
import com.sam.moca.crud.CrudManager.ConcurrencyMode;
import com.sam.moca.crud.CrudManager.FieldRequirement;
import com.sam.moca.server.expression.function.LowerFunction;
import com.sam.moca.crud.CrudMode;
import com.sam.moca.crud.DateInvalidException;
import com.sam.moca.crud.FlagInvalidException;
import com.sam.moca.crud.JDBCCrudManager;
import com.sam.moca.crud.MissingStackArgumentException;
import com.sam.moca.crud.TableDefinition;
import com.sam.moca.crud.TableFactory;
import com.sam.moca.util.MocaUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A CRUD Service class set that automatically handles creation and updating of
 * data based on the table definitions and variables on the context.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author Sam
 * @version $Revision$
 */
public class OkCoinCmd {

    /**
     * Creates a new HuoBiService class
     * 
     * @param mocaCtx
     *            The MOCA context
     * @throws MocaException
     */
    public OkCoinCmd(MocaContext mocaCtx) throws MocaException {
        _moca = mocaCtx;
        _manager = MocaUtils.crudManager(_moca);
    }

    /**
     * @param market:
     *            'US', 'CHN'
     * @param coinType:
     *            'btc','ltc'
     */
    public MocaResults getTickerRecord(String market, String coinType,
            Boolean save_db_flag) throws MocaException {

        /*
         * {"date":"1486790201","ticker":{"buy":"989.0","high":"990.0","last":"989.0","low":"920.0","sell":"990.0","vol":"2934.4551"}}
         */

        String url_key = market + "|" + coinType + "|" + URL_TYPE1;
        IStockRestApi stockGet = URLs.get(url_key);
        if (stockGet == null) {
            if ("US".equalsIgnoreCase(market)) {
                stockGet = new StockRestApi("https://www.okcoin.com", _moca);
            } else if ("CHN".equalsIgnoreCase(market)) {
                stockGet = new StockRestApi("https://www.okcoin.cn", _moca);
            }
            URLs.put(url_key, stockGet);
        }

        String ct = (coinType.equalsIgnoreCase("BTC") ? "btc_usd" : "ltc_usd");
        MocaResults rs;
        try {
                String text = stockGet.ticker(ct);
                _logger.debug("body:" + text);
                JSONObject body = new JSONObject(text);
                String date = body.getString("date");
                JSONObject ticker = body.getJSONObject("ticker");
                double high = ticker.getDouble("high");
                double low = ticker.getDouble("low");
                double vol = ticker.getDouble("vol");
                double buy = ticker.getDouble("buy");
                double sell = ticker.getDouble("sell");
                double last = ticker.getDouble("last");
                _logger.info("date:" + date);
                _logger.info("high:" + high);
                _logger.info("low:" + low);
                _logger.info("vol:" + vol);
                _logger.info("buy:" + buy);
                _logger.info("sell:" + sell);
                _logger.info("last:" + last);

                if (save_db_flag != null && save_db_flag.equals(Boolean.TRUE)) {
                    _moca.executeCommand("publish data where symbol = '" + ct + "' and time = '" + date + "'"
                            + "   and high = " + high + "   and low = " + low
                            + "   and vol = " + vol + "   and buy = " + buy
                            + "   and sell = " + sell + "   and last = " + last
                            + "   and ins_dt = sysdate " + "|"
                            + "create record where table_name = 'oc_ticker_data' and @* ");
                }
                rs = _moca.executeCommand("publish data where symbol = '" + ct + "' and time = '" + date + "'"
                        + "   and high = " + high + "   and low = " + low
                        + "   and vol = " + vol + "   and buy = " + buy
                        + "   and sell = " + sell + "   and last = " + last
                        + "   and ins_dt = sysdate ");
        } catch (Exception e) {
            e.printStackTrace();
            _logger.debug("getRealTimeRecord Exception:" + e.getMessage());
            throw new MocaException(-1403, e.getMessage());
        }

        return rs;
    }
    
    /* In max return 600 rows most recent records.
     * 
     */
    public MocaResults getTradeRecords(String market, String coinType,
            String ordid_since,
            Boolean save_db_flag) throws MocaException {

        /*
         * {"date":"1486790201","ticker":{"buy":"989.0","high":"990.0","last":"989.0","low":"920.0","sell":"990.0","vol":"2934.4551"}}
         */

        String url_key = market + "|" + coinType + "|" + URL_GETTRADERECORDS;
        IStockRestApi stockGet = URLs.get(url_key);
        if (stockGet == null) {
            if ("US".equalsIgnoreCase(market)) {
                stockGet = new StockRestApi("https://www.okcoin.com", _moca);
            } else if ("CHN".equalsIgnoreCase(market)) {
                stockGet = new StockRestApi("https://www.okcoin.cn", _moca);
            }
            URLs.put(url_key, stockGet);
        }
        
        if (ordid_since == null || ordid_since.isEmpty()) {
            ordid_since = "1";
        }

        String ct = (coinType.equalsIgnoreCase("BTC") ? "btc_usd" : "ltc_usd");
        EditableResults res = new SimpleResults();
        res.addColumn("symbol", MocaType.STRING);
        res.addColumn("time", MocaType.STRING);
        res.addColumn("type", MocaType.STRING);
        res.addColumn("price", MocaType.DOUBLE);
        res.addColumn("amount", MocaType.DOUBLE);
        res.addColumn("tid", MocaType.STRING);
            
        try {
                String text = stockGet.trades(ct, ordid_since);
                _logger.debug("body:" + text);
                JSONArray trades = new JSONArray(text);
                /*
                 * [
                 *    {
                 *        "date": "1367130137",
                 *        "date_ms": "1367130137000",
                 *        "price": 787.71,
                 *        "amount": 0.003,
                 *        "tid": "230433",
                 *        "type": "sell"
                 *    },
                 *    {
                 *        "date": "1367130137",
                 *        "date_ms": "1367130137000",
                 *        "price": 787.65,
                 *        "amount": 0.001,
                 *        "tid": "230434",
                 *        "type": "sell"
                 *    },
                 *    {
                 *        "date": "1367130137",
                 *        "date_ms": "1367130137000",
                 *        "price": 787.5,
                 *        "amount": 0.091,
                 *        "tid": "230435",
                 *        "type": "sell"
                 *    }
                 *]
                 */
                int sz = trades.length();
                for (int i = 0; i < sz; i++) {
                    if (ordid_since.equals("1") && i >= 1) {
                        _logger.info("only return last 1 trade since ordid_since is not passed.");
                        break;
                    }
                    JSONObject trade = trades.getJSONObject(sz - 1 - i);
                    String date = trade.getString("date");
                    double price = trade.getDouble("price");
                    double amount = trade.getDouble("amount");
                    String tid = trade.getString("tid");
                    String type = trade.getString("type");
                    _logger.info("date:" + date);
                    _logger.info("price:" + price);
                    _logger.info("amount:" + amount);
                    _logger.info("tid:" + tid);
                    _logger.info("type:" + type);
                    
                    if (save_db_flag != null && save_db_flag.equals(Boolean.TRUE)) {
                        _moca.executeCommand("publish data where symbol = '" + ct + "' and time = '" + date + "'"
                                + "   and price = " + price + "   and amount = " + amount
                                + "   and tid = " + tid + "   and type = '" + type
                                + "'   and ins_dt = sysdate " + "|"
                                + "create record where table_name = 'oc_trade_data' and @* ");
                    }
                    res.addRow();
                    res.setStringValue("symbol", ct);
                    res.setStringValue("time", date);
                    res.setStringValue("type", type);
                    res.setDoubleValue("price", price);
                    res.setDoubleValue("amount", amount);
                    res.setStringValue("tid", tid);
                }
        } catch (Exception e) {
            e.printStackTrace();
            _logger.debug("getTradeRecords Exception:" + e.getMessage());
            throw new MocaException(-1403, e.getMessage());
        }

        return res;
    }

    /**
     * @param coinType:
     *            1 'btc', 2 'ltc'
     *            {
     *             "result": true,
     *             "orders": [
     *                 {
     *                     "amount": 0.1,
     *                     "avg_price": 0,
     *                     "create_date": 1418008467000,
     *                     "deal_amount": 0,
     *                     "order_id": 10000591,
     *                     "orders_id": 10000591,
     *                     "price": 500,
     *                     "status": 0,
     *                     "symbol": "btc_usd",
     *                     "type": "sell"
     *                 },
     *                 {
     *                     "amount": 0.2,
     *                     "avg_price": 0,
     *                     "create_date": 1417417957000,
     *                     "deal_amount": 0,
     *                     "order_id": 10000724,
     *                     "orders_id": 10000724,
     *                     "price": 0.1,
     *                     "status": 0,
     *                     "symbol": "btc_usd",
     *                     "type": "buy"
     *                 }
     *             ]
     *            }
     */
    public MocaResults getOrderInfo(String market, String coinType, String id)
            throws MocaException {

        String ct = (coinType.equalsIgnoreCase("BTC") ? "btc_usd" : "ltc_usd");
        
        String url_key = market + "|" + ct + "|" + URL_GETORDERINFO;
        
        IStockRestApi stockPost = URLs.get(url_key);
        if (stockPost == null) {
            if ("US".equalsIgnoreCase(market)) {
                stockPost = new StockRestApi("https://www.okcoin.com", _moca);
            } else if ("CHN".equalsIgnoreCase(market)) {
                stockPost = new StockRestApi("https://www.okcoin.cn", _moca);
            }
            URLs.put(url_key, stockPost);
        }
        
        String resultStr = "";
        EditableResults res = new SimpleResults();
        JSONObject body = null;
        _logger.info("id:" + id);
        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = stockPost.order_info(ct, id);
            body = new JSONObject(resultStr);
            
            JSONArray orders = body.getJSONArray("orders");
            JSONObject order = orders.getJSONObject(0);
            String oid = order.getString("order_id");
            String type = order.getString("type");
            double price = order.getDouble("price");
            double amount = order.getDouble("amount");
            double avg_price = order.getDouble("avg_price");
            double deal_amount = order.getDouble("deal_amount");
            int status = order.getInt("status");
            String symbol = order.getString("symbol");
            String create_date = order.getString("create_date");
            
            res.addColumn("id", MocaType.STRING);
            res.addColumn("symbol", MocaType.STRING);
            res.addColumn("type", MocaType.STRING);
            res.addColumn("price", MocaType.DOUBLE);
            res.addColumn("amount", MocaType.DOUBLE);
            res.addColumn("avg_price", MocaType.DOUBLE);
            res.addColumn("deal_amount", MocaType.DOUBLE);
            res.addColumn("status", MocaType.INTEGER);
            res.addColumn("create_date", MocaType.STRING);
            
            res.addRow();
            res.setStringValue("id", oid);
            res.setStringValue("symbol", symbol);
            res.setStringValue("type", type);
            res.setDoubleValue("price", price);
            res.setDoubleValue("amount", amount);
            res.setDoubleValue("avg_price", avg_price);
            res.setDoubleValue("deal_amount", deal_amount);
            res.setIntValue("status", status);
            res.setStringValue("create_date", create_date);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
            int code = 0;
            try {
                 code = body.getInt("error_code");
            }
            catch(Exception ee) {
                ee.printStackTrace();
            }
            throw new MocaException(code);
        }
        return res;
    }

    /**
     * @param coinType:
     *            1 'btc', 2 'ltc'
     */
    public MocaResults getOrders(String coinType) throws MocaException {

        int ct = 0;
        if ("btc".equalsIgnoreCase(coinType)) {
            ct = 1;
        } else {
            ct = 2;
        }
        HuobiService service = new HuobiService(_moca);
        String resultStr = "";
        EditableResults res = new SimpleResults();

        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = service.getOrders(ct, GET_ORDERS);

        _logger.debug("ResultStr:[" + resultStr + "]");
        
            JSONArray orders = new JSONArray(resultStr);
            
            if ( orders.length() <= 0) {
                res.addColumn("result", MocaType.STRING);
                res.addColumn("error", MocaType.INTEGER);
                res.addRow();
                res.setStringValue("result", "No Row Found");
                res.setIntValue("error", -1403);
            }
            else {
                res.addColumn("id", MocaType.STRING);
                res.addColumn("type", MocaType.STRING);
                res.addColumn("order_price", MocaType.DOUBLE);
                res.addColumn("order_amount", MocaType.DOUBLE);
                res.addColumn("processed_amount", MocaType.DOUBLE);
                res.addColumn("order_time", MocaType.STRING);
                
                for (int i = 0; i < orders.length(); i++) {
                    JSONObject order = ((JSONObject) orders.get(i));
                    String id = order.getString("id");
                    String type = order.getString("type");
                    double order_price = order.getDouble("order_price");
                    double order_amount = order.getDouble("order_amount");
                    double processed_amount = order.getDouble("processed_amount");
                    String order_time = order.getString("order_time");
                    res.addRow();
                    res.setStringValue("id", id);
                    res.setStringValue("type", type);
                    res.setDoubleValue("order_price", order_price);
                    res.setDoubleValue("order_amount", order_amount);
                    res.setDoubleValue("processed_amount", processed_amount);
                    res.setStringValue("order_time", order_time);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
            throw new MocaException(-1403, e.getMessage());
        }
        return res;
    }

    /**
     * @param market:
     *            'USD', 'CNY'
     * @param coinType:
     *            1 'btc', 2 'ltc'
     * @param id,
     *            order id
     */
    public MocaResults getOrderIdByTradeId(String coinType, Integer tradeid)
            throws MocaException {

        int ct = 0;
        if ("btc".equalsIgnoreCase(coinType)) {
            ct = 1;
        } else {
            ct = 2;
        }
        HuobiService service = new HuobiService(_moca);
        String resultStr = "";
        EditableResults res = new SimpleResults();
        res.addColumn("result", MocaType.STRING);
        res.addColumn("id", MocaType.INTEGER);
        
        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = service.getOrderIdByTradeId(ct, tradeid,
                    ORDER_ID_BY_TRADE_ID);

            JSONObject body = new JSONObject(resultStr);
            int code = body.getInt("code");
            String msg = body.getString("msg");
            res.addRow();
            res.setStringValue("result", msg);
            res.setIntValue("id", code);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
            throw new MocaException(-1403, e.getMessage());
        }
        return res;
    }

    /**
     * @param coinType:
     *            1 'btc', 2 'ltc'
     */
    public MocaResults getNewDealOrders(String coinType) throws MocaException {

        int ct = 0;
        if ("btc".equalsIgnoreCase(coinType)) {
            ct = 1;
        } else {
            ct = 2;
        }
        HuobiService service = new HuobiService(_moca);
        String resultStr = "";
        EditableResults res = new SimpleResults();
        res.addColumn("result", MocaType.STRING);
        res.addColumn("id", MocaType.INTEGER);
        
        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = service.getNewDealOrders(ct, NEW_DEAL_ORDERS);

            JSONObject body = new JSONObject(resultStr);
            int code = body.getInt("code");
            String msg = body.getString("msg");

            res.addRow();
            res.setStringValue("result", msg);
            res.setIntValue("id", code);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
            throw new MocaException(-1403, e.getMessage());
        }
        return res;
    }

    /**
     * {"total":"0.00", "net_asset":"0.00", "available_cny_display":"0.00",
     * "available_btc_display":"0.0000", "available_ltc_display":"0.0000",
     * "frozen_cny_display":"0.00", "frozen_btc_display":"0.0000",
     * "frozen_ltc_display":"0.0000", "loan_cny_display":"0.00",
     * "loan_btc_display":"0.0000", "loan_ltc_display":"0.0000"}
     * 
     * {"info":
     *  {"funds":
     *      {"asset":
     *          {"net":"0","total":"0"},
     *        "free":
     *          {"btc":"0","ltc":"0","usd":"0"},
     *        "freezed":
     *          {"btc":"0","ltc":"0","usd":"0"}
     *      }
     *  },
     *  "result":true
     * }

     */
    public MocaResults getAccountInfo(String market, String hak, String hsk)
            throws MocaException {
        
        String url_key = market + "|" + URL_GETACCOUNTINFO;
        
        IStockRestApi stockPost = URLs.get(url_key);
        if (stockPost == null) {
            if ("US".equalsIgnoreCase(market)) {
                stockPost = new StockRestApi("https://www.okcoin.com", _moca);
            } else if ("CHN".equalsIgnoreCase(market)) {
                stockPost = new StockRestApi("https://www.okcoin.cn", _moca);
            }
            URLs.put(url_key, stockPost);
        }
        
        String resultStr = "";
        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = stockPost.userinfo();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
        }
        System.out.print(resultStr);
        _logger.info("resultStr:" + resultStr);
        
        JSONObject body = null;
        try {
            body = new JSONObject(resultStr);
            JSONObject info = body.getJSONObject("info");
            JSONObject funds = info.getJSONObject("funds");
            JSONObject asset = funds.getJSONObject("asset");
            JSONObject free = funds.getJSONObject("free");
            JSONObject freezed = funds.getJSONObject("freezed");
            double total = asset.getDouble("total");
            double net = asset.getDouble("net");
            double free_usd = free.getDouble("usd");
            double free_ltc = free.getDouble("ltc");
            double free_btc = free.getDouble("btc");
            double freezed_usd = freezed.getDouble("usd");
            double freezed_btc = freezed.getDouble("btc");
            double freezed_ltc = freezed.getDouble("ltc");

            EditableResults res = new SimpleResults();
            res.addColumn("total", MocaType.DOUBLE);
            res.addColumn("net", MocaType.DOUBLE);
            res.addColumn("free_usd", MocaType.DOUBLE);
            res.addColumn("free_ltc", MocaType.DOUBLE);
            res.addColumn("free_btc", MocaType.DOUBLE);
            res.addColumn("freezed_usd", MocaType.DOUBLE);
            res.addColumn("freezed_btc", MocaType.DOUBLE);
            res.addColumn("freezed_ltc", MocaType.DOUBLE);
            res.addRow();
            res.setDoubleValue("total", total);
            res.setDoubleValue("net", net);
            res.setDoubleValue("free_usd", free_usd);
            res.setDoubleValue("free_ltc", free_ltc);
            res.setDoubleValue("free_btc", free_btc);
            res.setDoubleValue("freezed_usd", freezed_usd);
            res.setDoubleValue("freezed_btc", freezed_btc);
            res.setDoubleValue("freezed_ltc", freezed_ltc);
            return res;
        }
        catch (JSONException e) {
            throw new MocaException(-1403, e.getMessage());
        }
    }

    /**
     * @param market:
     *            'USD', 'CNY'
     * @param coinType:
     *            1 'btc', 2 'ltc'
     * @param id,
     *            order id
     */
    public MocaResults cancelOrder(String market, String coinType, Integer id)
            throws MocaException {

        int ct = 0;
        if ("btc".equalsIgnoreCase(coinType)) {
            ct = 1;
        } else {
            ct = 2;
        }
        HuobiService service = new HuobiService(_moca);
        String resultStr = "";
        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = service.cancelOrder(ct, id, CANCEL_ORDER);

            JSONObject body = new JSONObject(resultStr);
            int code = body.getInt("code");
            String msg = body.getString("msg");
            EditableResults res = new SimpleResults();
            res.addColumn("result", MocaType.STRING);
            res.addColumn("id", MocaType.INTEGER);
            res.addRow();
            res.setStringValue("result", msg);
            res.setIntValue("id", code);
            return res;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
            throw new MocaException(-1403, e.getMessage());
        }
    }

    /**
     * @param market:
     *            'USD', 'CNY'
     * @param coinType:
     *            1 'btc', 2 'ltc'
     */
    public MocaResults createBuyOrder(String market, String coinType,
            Double price, Double amount, String tradePassword, Integer tradeid,
            String reacod)
            throws MocaException {

        String ct = (coinType.equalsIgnoreCase("BTC") ? "btc_usd" : "ltc_usd");
        
        String url_key = market + "|" + ct + "|" + URL_CREATEBUYORDER;
        
        IStockRestApi stockPost = URLs.get(url_key);
        if (stockPost == null) {
            if ("US".equalsIgnoreCase(market)) {
                stockPost = new StockRestApi("https://www.okcoin.com", _moca);
            } else if ("CHN".equalsIgnoreCase(market)) {
                stockPost = new StockRestApi("https://www.okcoin.cn", _moca);
            }
            URLs.put(url_key, stockPost);
        }
        
        String resultStr = "";
        JSONObject body = null;
        MocaResults rs = null;
        try {
            resultStr = stockPost.trade(ct, "buy", String.valueOf(price), String.valueOf(amount));

            body = new JSONObject(resultStr);
            String orderid = body.getString("order_id");
            String result = body.getString("result");
            
            rs = _moca.executeCommand("get order info for oc where market = '" + market + "' and coinType = '"
                    + coinType + "' and id = '" + orderid + "'");
            
            rs.next();
            
            String oid = rs.getString("id");
            String type = rs.getString("type");
            String symbol = rs.getString("symbol");
            double order_price = rs.getDouble("price");
            double order_amount = rs.getDouble("amount");
            double avg_price = rs.getDouble("avg_price");
            double deal_amount = rs.getDouble("deal_amount");
            int status = rs.getInt("status");
            
            _moca.executeCommand("publish data "
                               + "  where id = '" + oid + "'"
                               + "    and type = '" + type + "'"
                               + "    and symbol = '" + symbol + "'"
                               + "    and price =" + order_price
                               + "    and amount =" + order_amount
                               + "    and avg_price =" + avg_price
                               + "    and deal_amount =" + deal_amount
                               + "    and status =" + status
                               + "    and reacod = '" + reacod + "'"
                               + "    and ins_dt = sysdate "
                               + "| "
                               + "create record where table_name = 'oc_buysell_data' and @*");
            rs.reset();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
            int code = 0;
            try {
                code = body.getInt("error_code");
            }
            catch (Exception ee) {
                ee.printStackTrace();
            }
            throw new MocaException(code);
        }
        return rs;
    }

    /**
     * @param market:
     *            'USD', 'CNY'
     * @param coinType:
     *            1 'btc', 2 'ltc'
     */
    public MocaResults createBuyOrderWithMarketPrice(String market,
            String coinType, Double amount, String tradePassword,
            Integer tradeid,
            String reacod) throws MocaException {

        int ct = 0;
        if ("btc".equalsIgnoreCase(coinType)) {
            ct = 1;
        } else {
            ct = 2;
        }
        HuobiService service = new HuobiService(_moca);
        String resultStr = "";
        JSONObject body = null;
        MocaResults rs = null;
        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = service.buyMarket(ct, amount.toString(), tradePassword,
                    tradeid, BUY_MARKET);

            body = new JSONObject(resultStr);
            String orderid = body.getString("id");
            String result = body.getString("result");
            
            rs = _moca.executeCommand("get order info for oc where coinType = '"
                    + coinType + "' and id = '" + orderid + "'");
            
            rs.next();
            
            String oid = rs.getString("id");
            String type = rs.getString("type");
            double order_price = rs.getDouble("order_price");
            double order_amount = rs.getDouble("order_amount");
            double processed_price = rs.getDouble("processed_price");
            double processed_amount = rs.getDouble("processed_amount");
            double vot = rs.getDouble("vot");
            double fee = rs.getDouble("fee");
            double total = rs.getDouble("total");
            int status = rs.getInt("status");
            
            _moca.executeCommand("publish data "
                               + "  where id = '" + oid + "'"
                               + "    and type = '" + type + "'"
                               + "    and order_price =" + order_price
                               + "    and order_amount =" + order_amount
                               + "    and processed_price =" + processed_price
                               + "    and processed_amount =" + processed_amount
                               + "    and vot =" + vot
                               + "    and fee =" + fee
                               + "    and total =" + total
                               + "    and status =" + status
                               + "    and reacod = '" + reacod + "'"
                               + "    and ins_dt = sysdate "
                               + "| "
                               + "create record where table_name = 'oc_buysell_data' and @*");
            rs.reset();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
            int code = body.getInt("code");
            String msg = body.getString("msg");
            throw new MocaException(code, msg);
        }
        return rs;
    }

    /**
     * @param market:
     *            'USD', 'CNY'
     * @param coinType:
     *            1 'btc', 2 'ltc'
     */
    public MocaResults createSellOrder(String market, String coinType,
            Double price, Double amount, String tradePassword, Integer tradeid,
            String reacod)
            throws MocaException {

        String ct = (coinType.equalsIgnoreCase("BTC") ? "btc_usd" : "ltc_usd");
        
        String url_key = market + "|" + ct + "|" + URL_CREATEBUYORDER;
        
        IStockRestApi stockPost = URLs.get(url_key);
        if (stockPost == null) {
            if ("US".equalsIgnoreCase(market)) {
                stockPost = new StockRestApi("https://www.okcoin.com", _moca);
            } else if ("CHN".equalsIgnoreCase(market)) {
                stockPost = new StockRestApi("https://www.okcoin.cn", _moca);
            }
            URLs.put(url_key, stockPost);
        }
        
        String resultStr = "";
        JSONObject body = null;
        MocaResults rs = null;
        try {
            resultStr = stockPost.trade(ct, "sell", String.valueOf(price), String.valueOf(amount));

            body = new JSONObject(resultStr);
            String orderid = body.getString("order_id");
            String result = body.getString("result");
            
            rs = _moca.executeCommand("get order info for oc where market = '" + market + "' and coinType = '"
                    + coinType + "' and id = '" + orderid + "'");
            
            rs.next();
            
            String oid = rs.getString("id");
            String type = rs.getString("type");
            String symbol = rs.getString("symbol");
            double order_price = rs.getDouble("price");
            double order_amount = rs.getDouble("amount");
            double avg_price = rs.getDouble("avg_price");
            double deal_amount = rs.getDouble("deal_amount");
            int status = rs.getInt("status");
            
            _moca.executeCommand("publish data "
                               + "  where id = '" + oid + "'"
                               + "    and type = '" + type + "'"
                               + "    and symbol = '" + symbol + "'"
                               + "    and price =" + order_price
                               + "    and amount =" + order_amount
                               + "    and avg_price =" + avg_price
                               + "    and deal_amount =" + deal_amount
                               + "    and status =" + status
                               + "    and reacod = '" + reacod + "'"
                               + "    and ins_dt = sysdate "
                               + "| "
                               + "create record where table_name = 'oc_buysell_data' and @*");
            rs.reset();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
            int code = 0;
            try {
                code = body.getInt("error_code");
            }
            catch (Exception ee) {
                ee.printStackTrace();
            }
            throw new MocaException(code);
        }
        return rs;
    }

    /**
     * @param market:
     *            'USD', 'CNY'
     * @param coinType:
     *            1 'btc', 2 'ltc'
     */
    public MocaResults createSellOrderWithMarketPrice(String market,
            String coinType, Double amount, String tradePassword,
            Integer tradeid,
            String reacod) throws MocaException {

        int ct = 0;
        if ("btc".equalsIgnoreCase(coinType)) {
            ct = 1;
        } else {
            ct = 2;
        }
        HuobiService service = new HuobiService(_moca);
        String resultStr = "";
        JSONObject body = null;
        MocaResults rs = null;
        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = service.sellMarket(ct, amount.toString(), tradePassword,
                    tradeid, SELL_MARKET);


            body = new JSONObject(resultStr);
            String orderid = body.getString("id");
            String result = body.getString("result");
            
            rs = _moca.executeCommand("get order info for oc where coinType = '"
                    + coinType + "' and id = '" + orderid + "'");
            
            rs.next();
            
            String oid = rs.getString("id");
            String type = rs.getString("type");
            double order_price = rs.getDouble("order_price");
            double order_amount = rs.getDouble("order_amount");
            double processed_price = rs.getDouble("processed_price");
            double processed_amount = rs.getDouble("processed_amount");
            double vot = rs.getDouble("vot");
            double fee = rs.getDouble("fee");
            double total = rs.getDouble("total");
            int status = rs.getInt("status");
            
            _moca.executeCommand("publish data "
                               + "  where id = '" + oid + "'"
                               + "    and type = '" + type + "'"
                               + "    and order_price =" + order_price
                               + "    and order_amount =" + order_amount
                               + "    and processed_price =" + processed_price
                               + "    and processed_amount =" + processed_amount
                               + "    and vot =" + vot
                               + "    and fee =" + fee
                               + "    and total =" + total
                               + "    and status =" + status
                               + "    and reacod = '" + reacod + "'"
                               + "    and ins_dt = sysdate "
                               + "| "
                               + "create record where table_name = 'oc_buysell_data' and @*");
            rs.reset();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
            int code = body.getInt("code");
            String msg = body.getString("msg");
            throw new MocaException(code, msg);
        }
        return rs;
    }

    /**
     * @param market:
     *            'US', 'CHN'
     * @param coinType:
     *            'btc','ltc'
     */
    public MocaResults getTop10Data(String market, String coinType,
            Boolean save_db_flag)
            throws MocaException {
        /*
         * {"asks":[[1306.8,93.5],
         *         [1298,1],
         *         [1296.9,93],
         *         [1287,92.5],
         *         [1281,0.17],
         *         [1277.1,92],
         *         [1270,1],
         *         ...
         *  "bids":[[989,0.189],
         *         [988.4,13.313],
         *         [988.37,0.163],
         *         [987.96,0.24],
         *         [987.92,0.149],
         *         [987.88,0.303],
         *         [987.83,0.155],
         *         [987.82,0.026],
         *         ...
         */
        String url_key = market + "|" + coinType + "|" + URL_TYPE2;
        String ct = (coinType.equalsIgnoreCase("BTC") ? "btc_usd" : "ltc_usd");
        
        IStockRestApi stockGet = URLs.get(url_key);
        if (stockGet == null) {
            if ("US".equalsIgnoreCase(market)) {
                stockGet = new StockRestApi("https://www.okcoin.com", _moca);
            } else if ("CHN".equalsIgnoreCase(market)) {
                stockGet = new StockRestApi("https://www.okcoin.cn", _moca);
            }
            URLs.put(url_key, stockGet);
        }
        
        
        MocaResults rs = null;
        try {
                String text = stockGet.depth(ct);
                _logger.debug("body:" + text);
                JSONObject body = new JSONObject(text);
                JSONArray buys = body.getJSONArray("bids");
                JSONArray sells = body.getJSONArray("asks");
                double[] buy_amounts = new double[10];
                double[] buy_prices = new double[10];
                double[] sell_amounts = new double[10];
                double[] sell_prices = new double[10];
                int sz = sells.length();
                for (int i = 0; i < 10; i++) {
                    buy_amounts[i] = ((JSONArray) buys.get(i)).getDouble(1);
                    buy_prices[i] = ((JSONArray) buys.get(i)).getDouble(0);
                    sell_amounts[i] = ((JSONArray) sells.get(sz- 1 - i)).getDouble(1);
                    sell_prices[i] = ((JSONArray) sells.get(sz - 1 - i)).getDouble(0);
                }

                MocaResults rs2 = null;
                rs2 = _moca.executeCommand(
                        "publish data where tm = to_char(sysdate, 'yyyymmddhhmiss')");
                rs2.next();
                String time = rs2.getString("tm");
                rs2 = null;

                if (save_db_flag != null && save_db_flag.equals(Boolean.TRUE)) {
                    _moca.executeCommand("publish data " + " where symbol = '"
                            + ct + "'" + "   and time = '" + time + "'"
                            + "   and b_amt1 = " + buy_amounts[0]
                            + "   and b_pri1 = " + buy_prices[0]
                            + "   and b_amt2 = " + buy_amounts[1]
                            + "   and b_pri2 = " + buy_prices[1]
                            + "   and b_amt3 = " + buy_amounts[2]
                            + "   and b_pri3 = " + buy_prices[2]
                            + "   and b_amt4 = " + buy_amounts[3]
                            + "   and b_pri4 = " + buy_prices[3]
                            + "   and b_amt5 = " + buy_amounts[4]
                            + "   and b_pri5 = " + buy_prices[4]
                            + "   and b_amt6 = " + buy_amounts[5]
                            + "   and b_pri6 = " + buy_prices[5]
                            + "   and b_amt7 = " + buy_amounts[6]
                            + "   and b_pri7 = " + buy_prices[6]
                            + "   and b_amt8 = " + buy_amounts[7]
                            + "   and b_pri8 = " + buy_prices[7]
                            + "   and b_amt9 = " + buy_amounts[8]
                            + "   and b_pri9 = " + buy_prices[8]
                            + "   and b_amt10 = " + buy_amounts[9]
                            + "   and b_pri10 = " + buy_prices[9]
                            + "   and s_amt1 = " + sell_amounts[0]
                            + "   and s_pri1 = " + sell_prices[0]
                            + "   and s_amt2 = " + sell_amounts[1]
                            + "   and s_pri2 = " + sell_prices[1]
                            + "   and s_amt3 = " + sell_amounts[2]
                            + "   and s_pri3 = " + sell_prices[2]
                            + "   and s_amt4 = " + sell_amounts[3]
                            + "   and s_pri4 = " + sell_prices[3]
                            + "   and s_amt5 = " + sell_amounts[4]
                            + "   and s_pri5 = " + sell_prices[4]
                            + "   and s_amt6 = " + sell_amounts[5]
                            + "   and s_pri6 = " + sell_prices[5]
                            + "   and s_amt7 = " + sell_amounts[6]
                            + "   and s_pri7 = " + sell_prices[6]
                            + "   and s_amt8 = " + sell_amounts[7]
                            + "   and s_pri8 = " + sell_prices[7]
                            + "   and s_amt9 = " + sell_amounts[8]
                            + "   and s_pri9 = " + sell_prices[8]
                            + "   and s_amt10 = " + sell_amounts[9]
                            + "   and s_pri10 = " + sell_prices[9]
                            + "   and ins_dt = sysdate " + "|"
                            + "create record where table_name = 'oc_top10_data' and @* ");
                }
                    rs = _moca.executeCommand("publish data "
                            + " where symbol = '" + ct + "'"
                            + "   and time = '" + time + "'"
                            + "   and b_amt1 = " + buy_amounts[0]
                            + "   and b_pri1 = " + buy_prices[0]
                            + "   and b_amt2 = " + buy_amounts[1]
                            + "   and b_pri2 = " + buy_prices[1]
                            + "   and b_amt3 = " + buy_amounts[2]
                            + "   and b_pri3 = " + buy_prices[2]
                            + "   and b_amt4 = " + buy_amounts[3]
                            + "   and b_pri4 = " + buy_prices[3]
                            + "   and b_amt5 = " + buy_amounts[4]
                            + "   and b_pri5 = " + buy_prices[4]
                            + "   and b_amt6 = " + buy_amounts[5]
                            + "   and b_pri6 = " + buy_prices[5]
                            + "   and b_amt7 = " + buy_amounts[6]
                            + "   and b_pri7 = " + buy_prices[6]
                            + "   and b_amt8 = " + buy_amounts[7]
                            + "   and b_pri8 = " + buy_prices[7]
                            + "   and b_amt9 = " + buy_amounts[8]
                            + "   and b_pri9 = " + buy_prices[8]
                            + "   and b_amt10 = " + buy_amounts[9]
                            + "   and b_pri10 = " + buy_prices[9]
                            + "   and s_amt1 = " + sell_amounts[0]
                            + "   and s_pri1 = " + sell_prices[0]
                            + "   and s_amt2 = " + sell_amounts[1]
                            + "   and s_pri2 = " + sell_prices[1]
                            + "   and s_amt3 = " + sell_amounts[2]
                            + "   and s_pri3 = " + sell_prices[2]
                            + "   and s_amt4 = " + sell_amounts[3]
                            + "   and s_pri4 = " + sell_prices[3]
                            + "   and s_amt5 = " + sell_amounts[4]
                            + "   and s_pri5 = " + sell_prices[4]
                            + "   and s_amt6 = " + sell_amounts[5]
                            + "   and s_pri6 = " + sell_prices[5]
                            + "   and s_amt7 = " + sell_amounts[6]
                            + "   and s_pri7 = " + sell_prices[6]
                            + "   and s_amt8 = " + sell_amounts[7]
                            + "   and s_pri8 = " + sell_prices[7]
                            + "   and s_amt9 = " + sell_amounts[8]
                            + "   and s_pri9 = " + sell_prices[8]
                            + "   and s_amt10 = " + sell_amounts[9]
                            + "   and s_pri10 = " + sell_prices[9]
                            + "   and ins_dt = sysdate ");
        } catch (Exception e) {
            e.printStackTrace();
            _logger.debug(e.getMessage());
            throw new MocaException(-1403, e.getMessage());
        }
        return rs;
    }

    // Private fields
    private final MocaContext _moca;
    private final CrudManager _manager;
    private final String URL_TYPE1 = "getTickerRecord";
    private final String URL_TYPE2 = "getTop10Data";
    private final String URL_GETACCOUNTINFO = "getAccountInfo";
    private final String URL_GETORDERINFO = "getOrderInfo";
    private final String URL_CREATEBUYORDER = "createBuyOrder";
    private final String URL_GETTRADERECORDS = "getTradeRecords";
    private static String BUY = "buy";
    private static String BUY_MARKET = "buy_market";
    private static String CANCEL_ORDER = "cancel_order";
    private static String ACCOUNT_INFO = "get_account_info";
    private static String NEW_DEAL_ORDERS = "get_new_deal_orders";
    private static String ORDER_ID_BY_TRADE_ID = "get_order_id_by_trade_id";
    private static String GET_ORDERS = "get_orders";
    private static String ORDER_INFO = "order_info";
    private static String SELL = "sell";
    private static String SELL_MARKET = "sell_market";
    static Map<String, IStockRestApi> URLs = new HashMap<String, IStockRestApi>();
    private Logger _logger = LogManager.getLogger(OkCoinCmd.class);
}
