package org.phantom.analyze.statistics;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.common.Config;
import org.phantom.analyze.load.LoadOracleData;
import java.util.*;

public class AmountOfBond {

    private static SparkSession session;
    private static Properties properties;

    private static Map<String, List<String>> exponentialMap = new HashMap<String, List<String>>();
    private static Map<String, List<String>> increaseMap = new HashMap<String, List<String>>();
    private static Map<String, List<String>> averageExponentialMap = new HashMap<String, List<String>>();
    private static Map<String, List<String>> averageIncreaseMap = new HashMap<String, List<String>>();
    private static Map<String, List<String>> hongSanBingMap = new HashMap<String, List<String>>();

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
        Map<String, List<Row>> map = getOriginalMap();
        // issue size strategy
        //issueSize(rows);
        // exponential function strategy
        exponentialFunction(map);
        // increase strategy
        increase(map);
        // moving average strategy for exponential function
        movingAverageExponentialFunction(map);
        // moving average strategy for increase
        movingAverageIncrease(map);
        // hong san bing strategy
        hongSanBing(map);
        // common condition about below
        strategy(map);
    }

    private void strategy(Map<String, List<Row>> map) throws Exception {
        int count = 4;
        for(int k=1; k<=40; k++){
            for(int j=k; j<=40; j++){
                double allSum = 0;
                double successSum = 0;
                double failSum = 0;
                int allCount = 0;
                int successCount = 0;
                int failCount = 0;
                for(String ts_code : hongSanBingMap.keySet()){
                    List<String> list = hongSanBingMap.get(ts_code);
                    for(String result : list){
                        String[] str = result.split(":");
                        Long num = Long.valueOf(str[0]);
                        String startDate = str[1];
                        String endDate = str[2];
                        if(num >= count){
                            String end = getEndDate(ts_code,startDate,count,map);
                            if(contain(ts_code,startDate,end)){
                                double tmp = buy(ts_code,end,map,k,j);
                                allSum += tmp;
                                allCount++;
                                if(tmp > 0){
                                    successSum += tmp;
                                    successCount++;
                                }else{
                                    failSum += tmp;
                                    failCount++;
                                }
                            }
                        }
                    }
                }
                System.out.println(k+":"+j+":"+allSum+":"+successSum+":"+failSum+":"+allCount+":"+successCount+":"+failCount);
            }
        }
    }

    private double buy(String ts_code,String start,Map<String, List<Row>> map,int k, int j) throws Exception {
        List<Row> list = map.get(ts_code);
        for(int m=0; m<list.size(); m++){
            Row row = list.get(m);
            int i = 1;
            String trade_date = row.getString(i++);
            Double open = row.getDouble(i++);
            Double high = row.getDouble(i++);
            Double low = row.getDouble(i++);
            Double close = row.getDouble(i++);
            Double amount = row.getDouble(i++);
            Double issue_size = row.getDouble(i++);
            if(start.equals(trade_date)){
                int buy = m+k;
                int sell = m+j < list.size() ? m+j : list.size()-1;
                if(buy < list.size()){
                    Double buyOpen = list.get(buy).getDouble(2);
                    Double sellClose = list.get(sell).getDouble(5);
                    return (sellClose-buyOpen)*100/buyOpen;
                }
            }
        }
        return 0;
    }

    private String getEndDate(String ts_code,String start,int count,Map<String, List<Row>> map) throws Exception {
        List<Row> list = map.get(ts_code);
        for(int m=0; m<list.size(); m++){
            Row row = list.get(m);
            int i = 1;
            String trade_date = row.getString(i++);
            Double open = row.getDouble(i++);
            Double high = row.getDouble(i++);
            Double low = row.getDouble(i++);
            Double close = row.getDouble(i++);
            Double amount = row.getDouble(i++);
            Double issue_size = row.getDouble(i++);
            if(start.equals(trade_date)){
                int j = m+count-1;
                if(j < list.size()){
                    return list.get(j).getString(1);
                }else{
                    return "";
                }
            }
        }
        return "";
    }

    private boolean contain(String ts_code,String start,String end) throws Exception {
        boolean isContain = false;
        List<String> list = new ArrayList<String>();
        if(averageIncreaseMap.containsKey(ts_code)){
            list = averageIncreaseMap.get(ts_code);
        }
        for(String result : list){
            String[] str = result.split(":");
            Long num = Long.valueOf(str[0]);
            String startDate = str[1];
            String endDate = str[2];
            if(start.compareTo(startDate)>=0 && end.compareTo(endDate)<=0){
                isContain = true;
                break;
            }
        }
        return isContain;
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

    private void issueSize(List<Row> rows) throws Exception {
        for(int times = 1; times < 11; times++){
            for (int i = 0; i < 1; i++) {
                for (int j = 1; j < 11; j++) {
                    for (int m = 1; m < 21; m++) {
                        for (int n = m; n < 21; n++) {
                            issueSizeDetail(rows, times, i, j, m, n);
                        }
                    }
                }
            }
        }
    }

    private void issueSizeDetail(List<Row> rows, int times, int interval, int conditionOfBuy, int startofBuy, int endOfBuy) throws Exception {
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

    private void movingAverageExponentialFunction(Map<String, List<Row>> map) throws Exception {
        for(String ts_code : map.keySet()){
            List<Row> list = map.get(ts_code);
            for(int m=0; m<list.size(); m++){
                Row row = list.get(m);
                int i = 1;
                String trade_date = row.getString(i++);
                Double open = row.getDouble(i++);
                Double high = row.getDouble(i++);
                Double low = row.getDouble(i++);
                Double close = row.getDouble(i++);
                Double amount = row.getDouble(i++);
                Double issue_size = row.getDouble(i++);
                amount = getAverage(list, m, 5);
                for(int n=m+1; n<list.size(); n++){
                    Row row_new = list.get(n);
                    int j = 1;
                    String trade_date_new = row_new.getString(j++);
                    Double open_new = row_new.getDouble(j++);
                    Double high_new = row_new.getDouble(j++);
                    Double low_new = row_new.getDouble(j++);
                    Double close_new = row_new.getDouble(j++);
                    Double amount_new = row_new.getDouble(j++);
                    Double issue_size_new = row_new.getDouble(j++);
                    amount_new = getAverage(list, n, 5);
                    if(amount_new >= amount * 2){
                        amount = amount_new;
                        if(n == list.size()-1){
                            List<String> li = new ArrayList<String>();
                            if(averageExponentialMap.containsKey(ts_code)){
                                li = averageExponentialMap.get(ts_code);
                            }
                            li.add((n+1-m)+":"+trade_date+":"+trade_date_new);
                            averageExponentialMap.put(ts_code,li);
                            m = n;
                        }
                        continue;
                    }else{
                        if(n-m > 1){
                            List<String> li = new ArrayList<String>();
                            if(averageExponentialMap.containsKey(ts_code)){
                                li = averageExponentialMap.get(ts_code);
                            }
                            li.add((n-m)+":"+trade_date+":"+list.get(n-1).getString(1));
                            averageExponentialMap.put(ts_code,li);
                            m = n - 1;
                        }
                        break;
                    }
                }
            }
        }
    }

    private void movingAverageIncrease(Map<String, List<Row>> map) throws Exception {
        for(String ts_code : map.keySet()){
            List<Row> list = map.get(ts_code);
            for(int m=0; m<list.size(); m++){
                Row row = list.get(m);
                int i = 1;
                String trade_date = row.getString(i++);
                Double open = row.getDouble(i++);
                Double high = row.getDouble(i++);
                Double low = row.getDouble(i++);
                Double close = row.getDouble(i++);
                Double amount = row.getDouble(i++);
                Double issue_size = row.getDouble(i++);
                amount = getAverage(list, m, 5);
                for(int n=m+1; n<list.size(); n++){
                    Row row_new = list.get(n);
                    int j = 1;
                    String trade_date_new = row_new.getString(j++);
                    Double open_new = row_new.getDouble(j++);
                    Double high_new = row_new.getDouble(j++);
                    Double low_new = row_new.getDouble(j++);
                    Double close_new = row_new.getDouble(j++);
                    Double amount_new = row_new.getDouble(j++);
                    Double issue_size_new = row_new.getDouble(j++);
                    amount_new = getAverage(list, n, 5);
                    if(amount_new >= amount){
                        amount = amount_new;
                        if(n == list.size()-1){
                            List<String> li = new ArrayList<String>();
                            if(averageIncreaseMap.containsKey(ts_code)){
                                li = averageIncreaseMap.get(ts_code);
                            }
                            li.add((n+1-m)+":"+trade_date+":"+trade_date_new);
                            averageIncreaseMap.put(ts_code,li);
                            m = n;
                        }
                        continue;
                    }else{
                        if(n-m > 1){
                            List<String> li = new ArrayList<String>();
                            if(averageIncreaseMap.containsKey(ts_code)){
                                li = averageIncreaseMap.get(ts_code);
                            }
                            li.add((n-m)+":"+trade_date+":"+list.get(n-1).getString(1));
                            averageIncreaseMap.put(ts_code,li);
                            m = n - 1;
                        }
                        break;
                    }
                }
            }
        }
    }

    private Double getAverage(List<Row> list, int index, int averageCount){
        int start = 0;
        int end = index;
        int realCount = averageCount;
        if(index < averageCount-1){
            realCount = index + 1;
        }else{
            start = index - averageCount + 1;
        }
        Double result = 0.0;
        for(int i=start; i<=end; i++){
            result += list.get(i).getDouble(6);
        }
        return result/realCount;
    }

    private void exponentialFunction(Map<String, List<Row>> map) throws Exception {
        for(String ts_code : map.keySet()){
            List<Row> list = map.get(ts_code);
            for(int m=0; m<list.size(); m++){
                Row row = list.get(m);
                int i = 1;
                String trade_date = row.getString(i++);
                Double open = row.getDouble(i++);
                Double high = row.getDouble(i++);
                Double low = row.getDouble(i++);
                Double close = row.getDouble(i++);
                Double amount = row.getDouble(i++);
                Double issue_size = row.getDouble(i++);
                for(int n=m+1; n<list.size(); n++){
                    Row row_new = list.get(n);
                    int j = 1;
                    String trade_date_new = row_new.getString(j++);
                    Double open_new = row_new.getDouble(j++);
                    Double high_new = row_new.getDouble(j++);
                    Double low_new = row_new.getDouble(j++);
                    Double close_new = row_new.getDouble(j++);
                    Double amount_new = row_new.getDouble(j++);
                    Double issue_size_new = row_new.getDouble(j++);
                    if(amount_new >= amount * 2){
                        amount = amount_new;
                        if(n == list.size()-1){
                            List<String> li = new ArrayList<String>();
                            if(exponentialMap.containsKey(ts_code)){
                                li = exponentialMap.get(ts_code);
                            }
                            li.add((n+1-m)+":"+trade_date+":"+trade_date_new);
                            exponentialMap.put(ts_code,li);
                            m = n;
                        }
                        continue;
                    }else{
                        if(n-m > 1){
                            List<String> li = new ArrayList<String>();
                            if(exponentialMap.containsKey(ts_code)){
                                li = exponentialMap.get(ts_code);
                            }
                            li.add((n-m)+":"+trade_date+":"+list.get(n-1).getString(1));
                            exponentialMap.put(ts_code,li);
                            m = n - 1;
                        }
                        break;
                    }
                }
            }
        }
    }

    private void increase(Map<String, List<Row>> map) throws Exception {
        for(String ts_code : map.keySet()){
            List<Row> list = map.get(ts_code);
            for(int m=0; m<list.size(); m++){
                Row row = list.get(m);
                int i = 1;
                String trade_date = row.getString(i++);
                Double open = row.getDouble(i++);
                Double high = row.getDouble(i++);
                Double low = row.getDouble(i++);
                Double close = row.getDouble(i++);
                Double amount = row.getDouble(i++);
                Double issue_size = row.getDouble(i++);
                for(int n=m+1; n<list.size(); n++){
                    Row row_new = list.get(n);
                    int j = 1;
                    String trade_date_new = row_new.getString(j++);
                    Double open_new = row_new.getDouble(j++);
                    Double high_new = row_new.getDouble(j++);
                    Double low_new = row_new.getDouble(j++);
                    Double close_new = row_new.getDouble(j++);
                    Double amount_new = row_new.getDouble(j++);
                    Double issue_size_new = row_new.getDouble(j++);
                    if(amount_new >= amount){
                        amount = amount_new;
                        if(n == list.size()-1){
                            List<String> li = new ArrayList<String>();
                            if(increaseMap.containsKey(ts_code)){
                                li = increaseMap.get(ts_code);
                            }
                            li.add((n+1-m)+":"+trade_date+":"+trade_date_new);
                            increaseMap.put(ts_code,li);
                            m = n;
                        }
                        continue;
                    }else{
                        if(n-m > 1){
                            List<String> li = new ArrayList<String>();
                            if(increaseMap.containsKey(ts_code)){
                                li = increaseMap.get(ts_code);
                            }
                            li.add((n-m)+":"+trade_date+":"+list.get(n-1).getString(1));
                            increaseMap.put(ts_code,li);
                            m = n - 1;
                        }
                        break;
                    }
                }
            }
        }
    }

    private void hongSanBing(Map<String, List<Row>> map) throws Exception {
        for(String ts_code : map.keySet()){
            List<Row> list = map.get(ts_code);
            for(int m=0; m<list.size(); m++){
                Row row = list.get(m);
                int i = 1;
                String trade_date = row.getString(i++);
                Double open = row.getDouble(i++);
                Double high = row.getDouble(i++);
                Double low = row.getDouble(i++);
                Double close = row.getDouble(i++);
                Double amount = row.getDouble(i++);
                Double issue_size = row.getDouble(i++);
                // change
                if(close <= open){
                    continue;
                }
                for(int n=m+1; n<list.size(); n++){
                    Row row_new = list.get(n);
                    int j = 1;
                    String trade_date_new = row_new.getString(j++);
                    Double open_new = row_new.getDouble(j++);
                    Double high_new = row_new.getDouble(j++);
                    Double low_new = row_new.getDouble(j++);
                    Double close_new = row_new.getDouble(j++);
                    Double amount_new = row_new.getDouble(j++);
                    Double issue_size_new = row_new.getDouble(j++);
                    // change
                    if(close_new > open_new && open_new > close){
                        close = close_new;
                        if(n == list.size()-1){
                            List<String> li = new ArrayList<String>();
                            if(hongSanBingMap.containsKey(ts_code)){
                                li = hongSanBingMap.get(ts_code);
                            }
                            li.add((n+1-m)+":"+trade_date+":"+trade_date_new);
                            hongSanBingMap.put(ts_code,li);
                            m = n;
                        }
                        continue;
                    }else{
                        if(n-m > 1){
                            List<String> li = new ArrayList<String>();
                            if(hongSanBingMap.containsKey(ts_code)){
                                li = hongSanBingMap.get(ts_code);
                            }
                            li.add((n-m)+":"+trade_date+":"+list.get(n-1).getString(1));
                            hongSanBingMap.put(ts_code,li);
                            m = n - 1;
                        }
                        break;
                    }
                }
            }
        }
    }
}