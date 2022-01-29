package org.phantom.analyze.load;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;

import java.util.*;

public class LoadExtendInformation {

    private static SparkSession session;
    private static Properties properties;

    public LoadExtendInformation(){
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public void load(Map<String, List<StockBean>> map) throws Exception {
        // 北向资金
        bxzj(map);
        // 趋势
        trend(map);
        // 支撑压力位
        resistenceAndSupportPosition(map);
        // macd
        macd(map);
    }

    private void bxzj(Map<String, List<StockBean>> map) throws Exception {
        setAvg(map, 5);
        setAvg(map, 10);
        setAvg(map, 20);
        setAvg(map, 30);
        setAvg(map, 60);
    }

    private void trend(Map<String, List<StockBean>> map) throws Exception {
        Map<String, Integer> li = new HashMap<String, Integer>();
        for(String tsCode : map.keySet()){
            List<StockBean> list = map.get(tsCode);
            for (int i=0; i<list.size(); i++) {
                StockBean bean = list.get(i);
                StockBean bean10 = list.get(i-10 > 0 ? i-10 : 0);
                StockBean bean60 = list.get(i-60 > 0 ? i-60 : 0);
                StockBean bean240 = list.get(i-240 > 0 ? i-240 : 0);
                bean.setTrend_short(calTrend(bean10.getClose(),bean.getClose(),0.05));
                bean.setTrend_medium(calTrend(bean60.getClose(),bean.getClose(),0.15));
                bean.setTrend_long(calTrend(bean240.getClose(),bean.getClose(),0.3));
                String str = bean.getTrend_long() + ": " + bean.getTrend_medium() + ": " + bean.getTrend_short();
                System.out.println(bean.getTrade_date() + ": " + str);
                if(li.containsKey(str)){
                    li.put(str, li.get(str)+1);
                }else{
                    li.put(str, 1);
                }
            }
            for(String key : li.keySet()){
                System.out.println(key + ": " + li.get(key));
            }
        }
    }

    private void resistenceAndSupportPosition(Map<String, List<StockBean>> map) throws Exception {

    }

    private void macd(Map<String, List<StockBean>> map) throws Exception {

    }

    private double calTrend(double close1, double close2, double x) throws Exception {
        double rate = close2 / close1;
        double maxRate = 1+x;
        double minRate = 1 / (1+x);
        if(rate < minRate){
            return -1; // 下降趋势
        }else if(rate > maxRate){
            return 1; // 上升趋势
        }else{
            return 0; // 盘整趋势
        }
    }

    private void setAvg(Map<String, List<StockBean>> map, int num) throws Exception {
        for(String tsCode : map.keySet()){
            List<StockBean> list = map.get(tsCode);
            Double sum = 0.0;
            int j = 0;
            boolean start = false;
            for (int i=0; i<list.size(); i++) {
                StockBean bean = list.get(i);
                int bxStatus = bean.getBx_status();
                if(bxStatus==1){
                    start = true;
                }
                if(start){
                    Double ratio = bean.getBx_ratio();
                    if(j<num){
                        sum += ratio;
                        choiceAvg(bean, num, sum / (j+1));
                        j++;
                    }else{
                        sum += ratio - list.get(i-num).getBx_ratio();
                        choiceAvg(bean, num, sum / num);
                    }
                }
                if(bxStatus==-1){
                    start = false;
                }
            }
        }
    }

    private void choiceAvg(StockBean bean, int num, double value) throws Exception {
        switch (num){
            case 5:
                bean.setBx_avg_5(value);
                break;
            case 10:
                bean.setBx_avg_10(value);
                break;
            case 20:
                bean.setBx_avg_20(value);
                break;
            case 30:
                bean.setBx_avg_30(value);
                break;
            case 60:
                bean.setBx_avg_60(value);
                break;
        }
    }

    public void loadBxDetails2(Map<String, List<StockBean>> map) throws Exception {
        Map<String, StockBean> hkHoldMap = new HashMap<String, StockBean>();
        List<Row> hkHoldList = session.sql("select trade_date,ratio from hk_hold where ts_code in(select ts_code from white_list) order by ts_code,trade_date").collectAsList();
        boolean first = true;
        for (int i = 0; i < hkHoldList.size(); i++) {
            Row row = hkHoldList.get(i);
            StockBean bean = new StockBean();
            bean.setTrade_date(row.getString(0));
            bean.setBx_ratio(row.getDouble(1));
            if (first && bean.getBx_ratio() == 0) {
                continue;
            } else {
                if (first) {
                    bean.setBx_status(1);
                }
                if (i == hkHoldList.size() - 1) {
                    bean.setBx_status(-1);
                }
                first = false;
                hkHoldMap.put(bean.getTrade_date(), bean);
            }
        }
    }
}
