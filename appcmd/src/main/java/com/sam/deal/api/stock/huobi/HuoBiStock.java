package com.sam.deal.api.stock.huobi;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.deal.api.stock.huobi.HuoBiStock;
import com.sam.deal.api.stock.BoundArrayList;
import com.sam.deal.api.stock.IStock;
import com.sam.deal.api.stock.eSTOCKTREND;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.crud.CrudManager;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.util.MocaUtils;

public class HuoBiStock implements IStock{

    private Logger _logger = LogManager.getLogger(HuoBiStock.class);
    
    static public final int SECONDS_PER_FETCH = 5;
    static public final int STEP_FOR_CAL_TREND = 1;
    static public final double BIG_PRICE_DIFF = 5;
    static public final double SMALL_PRICE_DIFF = 2;

    /**
     * @param args
     */
    private String symbol;
    private Top10Data top10;
    private TradeData trd;
    private TickerData tid;
    private int MAX_SZ = 800;
    private final MocaContext _moca;
    private final CrudManager _manager;
    private eSTOCKTREND st = null;
    
    public class Top10Data{
        List<String> time_st =  null;
        List<Double> b_amt1_lst = null;
        List<Double> b_amt2_lst = null;
        List<Double> b_amt3_lst = null;
        List<Double> b_amt4_lst = null;
        List<Double> b_amt5_lst = null;
        List<Double> b_amt6_lst = null;
        List<Double> b_amt7_lst = null;
        List<Double> b_amt8_lst = null;
        List<Double> b_amt9_lst = null;
        List<Double> b_amt10_lst = null;
        List<Double> b_pri1_lst = null;
        List<Double> b_pri2_lst = null;
        List<Double> b_pri3_lst = null;
        List<Double> b_pri4_lst = null;
        List<Double> b_pri5_lst = null;
        List<Double> b_pri6_lst = null;
        List<Double> b_pri7_lst = null;
        List<Double> b_pri8_lst = null;
        List<Double> b_pri9_lst = null;
        List<Double> b_pri10_lst = null;
        
        List<Double> s_amt1_lst = null;
        List<Double> s_amt2_lst = null;
        List<Double> s_amt3_lst = null;
        List<Double> s_amt4_lst = null;
        List<Double> s_amt5_lst = null;
        List<Double> s_amt6_lst = null;
        List<Double> s_amt7_lst = null;
        List<Double> s_amt8_lst = null;
        List<Double> s_amt9_lst = null;
        List<Double> s_amt10_lst = null;
        List<Double> s_pri1_lst = null;
        List<Double> s_pri2_lst = null;
        List<Double> s_pri3_lst = null;
        List<Double> s_pri4_lst = null;
        List<Double> s_pri5_lst = null;
        List<Double> s_pri6_lst = null;
        List<Double> s_pri7_lst = null;
        List<Double> s_pri8_lst = null;
        List<Double> s_pri9_lst = null;
        List<Double> s_pri10_lst = null;
        
        List<Double> p_new_lst = null;
        List<Double> p_last_lst = null;
        List<Double> p_low_lst = null;
        List<Double> p_open_lst = null;
        List<Double> p_high_lst = null;
        List<Double> total_lst = null;
        
