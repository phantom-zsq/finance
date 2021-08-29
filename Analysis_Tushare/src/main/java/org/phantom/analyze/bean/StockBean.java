package org.phantom.analyze.bean;

public class StockBean {

    /********** 基本信息-股票 **********/
    private double close; // 收盘价

    /********** 基本信息-北向资金 **********/
    private double ratio; // 收盘价

    /********** 扩展信息 **********/

    /********** 分析信息 **********/
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
}
