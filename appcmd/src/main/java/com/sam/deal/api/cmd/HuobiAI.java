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
import org.eclipse.jetty.util.log.Log;

import com.sam.moca.SimpleResults;
import com.sam.deal.api.cmd.HuobiCmd;
import com.sam.deal.api.huobi.HuobiService;
import com.sam.deal.api.stock.BoundArrayList;
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
public class HuobiAI {

    class PERIODDATA{
        boolean isBuyTurnaround;
        boolean isSellTurnaround;
        boolean isLostPeriod;
        boolean isCrossPeriod;
        double pricePct;
        double volumePct;
    }
    /**
     * Creates a new HuoBiService class
     * 
     * @param mocaCtx
     *            The MOCA context
     * @throws MocaException
     */
    public HuobiAI(MocaContext mocaCtx) throws MocaException {
        _moca = mocaCtx;
        _manager = MocaUtils.crudManager(_moca);
    }

    /* Return:
     */
    public MocaResults processPeriodStatData(String market, String coinType, int dayShift)
            throws MocaException {
        
        EditableResults res = new SimpleResults();
        res.addColumn("isBuyTurnaroundForMonth", MocaType.BOOLEAN);
        res.addColumn("isSellTurnaroundForMonth", MocaType.BOOLEAN);
        res.addColumn("isLostPeriodForMonth", MocaType.BOOLEAN);
        res.addColumn("isCrossPeriodForMonth", MocaType.BOOLEAN);
        res.addColumn("pricePctForMonth", MocaType.DOUBLE);
        res.addColumn("volumePctForMonth", MocaType.DOUBLE);
        
        res.addColumn("isBuyTurnaroundForWeek", MocaType.BOOLEAN);
        res.addColumn("isSellTurnaroundForWeek", MocaType.BOOLEAN);
        res.addColumn("isLostPeriodForWeek", MocaType.BOOLEAN);
        res.addColumn("isCrossPeriodForWeek", MocaType.BOOLEAN);
        res.addColumn("pricePctForWeek", MocaType.DOUBLE);
        res.addColumn("volumePctForWeek", MocaType.DOUBLE);
        
        res.addColumn("isBuyTurnaroundForDay", MocaType.BOOLEAN);
        res.addColumn("isSellTurnaroundForDay", MocaType.BOOLEAN);
        res.addColumn("isLostPeriodForDay", MocaType.BOOLEAN);
        res.addColumn("isCrossPeriodForDay", MocaType.BOOLEAN);
        res.addColumn("pricePctForDay", MocaType.DOUBLE);
        res.addColumn("volumePctForDay", MocaType.DOUBLE);
        
        res.addColumn("isBuyTurnaroundForHour", MocaType.BOOLEAN);
        res.addColumn("isSellTurnaroundForHour", MocaType.BOOLEAN);
        res.addColumn("isLostPeriodForHour", MocaType.BOOLEAN);
        res.addColumn("isCrossPeriodForHour", MocaType.BOOLEAN);
        res.addColumn("pricePctForHour", MocaType.DOUBLE);
        res.addColumn("volumePctForHour", MocaType.DOUBLE);
        
        PERIODDATA pdm = getPeriodData(market, coinType, "300", 6, dayShift); //month
        PERIODDATA pdw = getPeriodData(market, coinType, "200", 12, dayShift); //week
        PERIODDATA pdd = getPeriodData(market, coinType, "100", 45, dayShift); //day
        PERIODDATA pdh = getPeriodData(market, coinType, "060", 72, dayShift); //hour
        res.addRow();
        res.setBooleanValue("isBuyTurnaroundForMonth", pdm.isBuyTurnaround);
        res.setBooleanValue("isSellTurnaroundForMonth", pdm.isSellTurnaround);
        res.setBooleanValue("isLostPeriodForMonth", pdm.isLostPeriod);
        res.setBooleanValue("isCrossPeriodForMonth", pdm.isCrossPeriod);
        res.setDoubleValue("pricePctForMonth", pdm.pricePct);
        res.setDoubleValue("volumePctForMonth", pdm.volumePct);
        
        res.setBooleanValue("isBuyTurnaroundForWeek", pdw.isBuyTurnaround);
        res.setBooleanValue("isSellTurnaroundForWeek", pdw.isSellTurnaround);
        res.setBooleanValue("isLostPeriodForWeek", pdw.isLostPeriod);
        res.setBooleanValue("isCrossPeriodForWeek", pdw.isCrossPeriod);
        res.setDoubleValue("pricePctForWeek", pdw.pricePct);
        res.setDoubleValue("volumePctForWeek", pdw.volumePct);
        
        res.setBooleanValue("isBuyTurnaroundForDay", pdd.isBuyTurnaround);
        res.setBooleanValue("isSellTurnaroundForDay", pdd.isSellTurnaround);
        res.setBooleanValue("isLostPeriodForDay", pdd.isLostPeriod);
        res.setBooleanValue("isCrossPeriodForDay", pdd.isCrossPeriod);
        res.setDoubleValue("pricePctForDay", pdd.pricePct);
        res.setDoubleValue("volumePctForDay", pdd.volumePct);
        
        res.setBooleanValue("isBuyTurnaroundForHour", pdh.isBuyTurnaround);
        res.setBooleanValue("isSellTurnaroundForHour", pdh.isSellTurnaround);
        res.setBooleanValue("isLostPeriodForHour", pdh.isLostPeriod);
        res.setBooleanValue("isCrossPeriodForHour", pdh.isCrossPeriod);
        res.setDoubleValue("pricePctForHour", pdh.pricePct);
        res.setDoubleValue("volumePctForHour", pdh.volumePct);

        return res;
    }
    
