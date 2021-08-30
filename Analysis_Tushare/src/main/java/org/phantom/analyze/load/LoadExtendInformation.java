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

    }
}
