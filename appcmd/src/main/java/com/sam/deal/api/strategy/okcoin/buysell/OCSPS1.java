package com.sam.deal.api.strategy.okcoin.buysell;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.deal.api.strategy.huobi.buysell.ISellPointSelector;
import com.sam.deal.api.stock.IStock;
import com.sam.deal.api.stock.eSTOCKTREND;
import com.sam.deal.api.stock.okcoin.OkCoinStock;
import com.sam.deal.api.stock.okcoin.OkCoinStock.TickerData;
import com.sam.deal.api.strategy.okcoin.account.OkCoinCashAcnt;
import com.sam.deal.api.strategy.huobi.account.ICashAccount;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaResults;
import com.sam.moca.crud.CrudManager;
import com.sam.moca.util.MocaUtils;

public class OCSPS1 implements ISellPointSelector {

    static Logger log = LogManager.getLogger(OCSPS1.class);
    private final MocaContext _moca;
    private final CrudManager _manager;
    private double soldQty = 0;
    private double soldLstPri = 0;
    private boolean wasStockInUnstableMode = false;
    private boolean firstGoToStockInUnstableMode = false;
    private double FIRST_SHARPMODE_SELL_RATIO = -1;

    public OCSPS1()
    {
        _moca = MocaUtils.currentContext();
        _manager = MocaUtils.crudManager(_moca);
    }
    
	public boolean isGoodSellPoint(IStock s, ICashAccount ac) {
        if (s instanceof OkCoinStock) {
            OkCoinStock hs = (OkCoinStock) s;
            //eSTOCKTREND et = hs.getStockTrend();
            if (hs.isLstPriTurnaround(false) && hs.isLstPriAboveWaterLevel(0.8)) {
                log.info("Stock trend truns down at 0.8 level, OCSPS1 return true.");
                return true;
            }
            else {
                if (hs.getStockTrend() == eSTOCKTREND.SDOWN) {
                    log.info("Price trend is SDOWN, OCSPS1 return true!");
                    return true;
                }
                log.info("OCSPS1 return false.");
                return false;
            }
        }
        else {
            log.info("IStock is not OkCoinStock, return false.");
            return false;
        }
	}

