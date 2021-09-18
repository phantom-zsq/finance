package org.phantom.analyze.bean;

public class BoneBean {

    /********** 基本信息-债券 **********/
    private String trade_date; // 交易日期
    private double high; // 最高价

    public String getTrade_date() {
        return trade_date;
    }

    public void setTrade_date(String trade_date) {
        this.trade_date = trade_date;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }
}
