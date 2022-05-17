package org.phantom.analyze.util;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.HighLowBean;
import org.phantom.analyze.common.Config;
import org.phantom.analyze.load.LoadOracleData;

import java.util.*;

public class HighLowUtil {

    private static SparkSession session;
    private static Properties properties;

    public HighLowUtil() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        HighLowUtil stockUtil = new HighLowUtil();
        new LoadOracleData().load();
        Map<String, List<HighLowBean>> map = stockUtil.getOriginalMap();
        System.out.println(map.keySet().size());
        for(String ts_code : map.keySet()){
            List<HighLowBean> list = stockUtil.getHighLow(map.get(ts_code), 10);
            session.createDataFrame(list, HighLowBean.class)
                    .write()
                    .mode(SaveMode.Append)
                    .jdbc(properties.getProperty("url"), "manual_cb_daily_high_low", properties);
        }
    }

    private Map<String, List<HighLowBean>> getOriginalMap() throws Exception {
        List<Row> rows = session.sql("select a.ts_code,a.trade_date,a.open,a.high,a.low,a.close,a.pre_close,a.change,a.pct_chg,a.vol,a.amount from cb_daily a where a.open > 0 order by a.ts_code,a.trade_date").collectAsList();
        Map<String, List<HighLowBean>> map = new HashMap<String, List<HighLowBean>>();
        for(Row row : rows){
            // columns
            int i = 0;
            String ts_code = row.getString(i++);
            String trade_date = row.getString(i++);
            Double open = row.getDouble(i++);
            Double high = row.getDouble(i++);
            Double low = row.getDouble(i++);
            Double close = row.getDouble(i++);
            Double pre_close = row.getDouble(i++);
            Double change = row.getDouble(i++);
            Double pct_chg = row.getDouble(i++);
            Double vol = row.getDouble(i++);
            Double amount = row.getDouble(i++);
            // bean
            HighLowBean bean = new HighLowBean();
            bean.setTs_code(ts_code);
            bean.setTrade_date(trade_date);
            bean.setOpen(open);
            bean.setHigh(high);
            bean.setLow(low);
            bean.setClose(close);
            bean.setPre_close(pre_close);
            bean.setChange(change);
            bean.setPct_chg(pct_chg);
            bean.setVol(vol);
            bean.setAmount(amount);
            // map
            List<HighLowBean> list = new ArrayList<HighLowBean>();
            if(map.containsKey(ts_code)){
                list = map.get(ts_code);
            }
            list.add(bean);
            map.put(ts_code,list);
        }
        return map;
    }

    public List<HighLowBean> getHighLow(List<HighLowBean> list, int time) throws Exception {
        int size = list.size();
        List<HighLowBean> highLowList = new ArrayList<HighLowBean>();
        // get high
        m:for(int i=0; i<size; i++){
            HighLowBean bean = list.get(i);
            Double currentClose = Double.valueOf(bean.getClose());
            int[] value = getMinMax(i, size, time);
            if(value == null){
                continue;
            }
            int min = value[0];
            int max = value[1];
            for(int j=min; j<=max; j++){
                Double tmpClose = Double.valueOf(list.get(j).getClose());
                if(currentClose < tmpClose){
                    continue m;
                }
            }
            bean.setStatus("high");
            bean.setIndex(i+1);
            highLowList.add(bean);
        }
        // get low
        n:for(int i=0; i<size; i++){
            HighLowBean bean = list.get(i);
            Double currentClose = Double.valueOf(bean.getClose());
            int[] value = getMinMax(i, size, time);
            if(value == null){
                continue;
            }
            int min = value[0];
            int max = value[1];
            for(int j=min; j<=max; j++){
                Double tmpClose = Double.valueOf(list.get(j).getClose());
                if(currentClose > tmpClose){
                    continue n;
                }
            }
            bean.setStatus("low");
            bean.setIndex(i+1);
            highLowList.add(bean);
        }
        // sort
        Collections.sort(highLowList, new Comparator<HighLowBean>() {
            public int compare(HighLowBean u1, HighLowBean u2) {
                return u1.getTrade_date().compareTo(u2.getTrade_date());
            }
        });
        // remove series high or low
        List<HighLowBean> result = new ArrayList<HighLowBean>();
        HighLowBean bean = new HighLowBean();
        for(int i=0; i<highLowList.size(); i++){
            HighLowBean addBean = bean;
            HighLowBean tmp = highLowList.get(i);
            if(i == 0){
                bean = tmp;
            }else if(bean.getStatus().equals(tmp.getStatus())){
                if("high".equals(bean.getStatus()) && Double.valueOf(bean.getClose()) < Double.valueOf(tmp.getClose())){
                    bean = tmp;
                }
                if("low".equals(bean.getStatus()) && Double.valueOf(bean.getClose()) > Double.valueOf(tmp.getClose())){
                    bean = tmp;
                }
            }else{
                result.add(addBean);
                bean = tmp;
            }
            if(i == highLowList.size()-1){
                result.add(bean);
            }
        }
        // set next_index
        for(int i=0; i<result.size()-1; i++){
            HighLowBean first = result.get(i);
            HighLowBean second = result.get(i+1);
            first.setNext_index(second.getIndex());
        }
        return result;
    }

    private int[] getMinMax(int i, int size, int time) {
        int[] value = new int[2];
        int min = i - time;
        int max = i + time;
        if(min < 0){
            return null;
        }
        if(max >= size){
            return null;
        }
        value[0] = min;
        value[1] = max;
        return value;
    }
}
