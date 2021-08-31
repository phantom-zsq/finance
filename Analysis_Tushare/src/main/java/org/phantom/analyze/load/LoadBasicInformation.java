package org.phantom.analyze.load;

import org.apache.avro.generic.GenericData;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;

import java.util.*;

public class LoadBasicInformation {

    private static SparkSession session;
    private static Properties properties;

    public LoadBasicInformation(){
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public List<StockBean> load() throws Exception {
        // 股价
        List<StockBean> list = new ArrayList<StockBean>();
        List<Row> proBarList = session.sql("select trade_date,close from pro_bar order by trade_date").collectAsList();
        for(Row row : proBarList){
            StockBean bean = new StockBean();
            bean.setTrade_date(row.getString(0));
            bean.setClose(row.getDouble(1));
            list.add(bean);
        }
        // 北向资金
        Map<String, StockBean> hkHoldMap = new HashMap<String, StockBean>();
        List<Row> hkHoldList = session.sql("select trade_date,ratio from hk_hold order by trade_date").collectAsList();
        for(Row row : hkHoldList){
            StockBean bean = new StockBean();
            bean.setTrade_date(row.getString(0));
            bean.setRatio(row.getDouble(1));
            hkHoldMap.put(row.getString(0), bean);
        }
        // 汇总
        for(StockBean bean : list){
            String trade_date = bean.getTrade_date();
            // 北向资金
            StockBean hkHoldBean = hkHoldMap.get(trade_date);
            if(hkHoldBean != null){
                bean.setRatio(hkHoldBean.getRatio());
            }
        }
        return list;
    }
}
