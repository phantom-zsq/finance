package org.phantom.analyze.statistics;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.BoneBean;
import org.phantom.analyze.common.Config;
import org.phantom.analyze.load.LoadOracleData;
import java.util.*;

public class Bond {

    private static SparkSession session;
    private static Properties properties;

    public Bond() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        Bond bond = new Bond();
        // 打新
        //bond.daxin();
        // 可转债买卖, 统计1-16周的涨跌情况
        bond.zhou();
    }

    public void zhou() throws Exception {
        new LoadOracleData().load();
        Dataset<Row> rows = session.sql("select *,row_number() over(partition by ts_code order by trade_date) as rank from cb_daily");
        rows.createOrReplaceTempView("cb_daily_rank");
        rows.cache();
        rows.count();
        //int[] start = {1,5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80};
        int[] start = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        for(int m=0; m<start.length-1; m++){
            int num1 = start[m]+1;
            for(int n=m+1; n<start.length; n++) {
                int num2 = start[n];
                System.out.print(num1+": "+num2);
                List<Row> result = session.sql("" +
                        "select sum(score),avg(score),count(*),sum(case when score>=0 then 1 else 0 end),sum(case when score>=0 then 1 else 0 end) * 100 / count(*) " +
                        "from(" +
                        "select cb_basic_rank_1.ts_code,(cb_basic_rank_2.close-cb_basic_rank_1.open) * 100 / cb_basic_rank_1.open as score " +
                        "from (select * from cb_daily_rank where rank="+num1+") cb_basic_rank_1 " +
                        "inner join (select * from cb_daily_rank where rank="+num2+") cb_basic_rank_2 on cb_basic_rank_1.ts_code=cb_basic_rank_2.ts_code " +
                        ") a"
                ).collectAsList();
                for (Row row : result) {
                    int i = 0;
                    System.out.println(" " + row.get(i++)+": "+row.get(i++)+": "+row.get(i++)+": "+row.get(i++)+": "+row.get(i++));
                }
            }
        }
    }

    public void daxin() throws Exception {
        new LoadOracleData().load();
        Map<String, List<BoneBean>> map = getInformation();
        System.out.println("----------high----------");
        high(map);
        System.out.println("----------high----------");
        for(int i=100; i<=200; i+=10){
            System.out.println("----------day "+i+"----------");
            day(map, i);
            System.out.println("----------day "+i+"----------");
        }
        for(int i=1; i<=10; i++){
            System.out.println("----------max "+i+"----------");
            max(map, i);
            System.out.println("----------max "+i+"----------");
        }
    }

    public Map<String, List<BoneBean>> getInformation() throws Exception {
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
        return map;
    }

    public void high(Map<String, List<BoneBean>> map) throws Exception {
        List<Double> lists = new ArrayList<Double>();
        for (String tsCode : map.keySet()) {
            List<BoneBean> list = map.get(tsCode);
            double max = 0;
            int locate = -100;
            for (int i = 0; i < list.size(); i++) {
                double high = list.get(i).getHigh();
                if (high > max) {
                    max = high;
                    locate = i + 1;
                }
            }
            lists.add(max);
        }
        intervalLength(lists);
    }

    public void day(Map<String, List<BoneBean>> map, double max) throws Exception {
        List<Double> lists = new ArrayList<Double>();
        for (String tsCode : map.keySet()) {
            List<BoneBean> list = map.get(tsCode);
            int locate = -100;
            for (int i = 0; i < list.size(); i++) {
                double high = list.get(i).getHigh();
                if (high >= max) {
                    locate = i + 1;
                    break;
                }
            }
            lists.add(Double.valueOf(locate));
        }
        intervalLength(lists);
    }

    public void max(Map<String, List<BoneBean>> map, int day) throws Exception {
        List<Double> lists = new ArrayList<Double>();
        for (String tsCode : map.keySet()) {
            List<BoneBean> list = map.get(tsCode);
            double max = 0;
            int locate = -100;
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
