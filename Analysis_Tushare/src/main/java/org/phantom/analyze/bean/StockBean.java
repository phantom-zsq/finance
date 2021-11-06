package org.phantom.analyze.bean;

public class StockBean {

    /********** 基本信息-股票-行情 **********/
    private String trade_date; // 交易日期
    private double open; // 开盘价
    private double high; // 最高价
    private double low; // 最低价
    private double close; // 收盘价
    private double pre_close; // 昨收价
    private double change; // 涨跌额
    private double pct_chg; // 涨跌幅
    private double vol; // 成交量(手)
    private double amount; // 成交额(千元)
    /********** 基本信息-股票-每日指标 **********/
    /********** 基本信息-股票-个股资金流向 **********/

    /********** 基本信息-北向资金-资金流向 **********/
    private int bx_used; // 当日是否有北向资金: 1有, -1无效
    /********** 基本信息-北向资金-十大成交股 **********/
    private double bx_rank; // 资金排名
    private double bx_amount; // 成交金额(元)
    private double bx_net_amount; // 净成交金额(元)
    private double bx_buy; // 买入金额(元)
    private double bx_sell; // 卖出金额(元)
    /********** 基本信息-北向资金-持股明细 **********/
    private int bx_status; // 开始结束状态: 1开始,-1结束,0维持原状态
    private double bx_vol; // 持股数量(股)
    private double bx_ratio; // 持股占比（%），占已发行股份百分比
    /********** 基本信息-其他 **********/
    private String end_date; // 交易日期

    /********** 扩展信息-北向资金-持股明细 **********/
    private double bx_avg_5; // 持仓比例5日平均
    private double bx_avg_10; // 持仓比例10日平均
    private double bx_avg_20; // 持仓比例20日平均
    private double bx_avg_30; // 持仓比例30日平均
    private double bx_avg_60; // 持仓比例60日平均

    /********** 策略信息 **********/
    private int status; // 买卖状态: 1买,-1卖,0不买不卖
    private long money; // 买卖金额

    /********** set and get method **********/
    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getBx_ratio() {
        return bx_ratio;
    }

    public void setBx_ratio(double bx_ratio) {
        this.bx_ratio = bx_ratio;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public String getTrade_date() {
        return trade_date;
    }

    public void setTrade_date(String trade_date) {
        this.trade_date = trade_date;
    }

    public int getBx_status() {
        return bx_status;
    }

    public void setBx_status(int bx_status) {
        this.bx_status = bx_status;
    }

    public double getBx_avg_5() {
        return bx_avg_5;
    }

    public void setBx_avg_5(double bx_avg_5) {
        this.bx_avg_5 = bx_avg_5;
    }

    public double getBx_avg_10() {
        return bx_avg_10;
    }

    public void setBx_avg_10(double bx_avg_10) {
        this.bx_avg_10 = bx_avg_10;
    }

    public double getBx_avg_20() {
        return bx_avg_20;
    }

    public void setBx_avg_20(double bx_avg_20) {
        this.bx_avg_20 = bx_avg_20;
    }

    public double getBx_avg_30() {
        return bx_avg_30;
    }

    public void setBx_avg_30(double bx_avg_30) {
        this.bx_avg_30 = bx_avg_30;
    }

    public double getBx_avg_60() {
        return bx_avg_60;
    }

    public void setBx_avg_60(double bx_avg_60) {
        this.bx_avg_60 = bx_avg_60;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
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

    public int getBx_used() {
        return bx_used;
    }

    public void setBx_used(int bx_used) {
        this.bx_used = bx_used;
    }

    public double getBx_net_amount() {
        return bx_net_amount;
    }

    public void setBx_net_amount(double bx_net_amount) {
        this.bx_net_amount = bx_net_amount;
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

    public double getBx_rank() {
        return bx_rank;
    }

    public void setBx_rank(double bx_rank) {
        this.bx_rank = bx_rank;
    }

    public double getBx_amount() {
        return bx_amount;
    }

    public void setBx_amount(double bx_amount) {
        this.bx_amount = bx_amount;
    }

    public double getBx_buy() {
        return bx_buy;
    }

    public void setBx_buy(double bx_buy) {
        this.bx_buy = bx_buy;
    }

    public double getBx_sell() {
        return bx_sell;
    }

    public void setBx_sell(double bx_sell) {
        this.bx_sell = bx_sell;
    }

    public double getBx_vol() {
        return bx_vol;
    }

    public void setBx_vol(double bx_vol) {
        this.bx_vol = bx_vol;
    }
}
