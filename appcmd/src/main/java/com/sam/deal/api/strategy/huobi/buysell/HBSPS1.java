package com.sam.deal.api.strategy.huobi.buysell;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.deal.api.strategy.huobi.buysell.HBSPS1;
import com.sam.deal.api.strategy.huobi.buysell.ISellPointSelector;
import com.sam.deal.api.stock.IStock;
import com.sam.deal.api.stock.eSTOCKTREND;
import com.sam.deal.api.stock.huobi.HuoBiStock;
import com.sam.deal.api.strategy.huobi.account.HuoBiCashAcnt;
import com.sam.deal.api.strategy.huobi.account.ICashAccount;
import com.sam.moca.MocaContext;
import com.sam.moca.crud.CrudManager;
import com.sam.moca.util.MocaUtils;

public class HBSPS1 implements ISellPointSelector {

    static Logger log = LogManager.getLogger(HBSPS1.class);
    private final MocaContext _moca;
    private final CrudManager _manager;

    public HBSPS1()
    {
        _moca = MocaUtils.currentContext();
        _manager = MocaUtils.crudManager(_moca);
    }
    
	public boolean isGoodSellPoint(IStock s, ICashAccount ac) {
        if (s instanceof HuoBiStock) {
            HuoBiStock hs = (HuoBiStock) s;
            eSTOCKTREND et = hs.getStockTrend();
            if (et == eSTOCKTREND.CDOWN || et == eSTOCKTREND.DOWN) {
                log.info("Stock trend goes CDOWN or DOWN, HBSPS1 return true.");
                return true;
            }
            else {
                log.info("Stock trend goes:" + et + ", HBSPS1 return false.");
                return false;
            }
        }
        else {
            log.info("IStock is not HuoBiStock, return false.");
            return false;
        }
	}

	@Override
	public double getSellQty(IStock s, ICashAccount ac) {
	    if (ac instanceof HuoBiCashAcnt && s instanceof HuoBiStock) {
	        HuoBiStock hs = (HuoBiStock)s;
	        HuoBiCashAcnt hac = (HuoBiCashAcnt) ac;
	        String ct = hs.getSymbol().substring(0, 3);
	        double sellable = hac.getAvaQty(ct);
	        return sellable;
	    }
	    return 0;
	}
	
	@Override
	public boolean sellStock(IStock s, ICashAccount ac) {
        if (s instanceof HuoBiStock && ac instanceof HuoBiCashAcnt) {
            HuoBiStock hs = (HuoBiStock)s;
            HuoBiCashAcnt hac = (HuoBiCashAcnt) ac;
            String ct = hs.getSymbol().substring(0, 3);
            double sellqty = getSellQty(s, ac);
            if (sellqty > 0) {
                try {
                    _moca.executeCommand("create market price sell order"
                                       + " where market = 'CHN'"
                                       + "   and coinType ='" + ct + "'"
                                       + "   and amount = " + sellqty);
                }
                catch(Exception e) {
                    log.debug(e.getMessage());
                    return false;
                }
                return true;
            }
            else {
                log.info("sellqty is 0, can not sell:" + sellqty);
                return false;
            }
        }
        log.info("IStock is not HuoBiStock(or HuoBiCashAcnt), can not sell huobi!");
        return false;
    }
}
