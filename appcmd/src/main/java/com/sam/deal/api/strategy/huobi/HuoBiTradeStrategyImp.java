package com.sam.deal.api.strategy.huobi;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.deal.api.strategy.huobi.HuoBiTradeStrategyImp;
import com.sam.deal.api.stock.IStock;
import com.sam.deal.api.stock.huobi.HuoBiStock;
import com.sam.deal.api.strategy.BaseTradeStrategyImp;
import com.sam.deal.api.strategy.huobi.account.HuoBiCashAcnt;
import com.sam.deal.api.strategy.huobi.account.ICashAccount;
import com.sam.deal.api.strategy.huobi.buysell.HBBPS1;
import com.sam.deal.api.strategy.huobi.buysell.HBSPS1;
import com.sam.deal.api.strategy.huobi.buysell.IBuyPointSelector;
import com.sam.deal.api.strategy.huobi.buysell.ISellPointSelector;

public class HuoBiTradeStrategyImp extends BaseTradeStrategyImp {

    private HuoBiStock hbs = null;
    private String market = null;
    private String coinType = null;
    public HuoBiTradeStrategyImp(IBuyPointSelector bps, ISellPointSelector sps, ICashAccount ca) {
        super(bps, sps, ca);
        // TODO Auto-generated constructor stub
    }

    static Logger log = LogManager.getLogger(HuoBiTradeStrategyImp.class);
    
    public HuoBiTradeStrategyImp(String mk,
                                 String ct,
                                 HuoBiStock huobistock,
                                 HBBPS1 bps,
                                 HBSPS1 sps,
                                 HuoBiCashAcnt ca) {
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
    public boolean startTrading() {
        hbs.loadMarketData(market, coinType);
        super.performTrade(hbs);
        return true;
    }
    
}
