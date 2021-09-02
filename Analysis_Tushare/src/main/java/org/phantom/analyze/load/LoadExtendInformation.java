package org.phantom.analyze.load;

import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;
import java.util.List;
import java.util.Properties;

public class LoadExtendInformation {

    private static SparkSession session;
    private static Properties properties;

    public LoadExtendInformation(){
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public void load(List<StockBean> list) throws Exception {
        // 北向资金
        bxzj(list);
        // 其他
    }

    private void bxzj(List<StockBean> list) throws Exception {
        setAvg(list, 5);
        setAvg(list, 10);
        setAvg(list, 20);
        setAvg(list, 30);
        setAvg(list, 60);
    }

    private void setAvg(List<StockBean> list, int num) throws Exception {
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
                Double ratio = bean.getRatio();
                if(j<num){
                    sum += ratio;
                    choiceAvg(bean, num, sum / (j+1));
                    j++;
                }else{
                    sum += ratio - list.get(i-num).getRatio();
                    choiceAvg(bean, num, sum / num);
                }
            }
            if(bxStatus==-1){
                start = false;
            }
        }
    }

    private void choiceAvg(StockBean bean, int num, double value) throws Exception {
        switch (num){
            case 5:
                bean.setAvg_5(value);
                break;
            case 10:
                bean.setAvg_10(value);
                break;
            case 20:
                bean.setAvg_20(value);
                break;
            case 30:
                bean.setAvg_30(value);
                break;
            case 60:
                bean.setAvg_60(value);
                break;
        }
    }
}
