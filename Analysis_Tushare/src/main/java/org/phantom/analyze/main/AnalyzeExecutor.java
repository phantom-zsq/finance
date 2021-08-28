package org.phantom.analyze.main;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AnalyzeExecutor {

    private static SparkSession session;
    private static Properties properties;
    private static double SCORE = 3;

    public AnalyzeExecutor(SparkSession session, Properties properties){
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {
        List<String> stockList = getStockList();
        int i = 1;
        for(String str: stockList){
            try {
                analyze(str);
                System.out.println("done: " + i++);
            }catch (Exception e){
                System.out.println("error: " + str);
                e.printStackTrace();
            }
        }
    }

    public void analyze(String str) throws Exception {
        Dataset<Row> hkHoldDS = session.read().jdbc(properties.getProperty("url"), "(select ts_code,date_format(trade_date,'%Y-%m-%d') as trade_date,ratio from hk_hold where ts_code=\'"+str+"\') tt", properties);
        hkHoldDS.createOrReplaceTempView("hk_hold");
        List<Row> listOriginal = session.sql("select trade_date,ratio from hk_hold order by trade_date").collectAsList();
        List<Row> list = deal(listOriginal);
        Double[] avg_5 = avg(list, 5);
        Double[] avg_10 = avg(list, 10);
        Double[] avg_20 = avg(list, 20);
        Double[] avg_30 = avg(list, 30);
        Double[] avg_60 = avg(list, 60);
        Double[] avg_120 = avg(list, 120);
        Double[] avg_240 = avg(list, 240);
        Double[] avg_all = avg(list, list.size());
        Double[] rate = new Double[list.size()];
        Integer score = 0;
        for (int i=0; i<list.size(); i++) {
            Row row = list.get(i);
            String tradeDate = row.getString(0);
            Double hkHold = row.getDouble(1);
            Double avg = avg_60[i];
            rate[i] = hkHold / avg;
            //System.out.println(tradeDate + ": " + hkHold + ": " + avg + ": " + rate[i]);
            if(i>10 && rate[i]>=SCORE){
                if(rate[i-1]>=SCORE || rate[i-2]>=SCORE || rate[i-3]>=SCORE){
                    score++;
                }
                System.out.println("----------" + tradeDate + ": " + hkHold + ": " + avg + ": " + rate[i] + ": " + score);
            }else{
                if(i>1 && (rate[i-1]<SCORE && rate[i-2]<SCORE)){
                    score = 0;
                }
            }
        }
        System.out.println("**********");
        Arrays.sort(rate);
        for (int i=0; i<list.size(); i++) {
            //System.out.println(rate[i]);
        }
    }

    public Double[] avg(List<Row> list, int num) throws Exception {
        Double sum = 0.0;
        Double[] avgs = new Double[list.size()];
        for (int i=0; i<list.size(); i++) {
            Row row = list.get(i);
            Double hkHold = row.getDouble(1);
            if(i<num){
                sum += hkHold;
                avgs[i] = sum / (i+1);
            }else{
                sum += hkHold - list.get(i-num).getDouble(1);
                avgs[i] = sum / num;
            }
        }
        return avgs;
    }

    public List<Row> deal(List<Row> listOriginal) throws Exception {
        List<Row> list = new ArrayList<Row>();
        boolean first = true;
        for (int i=0; i<listOriginal.size(); i++) {
            Row row = listOriginal.get(i);
            Double hkHold = row.getDouble(1);
            if(first && hkHold == 0){
                continue;
            }else{
                list.add(row);
                first = false;
            }
        }
        return list;
    }

    public List<String> getStockList() throws Exception {
        List<Row> list = session.read().jdbc(properties.getProperty("url"), "(select ts_code from hs_const where ts_code='603882.SH' order by ts_code desc) tt", properties).collectAsList();
        List<String> result = new ArrayList<String>();
        for (Row row : list) {
            result.add(row.getString(0));
        }
        return result;
    }

}
