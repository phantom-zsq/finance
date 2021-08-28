package org.phantom.finance.bean;

public class StockInformationBasic {

    // 代码
    private String code;
    // 名称
    private String name;
    // 上市时间
    private String start_time;
    // 退市时间
    private String end_time;
    // 沪市A股, 1表示是, 0表示否
    private int is_sh = 0;
    // 深市A股, 1表示是, 0表示否
    private int is_sz = 0;
    // 科创板, 1表示是, 0表示否
    private int is_kc = 0;
    // 中小板, 1表示是, 0表示否
    private int is_zx = 0;
    // 创业板, 1表示是, 0表示否
    private int is_cy = 0;
    // 高价股, 1表示是, 0表示否
    private int is_high = 0;
    // 中价股, 1表示是, 0表示否
    private int is_medium = 0;
    // 低价股, 1表示是, 0表示否
    private int is_low = 0;
    // 大盘股, 1表示是, 0表示否
    private int is_big = 0;
    // 中盘股, 1表示是, 0表示否
    private int is_middle = 0;
    // 小盘股, 1表示是, 0表示否
    private int is_small = 0;
    // A+H股, 1表示是, 0表示否
    private int is_h = 0;
    // 次新股, 1表示是, 0表示否
    private int is_second_new = 0;
    // 风险警示, 1表示是, 0表示否
    private int is_risk = 0;
    // 退市整理, 1表示是, 0表示否
    private int is_delisting = 0;
    // 是否退市, 1表示是, 0表示否
    private int is_end = 0;

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

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public int getIs_sh() {
        return is_sh;
    }

    public void setIs_sh(int is_sh) {
        this.is_sh = is_sh;
    }

    public int getIs_sz() {
        return is_sz;
    }

    public void setIs_sz(int is_sz) {
        this.is_sz = is_sz;
    }

    public int getIs_kc() {
        return is_kc;
    }

    public void setIs_kc(int is_kc) {
        this.is_kc = is_kc;
    }

    public int getIs_zx() {
        return is_zx;
    }

    public void setIs_zx(int is_zx) {
        this.is_zx = is_zx;
    }

    public int getIs_cy() {
        return is_cy;
    }

    public void setIs_cy(int is_cy) {
        this.is_cy = is_cy;
    }

    public int getIs_high() {
        return is_high;
    }

    public void setIs_high(int is_high) {
        this.is_high = is_high;
    }

    public int getIs_medium() {
        return is_medium;
    }

    public void setIs_medium(int is_medium) {
        this.is_medium = is_medium;
    }

    public int getIs_low() {
        return is_low;
    }

    public void setIs_low(int is_low) {
        this.is_low = is_low;
    }

    public int getIs_big() {
        return is_big;
    }

    public void setIs_big(int is_big) {
        this.is_big = is_big;
    }

    public int getIs_middle() {
        return is_middle;
    }

    public void setIs_middle(int is_middle) {
        this.is_middle = is_middle;
    }

    public int getIs_small() {
        return is_small;
    }

    public void setIs_small(int is_small) {
        this.is_small = is_small;
    }

    public int getIs_h() {
        return is_h;
    }

    public void setIs_h(int is_h) {
        this.is_h = is_h;
    }

    public int getIs_second_new() {
        return is_second_new;
    }

    public void setIs_second_new(int is_second_new) {
        this.is_second_new = is_second_new;
    }

    public int getIs_risk() {
        return is_risk;
    }

    public void setIs_risk(int is_risk) {
        this.is_risk = is_risk;
    }

    public int getIs_delisting() {
        return is_delisting;
    }

    public void setIs_delisting(int is_delisting) {
        this.is_delisting = is_delisting;
    }

    public int getIs_end() {
        return is_end;
    }

    public void setIs_end(int is_end) {
        this.is_end = is_end;
    }
}
