package org.phantom.analyze.bean;

public class FirstNDaysOfBondBean {

    private String ts_code;
    private String stk_code;
    private String trade_date;
    private Double high_yield;
    private Double yield;
    private Double issue_size;
    private Double premium_rate;
    private String newest_rating;
    private Double before_start_amount_rate;
    private Double first_start_amount_rate;
    private Double first_open;
    private Double first_yield;
    private Double first_amount_rate;

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

    public Double getHigh_yield() {
        return high_yield;
    }

    public void setHigh_yield(Double high_yield) {
        this.high_yield = high_yield;
    }

    public Double getYield() {
        return yield;
    }

    public void setYield(Double yield) {
        this.yield = yield;
    }

    public Double getIssue_size() {
        return issue_size;
    }

    public void setIssue_size(Double issue_size) {
        this.issue_size = issue_size;
    }

    public Double getPremium_rate() {
        return premium_rate;
    }

    public void setPremium_rate(Double premium_rate) {
        this.premium_rate = premium_rate;
    }

    public String getNewest_rating() {
        return newest_rating;
    }

    public void setNewest_rating(String newest_rating) {
        this.newest_rating = newest_rating;
    }

    public Double getBefore_start_amount_rate() {
        return before_start_amount_rate;
    }

    public void setBefore_start_amount_rate(Double before_start_amount_rate) {
        this.before_start_amount_rate = before_start_amount_rate;
    }

    public Double getFirst_start_amount_rate() {
        return first_start_amount_rate;
    }

    public void setFirst_start_amount_rate(Double first_start_amount_rate) {
        this.first_start_amount_rate = first_start_amount_rate;
    }

    public Double getFirst_open() {
        return first_open;
    }

    public void setFirst_open(Double first_open) {
        this.first_open = first_open;
    }

    public Double getFirst_yield() {
        return first_yield;
    }

    public void setFirst_yield(Double first_yield) {
        this.first_yield = first_yield;
    }

    public Double getFirst_amount_rate() {
        return first_amount_rate;
    }

    public void setFirst_amount_rate(Double first_amount_rate) {
        this.first_amount_rate = first_amount_rate;
    }
}
