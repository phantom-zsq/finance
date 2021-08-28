package org.phantom.finance.stock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.phantom.finance.bean.StockInformationBasic;
import org.phantom.finance.constant.AH;
import org.phantom.finance.constant.Urls;
import java.util.*;

public class StockInformationBasicCrawler {

    private static SparkSession session;
    private static Properties properties;

    public StockInformationBasicCrawler(SparkSession session, Properties properties) {
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {
        String[] types = getTypes();
        System.out.println("getTypes done");
        Map<String, StockInformationBasic> basicMap = queryData(types);
        System.out.println("queryData done");
        setTime(basicMap);
        System.out.println("setTime done");
        setAH(basicMap);
        System.out.println("setAH done");
        List<StockInformationBasic> basicList = getList(basicMap);
        System.out.println("getList done");
        writeToDb(basicList);
        System.out.println("all done");
    }

    private Map<String, StockInformationBasic> queryData(String[] types) {
        Map<String, StockInformationBasic> basicMap = new HashMap<String, StockInformationBasic>();
        for (int i = 0; i < types.length; i++) {
            try {
                Thread.sleep(1000);
                int count = getCount(types[i]);
                System.out.println(types[i] + ": " + count);
                int times = count / 500 + 1;
                for(int k = 0; k < times; k++){
                    String url = Urls.NETEASE_FINANCE_STOCK_MARKET
                            .replaceAll("\\$\\{query\\}", types[i])
                            .replaceAll("\\$\\{page\\}", k+"")
                            .replaceAll("\\$\\{count\\}", "500");
                    String json = Jsoup.connect(url).get().text();
                    JSONArray array = JSON.parseObject(json).getJSONArray("list");
                    for (int j = 0; j < array.size(); j++) {
                        JSONObject obj = JSON.parseObject(array.get(j) + "");
                        String code = obj.get("CODE").toString().replaceAll("^\\d", "");
                        if (!code.matches("[036]\\d{5}")) {
                            continue;
                        }
                        StockInformationBasic basic = new StockInformationBasic();
                        if (basicMap.containsKey(code)) {
                            basic = basicMap.get(code);
                        } else {
                            basic.setCode(code);
                            basic.setName(obj.get("NAME").toString());
                        }
                        setType(i, basic);
                        basicMap.put(code, basic);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("error type: " + types[i]);
            }
        }
        return basicMap;
    }

    private int getCount(String type) throws Exception {
        String url = Urls.NETEASE_FINANCE_STOCK_MARKET
                .replaceAll("\\$\\{query\\}", type)
                .replaceAll("\\$\\{page\\}", "0")
                .replaceAll("\\$\\{count\\}", "24");
        String json = Jsoup.connect(url).get().text();
        int count = JSON.parseObject(json).getIntValue("total");
        return count;
    }

    // 分类市场
    private String[] getTypes() {
        String[] types = {
                "STYPE:EQA;EXCHANGE:CNSESH",// 沪市A股, 1表示是, 0表示否
                "STYPE:EQA;EXCHANGE:CNSESZ",// 深市A股, 1表示是, 0表示否
                "STYPE:EQA;KSH:true",// 科创板, 1表示是, 0表示否
                "STYPE:EQA;SME:true",// 中小板, 1表示是, 0表示否
                "STYPE:EQA;GEM:true",// 创业板, 1表示是, 0表示否
                "STYPE:EQA;PRICE_RNG:L",// 高价股, 1表示是, 0表示否
                "STYPE:EQA;PRICE_RNG:M",// 中价股, 1表示是, 0表示否
                "STYPE:EQA;PRICE_RNG:S",// 低价股, 1表示是, 0表示否
                "SCSTC27_RNG:L",// 大盘股, 1表示是, 0表示否
                "SCSTC27_RNG:M",// 中盘股, 1表示是, 0表示否
                "SCSTC27_RNG:S",// 小盘股, 1表示是, 0表示否
                "IPO_DATE:gt",// 次新股, 1表示是, 0表示否
                "NODEAL:FXJS",// 风险警示, 1表示是, 0表示否
                "NODEAL:TSZL",// 退市整理, 1表示是, 0表示否
        };
        return types;
    }

    private void setType(int i, StockInformationBasic basic) {
        switch (i) {
            case 0:
                basic.setIs_sh(1);
                break;
            case 1:
                basic.setIs_sz(1);
                break;
            case 2:
                basic.setIs_kc(1);
                break;
            case 3:
                basic.setIs_zx(1);
                break;
            case 4:
                basic.setIs_cy(1);
                break;
            case 5:
                basic.setIs_high(1);
                break;
            case 6:
                basic.setIs_medium(1);
                break;
            case 7:
                basic.setIs_low(1);
                break;
            case 8:
                basic.setIs_big(1);
                break;
            case 9:
                basic.setIs_middle(1);
                break;
            case 10:
                basic.setIs_small(1);
                break;
            case 11:
                basic.setIs_second_new(1);
                break;
            case 12:
                basic.setIs_risk(1);
                break;
            case 13:
                basic.setIs_delisting(1);
                break;
            default:
                break;
        }
    }

    private void setTime(Map<String, StockInformationBasic> basicMap) {
        int num = 0;
        for (String code : basicMap.keySet()) {
            System.out.println("setTime: " + ++num);
            int i = 10;
            while (i-- > 0) {
                try {
                    Thread.sleep(100);
                    Document stock = Jsoup.connect(Urls.NETEASE_FINANCE_STOCK.replaceAll("\\$\\{code\\}", code)).get();
                    String startTime = stock.select("[name=date_start_type]").first().attr("value").replaceAll("\\-", "");
                    String endTime = stock.select("[name=date_end_type]").first().attr("value").replaceAll("\\-", "");
                    String price = stock.select("[class=price]").first().text().trim();
                    StockInformationBasic basic = basicMap.get(code);
                    basic.setStart_time(startTime);
                    if ("已退市".equals(price)) {
                        basic.setEnd_time(endTime);
                        basic.setIs_end(1);
                    }
                    // if success only once, i = -2
                    i = -1;
                } catch (Exception e) {
                    try {
                        Thread.sleep(3000);
                    }catch (Exception e1){
                        // nothing
                    }
                }
            }
            if (i != -2) {
                System.out.println("error time: " + code);
            }
        }
    }

    private void setAH(Map<String, StockInformationBasic> basicMap) {
        for (String code : basicMap.keySet()) {
            if (AH.set.contains(code)) {
                basicMap.get(code).setIs_h(1);
            }
        }
    }

    private List<StockInformationBasic> getList(Map<String, StockInformationBasic> basicMap) {
        List<StockInformationBasic> basicList = new ArrayList<StockInformationBasic>();
        for (String code : basicMap.keySet()) {
            basicList.add(basicMap.get(code));
        }
        return basicList;
    }

    private void writeToDb(List<StockInformationBasic> basicList) {
        List<StockInformationBasic> tmpList = new ArrayList<StockInformationBasic>();
        for (int i = 0; i < basicList.size(); i++) {
            tmpList.add(basicList.get(i));
            if((i+1) % 500 == 0){
                // write to stock_information_basic
                session.createDataFrame(tmpList, StockInformationBasic.class)
                        .write()
                        .mode(SaveMode.Append)
                        .jdbc(properties.getProperty("url"), "stock_information_basic", properties);
                tmpList.clear();
            }
        }
        if(tmpList.size() > 0){
            // write to stock_information_basic
            session.createDataFrame(tmpList, StockInformationBasic.class)
                    .write()
                    .mode(SaveMode.Append)
                    .jdbc(properties.getProperty("url"), "stock_information_basic", properties);
        }
    }
}
