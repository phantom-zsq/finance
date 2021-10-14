package org.phantom.analyze.load;

import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LoadExtendInformation {

    private static SparkSession session;
    private static Properties properties;

    public LoadExtendInformation(){
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public void load(Map<String, List<StockBean>> map) throws Exception {
        // 北向资金
        bxzj(map);
        // 其他
        other(map);
    }

    private void bxzj(Map<String, List<StockBean>> map) throws Exception {
        setAvg(map, 5);
        setAvg(map, 10);
        setAvg(map, 20);
        setAvg(map, 30);
        setAvg(map, 60);
    }

    private void other(Map<String, List<StockBean>> map) throws Exception {

    }

    private void setAvg(Map<String, List<StockBean>> map, int num) throws Exception {
        for(String tsCode : map.keySet()){
            List<StockBean> list = map.get(tsCode);
            Double sum = 0.0;
            int j = 0;
            boolean start = false;
            for (int i=0; i<list.size(); i++) {
                StockBean bean = list.get(i);
                int bxStatus = bean.getBx_status();
                if(bxStatus==1){
                    start = true;
                }
                if(start){
                    Double ratio = bean.getBx_ratio();
                    if(j<num){
                        sum += ratio;
                        choiceAvg(bean, num, sum / (j+1));
                        j++;
                    }else{
                        sum += ratio - list.get(i-num).getBx_ratio();
                        choiceAvg(bean, num, sum / num);
                    }
                }
                if(bxStatus==-1){
                    start = false;
                }
            }
        }
    }

    private void choiceAvg(StockBean bean, int num, double value) throws Exception {
        switch (num){
            case 5:
                bean.setBx_avg_5(value);
                break;
            case 10:
                bean.setBx_avg_10(value);
                break;
            case 20:
                bean.setBx_avg_20(value);
                break;
            case 30:
                bean.setBx_avg_30(value);
                break;
            case 60:
                bean.setBx_avg_60(value);
                break;
        }
    }
}
