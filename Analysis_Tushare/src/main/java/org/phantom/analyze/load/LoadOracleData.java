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
        // A股复权行情
        Dataset<Row> proBar = session.read().jdbc(properties.getProperty("url"), "(select ts_code,date_format(trade_date,'%Y-%m-%d') as trade_date,close from pro_bar where ts_code=\'603997.SH\') tt", properties);
        proBar.createOrReplaceTempView("pro_bar");
        // 沪深股通成分股
        Dataset<Row> hkHoldDS = session.read().jdbc(properties.getProperty("url"), "(select * from hk_hold) tt", properties);
        hkHoldDS.createOrReplaceTempView("hs_const");
        // 沪深港通资金流向
        Dataset<Row> moneyflowHsgt = session.read().jdbc(properties.getProperty("url"), "(select * from moneyflow_hsgt) tt", properties);
        moneyflowHsgt.createOrReplaceTempView("moneyflow_hsgt");
        // 沪深股通十大成交股
        Dataset<Row> hsgtTop10 = session.read().jdbc(properties.getProperty("url"), "(select * from hsgt_top10) tt", properties);
        hsgtTop10.createOrReplaceTempView("hsgt_top10");
        // 沪深港股通成交明细
        Dataset<Row> hkHold = session.read().jdbc(properties.getProperty("url"), "(select ts_code,date_format(trade_date,'%Y-%m-%d') as trade_date,ratio from hk_hold where ts_code=\'603997.SH\') tt", properties);
        hkHold.createOrReplaceTempView("hk_hold");
    }
}