    /* Period:
     * 001     1鍒嗛挓绾�
     * 005     5鍒嗛挓
     * 015     15鍒嗛挓
     * 030     30鍒嗛挓
     * 060     60鍒嗛挓
     * 100     鏃ョ嚎
     * 200     鍛ㄧ嚎
     * 300     鏈堢嚎
     * 400     骞寸嚎
     */
    private PERIODDATA getPeriodData(String market, String coinType, String period, int Len, int dayShift) throws MocaException{
        
        double [] open = new double[Len];
        double [] high = new double[Len];
        double [] low = new double[Len];
        double [] close = new double[Len];
        double [] volume = new double[Len];
        
        String cmd = "get period data " +
                " where market ='" + market + "'" +
                "   and coinType ='" + coinType + "'" +
                "   and period = '" + period + "'" +
                "   and length = " + (Len + 1 + (-dayShift));
        MocaResults rs = null;
        try {
            rs = _moca.executeCommand(cmd);
            
            int i = 0;
            while (rs.next() && i < Len) {
                open[i] = rs.getDouble("open");
                high[i] = rs.getDouble("high");
                low[i] = rs.getDouble("low");
                close[i] = rs.getDouble("close");
                volume[i] = rs.getDouble("volume");
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            _logger.debug("getScoreForPeriod Exception:" + e.getMessage());
            throw new MocaException(-1403, e.getMessage());
        }
        
        int idx1, idx2, idx3;
        idx1 = close.length - 1;
        idx2 = close.length - 2;
        idx3 = close.length - 3;
        boolean isClsTurnaroundforBuy = close[idx1] > close[idx2] && close[idx3] > close[idx2];
        boolean isClsTurnaroundforSell = close[idx1] < close[idx2] && close[idx3] < close[idx2];
        
        PERIODDATA pd = new PERIODDATA();
        
        if (isClsTurnaroundforBuy) {
            pd.isBuyTurnaround = true;
        }
        else {
            pd.isBuyTurnaround = false;
        }
        
        if (isClsTurnaroundforSell) {
            pd.isSellTurnaround = true;
        }
        else {
            pd.isSellTurnaround = false;
        }
        
        double maxv = Double.MIN_VALUE;
        double minv = Double.MAX_VALUE;
        
        for (int i = 0; i < close.length; i++) {
            if (maxv < close[i]) {
                maxv = close[i];
            }
            if (minv > close[i]) {
                minv = close[i];
            }
        }
        
        double pct = (close[close.length - 1] - minv) / (maxv - minv);
        
        Log.info("maxv:" + maxv + ", minv:" + minv + ", pricePct:" + pct);
        
        pd.pricePct = pct;
        
        maxv = Double.MIN_VALUE;
        minv = Double.MAX_VALUE;
        
        for (int i = 0; i < volume.length; i++) {
            if (maxv < volume[i]) {
                maxv = volume[i];
            }
            if (minv > volume[i]) {
                minv = volume[i];
            }
        }
        
        pct = (volume[volume.length - 1] - minv) / (maxv - minv);
        
        Log.info("maxv:" + maxv + ", minv:" + minv + ", volumePct:" + pct);
        
        pd.volumePct = pct;
        
        double det_open_close = close[close.length - 1] - open[open.length - 1];
        double det_high_low = high[high.length - 1] - low[low.length - 1];
        
        if (det_open_close < 0) {
            pd.isLostPeriod = true;
        }
        else {
            pd.isLostPeriod = false;
        }
        
        Log.info("lst close pri:" + close[close.length - 1] + ", lst open pri:" + open[open.length - 1]);
        Log.info("lst high pri:" + high[high.length - 1] + ", lst low pri:" + low[low.length - 1]);
        Log.info("det_open_close / det_high_low:" + det_open_close / det_high_low);
        if (Math.abs((det_open_close / det_high_low)) < 0.1) {
            pd.isCrossPeriod = true;
        }
        else {
            pd.isCrossPeriod = false;
        }
        
        return pd;
    }

    // Private fields
    private final MocaContext _moca;
    private final CrudManager _manager;
    private Logger _logger = LogManager.getLogger(HuobiAI.class);
}
