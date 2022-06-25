package org.phantom.analyze.statistics;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.common.Config;
import org.phantom.analyze.load.LoadOracleData;
import java.util.*;

public class TradingSystem {

    private static double num = 0;
    private static double shengli = 0;
    private static double yingli = 0;
    private static double kuisun = 0;
    private static double jingli = 0;
    private static double tianshu = 0;
    private static double sum = 10000;

    private static SparkSession session;
    private static Properties properties;

    public TradingSystem() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        TradingSystem bond = new TradingSystem();
        bond.strategy();
    }

    public void strategy() throws Exception {
        // 1. load data
        new LoadOracleData().load();
        // 2. get all ts_code information
        Map<String, List<Row>> all = getOriginalMap();
        // 3. lookback
        // macd抽脚买，macd缩头卖
        System.out.println("----------macd抽脚买，macd缩头卖----------");
        for(String tsCode : all.keySet()){
            List<Row> list = all.get(tsCode);
            cal(list,1);
        }
        printSum();
        // macd金叉买,死叉卖
        System.out.println("----------macd金叉买,死叉卖----------");
        for(String tsCode : all.keySet()){
            List<Row> list = all.get(tsCode);
            //cal(list,2);
        }
        printSum();
        // macd金叉买,缩头卖
        System.out.println("----------macd金叉买,缩头卖----------");
        for(String tsCode : all.keySet()){
            List<Row> list = all.get(tsCode);
            //cal(list,3);
        }
        printSum();
        // macd抽脚买，死叉卖
        System.out.println("----------macd抽脚买，死叉卖----------");
        for(String tsCode : all.keySet()){
            List<Row> list = all.get(tsCode);
            //cal(list,4);
        }
        printSum();
    }

    private void cal(List<Row> list, int type) throws Exception {
        // cal macd
        double[] openPrice = getOpenArray(list);
        double[] highPrice = getHighArray(list);
        double[] lowPrice = getLowArray(list);
        double[] closePrice = getCloseArray(list);
        String[] tradeDate = getTradeDateArray(list);
        double[] dif = new double[closePrice.length];
        double[] dea = new double[closePrice.length];
        double[] macd = new double[closePrice.length];
        macd(closePrice,12,26,9,dif,dea,macd);
        // strategy
        switch (type){
            case 1:
                // macd抽脚买，macd缩头卖
                choujiao_suotou(list.get(0).getString(0),openPrice,highPrice,lowPrice,closePrice,tradeDate,dif,dea,macd);
                break;
            case 2:
                // macd金叉买,死叉卖
                jincha_sicha(list.get(0).getString(0),openPrice,highPrice,lowPrice,closePrice,tradeDate,dif,dea,macd);
                break;
            case 3:
                // macd金叉买,缩头卖
                jincha_suotou(list.get(0).getString(0),openPrice,highPrice,lowPrice,closePrice,tradeDate,dif,dea,macd);
                break;
            case 4:
                // macd抽脚买，死叉卖
                choujiao_sicha(list.get(0).getString(0),openPrice,highPrice,lowPrice,closePrice,tradeDate,dif,dea,macd);
                break;
            default:
                break;
        }
    }

    private Map<String, List<Row>> getOriginalMap() throws Exception {
        List<Row> rows = session.sql("select a.ts_code,a.trade_date,a.open,a.high,a.low,a.close,a.amount * 10000 as amount,b.issue_size * 100000000 as issue_size from cb_daily a inner join cb_issue b on a.ts_code=b.ts_code where a.open > 0 order by a.ts_code,a.trade_date").collectAsList();
        Map<String, List<Row>> map = new HashMap<String, List<Row>>();
        for(Row row : rows){
            int i = 0;
            String ts_code = row.getString(i++);
            List<Row> list = new ArrayList<Row>();
            if(map.containsKey(ts_code)){
                list = map.get(ts_code);
            }
            list.add(row);
            map.put(ts_code,list);
        }
        return map;
    }

    private String[] getTradeDateArray(List<Row> list) throws Exception {
        String[] results = new String[list.size()];
        for(int i=0; i<list.size(); i++){
            Row row = list.get(i);
            String tradeDate = row.getString(1);
            results[i] = tradeDate;
        }
        return results;
    }

    private double[] getOpenArray(List<Row> list) throws Exception {
        double[] results = new double[list.size()];
        for(int i=0; i<list.size(); i++){
            Row row = list.get(i);
            double close = row.getDouble(2);
            results[i] = close;
        }
        return results;
    }

    private double[] getHighArray(List<Row> list) throws Exception {
        double[] results = new double[list.size()];
        for(int i=0; i<list.size(); i++){
            Row row = list.get(i);
            double close = row.getDouble(3);
            results[i] = close;
        }
        return results;
    }

    private double[] getLowArray(List<Row> list) throws Exception {
        double[] results = new double[list.size()];
        for(int i=0; i<list.size(); i++){
            Row row = list.get(i);
            double close = row.getDouble(4);
            results[i] = close;
        }
        return results;
    }

    private double[] getCloseArray(List<Row> list) throws Exception {
        double[] results = new double[list.size()];
        for(int i=0; i<list.size(); i++){
            Row row = list.get(i);
            double close = row.getDouble(5);
            results[i] = close;
        }
        return results;
    }

    private void macd(double[] closePrice, int optInFastPeriod, int optInSlowPeriod, int optInSignalPeriod, double[] dif, double[] dea, double[] macd) throws Exception {
        // init ema
        double[] ema12 = new double[closePrice.length];
        double[] ema26 = new double[closePrice.length];
        // init value
        ema12[0] = closePrice[0];
        ema26[0] = closePrice[0];
        dif[0] = 0;
        dea[0] = 0;
        macd[0] = 0;
        // init weight
        double k12 = 2.0D / (double)(optInFastPeriod + 1);
        double k26 = 2.0D / (double)(optInSlowPeriod + 1);
        double k9 = 2.0D / (double)(optInSignalPeriod + 1);
        // macd
        for(int i=1; i<closePrice.length; i++){
            ema12[i] = closePrice[i] * k12 + ema12[i-1] * (1-k12);
            ema26[i] = closePrice[i] * k26 + ema26[i-1] * (1-k26);
            dif[i] = ema12[i] - ema26[i];
            dea[i] = dif[i] * k9 + dea[i-1] * (1-k9);
            macd[i] = (dif[i] - dea[i]) * 2;
        }
    }

    private void choujiao_suotou(String tsCode,double[] openPrice,double[] highPrice,double[] lowPrice,double[] closePrice,String[] tradeDate,double[] dif,double[] dea,double[] macd) throws Exception {
        int buy_index = -1;
        int sell_index = -1;
        double sell = -1;
        for(int i=1; i<closePrice.length; i++){
            double current_macd = macd[i];
            if(buy_index < 0 && current_macd > macd[i-1] && Math.abs(current_macd)>=0.128){
                buy_index = i + 1;
                continue;
            }
            if(buy_index > 0){
                double zhiying = openPrice[buy_index] * (1 + 0.25);
                double zhisun = openPrice[buy_index] * (1 - 0.09);
                if(current_macd < macd[i-1]){
                    sell_index = i + 1;
                    break;
                }
                if(highPrice[i] > zhiying){
                    sell_index = i;
                    sell = zhiying;
                    break;
                }
                if(lowPrice[i] < zhisun){
                    sell_index = i;
                    sell = zhisun;
                    break;
                }
            }
        }
        buyAndSell(tsCode,tradeDate,buy_index,sell_index,sell,openPrice,closePrice);
    }

    private void jincha_sicha(String tsCode,double[] openPrice,double[] highPrice,double[] lowPrice,double[] closePrice,String[] tradeDate,double[] dif,double[] dea,double[] macd) throws Exception {
        int buy_index = -1;
        int sell_index = -1;
        double sell = -1;
        for(int i=1; i<closePrice.length; i++){
            double current_macd = macd[i];
            if(buy_index < 0 && current_macd > 0 && macd[i-1] < 0){
                buy_index = i + 1;
                continue;
            }
            if(buy_index > 0 && current_macd < 0 && macd[i-1] > 0){
                sell_index = i + 1;
                break;
            }
        }
        buyAndSell(tsCode,tradeDate,buy_index,sell_index,sell,openPrice,closePrice);
    }

    private void jincha_suotou(String tsCode,double[] openPrice,double[] highPrice,double[] lowPrice,double[] closePrice,String[] tradeDate,double[] dif,double[] dea,double[] macd) throws Exception {
        int buy_index = -1;
        int sell_index = -1;
        double sell = -1;
        for(int i=1; i<closePrice.length; i++){
            double current_macd = macd[i];
            if(buy_index < 0 && current_macd > 0 && macd[i-1] < 0){
                buy_index = i + 1;
                continue;
            }
            if(buy_index > 0 && current_macd < macd[i-1]){
                sell_index = i + 1;
                break;
            }
        }
        buyAndSell(tsCode,tradeDate,buy_index,sell_index,sell,openPrice,closePrice);
    }

    private void choujiao_sicha(String tsCode,double[] openPrice,double[] highPrice,double[] lowPrice,double[] closePrice,String[] tradeDate,double[] dif,double[] dea,double[] macd) throws Exception {
        int buy_index = -1;
        int sell_index = -1;
        double sell = -1;
        for(int i=1; i<closePrice.length; i++){
            double current_macd = macd[i];
            if(buy_index < 0 && current_macd > macd[i-1]){
                buy_index = i + 1;
                continue;
            }
            if(buy_index > 0 && current_macd < 0 && macd[i-1] > 0){
                sell_index = i + 1;
                break;
            }
        }
        buyAndSell(tsCode,tradeDate,buy_index,sell_index,sell,openPrice,closePrice);
    }

    private void buyAndSell(String tsCode,String[] tradeDate,int buy_index, int sell_index, double sell, double[] openPrice,double[] closePrice) throws Exception {
        if(buy_index > 0 && sell_index > 0 && sell_index >= buy_index){
            if(sell < 0){
                sell = openPrice[sell_index];
            }
            double value = (sell-openPrice[buy_index])*100/openPrice[buy_index];
            if(value > 0){
                shengli++;
                yingli += value;
            }else{
                kuisun += value;
            }
            num++;
            jingli += value;
            tianshu += sell_index - buy_index + 1;
            sum = sum * (1 + value / 100);
            System.out.println("ts_code:"+tsCode+" trade_date:"+tradeDate[buy_index]+"-"+tradeDate[sell_index]+" tianshu:"+(sell_index - buy_index + 1)+" buy_index:"+buy_index+" sell_index:"+sell_index+" value:"+value+" sum:"+sum);
        }
    }

    private void printSum() throws Exception {
        System.out.println("num: "+num);
        System.out.println("shenglv: "+(shengli * 100 / num));
        System.out.println("yingli: "+yingli);
        System.out.println("kuisun: "+kuisun);
        System.out.println("jingli: "+jingli);
        System.out.println("yingkuibi: "+yingli*(num-shengli)/kuisun/shengli);
        System.out.println("tianshu: "+tianshu);
        System.out.println("pingjuntianshu: "+tianshu/num);
        System.out.println("sum: "+sum);
        num = 0;
        shengli = 0;
        yingli = 0;
        kuisun = 0;
        jingli = 0;
        sum = 10000;
    }
}