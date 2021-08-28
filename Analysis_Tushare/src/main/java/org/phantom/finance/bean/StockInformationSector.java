package org.phantom.finance.bean;

public class StockInformationSector {

    // 板块编码
    private String code;
    // 板块名字
    private String name;
    // 1.行业板块 2.概念板块 3.地域板块
    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
