package com.sam.deal.api.stock;

import com.sam.deal.api.stock.eSTOCKTREND;

public interface IStock {
    
    public String getSymbol();
    public Double getCurPri();
    public Double getHighPri();
    public Double getLowPri();
    public Double getOpenPri();
    public Double getLastPri();
    public Double getVolume();
    
    public eSTOCKTREND getStockTrend();
    public eSTOCKTREND calStockTrend();
}
