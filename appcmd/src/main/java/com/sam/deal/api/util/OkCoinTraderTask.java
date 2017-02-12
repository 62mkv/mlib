/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.deal.api.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.deal.api.cmd.HuobiTrader;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.util.MocaUtils;
import com.sam.moca.util.Options;
import com.sam.moca.util.OptionsException;

/**
 * Task to run a socket server on some port.  The port and processing command are passed as "command line" arguments
 * to the task.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class OkCoinTraderTask implements Runnable {
    
   public static void main(String [] args) {
        args = new String[8];
        args[0] = "-k";
        args[1] = "CHN";
        args[2] = "-t";
        args[3] = "btc";
        try {
            ServerUtils.setupDaemonContext("OkCoinTraderTaskTest", true);
        }
        catch(SystemConfigurationException e) {
            e.printStackTrace();
        }
        
        OkCoinTraderTask hbt;
        try {
            hbt = new OkCoinTraderTask(args);
            hbt.run();
        } catch (OptionsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public OkCoinTraderTask(String[] args) throws OptionsException {
//        for(int i=0;i<args.length;i++) {
//            _log.info("arg"+i+":" + args[i]);
//        }
        moca = MocaUtils.currentContext();
        Options opt = Options.parse("k:t:", args);
        _market = opt.getArgument('k');
        _coinType = opt.getArgument('t');
        _log.info("_market:" + _market + "\n_coinType:" + _coinType);
    }

    @Override
    public void run() {
        _log.info("Start running task OkCoinTraderTask...");
        try {
            moca.executeCommand("process okcoin trade where mk = '" + _market + "'" +
                                "  and ct ='" + _coinType + "'");
        }
        catch (Exception e) {
            e.printStackTrace();
            _log.error(e.getMessage());
            return;
        }
        _log.info("End running task OkCoinTraderTask...");
    }
    
    private MocaContext moca = null;
    private final String _market;
    private final String _coinType;
    private final static Logger _log = LogManager.getLogger(OkCoinTraderTask.class); 
}
