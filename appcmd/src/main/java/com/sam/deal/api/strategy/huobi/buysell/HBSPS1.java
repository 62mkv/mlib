package com.sam.deal.api.strategy.huobi.buysell;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.deal.api.strategy.huobi.buysell.HBSPS1;
import com.sam.deal.api.strategy.huobi.buysell.ISellPointSelector;
import com.sam.deal.api.stock.IStock;
import com.sam.deal.api.stock.eSTOCKTREND;
import com.sam.deal.api.stock.huobi.HuoBiStock;
import com.sam.deal.api.stock.huobi.HuoBiStock.TickerData;
import com.sam.deal.api.strategy.huobi.account.HuoBiCashAcnt;
import com.sam.deal.api.strategy.huobi.account.ICashAccount;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaResults;
import com.sam.moca.crud.CrudManager;
import com.sam.moca.util.MocaUtils;

public class HBSPS1 implements ISellPointSelector {

    static Logger log = LogManager.getLogger(HBSPS1.class);
    private final MocaContext _moca;
    private final CrudManager _manager;
    private double soldQty = 0;
    private double soldLstPri = 0;
    private boolean wasStockInUnstableMode = false;
    private boolean firstGoToStockInUnstableMode = false;
    private boolean replenishStockMode = false;
    private double FIRST_SHARPMODE_SELL_RATIO = -1;
    private double maxWaterLvl = 0.0;
    private HuoBiStock stock = null;
    private HuoBiCashAcnt account = null;

    public HBSPS1(HuoBiStock s, HuoBiCashAcnt ac)
    {
        _moca = MocaUtils.currentContext();
        _manager = MocaUtils.crudManager(_moca);
        stock = s;
        account = ac;
        
        if (FIRST_SHARPMODE_SELL_RATIO < 0) {
            String polvar = (account.getCoinType().equalsIgnoreCase("btc") ? "BTC" : "LTC");
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
        
        if (maxWaterLvl <= 0) {
            String polvar = (account.getCoinType().equalsIgnoreCase("btc") ? "BTC" : "LTC");
            try {
                MocaResults rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '" + polvar
                        + "' and polval ='DEALWATERLVL' and grp_id = '----'");
                
                rs.next();
                
                maxWaterLvl = rs.getDouble("rtflt2");
                log.info("got maxWaterLvl:" + maxWaterLvl);
            }
            catch(Exception e) {
                log.debug(e.getMessage());
                maxWaterLvl = 0.8;
                log.info("set maxWaterLvl default to 0.8");
            }
        }
    }
    
    private boolean reachedMaxStockInhandLevel () {
        double maxPct = account.getMaxStockPct();
        double stockMny = account.getAvaQty(account.getCoinType()) * stock.getLastPri();
        double avaMny = account.getMaxAvaMny();
        double totalAsset = stockMny + avaMny;
        
        double actPct = stockMny / totalAsset;
        
        log.info("actual stock inhand level:" + actPct + ", maxPct:" + maxPct);
        if (actPct - maxPct >= 0.01) {
            log.info("Stock inhand level reached max value, return true");
            return true;
        }
        
        log.info("Stock inhand level NOT reached max value, return false");
        return false;
    }
    
	public boolean isGoodSellPoint() {
        if (stock.isLstPriTurnaround(false) && stock.isLstPriAboveWaterLevel(maxWaterLvl)) {
            log.info("Stock trend truns down at "+ maxWaterLvl + " level, HBSPS1 return true.");
            return true;
        }
        else {
            if (stock.getStockTrend() == eSTOCKTREND.SDOWN) {
                log.info("Price trend is SDOWN, HBSPS1 return true!");
                return true;
            }
            else if (stock.isShortCrossLong(false) || reachedMaxStockInhandLevel()) {
                log.info("Short term is dead cross long term or reached max level, enable replenishStockMode, but HBSPS1 return false!");
                replenishStockMode = true;
                return false;
            }
            log.info("HBSPS1 return false.");
            return false;
        }
	}

	@Override
	public double getSellQty() {
	    String ct = stock.getSymbol().substring(0, 3);
	    double avaStock = account.getAvaQty(ct);
	    double sellableAmt = 0;
	    
        firstGoToStockInUnstableMode = false;
        if (stock.isStockUnstableMode() && !wasStockInUnstableMode) {
            
            log.info("stock first goes into SDOWN mode, sell with stocks!");
            wasStockInUnstableMode = true;
            firstGoToStockInUnstableMode = true;
            sellableAmt = avaStock * FIRST_SHARPMODE_SELL_RATIO;
            log.info("got sellableAmt:" + sellableAmt + " with FIRST_SHARPMODE_BUY_RATIO:" + FIRST_SHARPMODE_SELL_RATIO + " * ava stock:" + avaStock);
            return sellableAmt;
        }
        else if (!stock.isStockUnstableMode() && wasStockInUnstableMode) {
            log.info("reset wasStockInUnstableMode to false.");
            wasStockInUnstableMode = false;
        }
        else if (replenishStockMode) {
            log.info("stock replenishStockMode and reached max stock level, sell with ratio ava money");
            sellableAmt = avaStock * FIRST_SHARPMODE_SELL_RATIO;
            log.info("got sellableAmt:" + sellableAmt + " with FIRST_SHARPMODE_BUY_RATIO:" + FIRST_SHARPMODE_SELL_RATIO + " * ava stock:" + avaStock);
            return sellableAmt;
        }
	    
        double sellabeleMny = account.getSellableMny();
	    double lstPri = stock.getLastPri();
	    sellableAmt = ((sellabeleMny / lstPri) > avaStock ? avaStock : (sellabeleMny / lstPri));
	    log.info("avaStock:" + avaStock + ", sellableMny:" + sellabeleMny +
	            ", lstPri:" + lstPri + ", sellableMny / lstPri:" + sellabeleMny / lstPri +
	            ", return sellableAmt:" + sellableAmt);
	    return sellableAmt;
	}
	
