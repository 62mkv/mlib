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
    private boolean replenishStockMode = false;
    private double FIRST_SHARPMODE_SELL_RATIO = -1;
    private double maxWaterLvl = 0.0;
    private double minWaterLvl = 0.0;
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
                minWaterLvl = rs.getDouble("rtflt1");
                log.info("got maxWaterLvl:" + maxWaterLvl + ", minWaterLvl:" + minWaterLvl);
            }
            catch(Exception e) {
                log.debug(e.getMessage());
                maxWaterLvl = 0.8;
                minWaterLvl = 0.2;
                log.info("set maxWaterLvl default to 0.8");
            }
        }
    }
    
	public boolean isGoodSellPoint() {
	    boolean b1 = stock.isLstPriBreakButtomBorder(3);
	    boolean b2 = stock.wasClosePriHighLvlAndCross(0.7, 3);
	    if (b1 && b2) {
	         log.info("close price at high level, and down breaking last 3 days close prices,  HBSPS1 return true!");
	         return true;
	     }
	    else if (!b2){
            if ((stock.isLstPriTurnaround(false) || stock.isStockUnstableMode()) && stock.isLstPriAboveWaterLevel(maxWaterLvl)) {
                log.info("Stock trend truns down at "+ maxWaterLvl + " level, HBSPS1 return true.");
                return true;
            }
            else {
//                if (!stock.isStockUnstableMode() && account.StockInhandLevelOverExpect(stock.getLastPri())) {
//                    log.info("Expected stock level control, enable replenishStockMode, HBSPS1 return true!");
//                    replenishStockMode = true;
//                    return true;
//                }
                log.info("HBSPS1 return false.");
                return false;
            }
	    }
	    return false;
	}

	@Override
	public double getSellQty() {
	    String ct = stock.getSymbol().substring(0, 3);
	    double avaStock = account.getAvaQty(ct);
	    double sellableAmt = 0;
        double minPct = account.getMinStockPct();
        double stockMny = account.getAvaQty(account.getCoinType()) * stock.getLastPri();
        double avaMny = account.getMaxAvaMny();
        double totalAsset = stockMny + avaMny;
        double sellabeleMny = account.getSellableMny();
        double lstPri = stock.getLastPri();
        
        double avaPct = stockMny / totalAsset - minPct;
        double sellableMny2 = avaPct * totalAsset;
        
        if (replenishStockMode) {
            double expPct = account.getExpStockPct(stock.getLastPri());
            if (stockMny / totalAsset > expPct + 1.0 / account.getMoneyLevels()) {
                log.info("reset expPct by 1.0 / moneyLevels:" + 1.0 / account.getMoneyLevels() + ", expPct:" + expPct);
                expPct += 1.0 / account.getMoneyLevels();
            }
            sellabeleMny = totalAsset * (stockMny / totalAsset - expPct);
            log.info("stock replenishStockMode, buy with expected stock level expPct:" + expPct + ", sellabeleMny:" + sellabeleMny);
        }
        else if (stock.isStockUnstableMode()) {
            sellabeleMny += sellabeleMny * FIRST_SHARPMODE_SELL_RATIO;
            log.info("stock is in unstable mode, sell with ratio money:" + sellabeleMny);
        }

        if (sellabeleMny > sellableMny2) {
            sellabeleMny = sellableMny2;
        }
	    
        sellableAmt = ((sellabeleMny / lstPri) > avaStock ? avaStock : (sellabeleMny / lstPri));
	    log.info("avaStock:" + avaStock + ", sellableMny:" + sellabeleMny +
	            ", lstPri:" + lstPri + ", sellableMny / lstPri:" + sellabeleMny / lstPri +
	            ", return sellableAmt:" + sellableAmt);
	    return sellableAmt;
	}
	
	@Override
	public boolean sellStock() {
        String ct = stock.getSymbol().substring(0, 3);
        String reacod = "GoodPrice";
        double sellableQty = getSellQty();
        boolean soldComplete = false;
            
        if (replenishStockMode) {
            reacod = "ReplenishmentMode";
        }
        else if (stock.isStockUnstableMode()) {
            reacod = "GoodPrice-UnstableMode";
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
                        if (!replenishStockMode) {
                            break;
                        }
                        else if (replenishStockMode) {
                            log.info("price is not good, will sell:" + (sellableQty - soldQty) + " with price:" + (lstPri - bpg));
                            sellWithFixedPrice = true;
                            buyPri = lstPri - bpg;
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
                                       + "   and reacod = '" + reacod + "'"
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
