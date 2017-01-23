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
import com.sam.deal.api.cmd.HuobiTrader;
import com.sam.deal.api.demo.huobi.HuobiService;
import com.sam.deal.api.stock.huobi.HuoBiStock;
import com.sam.deal.api.strategy.huobi.HuoBiTradeStrategyImp;
import com.sam.deal.api.strategy.huobi.account.HuoBiCashAcnt;
import com.sam.deal.api.strategy.huobi.buysell.HBBPS1;
import com.sam.deal.api.strategy.huobi.buysell.HBSPS1;
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
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
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
 * A HuoBi trader Service class 
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
public class HuobiTrader {

    static HuoBiTradeStrategyImp hbts= null;
    /**
     * Creates a new HuoBiService class
     * 
     * @param mocaCtx
     *            The MOCA context
     * @throws MocaException
     */
    public HuobiTrader(MocaContext mocaCtx) throws MocaException {
        _moca = mocaCtx;
        _manager = MocaUtils.crudManager(_moca);
    }

    public static void main(String[] args) throws MocaException {
        // TODO Auto-generated method stub
        
        MocaContext mc = null;
        try {
            ServerUtils.setupDaemonContext("StockTest", true);
        }
        catch(SystemConfigurationException e) {
            e.printStackTrace();
        }
        
        mc = MocaUtils.currentContext();
        HuobiTrader s = new HuobiTrader(mc);
        s.performTrade("chn", "btc", 10, 2);
    }
    
    /**
     * @param market:
     *            'US', 'CHN'
     * @param coinType:
     *            'btc','ltc'
     */
    @SuppressWarnings("static-access")
    public void performTrade(String market,
                             String coinType,
                             Integer loopCount,
                             Integer loopGap) throws MocaException {
        _logger.info("Now performTrade");
        HBBPS1 buypointselector = new HBBPS1();
        HBSPS1 sellpointselector1 = new HBSPS1();
        HuoBiCashAcnt huobicashacnt = new HuoBiCashAcnt();
        HuoBiStock huobistock = new HuoBiStock(100);
        hbts = new HuoBiTradeStrategyImp(market, coinType, huobistock, buypointselector, sellpointselector1, huobicashacnt);
        
        boolean doInfiniteLoop = (loopCount == 0);
        int lg = loopGap;
        
        if (doInfiniteLoop) {
            while (true) {
                
                try {
                    Thread.currentThread().sleep(lg*1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    _logger.error(e.getMessage());
                }
                
                hbts.startTrading();
            }
        }
        else {
            _logger.info("Go to sleep for " + lg + " seconds.");
            for (int i = 0; i < loopCount; i++) {
                //_moca.executeCommand("go to sleep where time = " + lg);
                try {
                    Thread.currentThread().sleep(lg*1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    _logger.error(e.getMessage());
                }
                hbts.startTrading();
            }
        }
    }

    // Private fields
    private final MocaContext _moca;
    private final CrudManager _manager;
    private Logger _logger = LogManager.getLogger(HuobiTrader.class);
}
