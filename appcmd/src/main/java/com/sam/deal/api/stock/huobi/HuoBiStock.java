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
    


    /**
     * @param args
     */
    private String mk;
    private String ct;
    private String symbol;
    private Top10Data top10;
    private TradeData trd;
    private TickerData tid;
    private int MAX_SZ = 800;
    private int STEP_FOR_CAL_TREND = 1;
    private double BIG_PRICE_DIFF = 5;
    private double SMALL_PRICE_DIFF = 2;
    private double LAST_PRICE_BOX_GAP = 0;
    private final MocaContext _moca;
    private final CrudManager _manager;
    private eSTOCKTREND st = null;
    
    public class Top10Data{
        String symbol = null;
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
        
        public void setSymbol(String syb) {
            symbol = syb;
        }
        
        Top10Data() {
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
        
        public boolean dumpDatatoDB() {
            try {
                int sz = time_st.size();
                for (int i = 0; i < sz; i++) {
                _moca.executeCommand("publish data " + " where symbol = '"
                        + symbol + "'" + "   and time = '" + time_st.get(i) + "'"
                        + "   and b_amt1 = " + b_amt1_lst.get(i)
                        + "   and b_pri1 = " + b_pri1_lst.get(i)
                        + "   and b_amt2 = " + b_amt2_lst.get(i)
                        + "   and b_pri2 = " + b_pri2_lst.get(i)
                        + "   and b_amt3 = " + b_amt3_lst.get(i)
                        + "   and b_pri3 = " + b_pri3_lst.get(i)
                        + "   and b_amt4 = " + b_amt4_lst.get(i)
                        + "   and b_pri4 = " + b_pri4_lst.get(i)
                        + "   and b_amt5 = " + b_amt5_lst.get(i)
                        + "   and b_pri5 = " + b_pri5_lst.get(i)
                        + "   and b_amt6 = " + b_amt6_lst.get(i)
                        + "   and b_pri6 = " + b_pri6_lst.get(i)
                        + "   and b_amt7 = " + b_amt7_lst.get(i)
                        + "   and b_pri7 = " + b_pri7_lst.get(i)
                        + "   and b_amt8 = " + b_amt8_lst.get(i)
                        + "   and b_pri8 = " + b_pri8_lst.get(i)
                        + "   and b_amt9 = " + b_amt9_lst.get(i)
                        + "   and b_pri9 = " + b_pri9_lst.get(i)
                        + "   and b_amt10 = " + b_amt10_lst.get(i)
                        + "   and b_pri10 = " + b_pri10_lst.get(i)
                        + "   and s_amt1 = " + s_amt1_lst.get(i)
                        + "   and s_pri1 = " + s_pri1_lst.get(i)
                        + "   and s_amt2 = " + s_amt2_lst.get(i)
                        + "   and s_pri2 = " + s_pri2_lst.get(i)
                        + "   and s_amt3 = " + s_amt3_lst.get(i)
                        + "   and s_pri3 = " + s_pri3_lst.get(i)
                        + "   and s_amt4 = " + s_amt4_lst.get(i)
                        + "   and s_pri4 = " + s_pri4_lst.get(i)
                        + "   and s_amt5 = " + s_amt5_lst.get(i)
                        + "   and s_pri5 = " + s_pri5_lst.get(i)
                        + "   and s_amt6 = " + s_amt6_lst.get(i)
                        + "   and s_pri6 = " + s_pri6_lst.get(i)
                        + "   and s_amt7 = " + s_amt7_lst.get(i)
                        + "   and s_pri7 = " + s_pri7_lst.get(i)
                        + "   and s_amt8 = " + s_amt8_lst.get(i)
                        + "   and s_pri8 = " + s_pri8_lst.get(i)
                        + "   and s_amt9 = " + s_amt9_lst.get(i)
                        + "   and s_pri9 = " + s_pri9_lst.get(i)
                        + "   and s_amt10 = " + b_amt10_lst.get(i)
                        + "   and s_pri10 = " + b_pri10_lst.get(i)
                        + "   and p_new = " + p_new_lst.get(i) + "   and p_last = "
                        + p_last_lst.get(i) + "   and p_low = " + p_low_lst.get(i)
                        + "   and p_open = " + p_open_lst.get(i) + "   and p_high = "
                        + p_high_lst.get(i) + "   and total = " + total_lst.get(i)
                        + "   and ins_dt = sysdate " + "|"
                        + "create record where table_name = 'hb_top10_data' and @* ");
                }
            } catch (MocaException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                _logger.error(e.getMessage());
                _logger.info("dump top10 data error");
                return false;
            }
            return true;
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
        public boolean dumpDatatoDB() {
            try {
                int sz = symbol_st.size();
                for (int i = 0; i < sz; i++) {
                    _moca.executeCommand("publish data "
                        + " where symbol = '" + symbol_st.get(i) + "'"
                        + "   and time = '" + time_st.get(i) + "'"
                        + "   and amount = " + amount_lst.get(i)
                        + "   and price = " + price_lst.get(i)
                        + "   and type = '" + type_st.get(i) + "'"
                        + "   and ins_dt = sysdate " + "|"
                        + "create record where table_name = 'hb_trade_data' and @* ");}
            } catch (MocaException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                _logger.error(e.getMessage());
                _logger.info("dump TradeData error");
                return false;
            }
            return true;
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
        public boolean dumpDatatoDB() {
            try {
                int sz = symbol_st.size();
                for (int i = 0; i < sz; i++) {
                    _moca.executeCommand("publish data where symbol = '"
                            + symbol_st.get(i) + "'" + "   and time = '" + time_st.get(i) + "'"
                            + "   and high = " + high_lst.get(i) + "   and low = " + low_lst.get(i)
                            + "   and vol = " + vol_lst.get(i) + "   and buy = " + buy_lst.get(i)
                            + "   and sell = " + sell_lst.get(i) + "   and last = " + last_lst.get(i)
                            + "   and first = " + open_lst.get(i)
                            + "   and ins_dt = sysdate " + "|"
                            + "create record where table_name = 'hb_real_data' and @* ");
                    }
            } catch (MocaException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                _logger.error(e.getMessage());
                _logger.info("dump TickerData error");
                return false;
            }
            return true;
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
        HuoBiStock s = new HuoBiStock("chn", "btc");
        s.loadMarketData();
    }
    
    public HuoBiStock(String market, String coinType)
    {
        _moca = MocaUtils.currentContext();
        _manager = MocaUtils.crudManager(_moca);
        mk = market;
        ct =coinType;
        init();
    }
    
    public void init() {
        String polvar = (ct.equalsIgnoreCase("btc") ? "BTC" : "LTC");
        try {
            MocaResults rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '" + polvar
                               + "' and polval ='STOCK_QUEUE_SIZE' and grp_id = '----'");
            rs.next();
            MAX_SZ = rs.getInt("rtnum1");
            _logger.info("got policy, STOCK_QUEUE_SIZE:" + MAX_SZ);
        } catch (MocaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.error(e.getMessage());
            _logger.info("Get policy STOCK_QUEUE_SIZE error, use default value 360.");
            MAX_SZ = 360;
        }
        
        try {
            MocaResults rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '" + polvar
                               + "' and polval ='PRICE_DIFF' and grp_id = '----'");
            rs.next();
            SMALL_PRICE_DIFF = rs.getDouble("rtflt1");
            BIG_PRICE_DIFF = rs.getDouble("rtflt2");
            _logger.info("get policy SMALL_PRICE_DIFF:" + SMALL_PRICE_DIFF);
            _logger.info("get policy BIG_PRICE_DIFF:" + BIG_PRICE_DIFF);
        } catch (MocaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.error(e.getMessage());
            if (polvar.equalsIgnoreCase("BTC")) {
                SMALL_PRICE_DIFF = 2;
                BIG_PRICE_DIFF = 5;
                _logger.info("get policy error, use default SMALL_PRICE_DIFF:" + SMALL_PRICE_DIFF);
                _logger.info("get policy error, use default BIG_PRICE_DIFF:" + BIG_PRICE_DIFF);
            }
            else {
                SMALL_PRICE_DIFF = 0.1;
                BIG_PRICE_DIFF = 0.5;
                _logger.info("get policy error, use default SMALL_PRICE_DIFF:" + SMALL_PRICE_DIFF);
                _logger.info("get policy error, use default BIG_PRICE_DIFF:" + BIG_PRICE_DIFF);
            }
        }
        
        try {
            MocaResults rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '" + polvar
                               + "' and polval ='STEP_FOR_CAL_TREND' and grp_id = '----'");
            rs.next();
            STEP_FOR_CAL_TREND = rs.getInt("rtnum1");
            _logger.info("got policy, STEP_FOR_CAL_TREND:" + STEP_FOR_CAL_TREND);
        } catch (MocaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.error(e.getMessage());
            _logger.info("Get policy STEP_FOR_CAL_TREND error, use default value 1.");
            STEP_FOR_CAL_TREND = 1;
        }
        
        try {
            MocaResults rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '" + polvar
                               + "' and polval ='LAST_PRICE_BOX_GAP' and grp_id = '----'");
            rs.next();
            LAST_PRICE_BOX_GAP = rs.getInt("rtflt1");
            _logger.info("got policy, LAST_PRICE_BOX_GAP:" + LAST_PRICE_BOX_GAP);
        } catch (MocaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.error(e.getMessage());
            if (polvar.equalsIgnoreCase("BTC")) {
                LAST_PRICE_BOX_GAP = 50;
                _logger.info("get policy error, use default LAST_PRICE_BOX_GAP:" + LAST_PRICE_BOX_GAP);
            }
            else {
                LAST_PRICE_BOX_GAP = 1;
                _logger.info("get policy error, use default LAST_PRICE_BOX_GAP:" + LAST_PRICE_BOX_GAP);
            }
        }
        
        top10 = new Top10Data();
        trd = new TradeData();
        tid = new TickerData();
    }
    
    public void loadMarketData() {
        loadTop10Data();
        loadTradeData();
        loadTickerData();
        calStockTrend();
    }
    
    public void clearMarketData() {
        clearTop10Data();
        clearTradeData();
        clearTickerData();
        this.calStockTrend();
        _logger.debug("cleared all market data, stock trend is:" + this.st);
    }
    
    private void loadTop10Data() {
        MocaResults rs = null;
        try {
            rs = _moca.executeCommand(
                    "get top10 data where mk ='" + mk + "'" +
                    "  and ct = '" + ct + "'" +
                    "   and sdf = 0 and rtdf = 0");
            
            rs.next();
            this.symbol = rs.getString("symbol");
            top10.setSymbol(symbol);
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

            top10.p_new_lst.add(rs.getDouble("p_new"));
            top10.p_last_lst.add(rs.getDouble("p_last"));
            top10.p_low_lst.add(rs.getDouble("p_low"));
            top10.p_open_lst.add(rs.getDouble("p_open"));
            top10.p_high_lst.add(rs.getDouble("p_high"));
            top10.total_lst.add(rs.getDouble("total"));
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
            
            top10.p_new_lst.clear();
            top10.p_last_lst.clear();
            top10.p_low_lst.clear();
            top10.p_open_lst.clear();
            top10.p_high_lst.clear();
            top10.total_lst.clear();
            
            _logger.debug("Top10 data cleared!");;
    }
    
    private void loadTradeData() {
        MocaResults rs = null;
        try {
            rs = _moca.executeCommand(
                    "get top10 data where mk ='" + mk + "'" +
                    "  and ct = '" + ct + "'" +
                    "   and sdf = 0 and rtdf = 1");
            rs.next();
            trd.symbol_st.add(rs.getString("symbol"));
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
        trd.symbol_st.clear();
        trd.time_st.clear();
        trd.amount_lst.clear();
        trd.price_lst.clear();
        trd.type_st.clear();
        _logger.debug("TradeData cleared!");
    }
    
    private void loadTickerData() {
        MocaResults rs = null;
        try {
            rs = _moca.executeCommand(
                    "get real time trade record where mk ='" + mk + "'" +
                    "  and ct = '" + ct + "'" +
                    "   and sdf = 0");
            
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
    
    public boolean isLstPriTurnaround(boolean inc_flg) {
        if (!isMinMaxLstPriMatchBoxGap()) {
            _logger.info("last_lst does not match isMinMaxLstPriMatchBoxGap, isLstPriTurnaround is false.");
            return false;
        }
        int sz = tid.last_lst.size();
        double lstPri = tid.last_lst.get(sz - 1);
        double prePri = tid.last_lst.get(sz - 2);
        double lstDetPri = (inc_flg ? (lstPri - prePri) : (prePri - lstPri));
        _logger.info("lstDetPri is:" + lstDetPri + " size:" + sz + " inc_flg:" + inc_flg);
        if (lstDetPri >= SMALL_PRICE_DIFF) {
            _logger.info("lst price is:" + lstPri + ", pre price is:" + prePri + (inc_flg ? "big than" : "small than") + SMALL_PRICE_DIFF + ", isLstPriTurnaround return true.");
            return true;
        }
        else {
            _logger.info("lst price is:" + lstPri + ", pre price is:" + prePri + (inc_flg ? " not big than" : " not small than") + SMALL_PRICE_DIFF + ", isLstPriTurnaround return false.");
            return false;
        }
    }
    
    public boolean isLstPriAboveWaterLevel(double topPct) {
        if (!isMinMaxLstPriMatchBoxGap()) {
            _logger.info("last_lst does not match isMinMaxLstPriMatchBoxGap, isLstPriAboveWaterLevel is false.");
            return false;
        }
        int sz = tid.last_lst.size();
        double lstPri = tid.last_lst.get(sz - 1);
        double maxpri = lstPri;
        double minpri = lstPri;
        for (int i = 0; i < sz - 1; i++) {
            double pri = tid.last_lst.get(i);
            if (maxpri < pri) {
                maxpri = pri;
            }
            if (minpri > pri) {
                minpri = pri;
            }
        }
        _logger.info("maxPri:" + maxpri + "\n minPri:" + minpri + "\n lstPri:" + lstPri);
        
        if ((lstPri - minpri) / (maxpri - minpri) >= topPct) {
            _logger.info("lstPri is " + (topPct*100) + "% above:[" + minpri+"," + maxpri + "], return true");
            return true;
        }
        _logger.info("lstPri is not " + (topPct*100 ) + "% above:" + minpri+"," + maxpri + "], return false");
        return false;
    }
    
    public boolean isLstPriBreakUpBorder() {
        if (!isMinMaxLstPriMatchBoxGap()) {
            _logger.info("last_lst does not match isMinMaxLstPriMatchBoxGap, isLstPriBreakUpBorder is false.");
            return false;
        }
        int sz = tid.last_lst.size();
        double lstPri = tid.last_lst.get(sz - 1);
        double maxpri = lstPri;
        double minpri = lstPri;
        for (int i = 0; i < sz - 1; i++) {
            double pri = tid.last_lst.get(i);
            if (maxpri < pri) {
                maxpri = pri;
            }
            if (minpri > pri) {
                minpri = pri;
            }
        }
        _logger.info("maxPri:" + maxpri + "\n minPri:" + minpri + "\n lstPri:" + lstPri);
        
        if (lstPri >= maxpri) {
            _logger.info("lstPri is breaking above border" + maxpri + ", return true");
            return true;
        }
        _logger.info("lstPri is not breaking above border" + maxpri + ", return false");
        return false;
    }
    
    public boolean isLstPriUnderWaterLevel(double bottomPct) {
        int sz = tid.last_lst.size();
        if (!isMinMaxLstPriMatchBoxGap()) {
            _logger.info("last_lst does not match isMinMaxLstPriMatchBoxGap, isLstPriUnderWaterLevel is false.");
            return false;
        }
        double lstPri = tid.last_lst.get(sz - 1);
        double maxpri = lstPri;
        double minpri = lstPri;
        for (int i = 0; i < sz - 1; i++) {
            double pri = tid.last_lst.get(i);
            if (maxpri < pri) {
                maxpri = pri;
            }
            if (minpri > pri) {
                minpri = pri;
            }
        }
        _logger.info("maxPri:" + maxpri + "\n minPri:" + minpri + "\n lstPri:" + lstPri);
        
        if ((lstPri - minpri) / (maxpri - minpri) <= bottomPct) {
            _logger.info("lstPri is " + (bottomPct*100) + "% under:[" + minpri+"," + maxpri + "], return true");
            return true;
        }
        _logger.info("lstPri is not " + (bottomPct*100 ) + "% under:" + minpri+"," + maxpri + "], return false");
        return false;
    }
    
    public boolean isLstPriBreakButtomBorder() {
        int sz = tid.last_lst.size();
        if (!isMinMaxLstPriMatchBoxGap()) {
            _logger.info("last_lst does not match isMinMaxLstPriMatchBoxGap, isLstPriBreakButtomBorder is false.");
            return false;
        }
        double lstPri = tid.last_lst.get(sz - 1);
        double maxpri = lstPri;
        double minpri = lstPri;
        for (int i = 0; i < sz - 1; i++) {
            double pri = tid.last_lst.get(i);
            if (maxpri < pri) {
                maxpri = pri;
            }
            if (minpri > pri) {
                minpri = pri;
            }
        }
        _logger.info("maxPri:" + maxpri + "\n minPri:" + minpri + "\n lstPri:" + lstPri);
        
        if (lstPri <= minpri) {
            _logger.info("lstPri is breaking bottom border" + minpri + ", return true");
            return true;
        }
        _logger.info("lstPri is breaking bottom border" + minpri + ", return false");
        return false;
    }
    
    public boolean isMinMaxLstPriMatchBoxGap() {
        int sz = tid.last_lst.size();
        if (sz <= 0) {
            _logger.info("last price queue is empty, isMinMaxLstPriMatchBoxGap return false");
            return false;
        }
        double maxpri = tid.last_lst.get(sz - 1);
        double minpri = maxpri;
        for (int i = 0; i < sz - 1; i++) {
            double pri = tid.last_lst.get(i);
            if (maxpri < pri) {
                maxpri = pri;
            }
            if (minpri > pri) {
                minpri = pri;
            }
        }
        _logger.info("maxPri:" + maxpri + "\n minPri:" + minpri + "\n LAST_PRICE_BOX_GAP:" + LAST_PRICE_BOX_GAP);
        
        if ((maxpri - minpri) >= LAST_PRICE_BOX_GAP) {
            _logger.info("min/max price matches max gap, return true");
            return true;
        }
        _logger.info("min/max price DOES NOT matche max gap, return false");
        return false;
    }
    
    public boolean dumpDatatoDB() {
        boolean t1 = this.top10.dumpDatatoDB();
        boolean t2 = this.trd.dumpDatatoDB();
        boolean t3 = this.tid.dumpDatatoDB();
        return t1 && t2 && t3;
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
