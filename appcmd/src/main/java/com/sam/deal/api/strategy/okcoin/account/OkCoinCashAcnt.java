package com.sam.deal.api.strategy.okcoin.account;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.deal.api.strategy.huobi.account.ICashAccount;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.crud.CrudManager;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.util.MocaUtils;

public class OkCoinCashAcnt implements ICashAccount {

	static Logger _logger = LogManager.getLogger(OkCoinCashAcnt.class);
	private String actId;
	private String mk;
	private String ct;
	private double minMnyPerDeal = 0;
	private double maxMnyPerDeal = 0;
	private double total;
	private double priGap;
	private double net;
	private double free_usd;
	private double free_ltc;
	private double free_btc;
	private double freezed_usd;
	private double freezed_ltc;
	private double freezed_btc;
	
    private final MocaContext _moca;
    private final CrudManager _manager;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

        try {
            ServerUtils.setupDaemonContext("OkCoinAccountTest", true);
        }
        catch(SystemConfigurationException e) {
            e.printStackTrace();
        }
        
	    OkCoinCashAcnt acnt = new OkCoinCashAcnt("chn", "btc");
	}

	public OkCoinCashAcnt(String market, String coinType) {

        _moca = MocaUtils.currentContext();
        _manager = MocaUtils.crudManager(_moca);
        
	    actId = "OkCoinAccount";
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
            rs = _moca.executeCommand("get account info for oc where market = '" + mk + "' and coinType = '" + ct + "'");
            
            rs.next();
            total = rs.getDouble("total");
            net = rs.getDouble("net");
            free_usd = rs.getDouble("free_usd");
            free_ltc = rs.getDouble("free_ltc");
            free_btc = rs.getDouble("free_btc");
            freezed_usd = rs.getDouble("freezed_usd");
            freezed_btc = rs.getDouble("freezed_btc");
            freezed_ltc = rs.getDouble("freezed_ltc");
            
        }
        catch (MocaException e) {
            e.printStackTrace();
            _logger.debug("Exception:" + e.getMessage());
        }
        
        String polvar = (ct.equalsIgnoreCase("btc") ? "BTC" : "LTC");
        if (maxMnyPerDeal <= 0) {
            try {
                rs = _moca.executeCommand("list policies where polcod ='OKCOIN' and polvar = '" + polvar
                                   + "' and polval ='MNYPERDEAL' and grp_id = '----'");
                rs.next();
                minMnyPerDeal = rs.getInt("rtflt1");
                maxMnyPerDeal = rs.getInt("rtflt2");
                _logger.info("got policy, minMnyPerDeal:" + minMnyPerDeal + ", maxMnyPerDeal:" + maxMnyPerDeal);
            } catch (MocaException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                _logger.error(e.getMessage());
                _logger.info("Get policy MNYPERDEAL error, use default value.");
                minMnyPerDeal = 200;
                maxMnyPerDeal = 500;
            }
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
	    loadAccount();
	    if (coinType.equalsIgnoreCase("cny")) {
	        return free_usd;
	    }
	    else if (coinType.equalsIgnoreCase("ltc")) {
	        return free_ltc;
	    }
	    else {
	        return free_btc;
	    }
	}
	
	public double getFznQty(String coinType) {
	    loadAccount();
	     if (coinType.equalsIgnoreCase("cny")) {
	         return freezed_usd;
	     }
	     else if (coinType.equalsIgnoreCase("ltc")) {
	         return freezed_ltc;
	     }
	     else {
	         return freezed_btc;
	     }
	 }
	public double getLoanQty(String coinType) {
	    loadAccount();
	    if (coinType.equalsIgnoreCase("cny")) {
	        return 0;
	    }
	    else if (coinType.equalsIgnoreCase("ltc")) {
	        return 0;
	    }
	    else {
	        return 0;
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
            rs = _moca.executeCommand("[select count(id) cnt,                              " +
                                      "        type                                        " +
                                      "   from oc_buysell_data                             " +
                                      "  where id >= (select min(t1.id) " +
                                      "                 from oc_buysell_data t1 " +
                                      "                where t1.type = (select t2.type " +
                                      "                                   from oc_buysell_data t2 " +
                                      "                                  where t2.id = (select max(t3.id) " +
                                      "                                                   from oc_buysell_data t3)" +
                                      "                                ) " +
                                      "                  and not exists (select 'x' " +
                                      "                                    from oc_buysell_data t4 " +
                                      "                                   where t4.id > t1.id " +
                                      "                                     and t4.type <> t1.type) " +
                                      "               ) " +
                                      "    and ins_dt > sysdate - 12/24.0 " +//if trade within 12 hours.
                                      "  group by type]");
            rs.next();
            type = rs.getString("type");
            bscnt = rs.getInt("cnt");
            _logger.info("got cnt:" + bscnt + ", type:" + type);
            
            _logger.info("\ntype:" + type +
                         "\nforBuy:" + forBuy +
                         "\ncnt:" + bscnt);
            //want buy again
            if (forBuy && !type.equals("buy")) {
            	_logger.info("reset bscnt to 0 as forBuy, but type is not buy");
            	bscnt = 0;
            }
            else if (!forBuy && !type.equals("sell")) {
            	_logger.info("reset bscnt to 0 as forSell, but type is not sell");
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
        loadAccount();
        return free_usd;
    }
    
    @Override
    public double getBuyableMny() {
        loadAccount();
        if (free_usd > minMnyPerDeal) {
            int cnt = getLastBuySellGapCnt(ct, true);
            double buyableMny = minMnyPerDeal;
            int maxCnt = 3;
            
            if (cnt > maxCnt) {
                _logger.info("getLastBuySellGapCnt return > maxCnt:" + maxCnt + ", use it as max.");
                cnt = maxCnt;
            }
            
            double det = (maxMnyPerDeal - minMnyPerDeal) / maxCnt;
            _logger.info("\n free_usd:" + free_usd +
                         "\n minMnyPerDeal:" + minMnyPerDeal +
                         "\n cnt:" + cnt +
                         "\n (maxMnyPerDeal - minMnyPerDeal) / " + maxCnt + ":" + det +
                         "\n cnt * ((maxMnyPerDeal - minMnyPerDeal) / " + maxCnt + ":" + cnt * det);
            buyableMny = minMnyPerDeal + cnt * det;
            buyableMny = (buyableMny > free_usd ? free_usd : buyableMny);
            return buyableMny;
        } else {
            _logger.info("free_usd less than minMnyPerDeal, use free_usd:" + free_usd);
            return free_usd;
        }
    }
    
    @Override
    public double getSellableMny() {
        loadAccount();

        int cnt = getLastBuySellGapCnt(ct, false);
        double sellableMny = minMnyPerDeal;
        int maxCnt = 3;
        
        if (cnt > maxCnt) {
            _logger.info("getLastBuySellGapCnt return > maxCnt:" + maxCnt + ", use it as max.");
            cnt = maxCnt;
        }
        
        double det = (maxMnyPerDeal - minMnyPerDeal) / maxCnt;
        
        _logger.info("\n minMnyPerDeal:" + minMnyPerDeal +
                     "\n cnt:" + cnt +
                     "\n (maxMnyPerDeal - minMnyPerDeal) / " + maxCnt + ":" + det +
                     "\n cnt * ((maxMnyPerDeal - minMnyPerDeal) / " + maxCnt + ":" + cnt * det);
        sellableMny = minMnyPerDeal + cnt * det;
        return sellableMny;
    }
    
    @Override
    public double getUsedMny() {
        loadAccount();
        return freezed_usd;
    }
    
    @Override
    public void printAcntInfo() {
        _logger.info("AccountID:" + actId);
        _logger.info("minMnyPerDeal:" + minMnyPerDeal);
        _logger.info("maxMnyPerDeal:" + maxMnyPerDeal);
        _logger.info("total:" + total);
        _logger.info("net:" + net);
        _logger.info("free_usd:" + free_usd);
        _logger.info("free_ltc:" + free_ltc);
        _logger.info("free_btc:" + free_btc);
        _logger.info("freezed_usd:" + freezed_usd);
        _logger.info("freezed_btc:" + freezed_btc);
        _logger.info("freezed_ltc:" + freezed_ltc);
    }
}
