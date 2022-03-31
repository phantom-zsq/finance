package org.phantom.analyze.statistics;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.common.Config;
import org.phantom.analyze.load.LoadOracleData;
import java.util.*;

public class AmountOfBond {

    private static SparkSession session;
    private static Properties properties;

    public AmountOfBond() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        AmountOfBond bond = new AmountOfBond();
        bond.amount();
    }

    public void amount() throws Exception {
        new LoadOracleData().load();
        List<Row> rows = session.sql("select a.ts_code,a.trade_date,a.open,a.high,a.low,a.close,a.amount * 10000 as amount,b.issue_size * 100000000 as issue_size from cb_daily a inner join cb_issue b on a.ts_code=b.ts_code where a.open > 0 order by a.ts_code,a.trade_date").collectAsList();
        // issue size strategy
        for(int times = 1; times < 11; times++){
            for (int i = 0; i < 1; i++) {
                for (int j = 1; j < 11; j++) {
                    for (int m = 1; m < 21; m++) {
                        for (int n = m; n < 21; n++) {
                            issueSize(rows, times, i, j, m, n);
                        }
                    }
                }
            }
        }
        // moving average strategy
        movingAverage(rows);
    }

    private void issueSize(List<Row> rows, int times, int interval, int conditionOfBuy, int startofBuy, int endOfBuy) throws Exception {
        String last_ts_code = "";
        int greaterCount = 0;
        int lessCount = 0;
        Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
        List<Double> list = new ArrayList<Double>();
        for (int m = 0; m < rows.size(); m++) {
            // get result of rows
            Row row = rows.get(m);
            int i = 0;
            String ts_code = row.getString(i++);
            String trade_date = row.getString(i++);
            Double open = row.getDouble(i++);
            Double high = row.getDouble(i++);
            Double low = row.getDouble(i++);
            Double close = row.getDouble(i++);
            Double amount = row.getDouble(i++);
            Double issue_size = row.getDouble(i++);
            // meet new ts_code then reset parameters
            if (!ts_code.equals(last_ts_code)) {
                if (greaterCount > 0) {
                    if (map.containsKey(greaterCount)) {
                        map.put(greaterCount, map.get(greaterCount) + 1);
                    } else {
                        map.put(greaterCount, 1);
                    }
                    greaterCount = 0;
                }
                last_ts_code = ts_code;
            }
            // amount greater than issue_size
            //if (amount >= issue_size * times) {
            if (amount >= issue_size * times) {
                greaterCount++;
                lessCount = 0;
            } else {
                lessCount++;
            }
            // buy
            if (greaterCount == conditionOfBuy && lessCount == 0) {
                Double rate = getRate(rows, m, startofBuy, endOfBuy);
                if(rate != 999){
                    list.add(rate);
                }
            }
            // end
            if (lessCount > interval) {
                if (greaterCount > 0) {
                    if (map.containsKey(greaterCount)) {
                        map.put(greaterCount, map.get(greaterCount) + 1);
                    } else {
                        map.put(greaterCount, 1);
                    }
                    greaterCount = 0;
                }
            }
        }
        if (greaterCount > 0) {
            if (map.containsKey(greaterCount)) {
                map.put(greaterCount, map.get(greaterCount) + 1);
            } else {
                map.put(greaterCount, 1);
            }
        }
        Double sum = 0.0;
        int num = 0;
        for(Double rate : list){
            if(rate > 0){
                num++;
            }
            sum += rate;
        }
        if(sum > 0){
            System.out.println("********************" + times + ": " + interval + ": " + conditionOfBuy + ": " + startofBuy +  ": " + endOfBuy + "********************");
            System.out.println(sum/(endOfBuy-startofBuy+1) + ": " + sum + ": " + num + ": " + list.size());
        }
    }

    private Double getRate(List<Row> rows, int m, int startofBuy, int endOfBuy) throws Exception {
        Double rate = 999.0;
        int start = m + startofBuy;
        int end = m + endOfBuy;
        if(start >= rows.size()){
            return rate;
        }
        if(end >= rows.size()){
            end = rows.size() - 1;
        }
        Row current = rows.get(m);
        Row buy = rows.get(start);
        if (current.getString(0).equals(buy.getString(0))) {
            Double open = buy.getDouble(2);
            for (int i = start; i <= end; i++) {
                if (buy.getString(0).equals(rows.get(i).getString(0))) {
                    Double close = rows.get(i).getDouble(5);
                    rate = (close - open) / open;
                }
            }
        }
        return rate;
    }

    private void movingAverage(List<Row> rows) throws Exception {

    }
}