package com.sam.deal.api.strategy.huobi.buysell;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import com.sam.deal.api.stock.IStock;
import com.sam.deal.api.strategy.huobi.account.ICashAccount;

public interface IBuyPointSelector {

    /**
     * @param args
     */
    public boolean isGoodBuyPoint(IStock s, ICashAccount ac);
    public double getBuyQty(IStock s, ICashAccount ac);
    public boolean buyStock(IStock s, ICashAccount ac);
}
