package org.phantom.analyze.load;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;

import java.util.ArrayList;
import java.util.Arrays;
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

    public void bxzj(List<StockBean> list) throws Exception {
        avg(list, 60);
    }

    public void avg(List<StockBean> list, int num) throws Exception {
        Double sum = 0.0;
        boolean start = false;
        int j = 0;
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
                    bean.setAvg_60(sum / (j+1));
                }else{
                    sum += ratio - list.get(i-num).getRatio();
                    bean.setAvg_60(sum / num);
                }
                j++;
            }
            if(bxStatus==-1){
                start = false;
            }
        }
    }
}