        Top10Data(int sz) {
            MAX_SZ = sz;
            
            time_st = new BoundArrayList<String>(MAX_SZ);
            
            b_amt1_lst = new BoundArrayList<Double>(MAX_SZ);
            b_amt2_lst = new BoundArrayList<Double>(MAX_SZ);
            b_amt3_lst = new BoundArrayList<Double>(MAX_SZ);
            b_amt4_lst = new BoundArrayList<Double>(MAX_SZ);
            b_amt5_lst = new BoundArrayList<Double>(MAX_SZ);
            b_amt6_lst = new BoundArrayList<Double>(MAX_SZ);
            b_amt7_lst = new BoundArrayList<Double>(MAX_SZ);
            b_amt8_lst = new BoundArrayList<Double>(MAX_SZ);
            b_amt9_lst = new BoundArrayList<Double>(MAX_SZ);
            b_amt10_lst= new BoundArrayList<Double>(MAX_SZ);

            b_pri1_lst = new BoundArrayList<Double>(MAX_SZ);
            b_pri2_lst = new BoundArrayList<Double>(MAX_SZ);
            b_pri3_lst = new BoundArrayList<Double>(MAX_SZ);
            b_pri4_lst = new BoundArrayList<Double>(MAX_SZ);
            b_pri5_lst = new BoundArrayList<Double>(MAX_SZ);
            b_pri6_lst = new BoundArrayList<Double>(MAX_SZ);
            b_pri7_lst = new BoundArrayList<Double>(MAX_SZ);
            b_pri8_lst = new BoundArrayList<Double>(MAX_SZ);
            b_pri9_lst = new BoundArrayList<Double>(MAX_SZ);
            b_pri10_lst= new BoundArrayList<Double>(MAX_SZ);

            s_amt1_lst = new BoundArrayList<Double>(MAX_SZ);
            s_amt2_lst = new BoundArrayList<Double>(MAX_SZ);
            s_amt3_lst = new BoundArrayList<Double>(MAX_SZ);
            s_amt4_lst = new BoundArrayList<Double>(MAX_SZ);
            s_amt5_lst = new BoundArrayList<Double>(MAX_SZ);
            s_amt6_lst = new BoundArrayList<Double>(MAX_SZ);
            s_amt7_lst = new BoundArrayList<Double>(MAX_SZ);
            s_amt8_lst = new BoundArrayList<Double>(MAX_SZ);
            s_amt9_lst = new BoundArrayList<Double>(MAX_SZ);
            s_amt10_lst= new BoundArrayList<Double>(MAX_SZ);

            s_pri1_lst = new BoundArrayList<Double>(MAX_SZ);
            s_pri2_lst = new BoundArrayList<Double>(MAX_SZ);
            s_pri3_lst = new BoundArrayList<Double>(MAX_SZ);
            s_pri4_lst = new BoundArrayList<Double>(MAX_SZ);
            s_pri5_lst = new BoundArrayList<Double>(MAX_SZ);
            s_pri6_lst = new BoundArrayList<Double>(MAX_SZ);
            s_pri7_lst = new BoundArrayList<Double>(MAX_SZ);
            s_pri8_lst = new BoundArrayList<Double>(MAX_SZ);
            s_pri9_lst = new BoundArrayList<Double>(MAX_SZ);
            s_pri10_lst= new BoundArrayList<Double>(MAX_SZ);
            
            p_new_lst  = new BoundArrayList<Double>(MAX_SZ);
            p_last_lst = new BoundArrayList<Double>(MAX_SZ);
            p_low_lst  = new BoundArrayList<Double>(MAX_SZ);
            p_open_lst = new BoundArrayList<Double>(MAX_SZ);
            p_high_lst = new BoundArrayList<Double>(MAX_SZ);
            total_lst  = new BoundArrayList<Double>(MAX_SZ);
        }
    }
    
    public class TradeData{
        List<String> symbol_st =  null;
        List<String> time_st =  null;
        List<Double> amount_lst = null;
        List<Double> price_lst = null;
        List<String> type_st =  null;
        
        TradeData() {
            symbol_st = new BoundArrayList<String>(MAX_SZ * 60);
            time_st = new BoundArrayList<String>(MAX_SZ * 60);
            amount_lst = new BoundArrayList<Double>(MAX_SZ * 60);
            price_lst  = new BoundArrayList<Double>(MAX_SZ * 60);
            type_st = new BoundArrayList<String>(MAX_SZ * 60);
        }
    }
    
    public class TickerData{
        List<String> symbol_st =  null;
        List<String> time_st =  null;
        List<Double> vol_lst = null;
        List<Double> high_lst = null;
        List<Double> low_lst = null;
        List<Double> buy_lst = null;
        List<Double> sell_lst = null;
        List<Double> last_lst = null;
        List<Double> open_lst = null;
        