	@Override
	public double getSellQty(IStock s, ICashAccount ac) {
	    if (ac instanceof OkCoinCashAcnt && s instanceof OkCoinStock) {
	        OkCoinStock hs = (OkCoinStock)s;
	        OkCoinCashAcnt hac = (OkCoinCashAcnt) ac;
	        String ct = hs.getSymbol().substring(0, 3);
	        double avaStock = hac.getAvaQty(ct);
	        double sellableAmt = 0;
	        
            if (FIRST_SHARPMODE_SELL_RATIO < 0) {
                String polvar = (hac.getCoinType().equalsIgnoreCase("btc") ? "BTC" : "LTC");
                try {
                    MocaResults rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '" + polvar
                            + "' and polval ='FIRST_SHARPMODE_BUYSELL_RATIO' and grp_id = '----'");
                    
                    rs.next();
                    
                    FIRST_SHARPMODE_SELL_RATIO = rs.getDouble("rtflt2");
                    log.info("got FIRST_SHARPMODE_SELL_RATIO:" + FIRST_SHARPMODE_SELL_RATIO);
                }
                catch(Exception e) {
                    log.debug(e.getMessage());
                    FIRST_SHARPMODE_SELL_RATIO = 1;
                    log.info("got FIRST_SHARPMODE_SELL_RATIO default to 1");
                }
            }
            
            firstGoToStockInUnstableMode = false;
            if (hs.isStockUnstableMode() && !wasStockInUnstableMode) {
                
                log.info("stock first goes into SDOWN mode, sell with stocks!");
                wasStockInUnstableMode = true;
                firstGoToStockInUnstableMode = true;
                sellableAmt = avaStock * FIRST_SHARPMODE_SELL_RATIO;
                log.info("got sellableAmt:" + sellableAmt + " with FIRST_SHARPMODE_BUY_RATIO:" + FIRST_SHARPMODE_SELL_RATIO + " * ava stock:" + avaStock);
                return sellableAmt;
            }
            else if (!hs.isStockUnstableMode() && wasStockInUnstableMode) {
                log.info("reset wasStockInUnstableMode to false.");
                wasStockInUnstableMode = false;
            }
	        
            double sellabeleMny = hac.getSellableMny();
	        double lstPri = hs.getLastPri();
	        sellableAmt = ((sellabeleMny / lstPri) > avaStock ? avaStock : (sellabeleMny / lstPri));
	        log.info("avaStock:" + avaStock + ", sellableMny:" + sellabeleMny +
	                ", lstPri:" + lstPri + ", sellableMny / lstPri:" + sellabeleMny / lstPri +
	                ", return sellableAmt:" + sellableAmt);
	        return sellableAmt;
	    }
	    return 0;
	}
	
	@Override
	public boolean sellStock(IStock s, ICashAccount ac) {
        if (s instanceof OkCoinStock && ac instanceof OkCoinCashAcnt) {
            OkCoinStock hs = (OkCoinStock)s;
            OkCoinCashAcnt hac = (OkCoinCashAcnt) ac;
            String ct = hs.getSymbol().substring(0, 3);
            double sellableQty = getSellQty(s, ac);
            boolean soldComplete = false;
            if (sellableQty > 0) {
                TickerData tid = hs.geTickerData();
                int sz = tid.last_lst.size();
                double lstPri = tid.last_lst.get(sz - 1);
                try {
                    MocaResults rs = _moca.executeCommand(
                            "get top10 data where mk ='" + hac.getMarket() + "'" +
                            "  and ct = '" + hac.getCoinType() + "'" +
                            "   and sdf = 0 and rtdf = 0");
                    
                    rs.next();
                    
                    double [] top10BuyAmt = new double[10];
                    double [] top10BuyPri = new double[10];
                    top10BuyAmt[0] = rs.getDouble("b_amt1");
                    top10BuyAmt[1] = rs.getDouble("b_amt2");
                    top10BuyAmt[2] = rs.getDouble("b_amt3");
                    top10BuyAmt[3] = rs.getDouble("b_amt4");
                    top10BuyAmt[4] = rs.getDouble("b_amt5");
                    top10BuyAmt[5] = rs.getDouble("b_amt6");
                    top10BuyAmt[6] = rs.getDouble("b_amt7");
                    top10BuyAmt[7] = rs.getDouble("b_amt8");
                    top10BuyAmt[8] = rs.getDouble("b_amt9");
                    top10BuyAmt[9] = rs.getDouble("b_amt10");
                    
                    top10BuyPri[0] = rs.getDouble("b_pri1");
                    top10BuyPri[1] = rs.getDouble("b_pri2");
                    top10BuyPri[2] = rs.getDouble("b_pri3");
                    top10BuyPri[3] = rs.getDouble("b_pri4");
                    top10BuyPri[4] = rs.getDouble("b_pri5");
                    top10BuyPri[5] = rs.getDouble("b_pri6");
                    top10BuyPri[6] = rs.getDouble("b_pri7");
                    top10BuyPri[7] = rs.getDouble("b_pri8");
                    top10BuyPri[8] = rs.getDouble("b_pri9");
                    top10BuyPri[9] = rs.getDouble("b_pri10");
                    
                    double bpg = hs.getBigPriceDiff();
                    
                    if (soldQty > 0 && Math.abs(soldLstPri - lstPri) > bpg) {
                        log.info("Already soldQty:" + soldQty + ", reset to 0 as gap between soldLstPri:" + soldLstPri + " and lstPri:" + lstPri + "> bpg:" + bpg);
                        soldQty = 0;
                    }
                    
                    for (int i = 0; i < 10; i++) {
                        double buyAmt = top10BuyAmt[i];
                        double buyPri = top10BuyPri[i];
                        
                        if (buyPri + bpg < lstPri) {
                            log.info("process buy[" + (i+1) + "], skip sell more as buyPri:" + buyPri + " + BigPriceDiff:" + bpg + " < lstPri:" + lstPri);
                            log.info("check firstGoToStockInUnstableMode:" + firstGoToStockInUnstableMode + " for force sell all...");
                            if (!firstGoToStockInUnstableMode) {
                                break;
                            }
                            else {
                                log.info("still sell as stock fist goes to unstable mode.");
                            }
                        }
                        
                        double remainSellQty = sellableQty - soldQty;
                        double sellAmt = (remainSellQty > buyAmt ? buyAmt : remainSellQty);
                        try {
                        _moca.executeCommand("[select round(" + sellAmt + ", 4) sellAmt, round(" + (buyPri - 0.1) + ", 2) price from dual]"
                                           + "|"
                                           + "create sell order"
                                           + " where market = '" + hac.getMarket() + "'"
                                           + "   and coinType ='" + hac.getCoinType() + "'"
                                           + "   and amount = @sellAmt "
                                           + "   and price = @price");
                        }
                        catch(Exception e) {
                            log.debug(e.getMessage());
                            log.debug("process selling buy[" + (i + 1) + "] failed, continue next buy.");
                            continue;
                        }
                        soldQty += sellAmt;
                        
                        if (soldQty >= sellableQty) {
                            log.info("sold Qty:" + soldQty + " success, which is bigger then sellableQty:" + sellableQty + " return true.");
                            soldComplete = true;
                            soldQty = 0;
                            break;
                        }
                    }
                    
                    if (soldQty > 0) {
                        log.info("save soldLstPri with lstPri:" + lstPri);
                        soldLstPri = lstPri;
                    }
                }
                catch(Exception e) {
                    log.debug(e.getMessage());
                }
                return soldComplete;
//                try {
//                    _moca.executeCommand("create market price sell order"
//                                       + " where market = 'CHN'"
//                                       + "   and coinType ='" + ct + "'"
//                                       + "   and amount = " + sellqty);
//                }
//                catch(Exception e) {
//                    log.debug(e.getMessage());
//                    return false;
//                }
//                return true;
            }
            else {
                log.info("sellqty is 0, can not sell:" + sellableQty);
                return false;
            }
        }
        log.info("IStock is not OkCoinStock(or OkCoinCashAcnt), can not sell huobi!");
        return false;
    }
}
