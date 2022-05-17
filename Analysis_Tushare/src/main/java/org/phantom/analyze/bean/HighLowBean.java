package org.phantom.analyze.bean;

public class HighLowBean {

    private String ts_code; // 代码
    private String trade_date; // 交易日期
    private double open; // 开盘价
    private double high; // 最高价
    private double low; // 最低价
    private double close; // 收盘价
    private double pre_close; // 昨收价
    private double change; // 涨跌额
    private double pct_chg; // 涨跌幅
    private double vol; // 成交量(手)
    private double amount; // 成交额
    private String status; // high or low
    private long index; // 索引
    private long next_index; // 下一个高低点的索引

    public String getTs_code() {
        return ts_code;
    }

    public void setTs_code(String ts_code) {
        this.ts_code = ts_code;
    }

    public String getTrade_date() {
        return trade_date;
    }

    public void setTrade_date(String trade_date) {
        this.trade_date = trade_date;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getPre_close() {
        return pre_close;
    }

    public void setPre_close(double pre_close) {
        this.pre_close = pre_close;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getPct_chg() {
        return pct_chg;
    }

    public void setPct_chg(double pct_chg) {
        this.pct_chg = pct_chg;
    }

    public double getVol() {
        return vol;
    }

    public void setVol(double vol) {
        this.vol = vol;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getNext_index() {
        return next_index;
    }

    public void setNext_index(long next_index) {
        this.next_index = next_index;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
