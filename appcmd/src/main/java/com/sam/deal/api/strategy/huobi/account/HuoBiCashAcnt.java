package com.sam.deal.api.strategy.huobi.account;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.deal.api.strategy.huobi.account.HuoBiCashAcnt;
import com.sam.deal.api.strategy.huobi.account.ICashAccount;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.crud.CrudManager;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.util.MocaUtils;

public class HuoBiCashAcnt implements ICashAccount {

	static Logger _logger = LogManager.getLogger(HuoBiCashAcnt.class);
	private String actId;
	private double initMny;
	private int splitNum;
	private double total;
	private double net_asset;
	private double available_cny_display;
	private double available_ltc_display;
	private double available_btc_display;
	private double frozen_cny_display;
	private double frozen_btc_display;
	private double frozen_ltc_display;
	private double loan_cny_display;
	private double loan_btc_display;
	private double loan_ltc_display;
	
    private final MocaContext _moca;
    private final CrudManager _manager;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

        try {
            ServerUtils.setupDaemonContext("HuoBiAccountTest", true);
        }
        catch(SystemConfigurationException e) {
            e.printStackTrace();
        }
        
	    HuoBiCashAcnt acnt = new HuoBiCashAcnt();
	}

	public HuoBiCashAcnt() {

        _moca = MocaUtils.currentContext();
        _manager = MocaUtils.crudManager(_moca);
        
	    actId = "HuoBiAccount";
	    initMny = 100;
	    splitNum = 4;
	    loadAccount();
	    printAcntInfo();
	}

	private boolean loadAccount() {

        MocaResults rs = null;
        try {
            rs = _moca.executeCommand("get account info ");
            
            rs.next();
            total = rs.getDouble("total");
            net_asset = rs.getDouble("net_asset");
            available_cny_display = rs.getDouble("available_cny_display");
            available_ltc_display = rs.getDouble("available_ltc_display");
            available_btc_display = rs.getDouble("available_btc_display");
            frozen_cny_display = rs.getDouble("frozen_cny_display");
            frozen_btc_display = rs.getDouble("frozen_btc_display");
            frozen_ltc_display = rs.getDouble("frozen_ltc_display");
            loan_cny_display = rs.getDouble("loan_cny_display");
            loan_btc_display = rs.getDouble("loan_btc_display");
            loan_ltc_display = rs.getDouble("loan_ltc_display");
            
        }
        catch (MocaException e) {
            e.printStackTrace();
            _logger.debug("Exception:" + e.getMessage());
        }
        return true;
	}
	
	public double getAvaQty(String coinType) {
	    if (coinType.equalsIgnoreCase("cny")) {
	        return available_cny_display;
	    }
	    else if (coinType.equalsIgnoreCase("ltc")) {
	        return available_ltc_display;
	    }
	    else {
	        return available_btc_display;
	    }
	}
	
	public double getFznQty(String coinType) {
	     if (coinType.equalsIgnoreCase("cny")) {
	         return frozen_cny_display;
	     }
	     else if (coinType.equalsIgnoreCase("ltc")) {
	         return frozen_ltc_display;
	     }
	     else {
	         return frozen_btc_display;
	     }
	 }
	public double getLoanQty(String coinType) {
	    if (coinType.equalsIgnoreCase("cny")) {
	        return loan_cny_display;
	    }
	    else if (coinType.equalsIgnoreCase("ltc")) {
	        return loan_ltc_display;
	    }
	    else {
	        return loan_btc_display;
	    }
	 }

	@Override
	public double getInitMny() {
		return initMny;
	}

	@Override
	public int getSplitNum() {
		return splitNum;
	}

	@Override
    public String getAccountID() {
        return this.actId;
    }
    
    @Override
    public double getMaxAvaMny() {
        return available_cny_display;
    }
    
    @Override
    public double getBuyableMny() {
        if (available_cny_display > initMny / splitNum) {
            return initMny / splitNum;
        } else {
            return available_cny_display;
        }
    }
    
    @Override
    public double getUsedMny() {
        return frozen_cny_display;
    }
    
    @Override
    public void printAcntInfo() {
        _logger.info("AccountID:" + actId);
        _logger.info("initMny:" + initMny);
        _logger.info("splitNum:" + splitNum);
        _logger.info("total:" + total);
        _logger.info("net_asset:" + net_asset);
        _logger.info("available_cny_display:" + available_cny_display);
        _logger.info("available_ltc_display:" + available_ltc_display);
        _logger.info("available_btc_display:" + available_btc_display);
        _logger.info("frozen_cny_display:" + frozen_cny_display);
        _logger.info("frozen_btc_display:" + frozen_btc_display);
        _logger.info("frozen_ltc_display:" + frozen_ltc_display);
        _logger.info("loan_cny_display:" + loan_cny_display);
        _logger.info("loan_btc_display:" + loan_btc_display);
        _logger.info("loan_ltc_display:" + loan_ltc_display);
    }
}
