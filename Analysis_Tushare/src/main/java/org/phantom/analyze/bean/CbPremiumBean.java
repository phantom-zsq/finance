package org.phantom.analyze.bean;

public class CbPremiumBean {

    private String ts_code;
    private String stk_code;
    private String trade_date;
    private double price;
    private double price_hfq;
    private double total_share;
    private double free_share;

    public String getTs_code() {
        return ts_code;
    }

    public void setTs_code(String ts_code) {
        this.ts_code = ts_code;
    }

    public String getStk_code() {
        return stk_code;
    }

    public void setStk_code(String stk_code) {
        this.stk_code = stk_code;
    }

    public String getTrade_date() {
        return trade_date;
    }

    public void setTrade_date(String trade_date) {
        this.trade_date = trade_date;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice_hfq() {
        return price_hfq;
    }

    public void setPrice_hfq(double price_hfq) {
        this.price_hfq = price_hfq;
    }

    public double getTotal_share() {
        return total_share;
    }

    public void setTotal_share(double total_share) {
        this.total_share = total_share;
    }

    public double getFree_share() {
        return free_share;
    }

    public void setFree_share(double free_share) {
        this.free_share = free_share;
    }
}
