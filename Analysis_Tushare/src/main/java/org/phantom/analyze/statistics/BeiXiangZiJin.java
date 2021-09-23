package org.phantom.analyze.statistics;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;
import org.phantom.analyze.load.LoadOracleData;

import java.util.*;

public class BeiXiangZiJin {

    private static SparkSession session;
    private static Properties properties;

    public BeiXiangZiJin() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        new BeiXiangZiJin().top10();
    }

    public void top10() throws Exception {
        new LoadOracleData().load();
        List<StockBean> lists = getMoneyflowHsgt();
        Map<String, List<StockBean>> maps = getHsgtTop10();
        top10IntervalLength(lists, maps);
    }

    public List<StockBean> getMoneyflowHsgt() throws Exception {
        List<StockBean> list = new ArrayList<StockBean>();;
        List<Row> moneyflowHsgt = session.sql("select trade_date,north_money from moneyflow_hsgt where !(hgt is null and sgt is null) order by trade_date").collectAsList();
        for (Row row : moneyflowHsgt) {
            StockBean bean = new StockBean();
            bean.setTrade_date(row.getString(0));
            list.add(bean);
        }
        return list;
    }

    public Map<String, List<StockBean>> getHsgtTop10() throws Exception {
        Map<String, List<StockBean>> map = new HashMap<String, List<StockBean>>();
        List<Row> hsgtTop10 = session.sql("select ts_code,trade_date from hsgt_top10 where net_amount>0 order by ts_code,trade_date").collectAsList();
        for (Row row : hsgtTop10) {
            List<StockBean> list;
            if (map.containsKey(row.getString(0))) {
                list = map.get(row.getString(0));
            } else {
                list = new ArrayList<StockBean>();
                map.put(row.getString(0), list);
            }
            StockBean bean = new StockBean();
            bean.setTrade_date(row.getString(1));
            list.add(bean);
        }
        return map;
    }

    public void top10IntervalLength(List<StockBean> lists, Map<String, List<StockBean>> maps) throws Exception {
        Map<String, List<Double>> results = new TreeMap<String, List<Double>>();
        List<Double> all = new ArrayList<Double>();
        for (String tsCode : maps.keySet()) {
            List<StockBean> list = maps.get(tsCode);
            List<Double> locate = new ArrayList<Double>();
            int k = 0;
            for(int i=0; i<list.size(); i++){
                StockBean top10Bean = list.get(i);
                String top10TradeDate = top10Bean.getTrade_date();
                for(int j=k; j<lists.size(); j++){
                    StockBean bean = lists.get(j);
                    String tradeDate = bean.getTrade_date();
                    if(top10TradeDate.equals(tradeDate)){
                        if(i != 0){
                            locate.add(Double.valueOf(j)-Double.valueOf(k)-1);
                            all.add(Double.valueOf(j)-Double.valueOf(k)-1);
                        }
                        k = j;
                        break;
                    }
                }
            }
            results.put(tsCode, locate);
        }
        System.out.println("----------all----------");
        intervalLength(all);
        System.out.println("----------all----------");
        for (String tsCode : results.keySet()) {
            System.out.println("----------"+tsCode+"----------");
            intervalLength(results.get(tsCode));
            System.out.println("----------"+tsCode+"----------");
        }
    }

    public void intervalLength(List<Double> lists) throws Exception {
        Map<Integer, Integer> results = new TreeMap<Integer, Integer>();
        for (Double high : lists) {
            int highInt;
            if (high < 10){
                highInt = high.intValue() * 10 / 10;
            } else {
                highInt = high.intValue() / 10 * 10;
            }
            if (results.containsKey(highInt)) {
                results.put(highInt, results.get(highInt) + 1);
            } else {
                results.put(highInt, 1);
            }
        }
        for (Integer high : results.keySet()) {
            System.out.println(high + ": " + results.get(high));
        }
    }
}
