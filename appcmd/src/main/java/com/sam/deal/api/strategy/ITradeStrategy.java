package com.sam.deal.api.strategy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import com.sam.deal.api.stock.IStock;

public interface ITradeStrategy {

    public boolean sellStock(IStock s);
    public boolean buyStock(IStock s);
    public boolean calProfit();
    public boolean startTrading();
    
    public boolean performTrade(IStock s);
}
