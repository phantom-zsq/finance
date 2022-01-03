package org.phantom.analyze.bean;

public class FswBean {

    private String publishedTimeStr; // 公告时间
    private String day; // 公告日期
    private String title; // 标题
    private String type; // 类型
    private String year; // 年
    private String times; // 次数
    private String content; // 内容

    public String getPublishedTimeStr() {
        return publishedTimeStr;
    }

    public void setPublishedTimeStr(String publishedTimeStr) {
        this.publishedTimeStr = publishedTimeStr;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
