package org.phantom.analyze.strategy;

import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;

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
        System.out.println("strategy");
    }
}
