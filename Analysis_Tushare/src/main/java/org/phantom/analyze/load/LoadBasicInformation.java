package org.phantom.analyze.load;

import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;

import java.util.List;
import java.util.Properties;

public class LoadBasicInformation {

    private static SparkSession session;
    private static Properties properties;

    public LoadBasicInformation(){
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public List<StockBean> load() throws Exception {

        return null;
    }
}
