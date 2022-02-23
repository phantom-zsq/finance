package org.phantom.analyze.bean;

public class CbNewestRatingBean {

    private String ts_code;
    private String newest_rating;
    private String rating_comp;

    public String getTs_code() {
        return ts_code;
    }

    public void setTs_code(String ts_code) {
        this.ts_code = ts_code;
    }

    public String getNewest_rating() {
        return newest_rating;
    }

    public void setNewest_rating(String newest_rating) {
        this.newest_rating = newest_rating;
    }

    public String getRating_comp() {
        return rating_comp;
    }

    public void setRating_comp(String rating_comp) {
        this.rating_comp = rating_comp;
    }
}
