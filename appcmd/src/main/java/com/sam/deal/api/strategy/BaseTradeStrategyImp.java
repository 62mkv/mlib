package com.sam.deal.api.strategy;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.deal.api.strategy.BaseTradeStrategyImp;
import com.sam.deal.api.strategy.ITradeStrategy;
import com.sam.deal.api.stock.IStock;
import com.sam.deal.api.strategy.huobi.account.ICashAccount;
import com.sam.deal.api.strategy.huobi.buysell.IBuyPointSelector;
import com.sam.deal.api.strategy.huobi.buysell.ISellPointSelector;

public abstract class BaseTradeStrategyImp implements ITradeStrategy {

    static Logger log = LogManager.getLogger(BaseTradeStrategyImp.class);
    
    IBuyPointSelector buypoint_selector = null;
    ISellPointSelector sellpoint_selector = null;
    ICashAccount cash_account = null;
    
    public BaseTradeStrategyImp(IBuyPointSelector bps, ISellPointSelector sps, ICashAccount ca) {
        buypoint_selector = bps;
        sellpoint_selector = sps;
        cash_account =ca;
    }
    public boolean sellStock(IStock s) {
        if (sellpoint_selector.isGoodSellPoint(s, cash_account)) {
            return sellpoint_selector.sellStock(s, cash_account);
        }
        return false;
    }
    public boolean buyStock(IStock s) {
        if (buypoint_selector.isGoodBuyPoint(s, cash_account)) {
            return buypoint_selector.buyStock(s, cash_account);
        }
        return false;
    }
    public boolean calProfit() {
        return true;
    }
    
    public boolean performTrade(IStock s) {
        if (buyStock(s) || sellStock(s)) {
            return true;
        }
        return false;
    }
}
