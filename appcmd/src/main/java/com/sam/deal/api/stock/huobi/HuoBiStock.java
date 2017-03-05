package com.sam.deal.api.stock.huobi;

import java.util.List;
import java.util.ArrayList;

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
    private int SECONDS_AS_LOOP_GAP = 0;
    private double LAST_PRICE_BOX_GAP_MIN = 0;
    private double LAST_PRICE_BOX_GAP_MAX = 0;
    private double LAST_PRICE_BOX_GAP_MAX_RATIO = 1.5;
    private boolean IS_IN_UNSTABLE_MODE = false;
    private BoundArrayList<Double> det_buy_turnaround = new BoundArrayList<Double>(3);
    private BoundArrayList<Double> det_sell_turnaround = new BoundArrayList<Double>(3);
    private BoundArrayList<Double> det_buy_plusback = new BoundArrayList<Double>(3);
    private BoundArrayList<Double> det_sell_plusback = new BoundArrayList<Double>(3);
    private int SHORT_TERM = 0;
    private int LONG_TERM = 0;
    private String SHORT_PERIOD = "060";
    private String LONG_PERIOD = "100";
    private int loadCount = 0;
    private final MocaContext _moca;
    private final CrudManager _manager;
    private eSTOCKTREND st = null;
    double maxpri = 0;
    double minpri = 100000;
    double history_maxpri = 0;
    double history_minpri = 100000;
    String pre_gloden_time ="";
    String pre_dead_time ="";
    
    public double getSmallPriceDiff() {
        return SMALL_PRICE_DIFF;
    }
    
    public double getBigPriceDiff() {
        return BIG_PRICE_DIFF;
    }
    
    public double getLastPriceBoxGapMin() {
        return LAST_PRICE_BOX_GAP_MIN;
    }
    
    public double getLastPriceBoxGapMax() {
        return LAST_PRICE_BOX_GAP_MAX;
    }
    
    public Top10Data getTop10Data() {
        return top10;
    }
    
    public TradeData getTradeData() {
        return trd;
    }
    
    public TickerData geTickerData () {
        return tid;
    }
    
    public class Top10Data{
        public String symbol = null;
        public List<String> time_st =  null;
        public ArrayList<BoundArrayList<Double>> b_amt_lst = null;
        public ArrayList<BoundArrayList<Double>> b_pri_lst = null;
        
        public ArrayList<BoundArrayList<Double>> s_amt_lst = null;
        public ArrayList<BoundArrayList<Double>> s_pri_lst = null;
        
        public List<Double> p_new_lst = null;
        public List<Double> p_last_lst = null;
        public List<Double> p_low_lst = null;
        public List<Double> p_open_lst = null;
        public List<Double> p_high_lst = null;
        public List<Double> total_lst = null;
        
        public void setSymbol(String syb) {
            symbol = syb;
        }
        
        Top10Data() {
            time_st = new BoundArrayList<String>(MAX_SZ);
            
            b_amt_lst = new ArrayList<BoundArrayList<Double>>();
            b_pri_lst = new ArrayList<BoundArrayList<Double>>();

            s_amt_lst = new ArrayList<BoundArrayList<Double>>();
            s_pri_lst = new ArrayList<BoundArrayList<Double>>();
            
            for (int i = 0; i < 10 ; i++) {
                b_amt_lst.add(new BoundArrayList<Double>(MAX_SZ));
                b_pri_lst.add(new BoundArrayList<Double>(MAX_SZ));
                s_amt_lst.add(new BoundArrayList<Double>(MAX_SZ));
                s_pri_lst.add(new BoundArrayList<Double>(MAX_SZ));
            }
            
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
                        + "   and b_amt1 = " + b_amt_lst.get(0).get(i)
                        + "   and b_pri1 = " + b_pri_lst.get(0).get(i)
                        + "   and b_amt2 = " + b_amt_lst.get(1).get(i)
                        + "   and b_pri2 = " + b_pri_lst.get(1).get(i)
                        + "   and b_amt3 = " + b_amt_lst.get(2).get(i)
                        + "   and b_pri3 = " + b_pri_lst.get(2).get(i)
                        + "   and b_amt4 = " + b_amt_lst.get(3).get(i)
                        + "   and b_pri4 = " + b_pri_lst.get(3).get(i)
                        + "   and b_amt5 = " + b_amt_lst.get(4).get(i)
                        + "   and b_pri5 = " + b_pri_lst.get(4).get(i)
                        + "   and b_amt6 = " + b_amt_lst.get(5).get(i)
                        + "   and b_pri6 = " + b_pri_lst.get(5).get(i)
                        + "   and b_amt7 = " + b_amt_lst.get(6).get(i)
                        + "   and b_pri7 = " + b_pri_lst.get(6).get(i)
                        + "   and b_amt8 = " + b_amt_lst.get(7).get(i)
                        + "   and b_pri8 = " + b_pri_lst.get(7).get(i)
                        + "   and b_amt9 = " + b_amt_lst.get(8).get(i)
                        + "   and b_pri9 = " + b_pri_lst.get(8).get(i)
                        + "   and b_amt10 = " + b_amt_lst.get(9).get(i)
                        + "   and b_pri10 = " + b_pri_lst.get(9).get(i)
                        + "   and s_amt1 = " + s_amt_lst.get(0).get(i)
                        + "   and s_pri1 = " + s_pri_lst.get(0).get(i)
                        + "   and s_amt2 = " + s_amt_lst.get(1).get(i)
                        + "   and s_pri2 = " + s_pri_lst.get(1).get(i)
                        + "   and s_amt3 = " + s_amt_lst.get(2).get(i)
                        + "   and s_pri3 = " + s_pri_lst.get(2).get(i)
                        + "   and s_amt4 = " + s_amt_lst.get(3).get(i)
                        + "   and s_pri4 = " + s_pri_lst.get(3).get(i)
                        + "   and s_amt5 = " + s_amt_lst.get(4).get(i)
                        + "   and s_pri5 = " + s_pri_lst.get(4).get(i)
                        + "   and s_amt6 = " + s_amt_lst.get(5).get(i)
                        + "   and s_pri6 = " + s_pri_lst.get(5).get(i)
                        + "   and s_amt7 = " + s_amt_lst.get(6).get(i)
                        + "   and s_pri7 = " + s_pri_lst.get(6).get(i)
                        + "   and s_amt8 = " + s_amt_lst.get(7).get(i)
                        + "   and s_pri8 = " + s_pri_lst.get(7).get(i)
                        + "   and s_amt9 = " + s_amt_lst.get(8).get(i)
                        + "   and s_pri9 = " + s_pri_lst.get(8).get(i)
                        + "   and s_amt10 = " + s_amt_lst.get(9).get(i)
                        + "   and s_pri10 = " + s_pri_lst.get(9).get(i)
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
        public List<String> symbol_st =  null;
        public List<String> time_st =  null;
        public List<Double> amount_lst = null;
        public List<Double> price_lst = null;
        public List<String> type_st =  null;
        
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
        public List<String> symbol_st =  null;
        public List<String> time_st =  null;
        public List<Double> vol_lst = null;
        public List<Double> high_lst = null;
        public List<Double> low_lst = null;
        public List<Double> buy_lst = null;
        public List<Double> sell_lst = null;
        public List<Double> last_lst = null;
        public List<Double> open_lst = null;
        
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
            LAST_PRICE_BOX_GAP_MIN = rs.getInt("rtflt1");
            LAST_PRICE_BOX_GAP_MAX = rs.getInt("rtflt2");
            _logger.info("got policy, LAST_PRICE_BOX_GAP_MIN:" + LAST_PRICE_BOX_GAP_MIN + ", LAST_PRICE_BOX_GAP_MAX:" + LAST_PRICE_BOX_GAP_MAX);
        } catch (MocaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.error(e.getMessage());
            if (polvar.equalsIgnoreCase("BTC")) {
                LAST_PRICE_BOX_GAP_MIN = 50;
                LAST_PRICE_BOX_GAP_MAX = 80;
                _logger.info("get policy error, use default LAST_PRICE_BOX_GAP_MIN:" + LAST_PRICE_BOX_GAP_MIN + ", LAST_PRICE_BOX_GAP_MAX:" + LAST_PRICE_BOX_GAP_MAX);
            }
            else {
                LAST_PRICE_BOX_GAP_MIN = 0.5;
                LAST_PRICE_BOX_GAP_MAX = 1;
                _logger.info("get policy error, use default LAST_PRICE_BOX_GAP_MIN:" + LAST_PRICE_BOX_GAP_MIN + ", LAST_PRICE_BOX_GAP_MAX:" + LAST_PRICE_BOX_GAP_MAX);
            }
        }
        
        try {
            MocaResults rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '" + polvar
                               + "' and polval ='LAST_PRICE_BOX_GAP_RATIO' and grp_id = '----'");
            rs.next();
            LAST_PRICE_BOX_GAP_MAX_RATIO = rs.getDouble("rtflt1");
            _logger.info("got policy, LAST_PRICE_BOX_GAP_MAX_RATIO:" + LAST_PRICE_BOX_GAP_MAX_RATIO);
        } catch (MocaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _logger.error(e.getMessage());
            if (polvar.equalsIgnoreCase("BTC")) {
                LAST_PRICE_BOX_GAP_MAX_RATIO = 1.5;
                _logger.info("get policy BTC error, use default LAST_PRICE_BOX_GAP_MAX_RATIO:" + LAST_PRICE_BOX_GAP_MAX_RATIO);
            }
            else {
                LAST_PRICE_BOX_GAP_MAX_RATIO = 1.5;
                _logger.info("get policy LTC error, use default LAST_PRICE_BOX_GAP_MAX_RATIO:" + LAST_PRICE_BOX_GAP_MAX_RATIO);
            }
        }
        
        try {
            MocaResults rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '" + polvar
                               + "' and polval ='SHORT_LONG_TERM' and grp_id = '----'");
            rs.next();
            SHORT_TERM = rs.getInt("rtnum1");
            LONG_TERM = rs.getInt("rtnum2");
            SHORT_PERIOD = rs.getString("rtstr1");
            LONG_PERIOD = rs.getString("rtstr2");
            _logger.info("got policy, SHORT_LONG_TERM, SHORT_TERM:" + SHORT_TERM + ", LONG_TERM:" + LONG_TERM + ", SHORT_PERIOD:" + SHORT_PERIOD + ", LONG_PERIOD:" + LONG_PERIOD);
        } catch (MocaException e) {
            _logger.error(e.getMessage());
            if (polvar.equalsIgnoreCase("BTC")) {
                SHORT_TERM = 12 * 60 * 5; //1 hour since 5 seconds per fetch
                LONG_TERM = 12 * 60 * 10; //10 hour
                _logger.info("got policy error, use default SHORT_LONG_TERM, SHORT_TERM:" + SHORT_TERM + ", LONG_TERM:" + LONG_TERM);
            }
            else {
                SHORT_TERM = 12 * 60 * 5;
                LONG_TERM = 12 * 60 * 10;
                _logger.info("got policy error, use default SHORT_LONG_TERM, SHORT_TERM:" + SHORT_TERM + ", LONG_TERM:" + LONG_TERM);
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
        int sz = trd.price_lst.size();
        loadCount++;
    }
    
    public void clearMarketData() {
        clearTop10Data();
        clearTradeData();
        clearTickerData();
        this.calStockTrend();
        loadCount = 0;
        history_maxpri = maxpri;
        history_minpri = minpri;
        maxpri = 0;
        minpri = 100000;
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
            top10.b_amt_lst.get(0).add(rs.getDouble("b_amt1"));
            top10.b_amt_lst.get(1).add(rs.getDouble("b_amt2"));
            top10.b_amt_lst.get(2).add(rs.getDouble("b_amt3"));
            top10.b_amt_lst.get(3).add(rs.getDouble("b_amt4"));
            top10.b_amt_lst.get(4).add(rs.getDouble("b_amt5"));
            top10.b_amt_lst.get(5).add(rs.getDouble("b_amt6"));
            top10.b_amt_lst.get(6).add(rs.getDouble("b_amt7"));
            top10.b_amt_lst.get(7).add(rs.getDouble("b_amt8"));
            top10.b_amt_lst.get(8).add(rs.getDouble("b_amt9"));
            top10.b_amt_lst.get(9).add(rs.getDouble("b_amt10"));
            top10.b_pri_lst.get(0).add(rs.getDouble("b_pri1"));
            top10.b_pri_lst.get(1).add(rs.getDouble("b_pri2"));
            top10.b_pri_lst.get(2).add(rs.getDouble("b_pri3"));
            top10.b_pri_lst.get(3).add(rs.getDouble("b_pri4"));
            top10.b_pri_lst.get(4).add(rs.getDouble("b_pri5"));
            top10.b_pri_lst.get(5).add(rs.getDouble("b_pri6"));
            top10.b_pri_lst.get(6).add(rs.getDouble("b_pri7"));
            top10.b_pri_lst.get(7).add(rs.getDouble("b_pri8"));
            top10.b_pri_lst.get(8).add(rs.getDouble("b_pri9"));
            top10.b_pri_lst.get(9).add(rs.getDouble("b_pri10"));
            
            top10.s_amt_lst.get(0).add(rs.getDouble("s_amt1"));
            top10.s_amt_lst.get(1).add(rs.getDouble("s_amt2"));
            top10.s_amt_lst.get(2).add(rs.getDouble("s_amt3"));
            top10.s_amt_lst.get(3).add(rs.getDouble("s_amt4"));
            top10.s_amt_lst.get(4).add(rs.getDouble("s_amt5"));
            top10.s_amt_lst.get(5).add(rs.getDouble("s_amt6"));
            top10.s_amt_lst.get(6).add(rs.getDouble("s_amt7"));
            top10.s_amt_lst.get(7).add(rs.getDouble("s_amt8"));
            top10.s_amt_lst.get(8).add(rs.getDouble("s_amt9"));
            top10.s_amt_lst.get(9).add(rs.getDouble("s_amt10"));
            top10.s_pri_lst.get(0).add(rs.getDouble("s_pri1"));
            top10.s_pri_lst.get(1).add(rs.getDouble("s_pri2"));
            top10.s_pri_lst.get(2).add(rs.getDouble("s_pri3"));
            top10.s_pri_lst.get(3).add(rs.getDouble("s_pri4"));
            top10.s_pri_lst.get(4).add(rs.getDouble("s_pri5"));
            top10.s_pri_lst.get(5).add(rs.getDouble("s_pri6"));
            top10.s_pri_lst.get(6).add(rs.getDouble("s_pri7"));
            top10.s_pri_lst.get(7).add(rs.getDouble("s_pri8"));
            top10.s_pri_lst.get(8).add(rs.getDouble("s_pri9"));
            top10.s_pri_lst.get(9).add(rs.getDouble("s_pri10"));

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
            top10.b_amt_lst.get(0).clear();
            top10.b_amt_lst.get(1).clear();
            top10.b_amt_lst.get(2).clear();
            top10.b_amt_lst.get(3).clear();
            top10.b_amt_lst.get(4).clear();
            top10.b_amt_lst.get(5).clear();
            top10.b_amt_lst.get(6).clear();
            top10.b_amt_lst.get(7).clear();
            top10.b_amt_lst.get(8).clear();
            top10.b_amt_lst.get(9).clear();
            top10.b_pri_lst.get(0).clear();
            top10.b_pri_lst.get(1).clear();
            top10.b_pri_lst.get(2).clear();
            top10.b_pri_lst.get(3).clear();
            top10.b_pri_lst.get(4).clear();
            top10.b_pri_lst.get(5).clear();
            top10.b_pri_lst.get(6).clear();
            top10.b_pri_lst.get(7).clear();
            top10.b_pri_lst.get(8).clear();
            top10.b_pri_lst.get(9).clear();
            
            top10.s_amt_lst.get(0).clear();
            top10.s_amt_lst.get(1).clear();
            top10.s_amt_lst.get(2).clear();
            top10.s_amt_lst.get(3).clear();
            top10.s_amt_lst.get(4).clear();
            top10.s_amt_lst.get(5).clear();
            top10.s_amt_lst.get(6).clear();
            top10.s_amt_lst.get(7).clear();
            top10.s_amt_lst.get(8).clear();
            top10.s_amt_lst.get(9).clear();
            top10.s_pri_lst.get(0).clear();
            top10.s_pri_lst.get(1).clear();
            top10.s_pri_lst.get(2).clear();
            top10.s_pri_lst.get(3).clear();
            top10.s_pri_lst.get(4).clear();
            top10.s_pri_lst.get(5).clear();
            top10.s_pri_lst.get(6).clear();
            top10.s_pri_lst.get(7).clear();
            top10.s_pri_lst.get(8).clear();
            top10.s_pri_lst.get(9).clear();
            
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

        int sz = tid.last_lst.size();
        
        if (SECONDS_AS_LOOP_GAP == 0) {
            String polvar = (ct.equalsIgnoreCase("btc") ? "BTC" : "LTC");
            try {
                MocaResults rs = _moca.executeCommand("list policies where polcod ='HUOBI' and polvar = '"
                                                      + polvar + "' and polval = 'LOOP-PARAM' and grp_id = '----'");
                rs.next();
                SECONDS_AS_LOOP_GAP = rs.getInt("rtnum2");
            }
            catch (MocaException e) {
                e.printStackTrace();
                _logger.error(e.getMessage());
                _logger.info("Reading policy LOOP-PARAM error, use default value 5 seconds for loop gap.");
                SECONDS_AS_LOOP_GAP = 5;
            }
        }
        
        //take 1 minute for cal avg pri.
        int lenForAvg = 60 * 1 / SECONDS_AS_LOOP_GAP;
        
        if (!isMinMaxLstPriMatchBoxGap(inc_flg) || sz < 3 * lenForAvg) {
            _logger.info("last_lst does not match isMinMaxLstPriMatchBoxGap or sz:" + sz + " is small then 3 * lenForAvg:" + lenForAvg + "? isLstPriTurnaround return false.");
            if (inc_flg) {
                det_buy_turnaround.clear();
            }
            else {
                det_sell_turnaround.clear();
            }
            return false;
        }
        
        _logger.info(" size:" + sz + " inc_flg:" + inc_flg);
        
        double lstAvgPri = 0;
        for (int i = 0; i < lenForAvg; i++) {
            lstAvgPri += tid.last_lst.get(sz - 1 -i);
        }
        
        lstAvgPri = lstAvgPri / lenForAvg;
        
        double midAvgPri = 0;
        for (int i = 0; i < lenForAvg; i++) {
            midAvgPri += tid.last_lst.get(sz - 1 -i - lenForAvg);
        }
        
        midAvgPri = midAvgPri / lenForAvg;
        
        _logger.info("lstAvgPri:" + lstAvgPri + " midAvgPri:" + midAvgPri);
        double lstDetPri = (inc_flg ? (lstAvgPri - midAvgPri) : (midAvgPri - lstAvgPri));

        if (inc_flg) {
            det_buy_turnaround.add(lstDetPri);
            int szb = det_buy_turnaround.size();
            if (szb >= 3) {
                double b0 = det_buy_turnaround.get(0);
                double b1 = det_buy_turnaround.get(1);
                double b2 = det_buy_turnaround.get(2);
                _logger.info("det_buy_turnaround sz:" + szb + ", b0:" + b0 + ", b1:" + b1 + ", b2:" + b2);
                _logger.info("turnaround: b0 - b1 = " + (b0 - b1) + " > SMALL_PRICE_DIFF:" + SMALL_PRICE_DIFF +
                                         ", b2 - b1 = " + (b2 - b1) + " > SMALL_PRICE_DIFF:" + SMALL_PRICE_DIFF);
                if ((b0 - b1) > SMALL_PRICE_DIFF &&
                    (b2 - b1) > SMALL_PRICE_DIFF) {
                    _logger.info("isLstPriTurnaround return true for buy.");
                    return true;
                }
            }
            else {
                _logger.info("for buy sz is less than 3:" + szb + ", isLstPriTurnaround return false");
            }
        }
        else {
            det_sell_turnaround.add(lstDetPri);
            int szs = det_sell_turnaround.size();
            if (szs >= 3) {
                double s0 = det_sell_turnaround.get(0);
                double s1 = det_sell_turnaround.get(1);
                double s2 = det_sell_turnaround.get(2);
                _logger.info("det_sell_turnaround sz:" + szs + ", s0:" + s0 + ", s1:" + s1 + ", s2:" + s2);
                _logger.info("turnaround: s1 - s0 = " + (s1 - s0) + " > SMALL_PRICE_DIFF:" + SMALL_PRICE_DIFF +
                                         ", s1 - s2 = " + (s1 - s2) + " > SMALL_PRICE_DIFF:" + SMALL_PRICE_DIFF);
                if ((s1 - s0) > SMALL_PRICE_DIFF &&
                    (s1 - s2) > SMALL_PRICE_DIFF) {
                    _logger.info("isLstPriTurnaround return true for sell.");
                    return true;
                }
            }
            else {
                _logger.info("for sell sz is less than 3:" + szs + ", isLstPriTurnaround return false");
            }
        }
        return false;
    }
    
    public boolean isLstPriPlusBack(boolean inc_flg) {

        int sz = tid.last_lst.size();
        
        int lenForAvg = 2;
        
        if (sz < 3 * lenForAvg) {
            _logger.info("last_lst sz:" + sz + " is small then 3 * lenForAvg:" + lenForAvg + "? isLstPriTurnaround return false.");
            if (inc_flg) {
                det_buy_plusback.clear();
            }
            else {
                det_sell_plusback.clear();
            }
            return false;
        }
        
        _logger.info("plusback size:" + sz + " inc_flg:" + inc_flg);
        
        double lstAvgPri = 0;
        for (int i = 0; i < lenForAvg; i++) {
            lstAvgPri += tid.last_lst.get(sz - 1 -i);
        }
        
        lstAvgPri = lstAvgPri / lenForAvg;
        
        double midAvgPri = 0;
        for (int i = 0; i < lenForAvg; i++) {
            midAvgPri += tid.last_lst.get(sz - 1 -i - lenForAvg);
        }
        
        midAvgPri = midAvgPri / lenForAvg;
        
        _logger.info("plushback lstAvgPri:" + lstAvgPri + " midAvgPri:" + midAvgPri);
        double lstDetPri = (inc_flg ? (lstAvgPri - midAvgPri) : (midAvgPri - lstAvgPri));

        if (inc_flg) {
            det_buy_plusback.add(lstDetPri);
            int szb = det_buy_plusback.size();
            if (szb >= 3) {
                double b0 = det_buy_plusback.get(0);
                double b1 = det_buy_plusback.get(1);
                double b2 = det_buy_plusback.get(2);
                _logger.info("det_buy_plusback sz:" + szb + ", b0:" + b0 + ", b1:" + b1 + ", b2:" + b2);
                _logger.info("plusback: b0 - b1 = " + (b0 - b1) + " > SMALL_PRICE_DIFF:" + SMALL_PRICE_DIFF +
                                         ", b2 - b1 = " + (b2 - b1) + " > SMALL_PRICE_DIFF:" + SMALL_PRICE_DIFF);
                if ((b0 - b1) > SMALL_PRICE_DIFF &&
                    (b2 - b1) > SMALL_PRICE_DIFF) {
                    _logger.info("isLstPriPlusBack return true for buy.");
                    return true;
                }
            }
            else {
                _logger.info("for buy sz is less than 3:" + szb + ", isLstPriPlusBack return false");
            }
        }
        else {
            det_sell_plusback.add(lstDetPri);
            int szs = det_sell_plusback.size();
            if (szs >= 3) {
                double s0 = det_sell_plusback.get(0);
                double s1 = det_sell_plusback.get(1);
                double s2 = det_sell_plusback.get(2);
                _logger.info("det_sell_plusback sz:" + szs + ", s0:" + s0 + ", s1:" + s1 + ", s2:" + s2);
                _logger.info("plusback: s1 - s0 = " + (s1 - s0) + " > SMALL_PRICE_DIFF:" + SMALL_PRICE_DIFF +
                                         ", s1 - s2 = " + (s1 - s2) + " > SMALL_PRICE_DIFF:" + SMALL_PRICE_DIFF);
                if ((s1 - s0) > SMALL_PRICE_DIFF &&
                    (s1 - s2) > SMALL_PRICE_DIFF) {
                    _logger.info("det_sell_plusback return true for sell.");
                    return true;
                }
            }
            else {
                _logger.info("for sell sz is less than 3:" + szs + ", det_sell_plusback return false");
            }
        }
        return false;
    }
    
    public boolean isLstPriAboveWaterLevel(double topPct) {
        if (!isMinMaxLstPriMatchBoxGap(false)) {
            _logger.info("last_lst does not match isMinMaxLstPriMatchBoxGap, isLstPriAboveWaterLevel is false.");
            return false;
        }
        int sz = tid.last_lst.size();
        double lstPri = tid.last_lst.get(sz - 1);
        _logger.info("maxPri:" + maxpri + "\n minPri:" + minpri + "\n lstPri:" + lstPri);
        
        if ((lstPri - minpri) / (maxpri - minpri) >= topPct) {
            _logger.info("lstPri:" + lstPri + " is " + (topPct*100) + "% above:[" + minpri+"," + maxpri + "], return true");
            return true;
        }
        _logger.info("lstPri:" + lstPri + " is not " + (topPct*100 ) + "% above:[" + minpri+"," + maxpri + "], return false");
        return false;
    }
    
    public boolean isLstPriBreakUpBorder() {
        if (!isMinMaxLstPriMatchBoxGap(true)) {
            _logger.info("last_lst does not match isMinMaxLstPriMatchBoxGap, isLstPriBreakUpBorder is false.");
            return false;
        }
        int sz = tid.last_lst.size();
        double lstPri = tid.last_lst.get(sz - 1);
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
        if (!isMinMaxLstPriMatchBoxGap(true)) {
            _logger.info("last_lst does not match isMinMaxLstPriMatchBoxGap, isLstPriUnderWaterLevel is false.");
            return false;
        }
        double lstPri = tid.last_lst.get(sz - 1);
        _logger.info("maxPri:" + maxpri + "\n minPri:" + minpri + "\n lstPri:" + lstPri);
        
        if ((lstPri - minpri) / (maxpri - minpri) <= bottomPct) {
            _logger.info("lstPri:" + lstPri + " is " + (bottomPct*100) + "% under:[" + minpri+"," + maxpri + "], return true");
            return true;
        }
        _logger.info("lstPri:" + lstPri + " is not " + (bottomPct*100 ) + "% under:[" + minpri+"," + maxpri + "], return false");
        return false;
    }
    
    public boolean isLstPriBreakButtomBorder() {
        int sz = tid.last_lst.size();
        if (!isMinMaxLstPriMatchBoxGap(false)) {
            _logger.info("last_lst does not match isMinMaxLstPriMatchBoxGap, isLstPriBreakButtomBorder is false.");
            return false;
        }
        double lstPri = tid.last_lst.get(sz - 1);
        _logger.info("maxPri:" + maxpri + "\n minPri:" + minpri + "\n lstPri:" + lstPri);
        
        if (lstPri <= minpri) {
            _logger.info("lstPri is breaking bottom border" + minpri + ", return true");
            return true;
        }
        _logger.info("lstPri is breaking bottom border" + minpri + ", return false");
        return false;
    }
    
    public boolean isMinMaxLstPriMatchBoxGap(boolean forBuy) {
        int sz = tid.last_lst.size();
        if (sz <= 0) {
            _logger.info("last price queue is empty, isMinMaxLstPriMatchBoxGap return false");
            return false;
        }
        
        double queued_maxpri = Integer.MIN_VALUE;
        double queued_minpri = Integer.MAX_VALUE;
        
        for (int i = 0; i <= sz - 1; i++) {
            double pri = tid.last_lst.get(i);
            if (queued_maxpri < pri) {
                queued_maxpri = pri;
            }
            
            if (queued_minpri > pri) {
                queued_minpri = pri;
            }
        }
        
        _logger.info("queued max:" + queued_maxpri + " - queued min:" + queued_minpri + " = " + (queued_maxpri - queued_minpri));
        
        if ((queued_maxpri - queued_minpri) > LAST_PRICE_BOX_GAP_MAX) {
            _logger.info("queued max/min gap bigger than LAST_PRICE_BOX_GAP_MAX:" + LAST_PRICE_BOX_GAP_MAX + ", replace maxpri/minpri with queued value.");
            maxpri = queued_maxpri;
            minpri = queued_minpri;
        }
        
        if (maxpri < queued_maxpri) {
            maxpri = queued_maxpri;
        }
        
        if (minpri > queued_minpri) {
            minpri = queued_minpri;
        }
        
        //When the queue is full, then use smaller price gap.
        double RATIO_GAP_MIN = (IS_IN_UNSTABLE_MODE) ? (LAST_PRICE_BOX_GAP_MAX_RATIO * LAST_PRICE_BOX_GAP_MIN) : LAST_PRICE_BOX_GAP_MIN;
        double RATIO_GAP_MAX = (IS_IN_UNSTABLE_MODE) ? (LAST_PRICE_BOX_GAP_MAX_RATIO * LAST_PRICE_BOX_GAP_MAX) : LAST_PRICE_BOX_GAP_MAX;;
        
        double ageVal = RATIO_GAP_MIN + 1.0/(loadCount / MAX_SZ + 1) * (RATIO_GAP_MAX -RATIO_GAP_MIN);
        double expVal = ageVal;
        double act_gap_max = history_maxpri - history_minpri;
        
        // Use last act_gap_max only when:
        // 1. the value of act_gap_max in the range.
        // 2. Aged at least once.
        if (act_gap_max >= RATIO_GAP_MIN &&
            act_gap_max <= RATIO_GAP_MAX &&
            ageVal > act_gap_max &&
            ageVal == RATIO_GAP_MAX) {
            expVal = act_gap_max;
        }

        // We get the price of last deal, make sure
        // the gap of top or bottom with last deal price
        // matches the calculate expVal.
        double lstDealPri = 0.0;
        String type = "";
        MocaResults rs = null;
        String cls = "";
        
        //Only get last trade price when we have btc or ltc availalbe.
        //When it sold out, ignore the last deal price.
        if (forBuy) {
            if (ct.equalsIgnoreCase("btc")) {
                cls = " (@available_btc_display > 0.001)";
            }
            else {
                cls = " (@available_ltc_display > 0.001)";
            }
        }
        else {
            // when all money used, and try to sell, ignore last deal price.
             cls = " (@available_cny_display > 0.1)";
        }
        try {
            rs = _moca.executeCommand(" get account info " +
                                      "|" +
                                      " if " + cls +
                                      " {" +
                                      "    [select processed_price lstDealPri,                              " +
                                      "            type                                                     " +
                                      "       from hb_buysell_data                                          " +
                                      "      where id = (select max (id) from hb_buysell_data)              " +
                                      "        and type in ('1','2')                                        " +
                                      "        and ins_dt > sysdate - 12/24.0]            " +//if trade within 12 hours.
                                      "} " +
                                      "else {" +
                                      "    publish data where lstDealPri = 0 " +
                                      "}");
            rs.next();
            lstDealPri = rs.getDouble("lstDealPri");
            type = rs.getString("type");
            _logger.info("got lstDealPri:" + lstDealPri + ", type:" + type);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            _logger.error(e.getMessage());
            _logger.info("no buysell data found within 12 hours, skiping check previous deal price.");
        }
        
        _logger.info(
                "\n Queue size:" + sz +
                "\n loadCount:" + loadCount +
                "\n MAX_SZ:" + MAX_SZ + 
                "\n RATIO_GAP_MIN:" + RATIO_GAP_MIN +
                "\n RATIO_GAP_MAX:" + RATIO_GAP_MAX +
                "\n act_gap_max:" + act_gap_max +
                "\n Aging Gap value:" + ageVal +
                "\n Expression Value:" + expVal +
                "\n maxPri:" + maxpri +
                "\n minPri:" + minpri +
                "\n maxpri - minpri:" + (maxpri - minpri) +
                "\n lstDealPri:" + lstDealPri + ", type:" + (type.equals("1") ? " buy" : (type.isEmpty() ? " NoTradeIn12Hrs" : " sell")) +
                "\n maxpri - lstDealPri:" + (maxpri - lstDealPri) + ((!forBuy) ? (" should > expVal:" + expVal + " to sell") : "") +
                "\n lstDealPri - minpri:" + (lstDealPri - minpri) + (forBuy ? (" should > expVal:" + expVal + " to buy") : ""));

        if (lstDealPri > 0) {
            if (((!forBuy && (maxpri - lstDealPri) >= expVal) || (forBuy && (lstDealPri - minpri) >= expVal)) && maxpri - minpri >= BIG_PRICE_DIFF) {
                _logger.info("min/max/lstDealPri price matches expVal:" + expVal + ", return true");
                return true;
            }
        }
        else if (maxpri - minpri >= expVal){
            _logger.info("lstDealPri is 0, min/max price matches expVal:" + expVal + ", return true");
            return true;
        }
        _logger.info("min/max/lstDealPri price DOES NOT matche expVal:" + expVal + ", return false");
        return false;
    }
    
    public boolean dumpDatatoDB() {
        boolean t1 = this.top10.dumpDatatoDB();
        boolean t2 = this.trd.dumpDatatoDB();
        boolean t3 = this.tid.dumpDatatoDB();
        return t1 && t2 && t3;
    }
    
    @Override
    public boolean isShortCrossLong(boolean gloden_flag) {
        MocaResults rs = null;
        double shortAvgPri = 0;
        double longAvgPri = 0;
        double pre_shortAvgPri = 0;
        double pre_longAvgPri = 0;
        double shortAvgPri2 = 0;
        double longAvgPri2 = 0;
        double pre_shortAvgPri2 = 0;
        double pre_longAvgPri2 = 0;
        boolean result = false;
        boolean result2 = false;
        String gloden_time = "";
        String dead_time = "";
        int cnt = 0;
        try {
            rs = _moca.executeCommand(" get period data where market ='" + mk + "' and coinType = '" + ct + "' and period ='" + SHORT_PERIOD + "' and length =" + (LONG_TERM + 1));
            cnt = 0;
            while(rs.next()) {
                if (cnt >= LONG_TERM - SHORT_TERM && cnt < LONG_TERM) {
                    pre_shortAvgPri += rs.getDouble("close");
                }
                if (cnt < LONG_TERM) {
                    pre_longAvgPri += rs.getDouble("close");
                }
                
                if (cnt > LONG_TERM - SHORT_TERM && cnt <= LONG_TERM) {
                    shortAvgPri += rs.getDouble("close");
                }
                
                if (cnt > 0 && cnt <= LONG_TERM) {
                    longAvgPri += rs.getDouble("close");
                }
                if (cnt == LONG_TERM) {
                    gloden_time = rs.getString("time");
                    dead_time = rs.getString("time");
                }
                cnt++;
            }
            
            pre_shortAvgPri = pre_shortAvgPri / SHORT_TERM;
            pre_longAvgPri = pre_longAvgPri / LONG_TERM;
            
            shortAvgPri = shortAvgPri / SHORT_TERM;
            longAvgPri = longAvgPri / LONG_TERM;
            
            _logger.info("longAvgPri:" + longAvgPri + ", shortAvgPri:" + shortAvgPri + ", pre_longAvgPri:" + pre_longAvgPri + ", pre_shortAvgPri:" + pre_shortAvgPri);
            _logger.info("[shortAvgPri - longAvgPri]:" + (shortAvgPri - longAvgPri) + ", [pre_shortAvgPri - pre_longAvgPri]:" + (pre_shortAvgPri - pre_longAvgPri));

            if (gloden_flag) {
                if (pre_shortAvgPri + 0.01 <= pre_longAvgPri &&
                    shortAvgPri >= longAvgPri + 0.01) {
                    _logger.info("gloden cross return TRUE");
                    result = true;
                }
                else {
                        _logger.info("gloden cross return FALSE.");
                }
            }
            else {
                if (pre_shortAvgPri >= pre_longAvgPri + 0.01 &&
                    shortAvgPri + 0.01 <= longAvgPri ) {
                        _logger.info("dead cross return TRUE.");
                        result = true;
                }
                else {
                        _logger.info("dead cross return FALSE.");
                }
            }
            
            rs = _moca.executeCommand(" get period data where market ='" + mk + "' and coinType = '" + ct + "' and period ='" + LONG_PERIOD + "' and length =" + (LONG_TERM + 1));
            cnt = 0;
            while(rs.next()) {
                if (cnt >= LONG_TERM - SHORT_TERM && cnt < LONG_TERM) {
                    pre_shortAvgPri2 += rs.getDouble("close");
                }
                if (cnt < LONG_TERM) {
                    pre_longAvgPri2 += rs.getDouble("close");
                }
                
                if (cnt > LONG_TERM - SHORT_TERM && cnt <= LONG_TERM) {
                    shortAvgPri2 += rs.getDouble("close");
                }
                
                if (cnt > 0 && cnt <= LONG_TERM) {
                    longAvgPri2 += rs.getDouble("close");
                }
                cnt++;
            }
            
            pre_shortAvgPri2 = pre_shortAvgPri2 / SHORT_TERM;
            pre_longAvgPri2 = pre_longAvgPri2 / LONG_TERM;
            
            shortAvgPri2 = shortAvgPri2 / SHORT_TERM;
            longAvgPri2 = longAvgPri2 / LONG_TERM;

            _logger.info("longAvgPri2:" + longAvgPri2 + ", shortAvgPri2:" + shortAvgPri2 + ", pre_longAvgPri2:" + pre_longAvgPri2 + ", pre_shortAvgPri2:" + pre_shortAvgPri2);
            _logger.info("[shortAvgPri2 - longAvgPri2]:" + (shortAvgPri2 - longAvgPri2) + ", [pre_shortAvgPri2 - pre_longAvgPri2]:" + (pre_shortAvgPri2 - pre_longAvgPri2));

            if (gloden_flag) {
                if (pre_shortAvgPri2 + 0.01 <= pre_longAvgPri2 &&
                    shortAvgPri2 >= longAvgPri2 + 0.01) {
                    _logger.info("gloden2 cross return TRUE");
                    result2 = true;
                }
                else {
                        _logger.info("gloden2 cross return FALSE.");
                }
            }
            else {
                if (pre_shortAvgPri2 >= pre_longAvgPri2 + 0.01 &&
                    shortAvgPri2 + 0.01 <= longAvgPri2 ) {
                        _logger.info("dead2 cross return TRUE.");
                        result2 = true;
                }
                else {
                        _logger.info("dead2 cross return FALSE.");
                }
            }
        }
        catch (Exception e) {
            _logger.error("Exception happened when check shortCrossLong:" + e.getMessage());
        }
        
        if (result && result2) {
            _logger.info("both terms return true: gloden_flg:" + gloden_flag + ", pre_gloden_time:" + pre_gloden_time + ", pre_dead_time:" + pre_dead_time);
            if (gloden_flag && pre_gloden_time.equals(gloden_time)) {
                _logger.info("already gloden, reset to false.");
                result = result2 = false;
            }
            else if (!gloden_flag && pre_dead_time.equals(dead_time)) {
                _logger.info("already dead, reset to false.");
                result = result2 = false;
            }
            else {
                if (gloden_flag) {
                    pre_gloden_time = gloden_time;
                }
                else {
                    pre_dead_time = gloden_time;
                }
                _logger.info("saved cross time: gloden_flg:" + gloden_flag + ", pre_gloden_time:" + pre_gloden_time + ", pre_dead_time:" + pre_dead_time);
            }
        }
        return result && result2;
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
            
            st = eSTOCKTREND.NA;
                        
            //take 1 minute for cal avg gap.
            int lenForGap = 60 * 1 / SECONDS_AS_LOOP_GAP;
            
            //take 1 minute for cal ava pri.
            int lenForAvg = 60 / SECONDS_AS_LOOP_GAP;
            
            if (sz < 2 * lenForGap + lenForAvg) {
                _logger.info("sz:" + sz + " is smaller than 2*lenForGap:" + lenForGap + " + lenForAvg:" + lenForAvg + ", skip cal SUP or SDOWN");
            }
            else {
                double lstAvgPri = 0;
                for (int i = 0; i < lenForAvg; i++) {
                    lstAvgPri += tid.last_lst.get(sz - 1 -i);
                }
                
                lstAvgPri = lstAvgPri / lenForAvg;
                
                double midAvgPri = 0;
                for (int i = 0; i < lenForAvg; i++) {
                    midAvgPri += tid.last_lst.get(sz - 1 - i - lenForGap);
                }
                
                midAvgPri = midAvgPri / lenForAvg;
                
                double fstAvgPri = 0;
                for (int i = 0; i < lenForAvg; i++) {
                    fstAvgPri += tid.last_lst.get(sz - 1 - i - 2 * lenForGap);
                }
                
                fstAvgPri = fstAvgPri / lenForAvg;
                
                double DetPri = lstAvgPri - fstAvgPri;
                double lstDetPri = lstAvgPri - midAvgPri;
                double midDetPri = midAvgPri - fstAvgPri;
                
                _logger.info("\n Cal Trend:\n lstAvgPri:" + lstAvgPri + "\n fstAvgPri:" + fstAvgPri +
                             "\n DetPri is:" + DetPri +
                             "\n lstDetPri is:" + lstDetPri +
                             "\n midDetPri is:" + midDetPri +
                             "\n LAST_PRICE_BOX_GAP_MIN:" + LAST_PRICE_BOX_GAP_MIN +
                             "\n LAST_PRICE_BOX_GAP_MAX:" + LAST_PRICE_BOX_GAP_MAX +
                             "\n BIG_PRICE_DIFF:" + BIG_PRICE_DIFF +
                             "\n LAST_PRICE_BOX_GAP_MIN / 6:" + LAST_PRICE_BOX_GAP_MIN / 6);
                if (history_maxpri - lstAvgPri >= 4 * LAST_PRICE_BOX_GAP_MAX && lstDetPri > midDetPri + BIG_PRICE_DIFF) {
                    _logger.info("stock trend set to SUP, and IS_IN_UNSTABLE_MODE to true!");
                    st = eSTOCKTREND.SUP;
                    IS_IN_UNSTABLE_MODE = true;
                }
                else if (DetPri <= -2 * LAST_PRICE_BOX_GAP_MAX && lstDetPri + BIG_PRICE_DIFF < midDetPri) {
                    _logger.info("stock trend set to SDOWN, and IS_IN_UNSTABLE_MODE to true!");
                    st = eSTOCKTREND.SDOWN;
                    IS_IN_UNSTABLE_MODE = true;
                }
                else if (IS_IN_UNSTABLE_MODE && Math.abs(lstDetPri) < LAST_PRICE_BOX_GAP_MIN / 6 && Math.abs(midDetPri) < LAST_PRICE_BOX_GAP_MIN / 6) {
                    _logger.info("Both lstDetPri/midDetPri < LAST_PRICE_BOX_GAP_MIN / 6, reset IS_IN_UNSTABLE_MODE to false!");
                    IS_IN_UNSTABLE_MODE = false;
                }
            }
        }
        return st;
    }
    public boolean isStockUnstableMode() {
        _logger.info("getting IS_IN_UNSTABLE_MODE as:" + IS_IN_UNSTABLE_MODE);
        return IS_IN_UNSTABLE_MODE;
    }
}
