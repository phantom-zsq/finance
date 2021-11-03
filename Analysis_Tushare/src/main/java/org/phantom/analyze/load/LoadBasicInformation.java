package org.phantom.analyze.load;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;
import java.util.*;

public class LoadBasicInformation {

    private static SparkSession session;
    private static Properties properties;

    public LoadBasicInformation() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public Map<String, List<StockBean>> load() throws Exception {
        // A股复权行情
        Map<String, List<StockBean>> map = loadStock();
        // 北向资金
        loadBx(map);
        // 其他
        loadOthers(map);
        return map;
    }

    public Map<String, List<StockBean>> loadStock() throws Exception {
        Map<String, List<StockBean>> map = new HashMap<String, List<StockBean>>();
        List<Row> proBarList = session.sql("select ts_code,trade_date,open,high,low,close,pre_close,change,pct_chg,vol,amount from pro_bar where ts_code in(select ts_code from white_list) order by ts_code,trade_date").collectAsList();
        for (Row row : proBarList) {
            int i = 0;
            List<StockBean> list = new ArrayList<StockBean>();
            String tsCode = row.getString(i++);
            if (map.containsKey(tsCode)) {
                list = map.get(tsCode);
            } else {
                map.put(tsCode, list);
            }
            StockBean bean = new StockBean();
            bean.setTrade_date(row.getString(i++));
            bean.setOpen(row.getDouble(i++));
            bean.setHigh(row.getDouble(i++));
            bean.setLow(row.getDouble(i++));
            bean.setClose(row.getDouble(i++));
            bean.setPre_close(row.getDouble(i++));
            bean.setChange(row.getDouble(i++));
            bean.setPct_chg(row.getDouble(i++));
            bean.setVol(row.getDouble(i++));
            bean.setAmount(row.getDouble(i++));
            list.add(bean);
        }
        return map;
    }

    public void loadBx(Map<String, List<StockBean>> map) throws Exception {
        // 沪深港通资金流向
        loadBxMoneyFlow(map);
        // 沪深股通十大成交股
        loadBxTop10(map);
        // 沪深港股通成交明细
        loadBxDetails(map);
    }

    public void loadBxMoneyFlow(Map<String, List<StockBean>> map) throws Exception {

    }

    public void loadBxTop10(Map<String, List<StockBean>> map) throws Exception {
        Map<String, StockBean> hsgtTop10Map = new HashMap<String, StockBean>();
        List<Row> hsgtTop10List = session.sql("select ts_code,trade_date,rank,amount,net_amount,buy,sell from hsgt_top10 where ts_code in(select ts_code from white_list) order by ts_code,trade_date").collectAsList();
        for (int i = 0; i < hsgtTop10List.size(); i++) {
            Row row = hsgtTop10List.get(i);
            int j = 0;
            String tsCodeTradeDate = row.getString(j++)+row.getDouble(j++);
            StockBean bean = new StockBean();
            bean.setBx_rank(row.getDouble(j++));
            bean.setBx_amount(row.getDouble(j++));
            bean.setBx_net_amount(row.getDouble(j++));
            bean.setBx_buy(row.getDouble(j++));
            bean.setBx_sell(row.getDouble(j++));
            hsgtTop10Map.put(tsCodeTradeDate, bean);
        }
        for(String tsCode : map.keySet()){
            List<StockBean> list = map.get(tsCode);
            for (int i = 0; i < list.size(); i++) {
                StockBean bean = list.get(i);
                String tradeDate = bean.getTrade_date();
                String tsCodeTradeDate = tsCode+tradeDate;
                if(hsgtTop10Map.containsKey(tsCodeTradeDate)){
                    bean.setBx_rank(hsgtTop10Map.get(tsCodeTradeDate).getBx_rank());
                    bean.setBx_amount(hsgtTop10Map.get(tsCodeTradeDate).getBx_amount());
                    bean.setBx_net_amount(hsgtTop10Map.get(tsCodeTradeDate).getBx_net_amount());
                    bean.setBx_buy(hsgtTop10Map.get(tsCodeTradeDate).getBx_buy());
                    bean.setBx_sell(hsgtTop10Map.get(tsCodeTradeDate).getBx_sell());
                }
            }
        }
    }

    public void loadBxDetails(Map<String, List<StockBean>> map) throws Exception {
        Map<String, StockBean> hkHoldMap = new HashMap<String, StockBean>();
        List<Row> hkHoldList = session.sql("select ts_code,trade_date,vol,ratio from hk_hold where ts_code in(select ts_code from white_list) order by ts_code,trade_date").collectAsList();
        for (int i = 0; i < hkHoldList.size(); i++) {
            Row row = hkHoldList.get(i);
            int j = 0;
            String tsCodeTradeDate = row.getString(j++)+row.getDouble(j++);
            StockBean bean = new StockBean();
            bean.setBx_vol(row.getDouble(j++));
            bean.setBx_ratio(row.getDouble(j++));
            hkHoldMap.put(tsCodeTradeDate, bean);
        }
        for(String tsCode : map.keySet()){
            List<StockBean> list = map.get(tsCode);
            for (int i = 0; i < list.size(); i++) {
                StockBean bean = list.get(i);
                String tradeDate = bean.getTrade_date();
                String tsCodeTradeDate = tsCode+tradeDate;
                if(hkHoldMap.containsKey(tsCodeTradeDate)){
                    bean.setBx_vol(hkHoldMap.get(tsCodeTradeDate).getBx_vol());
                    bean.setBx_ratio(hkHoldMap.get(tsCodeTradeDate).getBx_ratio());
                }
            }
        }
    }

    public void loadOthers(Map<String, List<StockBean>> map) throws Exception {

    }
}
