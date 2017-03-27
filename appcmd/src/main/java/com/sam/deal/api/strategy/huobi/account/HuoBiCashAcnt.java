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
	private String mk;
	private String ct;
	private double minMnyPerDeal;
	private double maxMnyPerDeal;
	private double total;
	private double priGap;
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
	private double maxStockPct = 0.0;
	private double minStockPct = 0.0;
	private int moneyLevels = 0;
	private int minMaxDays = 0;
	
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
        
	    HuoBiCashAcnt acnt = new HuoBiCashAcnt("chn", "btc");
	}

	public HuoBiCashAcnt(String market, String coinType) {

        _moca = MocaUtils.currentContext();
        _manager = MocaUtils.crudManager(_moca);
        
	    actId = "HuoBiAccount";
	    minMnyPerDeal = maxMnyPerDeal = 0;
	    priGap = 0;
	    mk = market;
	    ct = coinType;
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
        
        String polvar = (ct.equalsIgnoreCase("btc") ? "BTC" : "LTC");
        try {
            rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '" + polvar
                               + "' and polval ='MNYPERDEAL' and grp_id = '----'");
            rs.next();
            minMnyPerDeal = rs.getDouble("rtflt1");
            maxMnyPerDeal = rs.getDouble("rtflt2");
            _logger.info("got policy, minMnyPerDeal:" + minMnyPerDeal + ", maxMnyPerDeal:" + maxMnyPerDeal);
        } catch (MocaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.error(e.getMessage());
            _logger.info("Get policy MNYPERDEAL error, use default value.");
            minMnyPerDeal = 2000;
            maxMnyPerDeal = 8000;
        }
        
        return true;
	}
	
	public String getMarket() {
	    return mk;
	}
	
	public String getCoinType() {
	    return ct;
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
	
	/*
	 * This function will calcluate the count
	 * of latest trade records.
	 */
	public int getLastBuySellGapCnt(String coinType, boolean forBuy) {
	    
	    MocaResults rs = null;
	    int bscnt = 0;
	    String type = "";
        try {
            rs = _moca.executeCommand("[select count(distinct round(round(processed_price)/10)) cnt,                              " +
                                      "        type                                        " +
                                      "   from hb_buysell_data                             " +
                                      "  where id >= (select min(t1.id) " +
                                      "                 from hb_buysell_data t1 " +
                                      "                where t1.type = (select t2.type " +
                                      "                                   from hb_buysell_data t2 " +
                                      "                                  where t2.id = (select max(t3.id) " +
                                      "                                                   from hb_buysell_data t3" +
                                      "                                                  where t3.reacod like 'GoodPrice%')" +
                                      "                                ) " +
                                      "                  and not exists (select 'x' " +
                                      "                                    from hb_buysell_data t4 " +
                                      "                                   where t4.id > t1.id " +
                                      "                                     and t4.reacod like 'GoodPrice%' " +
                                      "                                     and t4.type <> t1.type) " +
                                      "                  and t1.reacod like 'GoodPrice%' " +
                                      "               ) " +
                                      "    and ins_dt > sysdate - 12/24.0 " +//if trade within 12 hours.
                                      "    and reacod like 'GoodPrice%'" +
                                      "  group by type]");
            rs.next();
            type = rs.getString("type");
            bscnt = rs.getInt("cnt");
            _logger.info("got cnt:" + bscnt + ", type:" + type);
            
            _logger.info("\ntype:" + type +
                         "\nforBuy:" + forBuy +
                         "\ncnt:" + bscnt);
            //want buy again
            if (forBuy && !type.equals("1")) {
            	_logger.info("reset bscnt to 0 as forBuy, but type is not 1(buy)");
            	bscnt = 0;
            }
            else if (!forBuy && !type.equals("2")) {
            	_logger.info("reset bscnt to 0 as forSell, but type is not 2(sell)");
            	bscnt = 0;
            }
        } catch (MocaException e) {
            // TODO Auto-generated catch block
            _logger.error(e.getMessage());
            _logger.info("no buysell data found, use 0.");
        }
        
        _logger.info("return bscnt:" + bscnt);
        return bscnt;
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
        if (available_cny_display > minMnyPerDeal) {
            int cnt = getLastBuySellGapCnt(ct, true);
            double buyableMny = minMnyPerDeal;
            
            _logger.info("\navailable_cny_display:" + available_cny_display +
                         "\n minMnyPerDeal:" + minMnyPerDeal +
                         "\n cnt:" + cnt +
                         "\n cnt * minMnyPerDeal:" + cnt * minMnyPerDeal);
            buyableMny = minMnyPerDeal + cnt * minMnyPerDeal;
            buyableMny = (buyableMny > available_cny_display ? available_cny_display : buyableMny);
            buyableMny = (buyableMny > maxMnyPerDeal ? maxMnyPerDeal : buyableMny);
            return buyableMny;
        } else {
            _logger.info("available_cny_display less than minMnyPerDeal, use available_cny_display:" + available_cny_display);
            return available_cny_display;
        }
    }
    
    @Override
    public double getSellableMny() {

        int cnt = getLastBuySellGapCnt(ct, false);
        double sellableMny = minMnyPerDeal;
        
        sellableMny = minMnyPerDeal + cnt * minMnyPerDeal;
        sellableMny = (sellableMny > maxMnyPerDeal ? maxMnyPerDeal : sellableMny);
        
        _logger.info("\n minMnyPerDeal:" + minMnyPerDeal +
        		     "\n maxMnyPerDeal:" + maxMnyPerDeal +
                     "\n cnt:" + cnt +
                     "\n cnt * minMnyPerDeal:" + cnt * minMnyPerDeal +
                     "\n sellableMny:" + sellableMny);
        
        return sellableMny;
    }
    
    @Override
    public double getUsedMny() {
        return frozen_cny_display;
    }
    
    @Override
    public void refreshAccount() {
        loadAccount();
    }
    
    @Override
    public void printAcntInfo() {
        _logger.info("AccountID:" + actId);
        _logger.info("minMnyPerDeal:" + minMnyPerDeal);
        _logger.info("maxMnyPerDeal:" + maxMnyPerDeal);
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

    @Override
    public double getMaxStockPct() {
        // TODO Auto-generated method stub
        if (maxStockPct <= 0) {
            MocaResults rs = null;
            String polvar = (ct.equalsIgnoreCase("btc") ? "BTC" : "LTC");
            try {
                rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '" + polvar
                                   + "' and polval ='STOCKPCTLVL' and grp_id = '----'");
                rs.next();
                minStockPct = rs.getDouble("rtflt1");
                maxStockPct = rs.getDouble("rtflt2");
                moneyLevels = rs.getInt("rtnum1");
                minMaxDays = rs.getInt("rtnum2");
                _logger.info("got policy, minStockPct:" + minStockPct + ", maxStockPct:" + maxStockPct + ", moneyLevels:" + moneyLevels + ", minMaxDays:" + minMaxDays);
            } catch (MocaException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                _logger.error(e.getMessage());
                _logger.info("Get policy STOCKPCTLVL error, use default value 0.2 and 0.8, and moneyLevels 4.");
                minStockPct = 0.2;
                maxStockPct = 0.8;
                moneyLevels = 4;
                minMaxDays = 30;
            }
        }
        return maxStockPct;
    }
    
    @Override
    public int getMoneyLevels() {
        // TODO Auto-generated method stub
        if (maxStockPct <= 0) {
            getMaxStockPct();
        }
        return moneyLevels;
    }
    
    public boolean StockInhandLevelOverExpect(double stockLstPri) {
        double stockMny = getAvaQty(getCoinType()) * stockLstPri;
        double avaMny = getMaxAvaMny();
        double totalAsset = stockMny + avaMny;
        
        double actPct = stockMny / totalAsset;
        double expPct = getExpStockPct(stockLstPri);
        
        log.info("actual stock inhand level:" + actPct + ", expPct:" + expPct);
        if (actPct - expPct >= 1.0 / moneyLevels) {
            log.info("Stock inhand level " + 1.0 / moneyLevels + " over expPct value, return true");
            return true;
        }
        
        log.info("Stock inhand level NOT " + 1.0 / moneyLevels + " over expPct value, return false");
        return false;
    }
    
    public boolean StockInhandLevelUnderExpect (double stockLstPri) {
        double minPct = getMinStockPct();
        double stockMny = getAvaQty(getCoinType()) * stockLstPri;
        double avaMny = getMaxAvaMny();
        double totalAsset = stockMny + avaMny;
        
        double actPct = stockMny / totalAsset;
        double expPct = getExpStockPct(stockLstPri);
        
        log.info("actual stock inhand level:" + actPct + ", expPct:" + expPct);
        if (actPct + 1.0 / moneyLevels < expPct) {
            log.info("Stock inhand level " + 1.0 / moneyLevels + " less expPct value, return true");
            return true;
        }
        
        log.info("Stock inhand level NOT " + 1.0 / moneyLevels + " less  expPct value, return false");
        return false;
    }
    
    @Override
    public double getExpStockPct(double curStockPri) {
        // TODO Auto-generated method stub
            MocaResults rs = null;
            double maxPri = 0.0;
            double minPri = 0.0;
            double expPct = 0.5;
            
            if (maxStockPct <= 0) {
            	getMaxStockPct();
            }
            double maxExpStockPctVal = maxStockPct;
            double minExpStockPctVal = minStockPct;
            
            try {
                rs = _moca.executeCommand("[select nvl(max(processed_price), 0) maxPri, nvl(min(processed_price), 0) minPri "
                		                   + "from hb_buysell_data "
                		                   + "where processed_price > 0 "
                		                   + "  and ins_dt > sysdate - " + minMaxDays + "] "); // get minMaxDays days max/min
                rs.next();
                minPri = rs.getDouble("minPri");
                maxPri = rs.getDouble("maxPri");
                _logger.info("got policy, minPri:" + minPri + ", maxPri:" + maxPri);
            } catch (MocaException e) {
                // TODO Auto-generated catch block
                _logger.error(e.getMessage());
            }
            
            if (minPri > 0 && maxPri > 0) {
            	expPct = 1.0 - (curStockPri - minPri) / (maxPri - minPri);
            	expPct = minExpStockPctVal + expPct * (maxExpStockPctVal - minExpStockPctVal);
            	_logger.info("curStockPri:" + curStockPri + " is " + (curStockPri - minPri) / (maxPri - minPri) + " at:[" + minPri + "," + maxPri + "]");
            	_logger.info("expPct should be 1 - " + (curStockPri - minPri) / (maxPri - minPri) + ":" + (1 - (curStockPri - minPri) / (maxPri - minPri)) + " at:[" + minExpStockPctVal + "," + maxExpStockPctVal + "]");
            	_logger.info("got expPct:" + expPct);
            }
            else {
            	expPct = (maxExpStockPctVal + minExpStockPctVal) / 2.0;
            	_logger.info("use expPct as half of:[" + minExpStockPctVal + "," + maxExpStockPctVal + "]:" + expPct);
            }
        return expPct;
    }

    @Override
    public double getMinStockPct() {
        // TODO Auto-generated method stub
        if (maxStockPct <= 0) {
            getMaxStockPct();
        }
        return minStockPct;
    }
}
