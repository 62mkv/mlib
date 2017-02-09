package com.sam.deal.api.strategy.huobi.buysell;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.deal.api.strategy.huobi.buysell.HBBPS1;
import com.sam.deal.api.strategy.huobi.buysell.IBuyPointSelector;
import com.sam.deal.api.stock.BoundArrayList;
import com.sam.deal.api.stock.IStock;
import com.sam.deal.api.stock.eSTOCKTREND;
import com.sam.deal.api.stock.huobi.HuoBiStock;
import com.sam.deal.api.stock.huobi.HuoBiStock.TickerData;
import com.sam.deal.api.stock.huobi.HuoBiStock.Top10Data;
import com.sam.deal.api.strategy.huobi.account.HuoBiCashAcnt;
import com.sam.deal.api.strategy.huobi.account.ICashAccount;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaResults;
import com.sam.moca.crud.CrudManager;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.util.MocaUtils;

public class HBBPS1 implements IBuyPointSelector {

	static Logger log = LogManager.getLogger(HBBPS1.class);
    private final MocaContext _moca;
    private final CrudManager _manager;
    private double boughtMny = 0;
    private double boughtLstPri = 0;
    private boolean wasStockInUnstableMode = false;
    private boolean firstGoToStockInUnstableMode = false;
    private double FIRST_SHARPMODE_BUY_RATIO = -1;
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            ServerUtils.setupDaemonContext("HBBPS1Test", true);
        }
        catch(SystemConfigurationException e) {
            e.printStackTrace();
        }
        HuoBiStock hs = new HuoBiStock("CHN", "BTC");
        hs.loadMarketData();
        HuoBiCashAcnt hac = new HuoBiCashAcnt("CHN", "BTC");
        HBBPS1 bp = new HBBPS1();
        bp.buyStock(hs, hac);
    }
    
    public HBBPS1()
    {
        _moca = MocaUtils.currentContext();
        _manager = MocaUtils.crudManager(_moca);
    }
    
	@Override
	public boolean isGoodBuyPoint(IStock s, ICashAccount ac) {
	    if (s instanceof HuoBiStock) {
	        HuoBiStock hs = (HuoBiStock) s;
	        //eSTOCKTREND et = hs.getStockTrend();
	        if (hs.isLstPriTurnaround(true) && hs.isLstPriUnderWaterLevel(0.1)) {
	            log.info("Stock trend turn up and at low 0.1 level, HBBPS1 return true.");
	            return true;
	        }
	        else {
                if (hs.getStockTrend() == eSTOCKTREND.SUP) {
                    log.info("Price trend is SUP, HBBPS1 return true!");
                    return true;
                }
	            log.info("HBBPS1 return false.");
	            return false;
	        }
	    }
	    else {
	        log.info("IStock is not HuoBiStock, return false.");
	        return false;
	    }
	}
	
	@Override
	public double getBuyQty(IStock s, ICashAccount ac) {
		// TODO Auto-generated method stub
        if (s instanceof HuoBiStock && ac instanceof HuoBiCashAcnt) {
            HuoBiStock hs = (HuoBiStock)s;
            HuoBiCashAcnt hbac = (HuoBiCashAcnt)ac;
            double buyablemny = hbac.getBuyableMny();
            double buyprice = hs.getCurPri();
            log.info("buyablemny:" + buyablemny);
            log.info("buyprice:" + buyprice);
            return buyablemny / buyprice;
        }
        log.info("IStock is not HuoBiStock(or HuoBiCashAcnt), return 0 as buyqty!");
		return 0;
	}
	
	@Override
	public boolean buyStock(IStock s, ICashAccount ac) {
	    if (s instanceof HuoBiStock && ac instanceof HuoBiCashAcnt) {
            HuoBiStock hs = (HuoBiStock)s;
            HuoBiCashAcnt hac = (HuoBiCashAcnt) ac;
            String ct = hs.getSymbol().substring(0, 3);
            //double buyqty = getBuyQty(s, ac);
            double buyableMny = ac.getBuyableMny();
            
            if (FIRST_SHARPMODE_BUY_RATIO < 0) {
                String polvar = (hac.getCoinType().equalsIgnoreCase("btc") ? "BTC" : "LTC");
                try {
                    MocaResults rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '" + polvar
                            + "' and polval ='FIRST_SHARPMODE_BUYSELL_RATIO' and grp_id = '----'");
                    
                    rs.next();
                    
                    FIRST_SHARPMODE_BUY_RATIO = rs.getDouble("rtflt1");
                    log.info("got FIRST_SHARPMODE_BUY_RATIO:" + FIRST_SHARPMODE_BUY_RATIO);
                }
                catch(Exception e) {
                    log.debug(e.getMessage());
                    FIRST_SHARPMODE_BUY_RATIO = 0.5;
                    log.info("got FIRST_SHARPMODE_BUY_RATIO default to 0.5");
                }
            }
            
            firstGoToStockInUnstableMode = false;
            if (hs.isStockUnstableMode() && !wasStockInUnstableMode) {
                
                log.info("stock first goes into SUP mode, buy with ratio ava money");
                wasStockInUnstableMode = true;
                firstGoToStockInUnstableMode = true;
                buyableMny = hac.getMaxAvaMny() * FIRST_SHARPMODE_BUY_RATIO;
                log.info("got buyablemny:" + buyableMny + " with FIRST_SHARPMODE_BUY_RATIO:" + FIRST_SHARPMODE_BUY_RATIO + " * ava mny:" + hac.getMaxAvaMny());
            }
            else if (!hs.isStockUnstableMode() && wasStockInUnstableMode) {
                log.info("reset wasStockInUnstableMode to false.");
                wasStockInUnstableMode = false;
            }
            boolean boughtComplete = false;
            if (buyableMny > 1) {
                TickerData tid = hs.geTickerData();
                int sz = tid.last_lst.size();
                double lstPri = tid.last_lst.get(sz - 1);
                try {
                    MocaResults rs = _moca.executeCommand(
                            "get top10 data where mk ='" + hac.getMarket() + "'" +
                            "  and ct = '" + hac.getCoinType() + "'" +
                            "   and sdf = 0 and rtdf = 0");
                    
                    rs.next();
                    
                    double [] top10SellAmt = new double[10];
                    double [] top10SellPri = new double[10];
                    top10SellAmt[0] = rs.getDouble("s_amt1");
                    top10SellAmt[1] = rs.getDouble("s_amt2");
                    top10SellAmt[2] = rs.getDouble("s_amt3");
                    top10SellAmt[3] = rs.getDouble("s_amt4");
                    top10SellAmt[4] = rs.getDouble("s_amt5");
                    top10SellAmt[5] = rs.getDouble("s_amt6");
                    top10SellAmt[6] = rs.getDouble("s_amt7");
                    top10SellAmt[7] = rs.getDouble("s_amt8");
                    top10SellAmt[8] = rs.getDouble("s_amt9");
                    top10SellAmt[9] = rs.getDouble("s_amt10");
                    
                    top10SellPri[0] = rs.getDouble("s_pri1");
                    top10SellPri[1] = rs.getDouble("s_pri2");
                    top10SellPri[2] = rs.getDouble("s_pri3");
                    top10SellPri[3] = rs.getDouble("s_pri4");
                    top10SellPri[4] = rs.getDouble("s_pri5");
                    top10SellPri[5] = rs.getDouble("s_pri6");
                    top10SellPri[6] = rs.getDouble("s_pri7");
                    top10SellPri[7] = rs.getDouble("s_pri8");
                    top10SellPri[8] = rs.getDouble("s_pri9");
                    top10SellPri[9] = rs.getDouble("s_pri10");
                    
                    double bpg = hs.getBigPriceDiff();
                    
                    if (boughtMny > 0 && Math.abs(boughtLstPri - lstPri) > bpg) {
                        log.info("Already boughtMny:" + boughtMny + ", reset to 0 as gap between boughtLstPri:" + boughtLstPri + " and lstPri:" + lstPri + "> bpg:" + bpg);
                        boughtMny = 0;
                    }
                    
                    for (int i = 0; i < 10; i++) {
                        double sellAmt = top10SellAmt[i];
                        double sellPri = top10SellPri[i];
                        
                        if (sellPri > lstPri + bpg) {
                            log.info("process sell[" + (i+1) + "], skip buy more as sellPri:" + sellPri + "> BigPriceDiff:" + bpg + " + lstPri:" + lstPri);
                            log.info("check firstGoToStockInUnstableMode:" + firstGoToStockInUnstableMode + " for force buy...");
                            if (!firstGoToStockInUnstableMode) {
                                break;
                            }
                            else {
                                log.info("still buy as stock fist goes to unstable mode.");
                            }
                        }
                        
                        double remainBuyMny = buyableMny - boughtMny;
                        double buyableAmt = remainBuyMny / sellPri;
                        double buyAmt = (buyableAmt > sellAmt ? sellAmt : buyableAmt);
                        try {
                        _moca.executeCommand("[select round(" + buyAmt + ", 4) buyAmt, round(" + (sellPri + 0.1) + ", 2) price  from dual]"
                                           + "|"
                                           + "create buy order"
                                           + " where market = '" + hac.getMarket() + "'"
                                           + "   and coinType ='" + hac.getCoinType() + "'"
                                           + "   and amount = @buyAmt "
                                           + "   and price = @price");
                        }
                        catch(Exception e) {
                            log.debug(e.getMessage());
                            log.debug("process buying sell[" + (i + 1) + "] failed, continue next sell.");
                            continue;
                        }
                        boughtMny += buyAmt * sellPri;
                        
                        if (boughtMny >= buyableMny) {
                            log.info("bought money:" + boughtMny + " success, which is bigger then buyableMny:" + buyableMny + " return true.");
                            boughtComplete = true;
                            boughtMny = 0;
                            break;
                        }
                    }
                    
                    if (boughtMny > 0) {
                        log.info("save boughtLstPri with lstPri:" + lstPri);
                        boughtLstPri = lstPri;
                    }
                }
                catch(Exception e) {
                    log.debug(e.getMessage());
                    return false;
                }
                return boughtComplete;
//                try {
//                    _moca.executeCommand("create market price buy order"
//                                       + " where market = 'CHN'"
//                                       + "   and coinType ='" + ct + "'"
//                                       + "   and amount = " + buyMny);
//                }
//                catch(Exception e) {
//                    log.debug(e.getMessage());
//                    return false;
//                }
//                return true;
            }
            else {
                log.info("buyableMny is less then 1, can not buy:" + buyableMny);
                return false;
            }
	    }
	    log.info("IStock is not HuoBiStock(or HuoBiCashAcnt), can not buy huobi!");
	    return false;
	}
}
