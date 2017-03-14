package com.sam.deal.api.strategy.okcoin.buysell;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.deal.api.strategy.huobi.buysell.IBuyPointSelector;
import com.sam.deal.api.stock.BoundArrayList;
import com.sam.deal.api.stock.IStock;
import com.sam.deal.api.stock.eSTOCKTREND;
import com.sam.deal.api.stock.huobi.HuoBiStock;
import com.sam.deal.api.stock.okcoin.OkCoinStock;
import com.sam.deal.api.stock.okcoin.OkCoinStock.TickerData;
import com.sam.deal.api.stock.okcoin.OkCoinStock.Top10Data;
import com.sam.deal.api.strategy.okcoin.account.OkCoinCashAcnt;
import com.sam.deal.api.strategy.huobi.account.HuoBiCashAcnt;
import com.sam.deal.api.strategy.huobi.account.ICashAccount;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaResults;
import com.sam.moca.crud.CrudManager;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.util.MocaUtils;

public class OCBPS1 implements IBuyPointSelector {

	static Logger log = LogManager.getLogger(OCBPS1.class);
    private final MocaContext _moca;
    private final CrudManager _manager;
    private double boughtMny = 0;
    private double boughtLstPri = 0;
    private boolean replenishStockMode = false;
    private double FIRST_SHARPMODE_BUY_RATIO = -1;
    private double minWaterLvl = 0.0;
    private OkCoinStock stock = null;
    private OkCoinCashAcnt account = null;
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            ServerUtils.setupDaemonContext("OCBPS1Test", true);
        }
        catch(SystemConfigurationException e) {
            e.printStackTrace();
        }
        OkCoinStock hs = new OkCoinStock("CHN", "BTC");
        hs.loadMarketData();
        OkCoinCashAcnt hac = new OkCoinCashAcnt("CHN", "BTC");
        OCBPS1 bp = new OCBPS1(hs, hac);
        bp.buyStock();
    }
    
    public OCBPS1(OkCoinStock s, OkCoinCashAcnt ac)
    {
        _moca = MocaUtils.currentContext();
        _manager = MocaUtils.crudManager(_moca);
        stock = s;
        account = ac;
        
        if (FIRST_SHARPMODE_BUY_RATIO < 0) {
            String polvar = (account.getCoinType().equalsIgnoreCase("btc") ? "BTC" : "LTC");
            try {
                MocaResults rs = _moca.executeCommand("list policies where polcod ='OKCOIN' and polvar = '" + polvar
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
        
        if (minWaterLvl <= 0) {
            String polvar = (account.getCoinType().equalsIgnoreCase("btc") ? "BTC" : "LTC");
            try {
                MocaResults rs = _moca.executeCommand("list policies where polcod ='OKCOIN' and polvar = '" + polvar
                        + "' and polval ='DEALWATERLVL' and grp_id = '----'");
                
                rs.next();
                
                minWaterLvl = rs.getDouble("rtflt1");
                log.info("got minWaterLvl:" + minWaterLvl);
            }
            catch(Exception e) {
                log.debug(e.getMessage());
                minWaterLvl = 0.2;
                log.info("set minWaterLvl default to 0.2");
            }
        }
    }
    
	@Override
	public boolean isGoodBuyPoint() {
	    if ((stock.isLstPriTurnaround(true) || stock.isStockUnstableMode()) && stock.isLstPriUnderWaterLevel(minWaterLvl)) {
	        log.info("Stock trend turn up and at low " + minWaterLvl + " level, OCBPS1 return true.");
	        return true;
	    }
	    else {
            if (!stock.isStockUnstableMode() && account.StockInhandLevelUnderExpect(stock.getLastPri())) {
                log.info("Min level, enable replenishStockMode HBBPS1 return true!");
                replenishStockMode = true;
                return true;
            }
            else if (stock.isShortCrossLong(true)) {
                log.info("Short term is golden cross long term, enable replenishStockMode but OCBPS1 return false!");
                replenishStockMode = true;
                return true;
            }
	        log.info("OCBPS1 return false.");
	        return false;
	    }
	}
	
	@Override
	public double getBuyQty() {
		// TODO Auto-generated method stub
        double buyablemny = account.getBuyableMny();
        double buyprice = stock.getCurPri();
        log.info("buyablemny:" + buyablemny);
        log.info("buyprice:" + buyprice);
        return buyablemny / buyprice;
	}
	
	@Override
	public boolean buyStock() {
        String ct = stock.getSymbol().substring(0, 3);
        String reacod = "GoodPrice";
        double buyableMny = account.getBuyableMny();
        double maxPct = account.getMaxStockPct();
        double stockMny = account.getAvaQty(account.getCoinType()) * stock.getLastPri();
        double avaMny = account.getMaxAvaMny();
        double totalAsset = stockMny + avaMny;
        
        double avaPct = maxPct - (stockMny / totalAsset);
        log.info("StockMny:" + stockMny + ", avaMny:" + avaMny + ", totalAsset:" + totalAsset + ", avaPct:" + avaPct);
        double buyableMny2 = avaPct * totalAsset;
        log.info("buyableMny:" + buyableMny + ", stock ctl buyableMny2:" + buyableMny2);

        if (stock.isStockUnstableMode()) {
            buyableMny += buyableMny * FIRST_SHARPMODE_BUY_RATIO;
            reacod = "GoodPrice-UnstableMode";
            log.info("stock is in unstable mode, buy with ratio money:" + buyableMny);
        }
        else if (replenishStockMode) {
            double expPct = account.getExpStockPct(stock.getLastPri());
            if (stockMny / totalAsset < expPct - 1.0 / account.getMoneyLevels()) {
                log.info("reset expPct by -1.0 / moneyLevels:" + 1.0 / account.getMoneyLevels() + ", expPct:" + expPct);
                expPct -= 1.0 / account.getMoneyLevels();
            }
            buyableMny = totalAsset * (expPct - (stockMny / totalAsset));
            reacod = "ReplenishmentMode";
            log.info("stock replenishStockMode, buy with expected stock level expPct:" + expPct + ", buyableMny:" + buyableMny);
        }
        
        if (buyableMny > buyableMny2) {
            buyableMny = buyableMny2;
        }
        
        boolean boughtComplete = false;
        
        if (buyableMny < boughtMny + 1) {
        	log.info("Already bought:" + boughtMny + " + 1 > buyableMny:" + buyableMny + " reset boughtMny to 0.");
        	boughtMny = 0;
        }
        
        if (buyableMny > 1) {
            TickerData tid = stock.geTickerData();
            int sz = tid.last_lst.size();
            double lstPri = tid.last_lst.get(sz - 1);
            try {
                MocaResults rs = _moca.executeCommand(
                        "get top10 data for oc where mk ='" + account.getMarket() + "'" +
                        "  and ct = '" + account.getCoinType() + "'" +
                        "   and sdf = 0");
                
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
                
                double bpg = stock.getBigPriceDiff();
                
                if (boughtMny > 0 && Math.abs(boughtLstPri - lstPri) > bpg) {
                    log.info("Already boughtMny:" + boughtMny + ", reset to 0 as gap between boughtLstPri:" + boughtLstPri + " and lstPri:" + lstPri + "> bpg:" + bpg);
                    boughtMny = 0;
                }
                
                boolean buyWithFixedPrice = false;
                for (int i = 0; i < 10; i++) {
                    double sellAmt = top10SellAmt[i];
                    double sellPri = top10SellPri[i];
                    
                    if (sellPri > lstPri + bpg) {
                        log.info("process sell[" + (i+1) + "], skip buy more as sellPri:" + sellPri + "> BigPriceDiff:" + bpg + " + lstPri:" + lstPri);
                        if (!replenishStockMode) {
                            break;
                        }
                        else if (replenishStockMode){
                            log.info("price is not good, will buy:" + (buyableMny - boughtMny) /sellPri + " with price:" + (lstPri + bpg));
                            buyWithFixedPrice = true;
                            sellPri = lstPri + bpg;
                        }
                    }
                    
                    double remainBuyMny = buyableMny - boughtMny;
                    double buyableAmt = remainBuyMny / sellPri;
                    double buyAmt = (buyableAmt > sellAmt ? sellAmt : buyableAmt);
                    try {
                    _moca.executeCommand("[select round(" + buyAmt + ", 4) buyAmt, round(" + (sellPri + 0.1) + ", 2) price  from dual]"
                                       + "|"
                                       + "create buy order for oc "
                                       + " where market = '" + account.getMarket() + "'"
                                       + "   and coinType ='" + account.getCoinType() + "'"
                                       + "   and reacod = '" + reacod + "'"
                                       + "   and amount = @buyAmt "
                                       + "   and price = @price");
                    }
                    catch(Exception e) {
                        log.debug(e.getMessage());
                        log.debug("process buying sell[" + (i + 1) + "] failed, continue next sell.");
                        continue;
                    }
                    boughtMny += buyAmt * sellPri;
                    
                    if (boughtMny + 1 >= buyableMny) {
                        log.info("bought money:" + boughtMny + " success, which is bigger then buyableMny:" + buyableMny + " return true.");
                        boughtComplete = true;
                        replenishStockMode = false;
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
        }
        else {
            log.info("buyableMny is less then 1, can not buy:" + buyableMny);
            return false;
        }
	}
}
