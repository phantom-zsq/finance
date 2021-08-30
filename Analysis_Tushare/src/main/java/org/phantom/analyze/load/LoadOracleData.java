package org.phantom.analyze.load;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.common.Config;

import java.util.Properties;

public class LoadOracleData {

    private static SparkSession session;
    private static Properties properties;

    public LoadOracleData(){
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public void load() throws Exception {
        Dataset<Row> hkHoldDS = session.read().jdbc(properties.getProperty("url"), "(select ts_code,date_format(trade_date,'%Y-%m-%d') as trade_date,ratio from hk_hold where ts_code=\'603997\') tt", properties);
        hkHoldDS.createOrReplaceTempView("hk_hold");
    }
}