        TickerData() {
            symbol_st = new BoundArrayList<String>(MAX_SZ);
            time_st = new BoundArrayList<String>(MAX_SZ);
            vol_lst = new BoundArrayList<Double>(MAX_SZ);
            high_lst  = new BoundArrayList<Double>(MAX_SZ);
            low_lst  = new BoundArrayList<Double>(MAX_SZ);
            buy_lst  = new BoundArrayList<Double>(MAX_SZ);
            sell_lst  = new BoundArrayList<Double>(MAX_SZ);
            last_lst  = new BoundArrayList<Double>(MAX_SZ);
            open_lst  = new BoundArrayList<Double>(MAX_SZ);
        }
    }
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            ServerUtils.setupDaemonContext("StockTest", true);
        }
        catch(SystemConfigurationException e) {
            e.printStackTrace();
        }
        HuoBiStock s = new HuoBiStock(10);
        s.loadMarketData("chn", "btc");
    }
    
    public HuoBiStock(int sz)
    {
        _moca = MocaUtils.currentContext();
        _manager = MocaUtils.crudManager(_moca);
        init(sz);
    }
    
    public void init(int sz) {
        top10 = new Top10Data(sz);
        trd = new TradeData();
        tid = new TickerData();
    }
    
    public void loadMarketData(String market, String coinType) {
        loadTop10Data(market, coinType);
        loadTradeData(market, coinType);
        loadTickerData(market, coinType);
        try {
            _moca.commit();
        }
        catch(MocaException e) {
            _logger.debug(e.getMessage());
        }
        this.calStockTrend();
    }
    
    public void clearMarketData() {
        clearTop10Data();
        clearTradeData();
        clearTickerData();
        this.calStockTrend();
        _logger.debug("cleared all market data, stock trend is:" + this.st);
    }
    
    private void loadTop10Data(String market, String coinType) {
        MocaResults rs = null;
        try {
            rs = _moca.executeCommand(
                    "get top10 data where mk ='" + market + "'" +
                    "  and ct = '" + coinType + "'" +
                    "   and sdf = 0 and rtdf = 0");
            
            rs.next();
            this.symbol = rs.getString("symbol");
            top10.time_st.add(rs.getString("time"));
            top10.b_amt1_lst.add(rs.getDouble("b_amt1"));
            top10.b_amt2_lst.add(rs.getDouble("b_amt2"));
            top10.b_amt3_lst.add(rs.getDouble("b_amt3"));
            top10.b_amt4_lst.add(rs.getDouble("b_amt4"));
            top10.b_amt5_lst.add(rs.getDouble("b_amt5"));
            top10.b_amt6_lst.add(rs.getDouble("b_amt6"));
            top10.b_amt7_lst.add(rs.getDouble("b_amt7"));
            top10.b_amt8_lst.add(rs.getDouble("b_amt8"));
            top10.b_amt9_lst.add(rs.getDouble("b_amt9"));
            top10.b_amt10_lst.add(rs.getDouble("b_amt10"));
            top10.b_pri1_lst.add(rs.getDouble("b_pri1"));
            top10.b_pri2_lst.add(rs.getDouble("b_pri2"));
            top10.b_pri3_lst.add(rs.getDouble("b_pri3"));
            top10.b_pri4_lst.add(rs.getDouble("b_pri4"));
            top10.b_pri5_lst.add(rs.getDouble("b_pri5"));
            top10.b_pri6_lst.add(rs.getDouble("b_pri6"));
            top10.b_pri7_lst.add(rs.getDouble("b_pri7"));
            top10.b_pri8_lst.add(rs.getDouble("b_pri8"));
            top10.b_pri9_lst.add(rs.getDouble("b_pri9"));
            top10.b_pri10_lst.add(rs.getDouble("b_pri10"));
            
            top10.s_amt1_lst.add(rs.getDouble("s_amt1"));
            top10.s_amt2_lst.add(rs.getDouble("s_amt2"));
            top10.s_amt3_lst.add(rs.getDouble("s_amt3"));
            top10.s_amt4_lst.add(rs.getDouble("s_amt4"));
            top10.s_amt5_lst.add(rs.getDouble("s_amt5"));
            top10.s_amt6_lst.add(rs.getDouble("s_amt6"));
            top10.s_amt7_lst.add(rs.getDouble("s_amt7"));
            top10.s_amt8_lst.add(rs.getDouble("s_amt8"));
            top10.s_amt9_lst.add(rs.getDouble("s_amt9"));
            top10.s_amt10_lst.add(rs.getDouble("s_amt10"));
            top10.s_pri1_lst.add(rs.getDouble("s_pri1"));
            top10.s_pri2_lst.add(rs.getDouble("s_pri2"));
            top10.s_pri3_lst.add(rs.getDouble("s_pri3"));
            top10.s_pri4_lst.add(rs.getDouble("s_pri4"));
            top10.s_pri5_lst.add(rs.getDouble("s_pri5"));
            top10.s_pri6_lst.add(rs.getDouble("s_pri6"));
            top10.s_pri7_lst.add(rs.getDouble("s_pri7"));
            top10.s_pri8_lst.add(rs.getDouble("s_pri8"));
            top10.s_pri9_lst.add(rs.getDouble("s_pri9"));
            top10.s_pri10_lst.add(rs.getDouble("s_pri10"));
        }
        catch (MocaException e) {
            e.printStackTrace();
            _logger.debug("Exception:" + e.getMessage());;
        }
    }
    
    private void clearTop10Data() {
            top10.time_st.clear();
            top10.b_amt1_lst.clear();
            top10.b_amt2_lst.clear();
            top10.b_amt3_lst.clear();
            top10.b_amt4_lst.clear();
            top10.b_amt5_lst.clear();
            top10.b_amt6_lst.clear();
            top10.b_amt7_lst.clear();
            top10.b_amt8_lst.clear();
            top10.b_amt9_lst.clear();
            top10.b_amt10_lst.clear();
            top10.b_pri1_lst.clear();
            top10.b_pri2_lst.clear();
            top10.b_pri3_lst.clear();
            top10.b_pri4_lst.clear();
            top10.b_pri5_lst.clear();
            top10.b_pri6_lst.clear();
            top10.b_pri7_lst.clear();
            top10.b_pri8_lst.clear();
            top10.b_pri9_lst.clear();
            top10.b_pri10_lst.clear();
            
            top10.s_amt1_lst.clear();
            top10.s_amt2_lst.clear();
            top10.s_amt3_lst.clear();
            top10.s_amt4_lst.clear();
            top10.s_amt5_lst.clear();
            top10.s_amt6_lst.clear();
            top10.s_amt7_lst.clear();
            top10.s_amt8_lst.clear();
            top10.s_amt9_lst.clear();
            top10.s_amt10_lst.clear();
            top10.s_pri1_lst.clear();
            top10.s_pri2_lst.clear();
            top10.s_pri3_lst.clear();
            top10.s_pri4_lst.clear();
            top10.s_pri5_lst.clear();
            top10.s_pri6_lst.clear();
            top10.s_pri7_lst.clear();
            top10.s_pri8_lst.clear();
            top10.s_pri9_lst.clear();
            top10.s_pri10_lst.clear();
            _logger.debug("Top10 data cleared!");;
    }
    
    private void loadTradeData(String market, String coinType) {
        MocaResults rs = null;
        try {
            rs = _moca.executeCommand(
                    "get top10 data where mk ='" + market + "'" +
                    "  and ct = '" + coinType + "'" +
                    "   and sdf = 1 and rtdf = 1");
            rs.next();
            trd.time_st.add(rs.getString("time"));
            trd.amount_lst.add(rs.getDouble("amount"));
            trd.price_lst.add(rs.getDouble("price"));
            trd.type_st.add(rs.getString("type"));
        }
        catch (MocaException e) {
            e.printStackTrace();
            _logger.debug("Exception:" + e.getMessage());;
        }
    }
    
    private void clearTradeData() {
        trd.time_st.clear();
        trd.amount_lst.clear();
        trd.price_lst.clear();
        trd.type_st.clear();
        _logger.debug("TradeData cleared!");
    }
    
    private void loadTickerData(String market, String coinType) {
        MocaResults rs = null;
        try {
            rs = _moca.executeCommand(
                    "get real time trade record where mk ='" + market + "'" +
                    "  and ct = '" + coinType + "'" +
                    "   and sdf = 1");
            
            rs.next();
            tid.symbol_st.add(rs.getString("symbol"));
            tid.time_st.add(rs.getString("time"));
            tid.vol_lst.add(rs.getDouble("vol"));
            tid.high_lst.add(rs.getDouble("high"));
            tid.low_lst.add(rs.getDouble("low"));
            tid.buy_lst.add(rs.getDouble("buy"));
            tid.sell_lst.add(rs.getDouble("sell"));
            tid.last_lst.add(rs.getDouble("last"));
            tid.open_lst.add(rs.getDouble("first"));
        }
        catch (MocaException e) {
            e.printStackTrace();
            _logger.debug("Exception:" + e.getMessage());;
        }
    }
    
    private void clearTickerData() {
            tid.symbol_st.clear();
            tid.time_st.clear();
            tid.vol_lst.clear();
            tid.high_lst.clear();
            tid.low_lst.clear();
            tid.buy_lst.clear();
            tid.sell_lst.clear();
            tid.last_lst.clear();
            tid.open_lst.clear();
            _logger.debug("TickerData cleared!");
    }
    
    @Override
    public String getSymbol() {
        return symbol;
    }
    
    @Override
    public Double getCurPri() {
        int sz = tid.last_lst.size();
        if (sz > 0) {
            return tid.last_lst.get(sz - 1);
        }
        else {
            return null;
        }
    }
    
    @Override
    public Double getHighPri() {
        int sz = tid.high_lst.size();
        if (sz > 0) {
            return tid.high_lst.get(sz - 1);
        }
        else {
            return null;
        }
    }
    
    @Override
    public Double getLowPri() {
        int sz = tid.low_lst.size();
        if (sz > 0) {
            return tid.low_lst.get(sz - 1);
        }
        else {
            return null;
        }
    }
    
    @Override
    public Double getOpenPri() {
        int sz = tid.open_lst.size();
        if (sz > 0) {
            return tid.open_lst.get(sz - 1);
        }
        else {
            return null;
        }
    }
    
    @Override
    public Double getLastPri() {
        int sz = tid.last_lst.size();
        if (sz > 0) {
            return tid.last_lst.get(sz - 1);
        }
        else {
            return null;
        }
    }
    
    @Override
    public Double getVolume() {
        int sz = tid.vol_lst.size();
        if (sz > 0) {
            return tid.vol_lst.get(sz - 1);
        }
        else {
            return null;
        }
    }
    
    @Override
    public eSTOCKTREND getStockTrend() {
        if (st == null) {
            calStockTrend();
        }
        return st;
    }
    
    @Override
    public eSTOCKTREND calStockTrend() {
        int sz = tid.last_lst.size();
        if (sz < STEP_FOR_CAL_TREND * 2 + 1) {
            st = eSTOCKTREND.NA;
        }
        else {
            double first = tid.last_lst.get(sz - 1 - STEP_FOR_CAL_TREND * 2);
            double middle = tid.last_lst.get(sz - 1 - STEP_FOR_CAL_TREND * 1);
            double last = tid.last_lst.get(sz - 1);
            
            _logger.info("first:" + first);
            _logger.info("middle:" + middle);
            _logger.info("last:" + last);
            
            _logger.info("first + SMALL_PRICE_DIFF:" + (first + SMALL_PRICE_DIFF));
            _logger.info("middle + SMALL_PRICE_DIFF:" + (middle + SMALL_PRICE_DIFF));
            _logger.info("last + SMALL_PRICE_DIFF:" + (last + SMALL_PRICE_DIFF));
            
            _logger.info("first + BIG_PRICE_DIFF:" + (first + BIG_PRICE_DIFF));
            _logger.info("middle + BIG_PRICE_DIFF:" + (middle + BIG_PRICE_DIFF));
            _logger.info("last + BIG_PRICE_DIFF:" + (last + BIG_PRICE_DIFF));
            
            if (first + SMALL_PRICE_DIFF < middle && middle + SMALL_PRICE_DIFF < last) {
                st = eSTOCKTREND.UP;
            }
            else if (first > middle + SMALL_PRICE_DIFF && middle > last + SMALL_PRICE_DIFF) {
                st = eSTOCKTREND.DOWN;
            }
            else if (first + SMALL_PRICE_DIFF < middle && middle > last + BIG_PRICE_DIFF) {
                st = eSTOCKTREND.CDOWN;
            }
            else if (first > middle + SMALL_PRICE_DIFF && middle + BIG_PRICE_DIFF < last) {
                st = eSTOCKTREND.CUP;
            }
            else {
                st = eSTOCKTREND.EQUAL;
            }
        }
        return st;
    }
}