	@Override
	public boolean sellStock() {
        String ct = stock.getSymbol().substring(0, 3);
        double sellableQty = getSellQty();
        boolean soldComplete = false;
            
	    if (!firstGoToStockInUnstableMode) {
                double minPct = account.getMinStockPct();
                double stockMny = account.getAvaQty(account.getCoinType()) * stock.getLastPri();
                double avaMny = account.getMaxAvaMny();
                double totalAsset = stockMny + avaMny;
                
                double avaPct = (stockMny / totalAsset) - minPct;
                log.info("StockMny:" + stockMny + ", avaMny:" + avaMny + ", totalAsset:" + totalAsset + ", avaPct:" + avaPct);
                double sellableQty2 = avaPct * totalAsset / stock.getLastPri();
                log.info("sellableQty:" + sellableQty + ", stock ctl sellableQty2:" + sellableQty2);
                if (sellableQty > sellableQty2) {
                    sellableQty = sellableQty2;
                }
	    }
            
            if (sellableQty < soldQty + 0.001) {
            	log.info("Already sold:" + soldQty + " + 0.001 > sellableQty:" + sellableQty + " reset soldQty to 0.");
            	soldQty = 0;
            }
            
            if (sellableQty > 0.001) {
                TickerData tid = stock.geTickerData();
                int sz = tid.last_lst.size();
                double lstPri = tid.last_lst.get(sz - 1);
                try {
                    MocaResults rs = _moca.executeCommand(
                            "get top10 data where mk ='" + account.getMarket() + "'" +
                            "  and ct = '" + account.getCoinType() + "'" +
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
                    
                    double bpg = stock.getBigPriceDiff();
                    
                    if (soldQty > 0 && Math.abs(soldLstPri - lstPri) > bpg) {
                        log.info("Already soldQty:" + soldQty + ", reset to 0 as gap between soldLstPri:" + soldLstPri + " and lstPri:" + lstPri + "> bpg:" + bpg);
                        soldQty = 0;
                    }
                    
                    boolean sellWithFixedPrice = false;
                    for (int i = 0; i < 10; i++) {
                        double buyAmt = top10BuyAmt[i];
                        double buyPri = top10BuyPri[i];
                        
                        if (buyPri + bpg < lstPri) {
                            log.info("process buy[" + (i+1) + "], skip sell more as buyPri:" + buyPri + " + BigPriceDiff:" + bpg + " < lstPri:" + lstPri);
                            log.info("check firstGoToStockInUnstableMode:" + firstGoToStockInUnstableMode + " for force sell all...");
                            if (!firstGoToStockInUnstableMode && !replenishStockMode) {
                                break;
                            }
                            else {
                                if (replenishStockMode) {
                                    log.info("price is not good, will sell:" + (sellableQty - soldQty) + " with price:" + (lstPri - bpg));
                                    sellWithFixedPrice = true;
                                    buyPri = lstPri - bpg;
                                }
                                else {
                                    log.info("sell with whatever price for unstable mode.");
                                }
                            }
                        }
                        
                        double remainSellQty = sellableQty - soldQty;
                        double sellAmt = (remainSellQty > buyAmt ? buyAmt : remainSellQty);
                        
                        if (sellWithFixedPrice) {
                            sellAmt = remainSellQty;
                        }
                        
                        try {
                        _moca.executeCommand("[select round(" + sellAmt + ", 4) sellAmt, round(" + (buyPri - 0.1) + ", 2) price from dual]"
                                           + "|"
                                           + "create sell order"
                                           + " where market = '" + account.getMarket() + "'"
                                           + "   and coinType ='" + account.getCoinType() + "'"
                                           + "   and amount = @sellAmt "
                                           + "   and price = @price");
                        }
                        catch(Exception e) {
                            log.debug(e.getMessage());
                            log.debug("process selling buy[" + (i + 1) + "] failed, continue next buy.");
                            continue;
                        }
                        soldQty += sellAmt;
                        
                        if (soldQty + 0.001 >= sellableQty) {
                            log.info("sold Qty:" + soldQty + " success, which is bigger then sellableQty:" + sellableQty + " return true.");
                            soldComplete = true;
                            replenishStockMode = false;
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
            }
            else {
                log.info("sellqty is 0, can not sell:" + sellableQty);
                return false;
            }
    }
}
