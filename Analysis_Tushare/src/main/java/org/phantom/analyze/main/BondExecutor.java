package org.phantom.analyze.main;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.BoneBean;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;
import org.phantom.analyze.load.LoadBasicInformation;
import org.phantom.analyze.load.LoadExtendInformation;
import org.phantom.analyze.load.LoadOracleData;
import org.phantom.analyze.strategy.Strategy;
import org.phantom.analyze.verify.Verify;

import java.util.*;

public class BondExecutor {

    private static SparkSession session;
    private static Properties properties;

    public BondExecutor() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        new BondExecutor().cal();
    }

    public void cal() throws Exception {
        new LoadOracleData().load();
        // load basic information
        Map<String, List<BoneBean>> map = new HashMap<String, List<BoneBean>>();
        List<Row> cbDaily = session.sql("select ts_code,trade_date,high from cb_daily where ts_code in(select ts_code from cb_basic where list_date >= '2018-01-01') order by ts_code,trade_date").collectAsList();
        for (Row row : cbDaily) {
            List<BoneBean> list;
            if (map.containsKey(row.getString(0))) {
                list = map.get(row.getString(0));
            } else {
                list = new ArrayList<BoneBean>();
                map.put(row.getString(0), list);
            }
            BoneBean bean = new BoneBean();
            bean.setTrade_date(row.getString(1));
            bean.setHigh(row.getDouble(2));
            list.add(bean);
        }
        // high(map);
        // day(map, 100);
        max(map, 1);
        max(map, 2);
        max(map, 3);
        max(map, 4);
        max(map, 5);
        max(map, 6);
        max(map, 7);
        max(map, 8);
        max(map, 9);
        max(map, 10);
    }

    public void high(Map<String, List<BoneBean>> map) throws Exception {
        for (String tsCode : map.keySet()) {
            List<BoneBean> list = map.get(tsCode);
            double max = 0;
            int locate = 0;
            for (int i = 0; i < list.size(); i++) {
                double high = list.get(i).getHigh();
                if (high > max) {
                    max = high;
                    locate = i + 1;
                }
            }
            System.out.println(max + ": " + locate);
        }
    }

    public void day(Map<String, List<BoneBean>> map, double max) throws Exception {
        for (String tsCode : map.keySet()) {
            List<BoneBean> list = map.get(tsCode);
            int locate = 0;
            for (int i = 0; i < list.size(); i++) {
                double high = list.get(i).getHigh();
                if (high > max) {
                    locate = i + 1;
                    break;
                }
            }
            System.out.println(locate);
        }
    }

    public void max(Map<String, List<BoneBean>> map, int day) throws Exception {
        List<Double> lists = new ArrayList<Double>();
        for (String tsCode : map.keySet()) {
            List<BoneBean> list = map.get(tsCode);
            double max = 0;
            int locate = 0;
            for (int i = 0; i < list.size(); i++) {
                double high = list.get(i).getHigh();
                if (high > max) {
                    max = high;
                    locate = i + 1;
                }
                if(i == day-1){
                    break;
                }
            }
            lists.add(max);
        }
        intervalLength(lists);
    }

    public void intervalLength(List<Double> lists) throws Exception {
        Map<Integer, Integer> results = new TreeMap<Integer, Integer>();
        for (Double high : lists) {
            int highInt = high.intValue() / 10 * 10;
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
