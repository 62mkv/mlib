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
    public boolean isGoodBuyPoint();
    public double getBuyQty();
    public boolean buyStock();
}
