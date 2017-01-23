package com.sam.deal.api.strategy.huobi.buysell;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.deal.api.strategy.huobi.buysell.HBBPS1;
import com.sam.deal.api.strategy.huobi.buysell.IBuyPointSelector;
import com.sam.deal.api.stock.IStock;
import com.sam.deal.api.stock.eSTOCKTREND;
import com.sam.deal.api.stock.huobi.HuoBiStock;
import com.sam.deal.api.strategy.huobi.account.HuoBiCashAcnt;
import com.sam.deal.api.strategy.huobi.account.ICashAccount;
import com.sam.moca.MocaContext;
import com.sam.moca.crud.CrudManager;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.util.MocaUtils;

public class HBBPS1 implements IBuyPointSelector {

	static Logger log = LogManager.getLogger(HBBPS1.class);
    private final MocaContext _moca;
    private final CrudManager _manager;
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            ServerUtils.setupDaemonContext("HBBPS1Test", true);
        }
        catch(SystemConfigurationException e) {
            e.printStackTrace();
        }
        HuoBiStock hs = new HuoBiStock(100);
        hs.loadMarketData("CHN", "BTC");
        HuoBiCashAcnt hac = new HuoBiCashAcnt();
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
	        eSTOCKTREND et = hs.getStockTrend();
	        if (et == eSTOCKTREND.CUP || et == eSTOCKTREND.UP) {
	            log.info("Stock trend goes CUP or UP, HBBPS1 return true.");
	            return true;
	        }
	        else {
	            log.info("Stock trend goes:" + et + ", HBBPS1 return false.");
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
            double buyMny = ac.getBuyableMny();
            if (buyMny > 1) {
                try {
                    _moca.executeCommand("create market price buy order"
                                       + " where market = 'CHN'"
                                       + "   and coinType ='" + ct + "'"
                                       + "   and amount = " + buyMny);
                }
                catch(Exception e) {
                    log.debug(e.getMessage());
                    return false;
                }
                return true;
            }
            else {
                log.info("buyMny is less then 1, can not buy:" + buyMny);
                return false;
            }
	    }
	    log.info("IStock is not HuoBiStock(or HuoBiCashAcnt), can not buy huobi!");
	    return false;
	}
}
