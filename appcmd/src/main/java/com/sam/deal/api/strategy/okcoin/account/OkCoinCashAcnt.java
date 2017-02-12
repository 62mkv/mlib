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
	private double minMnyPerDeal;
	private double maxMnyPerDeal;
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
	    minMnyPerDeal = maxMnyPerDeal = 100;
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
            _logger.info("Get policy MNYPERDEAL error, use default value 1000.");
            minMnyPerDeal = 1000;
            maxMnyPerDeal = 1000;
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
	 * of latest trade record to the market price.
	 */
	public int getLastBuySellGapCnt(String coinType, boolean forBuy) {
	    
	    MocaResults rs = null;
	    int bscnt = 0;
	    String type = "";
	    double lstDealPri = 0;
        try {
            rs = _moca.executeCommand("[select avg_price lstDealPri,                              " +
                                      "        type                                                     " +
                                      "   from oc_buysell_data                                          " +
                                      "  where id = (select max (id) from oc_buysell_data)              " +
                                      "    and ins_dt > sysdate - 12/24.0]            ");//if trade within 12 hours.
            rs.next();
            lstDealPri = rs.getDouble("lstDealPri");
            type = rs.getString("type");
            _logger.info("got lstDealPri:" + lstDealPri + ", type:" + type);
            
            if (priGap <= 0) {
                String polvar = (ct.equalsIgnoreCase("btc") ? "BTC" : "LTC");
                try {
                    rs = _moca.executeCommand("list policies where polcod ='OKCOIN' and polvar = '" + polvar
                                       + "' and polval ='LAST_PRICE_BOX_GAP' and grp_id = '----'");
                    rs.next();
                
                    priGap = rs.getDouble("rtflt2");
                    _logger.info("got policy, LAST_PRICE_BOX_GAP_MAX:" + priGap);
                } catch (MocaException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    _logger.error(e.getMessage());
                    if (polvar.equalsIgnoreCase("BTC")) {
                        priGap = 100;
                        _logger.info("get policy error, use default LAST_PRICE_BOX_GAP_MAX:" + priGap);
                    }
                    else {
                        priGap = 0.5;
                        _logger.info("get policy error, use default LAST_PRICE_BOX_GAP_MAX:" + priGap);
                    }
                }
            }
            
            double mrkLstPri = lstDealPri;
            try {
                rs = _moca.executeCommand(
                        "get trade data for oc where mk ='" + mk + "'" +
                        "  and ct = '" + ct + "'" +
                        "   and sdf = 0");
                
                rs.next();
                mrkLstPri = rs.getDouble("price");
            }
            catch (MocaException e) {
                e.printStackTrace();
                _logger.debug("Exception:" + e.getMessage());;
            }
            
            _logger.info("\ntype:" + type +
                         "\nforBuy:" + forBuy +
                         "\nmrkLstPri:" + mrkLstPri +
                         "\nlstDealPri:" + lstDealPri +
                         "\npriGap:" + priGap);
            //want buy
            if (forBuy) {
                if (mrkLstPri >= lstDealPri) {
                    bscnt = 0;
                }
                else {
                    bscnt = (int)((lstDealPri - mrkLstPri) / priGap);
                }
            }
            else if (!forBuy) {
                if (mrkLstPri <= lstDealPri) {
                    bscnt = 0;
                }
                else {
                    bscnt = (int)((mrkLstPri - lstDealPri) / priGap);
                }
            }
        } catch (MocaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
