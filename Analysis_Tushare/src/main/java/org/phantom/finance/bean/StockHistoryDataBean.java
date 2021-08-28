package org.phantom.finance.bean;

public class StockHistoryDataBean {

    // 日期
    private String date;
    // 股票代码
    private String code;
    // 名称
    private String name;
    // 收盘价
    private String close;
    // 最高价
    private String high;
    // 最低价
    private String low;
    // 开盘价
    private String open;
    // 前收盘
    private String lopen;
    // 涨跌额
    private String chg;
    // 涨跌幅
    private String pchg;
    // 换手率
    private String turnover;
    // 成交量
    private String voturnover;
    // 成交金额
    private String vaturnover;
    // 总市值
    private String tcap;
    // 流通市值
    private String mcap;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getLopen() {
        return lopen;
    }

    public void setLopen(String lopen) {
        this.lopen = lopen;
    }

    public String getChg() {
        return chg;
    }

    public void setChg(String chg) {
        this.chg = chg;
    }

    public String getPchg() {
        return pchg;
    }

    public void setPchg(String pchg) {
        this.pchg = pchg;
    }

    public String getTurnover() {
        return turnover;
    }

    public void setTurnover(String turnover) {
        this.turnover = turnover;
    }

    public String getVoturnover() {
        return voturnover;
    }

    public void setVoturnover(String voturnover) {
        this.voturnover = voturnover;
    }

    public String getVaturnover() {
        return vaturnover;
    }

    public void setVaturnover(String vaturnover) {
        this.vaturnover = vaturnover;
    }

    public String getTcap() {
        return tcap;
    }

    public void setTcap(String tcap) {
        this.tcap = tcap;
    }

    public String getMcap() {
        return mcap;
    }

    public void setMcap(String mcap) {
        this.mcap = mcap;
    }
}
