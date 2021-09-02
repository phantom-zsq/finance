package org.phantom.analyze.load;

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
        boolean first = true;
        for(int i=0; i<hkHoldList.size(); i++){
            Row row = hkHoldList.get(i);
            StockBean bean = new StockBean();
            bean.setTrade_date(row.getString(0));
            bean.setRatio(row.getDouble(1));
            if(first && bean.getRatio() == 0){
                continue;
            }else{
                if(first){
                    bean.setBx_status(1);
                }
                if(i==hkHoldList.size()-1){
                    bean.setBx_status(-1);
                }
                first = false;
                hkHoldMap.put(row.getString(0), bean);
            }
        }
        // 其他

        // 汇总
        for(StockBean bean : list){
            String trade_date = bean.getTrade_date();
            // 北向资金
            StockBean hkHoldBean = hkHoldMap.get(trade_date);
            if(hkHoldBean != null){
                bean.setRatio(hkHoldBean.getRatio());
                bean.setBx_status(hkHoldBean.getBx_status());
            }
            // 其他
        }
        return list;
    }
}
