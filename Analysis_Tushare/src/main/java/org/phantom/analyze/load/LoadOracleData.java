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
        // A股截止到20201231的4265支股票列表
        Dataset<Row> stockBasic = session.read().jdbc(properties.getProperty("url"), "(select * from stock_basic where list_date<'20210101' and market is not null) tt", properties);
        stockBasic.createOrReplaceTempView("stock_basic");
        // A股截止到20201231的4265支股票复权行情
        Dataset<Row> proBar = session.read().jdbc(properties.getProperty("url"), "(select * from pro_bar) tt", properties);
        proBar.createOrReplaceTempView("pro_bar");
        // 沪深港通资金流向
        Dataset<Row> moneyflowHsgt = session.read().jdbc(properties.getProperty("url"), "(select * from moneyflow_hsgt where trade_date>='20160629') tt", properties);
        moneyflowHsgt.createOrReplaceTempView("moneyflow_hsgt");
        // 沪深股通十大成交股
        Dataset<Row> hsgtTop10 = session.read().jdbc(properties.getProperty("url"), "(select * from hsgt_top10 where trade_date>='20160629') tt", properties);
        hsgtTop10.createOrReplaceTempView("hsgt_top10");
        // 沪深港股通成交明细
        Dataset<Row> hkHold = session.read().jdbc(properties.getProperty("url"), "(select * from hk_hold where ts_code!='000043.SZ' and exchange in('SH','SZ')) tt", properties);
        hkHold.createOrReplaceTempView("hk_hold");
        // 可转债发行信息
        Dataset<Row> cbIssue = session.read().jdbc(properties.getProperty("url"), "(select * from manual_cb_issue) tt", properties);
        cbIssue.createOrReplaceTempView("cb_issue");
        // 可转债基本信息
        Dataset<Row> cbBasic = session.read().jdbc(properties.getProperty("url"), "(select * from manual_cb_basic) tt", properties);
        cbBasic.createOrReplaceTempView("cb_basic");
        // 可转债行情
        Dataset<Row> cbDaily = session.read().jdbc(properties.getProperty("url"), "(select * from cb_daily) tt", properties);
        cbDaily.createOrReplaceTempView("cb_daily");
        // 可转债转股价变动情况
        Dataset<Row> cbPriceChg = session.read().jdbc(properties.getProperty("url"), "(select * from cb_price_chg) tt", properties);
        cbPriceChg.createOrReplaceTempView("cb_price_chg");
        // 可转债各阶段日期
        Dataset<Row> manualCbDate = session.read().jdbc(properties.getProperty("url"), "(select * from manual_cb_date) tt", properties);
        manualCbDate.createOrReplaceTempView("manual_cb_date");
        // 可转债溢价率映射表
        Dataset<Row> manualCbPremium = session.read().jdbc(properties.getProperty("url"), "(select * from manual_cb_premium) tt", properties);
        manualCbPremium.createOrReplaceTempView("manual_cb_premium");
        // 可转债评级
        Dataset<Row> manualCbNewestRating = session.read().jdbc(properties.getProperty("url"), "(select * from manual_cb_newest_rating) tt", properties);
        manualCbNewestRating.createOrReplaceTempView("manual_cb_newest_rating");
    }
}
