package org.phantom.analyze.load;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.common.Config;

import java.util.Properties;

public class LoadWhiteListData {

    private static SparkSession session;
    private static Properties properties;

    public LoadWhiteListData(){
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public void load() throws Exception {
        Dataset<Row> whiteList = session.sql("select distinct ts_code from hsgt_top10 where ts_code in('000333.SZ')");
        whiteList.createOrReplaceTempView("white_list");
    }
}
