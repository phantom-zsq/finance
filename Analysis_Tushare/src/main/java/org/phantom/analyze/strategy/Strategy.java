package org.phantom.analyze.strategy;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Strategy {

    private static SparkSession session;
    private static Properties properties;

    public Strategy(){
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public void analyze(List<StockBean> list) throws Exception {
        boolean start = false;
        int skip = 10; // 跳过前10个
        double maxRate = 3.0; // 最大比例
        double score = 0;
        double maxScore = 8;
        int buyDate = 10;
        for (int i=0; i<list.size(); i++) {
            StockBean bean = list.get(i);
            int bxStatus = bean.getBx_status();
            if(bxStatus==1){
                start = true;
                i += skip-1;
                continue;
            }
            if(start){
                Double rate = bean.getRatio() / bean.getAvg_60();
                double rate1 = list.get(i-1).getRatio() / list.get(i-1).getAvg_60();
                double rate2 = list.get(i-2).getRatio() / list.get(i-2).getAvg_60();
                double rate3 = list.get(i-3).getRatio() / list.get(i-3).getAvg_60();
                if(rate >= maxRate) {
                    if(rate1>=maxRate || rate2>=maxRate || rate3>=maxRate){
                        score++;
                    }
                }else {
                    if(rate1<maxRate && rate2<maxRate){
                        score = 0;
                    }
                }
                if(score == maxScore) {
                    list.get(i+1).setStatus(1);
                    list.get(i+buyDate).setStatus(-1);
                    i += buyDate;
                }
            }
            if(bxStatus==-1){
                start = false;
            }
        }
    }

}
