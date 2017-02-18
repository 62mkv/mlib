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
public class HuobiCmd {

    /**
     * Creates a new HuoBiService class
     * 
     * @param mocaCtx
     *            The MOCA context
     * @throws MocaException
     */
    public HuobiCmd(MocaContext mocaCtx) throws MocaException {
        _moca = mocaCtx;
        _manager = MocaUtils.crudManager(_moca);
    }

    /**
     * @param market:
     *            'US', 'CHN'
     * @param coinType:
     *            'btc','ltc'
     */
    public MocaResults getRealTimeRecord(String market, String coinType,
            Boolean save_db_flag) throws MocaException {

        /*
         * {"time":"1378137600","ticker":{"high":86.48,"low":79.75,"symbol":
         * "btccny","last":83.9,"vol":2239560.1752883,"buy":83.88,"sell":83.9}}
         * 报价：最高价，最低价，当前价，成交量，买1，卖1
         */

        String url_key = market + "|" + coinType + "|" + URL_TYPE1;
        String marketDataUrl = URLs.get(url_key);
        if (marketDataUrl == null || marketDataUrl.isEmpty()) {
            if ("US".equalsIgnoreCase(market)) {
                marketDataUrl = "http://api.huobi.com/usdmarket/ticker_btc_json.js";
            } else if ("CHN".equalsIgnoreCase(market)) {
                if ("BTC".equalsIgnoreCase(coinType)) {
                    marketDataUrl = "http://api.huobi.com/staticmarket/ticker_btc_json.js";
                } else {
                    marketDataUrl = "http://api.huobi.com/staticmarket/ticker_ltc_json.js";
                }
            }
            URLs.put(url_key, marketDataUrl);
        }

        String cmd = "do http request where url = '" + marketDataUrl + "'";
        MocaResults rs = null;
        try {
            rs = _moca.executeCommand(cmd);
            if (rs.next()) {
                String text = rs.getString("body");
                _logger.debug("body:" + text);
                JSONObject body = new JSONObject(text);
                String time = body.getString("time");
                JSONObject ticker = body.getJSONObject("ticker");
                double high = ticker.getDouble("high");
                double low = ticker.getDouble("low");
                String symbol = ticker.getString("symbol");
                double vol = ticker.getDouble("vol");
                double buy = ticker.getDouble("buy");
                double sell = ticker.getDouble("sell");
                double last = ticker.getDouble("last");
                double first = ticker.getDouble("open");
                _logger.info("time:" + time);
                _logger.info("high:" + high);
                _logger.info("low:" + low);
                _logger.info("symbol:" + symbol);
                _logger.info("vol:" + vol);
                _logger.info("buy:" + buy);
                _logger.info("sell:" + sell);
                _logger.info("last:" + last);
                _logger.info("first:" + first);

                if (save_db_flag != null && save_db_flag.equals(Boolean.TRUE)) {
                    _moca.executeCommand("publish data where symbol = '"
                            + symbol + "'" + "   and time = '" + time + "'"
                            + "   and high = " + high + "   and low = " + low
                            + "   and vol = " + vol + "   and buy = " + buy
                            + "   and sell = " + sell + "   and last = " + last
                            + "   and first = " + first
                            + "   and ins_dt = sysdate " + "|"
                            + "create record where table_name = 'hb_real_data' and @* ");
                }
                rs = _moca.executeCommand("publish data " + " where symbol = '"
                        + symbol + "'" + "   and time = '" + time + "'"
                        + "   and high = " + high + "   and low = " + low
                        + "   and vol = " + vol + "   and buy = " + buy
                        + "   and sell = " + sell + "   and last = " + last
                        + "   and first = " + first
                        + "   and ins_dt = sysdate ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            _logger.debug("getRealTimeRecord Exception:" + e.getMessage());
            throw new MocaException(-1403, e.getMessage());
        }

        return rs;
    }

    /**
     * @param coinType:
     *            1 'btc', 2 'ltc'
     */
    public MocaResults getOrderInfo(String coinType, String id)
            throws MocaException {

        int ct = 0;
        if ("btc".equalsIgnoreCase(coinType)) {
            ct = 1;
        } else {
            ct = 2;
        }
        HuobiService service = new HuobiService();
        String resultStr = "";
        EditableResults res = new SimpleResults();
        JSONObject body = null;
        _logger.info("id:" + id);
        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = service.getOrderInfo(ct, Long.valueOf(id), ORDER_INFO);
            body = new JSONObject(resultStr);
            
            String oid = body.getString("id");
            String type = body.getString("type");
            double order_price = body.getDouble("order_price");
            double order_amount = body.getDouble("order_amount");
            double processed_price = body.getDouble("processed_price");
            double processed_amount = body.getDouble("processed_amount");
            double vot = body.getDouble("vot");
            double fee = body.getDouble("fee");
            double total = body.getDouble("total");
            int status = body.getInt("status");
            
            res.addColumn("id", MocaType.STRING);
            res.addColumn("type", MocaType.STRING);
            res.addColumn("order_price", MocaType.DOUBLE);
            res.addColumn("order_amount", MocaType.DOUBLE);
            res.addColumn("processed_price", MocaType.DOUBLE);
            res.addColumn("processed_amount", MocaType.DOUBLE);
            res.addColumn("vot", MocaType.DOUBLE);
            res.addColumn("fee", MocaType.DOUBLE);
            res.addColumn("total", MocaType.DOUBLE);
            res.addColumn("status", MocaType.INTEGER);
            
            res.addRow();
            res.setStringValue("id", oid);
            res.setStringValue("type", type);
            res.setDoubleValue("order_price", order_price);
            res.setDoubleValue("order_amount", order_amount);
            res.setDoubleValue("processed_price", processed_price);
            res.setDoubleValue("processed_amount", processed_amount);
            res.setDoubleValue("vot", vot);
            res.setDoubleValue("fee", fee);
            res.setDoubleValue("total", total);
            res.setIntValue("status", status);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
            int code = body.getInt("code");
            String msg = body.getString("msg");
            throw new MocaException(code, msg);
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
        HuobiService service = new HuobiService();
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
        HuobiService service = new HuobiService();
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
        HuobiService service = new HuobiService();
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
     */
    public MocaResults getAccountInfo(String hak, String hsk)
            throws MocaException {

        HuobiService service = new HuobiService();
        String resultStr = "";
        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = service.getAccountInfo(ACCOUNT_INFO, hak, hsk);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.debug(e.getMessage());
        }
        _logger.info("resultStr:" + resultStr);
        
        JSONObject body = null;
        System.out.println("Here is resultStr:" + resultStr);
        try {
            body = new JSONObject(resultStr);
            double total = body.getDouble("total");
            double net_asset = body.getDouble("net_asset");
            double available_cny_display = body.getDouble("available_cny_display");
            double available_ltc_display = body.getDouble("available_ltc_display");
            double available_btc_display = body.getDouble("available_btc_display");
            double frozen_cny_display = body.getDouble("frozen_cny_display");
            double frozen_btc_display = body.getDouble("frozen_btc_display");
            double frozen_ltc_display = body.getDouble("frozen_ltc_display");
            double loan_cny_display = body.getDouble("loan_cny_display");
            double loan_btc_display = body.getDouble("loan_btc_display");
            double loan_ltc_display = body.getDouble("loan_ltc_display");

            EditableResults res = new SimpleResults();
            res.addColumn("total", MocaType.DOUBLE);
            res.addColumn("net_asset", MocaType.DOUBLE);
            res.addColumn("available_cny_display", MocaType.DOUBLE);
            res.addColumn("available_ltc_display", MocaType.DOUBLE);
            res.addColumn("available_btc_display", MocaType.DOUBLE);
            res.addColumn("frozen_cny_display", MocaType.DOUBLE);
            res.addColumn("frozen_btc_display", MocaType.DOUBLE);
            res.addColumn("frozen_ltc_display", MocaType.DOUBLE);
            res.addColumn("loan_cny_display", MocaType.DOUBLE);
            res.addColumn("loan_btc_display", MocaType.DOUBLE);
            res.addColumn("loan_ltc_display", MocaType.DOUBLE);
            res.addRow();
            res.setDoubleValue("total", total);
            res.setDoubleValue("net_asset", net_asset);
            res.setDoubleValue("available_cny_display", available_cny_display);
            res.setDoubleValue("available_ltc_display", available_ltc_display);
            res.setDoubleValue("available_btc_display", available_btc_display);
            res.setDoubleValue("frozen_cny_display", frozen_cny_display);
            res.setDoubleValue("frozen_btc_display", frozen_btc_display);
            res.setDoubleValue("frozen_ltc_display", frozen_ltc_display);
            res.setDoubleValue("loan_cny_display", loan_cny_display);
            res.setDoubleValue("loan_btc_display", loan_btc_display);
            res.setDoubleValue("loan_ltc_display", loan_ltc_display);
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
        HuobiService service = new HuobiService();
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
            Double price, Double amount, String tradePassword, Integer tradeid)
            throws MocaException {

        int ct = 0;
        if ("btc".equalsIgnoreCase(coinType)) {
            ct = 1;
        } else {
            ct = 2;
        }
        HuobiService service = new HuobiService();
        String resultStr = "";
        JSONObject body = null;
        MocaResults rs = null;
        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = service.buy(ct, price.toString(), amount.toString(),
                    tradePassword, tradeid, BUY);

            body = new JSONObject(resultStr);
            String orderid = body.getString("id");
            String result = body.getString("result");
            
            rs = _moca.executeCommand("get order info where coinType = '"
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
                               + "    and ins_dt = sysdate "
                               + "| "
                               + "create record where table_name = 'hb_buysell_data' and @*");
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
    public MocaResults createBuyOrderWithMarketPrice(String market,
            String coinType, Double amount, String tradePassword,
            Integer tradeid) throws MocaException {

        int ct = 0;
        if ("btc".equalsIgnoreCase(coinType)) {
            ct = 1;
        } else {
            ct = 2;
        }
        HuobiService service = new HuobiService();
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
            
            rs = _moca.executeCommand("get order info where coinType = '"
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
                               + "    and ins_dt = sysdate "
                               + "| "
                               + "create record where table_name = 'hb_buysell_data' and @*");
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
            Double price, Double amount, String tradePassword, Integer tradeid)
            throws MocaException {

        int ct = 0;
        if ("btc".equalsIgnoreCase(coinType)) {
            ct = 1;
        } else {
            ct = 2;
        }
        HuobiService service = new HuobiService();
        String resultStr = "";
        JSONObject body = null;
        MocaResults rs = null;
        try {
            // 提交限价单接口 1btc 2ltc
            resultStr = service.sell(ct, price.toString(), amount.toString(),
                    tradePassword, tradeid, SELL);
            body = new JSONObject(resultStr);
            String orderid = body.getString("id");
            String result = body.getString("result");
            
            rs = _moca.executeCommand("get order info where coinType = '"
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
                               + "    and ins_dt = sysdate "
                               + "| "
                               + "create record where table_name = 'hb_buysell_data' and @*");
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
    public MocaResults createSellOrderWithMarketPrice(String market,
            String coinType, Double amount, String tradePassword,
            Integer tradeid) throws MocaException {

        int ct = 0;
        if ("btc".equalsIgnoreCase(coinType)) {
            ct = 1;
        } else {
            ct = 2;
        }
        HuobiService service = new HuobiService();
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
            
            rs = _moca.executeCommand("get order info where coinType = '"
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
                               + "    and ins_dt = sysdate "
                               + "| "
                               + "create record where table_name = 'hb_buysell_data' and @*");
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
            Boolean save_db_flag, Boolean rtn_trade_data_flg)
            throws MocaException {
        /*
         * {"symbol":"btccny", "amount":456675,
         * "buys":[{"amount":0.8203,"level":1,"price":6136.79},
         * {"amount":1,"level":1,"price":6136.49},
         * {"amount":2.06,"level":1,"price":6136.24},
         * {"amount":1.88,"level":1,"price":6136.23},
         * {"amount":0.024,"level":1,"price":6136.2},
         * {"amount":0.126,"level":1,"price":6136.13},
         * {"amount":0.375,"level":1,"price":6136.12},
         * {"amount":0.12,"level":1,"price":6136.06},
         * {"amount":0.12,"level":1,"price":6136.04},
         * {"amount":0.002,"level":1,"price":6136.02}], "amp":0, "level":6136,
         * "sells":[{"amount":0.156,"level":1,"price":6137.02},
         * {"amount":1.82,"level":1,"price":6137.3},
         * {"amount":0.071,"level":1,"price":6137.35},
         * {"amount":0.03,"level":1,"price":6137.51},
         * {"amount":0.03,"level":1,"price":6137.63},
         * {"amount":0.6,"level":1,"price":6137.78},
         * {"amount":0.03,"level":1,"price":6137.88},
         * {"amount":0.165,"level":1,"price":6137.95},
         * {"amount":0.002,"level":1,"price":6138.02},
         * {"amount":0.261,"level":1,"price":6138.1}], "p_new":6136.23,
         * "p_last":6138.64,
         * "trades":[{"amount":0.01,"price":6136.23,"time":"10:48:09","en_type":
         * "ask","type":"卖出"},
         * {"amount":0.0209,"price":6137.04,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.09,"price":6137,"time":"10:48:08","en_type":"bid","type":
         * "买入"},
         * {"amount":0.003,"price":6136.69,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.261,"price":6136.56,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.245,"price":6136.56,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.2511,"price":6136.49,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.11,"price":6136.49,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.0129,"price":6136.49,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.064,"price":6136.49,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.003,"price":6136.49,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.034,"price":6136.49,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.124,"price":6137.48,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.02,"price":6137.47,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.03,"price":6137.34,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.03,"price":6137.25,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.06,"price":6136.83,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.2198,"price":6136.13,"time":"10:48:08","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.3802,"price":6136.56,"time":"10:48:08","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.06,"price":6136.83,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.076,"price":6136.83,"time":"10:48:08","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.03,"price":6136.83,"time":"10:48:07","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.02,"price":6136.83,"time":"10:48:07","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.1098,"price":6136.56,"time":"10:48:07","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.0099,"price":6137.05,"time":"10:48:07","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.01,"price":6136.56,"time":"10:48:07","en_type":"ask",
         * "type":"卖出"},
         * {"amount":1.619,"price":6137.39,"time":"10:48:07","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.03,"price":6137.28,"time":"10:48:07","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.371,"price":6136.06,"time":"10:48:07","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.124,"price":6136.06,"time":"10:48:07","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.013,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.03,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.534,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.089,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.328,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.095,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.199,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.3,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.5,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.01,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.5,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.2,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.5,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.091,"price":6136.06,"time":"10:48:06","en_type":"bid",
         * "type":"买入"},
         * {"amount":1.599,"price":6136.11,"time":"10:48:06","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.2,"price":6136.35,"time":"10:48:06","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.0747,"price":6136.35,"time":"10:48:06","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.5,"price":6136.56,"time":"10:48:06","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.1243,"price":6136.35,"time":"10:48:05","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.0374,"price":6136.35,"time":"10:48:05","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.1,"price":6137.2,"time":"10:48:05","en_type":"bid","type"
         * :"买入"},
         * {"amount":0.208,"price":6137.2,"time":"10:48:05","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.169,"price":6137.2,"time":"10:48:05","en_type":"bid",
         * "type":"买入"},
         * {"amount":0.4276,"price":6136.35,"time":"10:48:05","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.5,"price":6136.56,"time":"10:48:05","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.089,"price":6136.65,"time":"10:48:05","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.1,"price":6137.2,"time":"10:48:05","en_type":"bid","type"
         * :"买入"},
         * {"amount":0.431,"price":6136.65,"time":"10:48:05","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.169,"price":6137.29,"time":"10:48:05","en_type":"ask",
         * "type":"卖出"},
         * {"amount":0.494,"price":6137.83,"time":"10:48:05","en_type":"bid",
         * "type":"买入"}],
         * "top_buy":[{"amount":0.8203,"level":1,"price":6136.79,"accu":0.8203},
         * {"amount":1,"level":1,"price":6136.49,"accu":1.8203},
         * {"amount":2.06,"level":1,"price":6136.24,"accu":3.8803},
         * {"amount":1.88,"level":1,"price":6136.23,"accu":5.7603},
         * {"amount":0.024,"level":1,"price":6136.2,"accu":5.7843}],
         * "total":2780156825.8904, "p_low":6042, "p_open":6048.96,
         * "top_sell":[{"amount":0.156,"level":1,"price":6137.02,"accu":0.156},
         * {"amount":1.82,"level":1,"price":6137.3,"accu":1.976},
         * {"amount":0.071,"level":1,"price":6137.35,"accu":2.047},
         * {"amount":0.03,"level":1,"price":6137.51,"accu":2.077},
         * {"amount":0.03,"level":1,"price":6137.63,"accu":2.107}],
         * "p_high":6334}
         */
        String url_key = market + "|" + coinType + "|" + URL_TYPE2;
        String marketDataUrl = URLs.get(url_key);
        if (marketDataUrl == null || marketDataUrl.isEmpty()) {
            if ("US".equalsIgnoreCase(market)) {
                marketDataUrl = "http://api.huobi.com/usdmarket/detail_btc_json.js";
            } else if ("CHN".equalsIgnoreCase(market)) {
                if ("BTC".equalsIgnoreCase(coinType)) {
                    marketDataUrl = "http://api.huobi.com/staticmarket/detail_btc_json.js";
                } else {
                    marketDataUrl = "http://api.huobi.com/staticmarket/detail_ltc_json.js";
                }
            }
            URLs.put(url_key, marketDataUrl);
        }

        String cmd = "do http request where url = '" + marketDataUrl + "'";
        MocaResults rs = null;
        try {
            rs = _moca.executeCommand(cmd);
            if (rs.next()) {
                String text = rs.getString("body");
                _logger.debug("body:" + text);
                JSONObject body = new JSONObject(text);
                JSONArray buys = body.getJSONArray("buys");
                JSONArray sells = body.getJSONArray("sells");
                JSONArray trades = body.getJSONArray("trades");
                double[] buy_amounts = new double[buys.length()];
                double[] buy_prices = new double[buys.length()];
                double[] sell_amounts = new double[buys.length()];
                double[] sell_prices = new double[buys.length()];
                for (int i = 0; i < buys.length(); i++) {
                    buy_amounts[i] = ((JSONObject) buys.get(i))
                            .getDouble("amount");
                    buy_prices[i] = ((JSONObject) buys.get(i))
                            .getDouble("price");
                    sell_amounts[i] = ((JSONObject) sells.get(i))
                            .getDouble("amount");
                    sell_prices[i] = ((JSONObject) sells.get(i))
                            .getDouble("price");
                }
                String symbol = body.getString("symbol");
                double p_new = body.getDouble("p_new");
                double p_last = body.getDouble("p_last");
                double p_low = body.getDouble("p_low");
                double p_open = body.getDouble("p_open");
                double p_high = body.getDouble("p_high");
                double total = body.getDouble("total");

                MocaResults rs2 = null;
                rs2 = _moca.executeCommand(
                        "publish data where tm = to_char(sysdate, 'yyyymmddhhmiss')");
                rs2.next();
                String time = rs2.getString("tm");
                rs2 = null;

                if (save_db_flag != null && save_db_flag.equals(Boolean.TRUE)) {
                    _moca.executeCommand("publish data " + " where symbol = '"
                            + symbol + "'" + "   and time = '" + time + "'"
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
                            + "   and p_new = " + p_new + "   and p_last = "
                            + p_last + "   and p_low = " + p_low
                            + "   and p_open = " + p_open + "   and p_high = "
                            + p_high + "   and total = " + total
                            + "   and ins_dt = sysdate " + "|"
                            + "create record where table_name = 'hb_top10_data' and @* ");

                    for (int i = 0; i < trades.length(); i++) {
                        double trade_amt = ((JSONObject) trades.get(i))
                                .getDouble("amount");
                        double trade_price = ((JSONObject) trades.get(i))
                                .getDouble("price");
                        String trade_type = ((JSONObject) trades.get(i))
                                .getString("en_type");
                        _moca.executeCommand("publish data "
                                + " where symbol = '" + symbol + "'"
                                + "   and time = '" + time + "'"
                                + "   and amount = " + trade_amt
                                + "   and price = " + trade_price
                                + "   and type = '" + trade_type + "'"
                                + "   and ins_dt = sysdate " + "|"
                                + "create record where table_name = 'hb_trade_data' and @* ");
                    }
                }
                if (rtn_trade_data_flg != null
                        && rtn_trade_data_flg.equals(Boolean.TRUE)) {

                    EditableResults res = new SimpleResults();
                    res.addColumn("symbol", MocaType.STRING);
                    res.addColumn("time", MocaType.STRING);
                    res.addColumn("amount", MocaType.DOUBLE);
                    res.addColumn("price", MocaType.DOUBLE);
                    res.addColumn("type", MocaType.STRING);

                    for (int i = 0; i < trades.length(); i++) {
                        double trade_amt = ((JSONObject) trades.get(i))
                                .getDouble("amount");
                        double trade_price = ((JSONObject) trades.get(i))
                                .getDouble("price");
                        String trade_type = ((JSONObject) trades.get(i))
                                .getString("en_type");
                        res.addRow();
                        res.setStringValue("symbol", symbol);
                        res.setStringValue("time", time);
                        res.setDoubleValue("amount", trade_amt);
                        res.setDoubleValue("price", trade_price);
                        res.setStringValue("type", trade_type);
                    }
                    rs = res;
                } else {
                    rs = _moca.executeCommand("publish data "
                            + " where symbol = '" + symbol + "'"
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
                            + "   and p_new = " + p_new + "   and p_last = "
                            + p_last + "   and p_low = " + p_low
                            + "   and p_open = " + p_open + "   and p_high = "
                            + p_high + "   and total = " + total);
                }
            }
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
    private final String URL_TYPE1 = "getRealTimeRecord";
    private final String URL_TYPE2 = "getTop10Data";
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
    static Map<String, String> URLs = new HashMap<String, String>();
    private Logger _logger = LogManager.getLogger(HuobiCmd.class);
}