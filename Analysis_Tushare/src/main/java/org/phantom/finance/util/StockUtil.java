package org.phantom.finance.util;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.finance.bean.StockHistoryDataBean;

import java.text.SimpleDateFormat;
import java.util.*;

public class StockUtil {

    private static SparkSession session;
    private static Properties properties;

    public StockUtil(SparkSession session, Properties properties) {
        this.session = session;
        this.properties = properties;
    }

    public List<StockHistoryDataBean> getStockList(String table, String code) throws Exception {
        List<Row> list = session.read().jdbc(properties.getProperty("url"), "(select * from "+table+" where code='" + code + "' order by date) tt", properties).collectAsList();
        List<StockHistoryDataBean> result = new ArrayList<StockHistoryDataBean>();
        for (Row row : list) {
            StockHistoryDataBean stock = new StockHistoryDataBean();
            int i = 0;
            // 日期
            stock.setDate(row.getString(i++));
            // 股票代码
            stock.setCode(row.getString(i++));
            // 收盘价
            stock.setClose(row.getString(i++));
            // 最高价
            stock.setHigh(row.getString(i++));
            // 最低价
            stock.setLow(row.getString(i++));
            // 开盘价
            stock.setOpen(row.getString(i++));
            // 前收盘
            stock.setLopen(row.getString(i++));
            // 涨跌额
            stock.setChg(row.getString(i++));
            // 涨跌幅
            stock.setPchg(row.getString(i++));
            // 换手率
            stock.setTurnover(row.getString(i++));
            // 成交量
            stock.setVoturnover(row.getString(i++));
            // 成交金额
            stock.setVaturnover(row.getString(i++));
            // 总市值
            stock.setTcap(row.getString(i++));
            // 流通市值
            stock.setMcap(row.getString(i++));
            result.add(stock);
        }
        return result;
    }

    public List<StockHistoryDataBean> getHighLow(List<StockHistoryDataBean> list, int time) throws Exception {
        // 时间间隔
        int size = list.size();
        // 高低点集合
        List<StockHistoryDataBean> highLowList = new ArrayList<StockHistoryDataBean>();
        // 获取高点
        m:for(int i=0; i<size; i++){
            Double currentClose = Double.valueOf(list.get(i).getClose());
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
            list.get(i).setName("高点");
            highLowList.add(list.get(i));
        }
        // 获取低点
        n:for(int i=0; i<size; i++){
            Double currentClose = Double.valueOf(list.get(i).getClose());
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
            list.get(i).setName("低点");
            highLowList.add(list.get(i));
        }
        // 排序
        Collections.sort(highLowList, new Comparator<StockHistoryDataBean>() {
            public int compare(StockHistoryDataBean u1, StockHistoryDataBean u2) {
                return u1.getDate().compareTo(u2.getDate()); //升序
            }
        });
        // 去掉连续的高点或低点
        List<StockHistoryDataBean> result = new ArrayList<StockHistoryDataBean>();
        StockHistoryDataBean bean = highLowList.get(0);
        for(int i=1; i<highLowList.size(); i++){
            StockHistoryDataBean addBean = bean;
            StockHistoryDataBean tmp = highLowList.get(i);
            if(bean.getName().equals(tmp.getName())){
                if("高点".equals(bean.getName()) && Double.valueOf(bean.getClose()) < Double.valueOf(tmp.getClose())){
                    bean = tmp;
                }
                if("低点".equals(bean.getName()) && Double.valueOf(bean.getClose()) > Double.valueOf(tmp.getClose())){
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

    public int getQushi(List<StockHistoryDataBean> history, int i) throws Exception {
        StockHistoryDataBean start = null;
        StockHistoryDataBean low = null;
        StockHistoryDataBean high = null;
        StockHistoryDataBean end = null;
        for(int j=history.size()-60; j<history.size(); j++){
            if(j==history.size()-60){
                start = history.get(j);
            }
            if(j==history.size()-1){
                end = history.get(j);
            }
        }
        return 1;
    }

    public int getMonthSpace(String date1, String date2) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(sdf.parse(date1));
        c2.setTime(sdf.parse(date2));
        int diff_day = c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR);
        int diff_year_day = (c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR)) * 365;
        int diff_month = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
        int diff_year_month = (c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR)) * 12;
        //return Math.abs(diff_month + diff_year_month);
        return Math.abs(diff_day + diff_year_day);
    }

    private void state() throws Exception {
        List<StockHistoryDataBean> result = new ArrayList<StockHistoryDataBean>();
        // 输出
        System.out.println(result.get(0).getName() + " : " +result.get(0).getDate() + " : " + result.get(0).getClose());
        for(int i=1; i<result.size(); i++){
            StockHistoryDataBean current = result.get(i);
            StockHistoryDataBean tmp = result.get(i-1);
            System.out.println(current.getName() + " : " +current.getDate() + " : " + current.getClose() + " : " + getMonthSpace(tmp.getDate(),current.getDate()) + " : " + 100.0 * (Double.valueOf(current.getClose())-Double.valueOf(tmp.getClose()))/Double.valueOf(tmp.getClose()));
        }
        // 统计
        for(int i=1; i<result.size(); i++){
            StockHistoryDataBean current = result.get(i);
            StockHistoryDataBean tmp = result.get(i-1);
            if(Double.valueOf(current.getClose())-Double.valueOf(tmp.getClose()) > 0){
                System.out.println(getMonthSpace(tmp.getDate(),current.getDate()));
            }
        }
    }
}
