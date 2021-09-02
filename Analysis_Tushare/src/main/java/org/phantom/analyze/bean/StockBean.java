package org.phantom.analyze.bean;

public class StockBean {

    /********** 基本信息-股票 **********/
    private String trade_date; // 交易日期
    private double close; // 收盘价

    /********** 基本信息-北向资金 **********/
    private int bx_status; // 开始结束状态: 1开始,-1结束,0维持原状态
    private double ratio; // 收盘价
    /********** 扩展信息-北向资金 **********/
    private double avg_5; // 5日平均
    private double avg_10; // 10日平均
    private double avg_20; // 20日平均
    private double avg_30; // 30日平均
    private double avg_60; // 60日平均

    /********** 基本信息-其他 **********/

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

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
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

    public double getAvg_5() {
        return avg_5;
    }

    public void setAvg_5(double avg_5) {
        this.avg_5 = avg_5;
    }

    public double getAvg_10() {
        return avg_10;
    }

    public void setAvg_10(double avg_10) {
        this.avg_10 = avg_10;
    }

    public double getAvg_20() {
        return avg_20;
    }

    public void setAvg_20(double avg_20) {
        this.avg_20 = avg_20;
    }

    public double getAvg_30() {
        return avg_30;
    }

    public void setAvg_30(double avg_30) {
        this.avg_30 = avg_30;
    }

    public double getAvg_60() {
        return avg_60;
    }

    public void setAvg_60(double avg_60) {
        this.avg_60 = avg_60;
    }
}
