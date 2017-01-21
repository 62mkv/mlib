package com.sam.deal.api.strategy.huobi.account;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sam.deal.api.strategy.huobi.account.ICashAccount;

public interface ICashAccount {

    static Logger log = Logger.getLogger(ICashAccount.class);
    
    /**
     * @param args
     */
    public String getAccountID();
    public double getMaxAvaMny();
    public double getBuyableMny();
    public double getInitMny();
    public double getUsedMny();
    public int getSplitNum();
    public void printAcntInfo();
}
