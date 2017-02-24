package com.sam.deal.api.strategy.okcoin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.deal.api.stock.IStock;
import com.sam.deal.api.stock.huobi.HuoBiStock;
import com.sam.deal.api.stock.okcoin.OkCoinStock;
import com.sam.deal.api.strategy.BaseTradeStrategyImp;
import com.sam.deal.api.strategy.huobi.account.HuoBiCashAcnt;
import com.sam.deal.api.strategy.huobi.account.ICashAccount;
import com.sam.deal.api.strategy.huobi.buysell.HBBPS1;
import com.sam.deal.api.strategy.huobi.buysell.HBSPS1;
import com.sam.deal.api.strategy.huobi.buysell.IBuyPointSelector;
import com.sam.deal.api.strategy.huobi.buysell.ISellPointSelector;
import com.sam.deal.api.strategy.okcoin.account.OkCoinCashAcnt;
import com.sam.deal.api.strategy.okcoin.buysell.OCBPS1;
import com.sam.deal.api.strategy.okcoin.buysell.OCSPS1;
import com.sam.moca.MocaContext;
import com.sam.moca.util.MocaUtils;

public class OkCoinTradeStrategyImp extends BaseTradeStrategyImp {

    private OkCoinStock hbs = null;
    private String market = null;
    private String coinType = null;
    public OkCoinTradeStrategyImp(IBuyPointSelector bps, ISellPointSelector sps, ICashAccount ca) {
        super(bps, sps, ca);
        // TODO Auto-generated constructor stub
    }

    static Logger log = LogManager.getLogger(OkCoinTradeStrategyImp.class);
    
    public OkCoinTradeStrategyImp(String mk,
                                 String ct,
                                 OkCoinStock huobistock,
                                 OCBPS1 bps,
                                 OCSPS1 sps,
                                 OkCoinCashAcnt ca) {
        super(bps, sps, ca);
        hbs = huobistock;
        market = mk;
        coinType = ct;
    }
    
    @Override
    public boolean calProfit() {
        return true;
    }
    
    @Override
    public boolean beforeTrade(IStock s) {
        log.info("run OkCoinTradeStrategyImp beforeTrade, loadMarketData.");
        super.beforeTrade(s);
        hbs.loadMarketData();
        return true;
    }
    
    @Override
    public boolean afterTrade(IStock s) {
        log.info("run OkCoinTradeStrategyImp afterTrade, clearMarketData.");
        hbs.dumpDatatoDB();
        hbs.clearMarketData();
        try {
            MocaContext _moca = MocaUtils.currentContext();
            _moca.commit();
        }
        catch(Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return true;
    }
    
    @Override
    public boolean startTrading() {
        return performTrade(hbs);
    }
    
}
