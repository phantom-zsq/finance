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
        List<Row> proBarList = session.sql("select ts_code,trade_date,close,pct_chg,vol,amount from pro_bar where ts_code in(select ts_code from white_list) order by ts_code,trade_date").collectAsList();
        for (Row row : proBarList) {
            List<StockBean> list = new ArrayList<StockBean>();
            String tsCode = row.getString(0);
            if (map.containsKey(tsCode)) {
                list = map.get(tsCode);
            } else {
                map.put(tsCode, list);
            }
            StockBean bean = new StockBean();
            bean.setTrade_date(row.getString(1));
            bean.setClose(row.getDouble(2));
            bean.setPct_chg(row.getDouble(3));
            bean.setVol(row.getDouble(4));
            bean.setAmount(row.getDouble(5));
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

    }

    public void loadBxDetails(Map<String, List<StockBean>> map) throws Exception {
        Map<String, StockBean> hkHoldMap = new HashMap<String, StockBean>();
        List<Row> hkHoldList = session.sql("select ts_code,trade_date,ratio from hk_hold where ts_code in(select ts_code from white_list) order by ts_code,trade_date").collectAsList();
        boolean first = true;
        for (int i = 0; i < hkHoldList.size(); i++) {
            Row row = hkHoldList.get(i);
            StockBean bean = new StockBean();
            bean.setTrade_date(row.getString(0));
            bean.setRatio(row.getDouble(1));
            if (first && bean.getRatio() == 0) {
                continue;
            } else {
                if (first) {
                    bean.setBx_status(1);
                }
                if (i == hkHoldList.size() - 1) {
                    bean.setBx_status(-1);
                }
                first = false;
                hkHoldMap.put(bean.getTrade_date(), bean);
            }
        }
    }

    public void loadOthers(Map<String, List<StockBean>> map) throws Exception {

    }
}
