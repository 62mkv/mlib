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
            if (beforeSell(s)) {
                boolean sell_success = sellpoint_selector.sellStock(s, cash_account);
                if (sell_success) {
                    return afterSell(s);
                }
            }
            return false;
        }
        return false;
    }
    public boolean buyStock(IStock s) {
        if (buypoint_selector.isGoodBuyPoint(s, cash_account)) {
            if (beforeBuy(s)) {
                boolean buy_success = buypoint_selector.buyStock(s, cash_account);
                if (buy_success) {
                    return afterBuy(s);
                }
            }
            else {
                return false;
            }
        }
        return false;
    }
    
    public boolean beforeBuy(IStock s) {
        log.info("run default beforeBuy, return true.");
        return true;
    }
    
    public boolean afterBuy(IStock s) {
        log.info("run default afterBuy, return true.");
        return true;
    }
    
    public boolean beforeSell(IStock s) {
        log.info("run default beforeSell, return true.");
        return true;
    }
    
    public boolean afterSell(IStock s) {
        log.info("run default afterSell, return true.");
        return true;
    }
    
    public boolean beforeTrade(IStock s) {
        log.info("run default beforeTrade, return true.");
        return true;
    }
    
    public boolean afterTrade(IStock s) {
        log.info("run default afterTrade, return true.");
        return true;
    }
    
    public boolean calProfit() {
        return true;
    }
    
    public boolean performTrade(IStock s) {
        boolean trade_result = false;
        if (beforeTrade(s)) {
            trade_result = buyStock(s) || sellStock(s);
            if (trade_result) {
                afterTrade(s);
            }
            return trade_result;
        }
        return false;
    }
}
