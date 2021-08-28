package org.phantom.finance.stock;

import org.apache.spark.sql.SparkSession;

import java.util.Properties;

public class StockExecutor {

    private static SparkSession session;
    private static Properties properties;

    public StockExecutor(SparkSession session, Properties properties){
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {
        // 股票基本信息
        //new StockInformationBasicCrawler(session, properties).execute();
        // 股票板块信息
        //new StockInformationSectorCrawler(session, properties).execute();
        // 股票历史交易数据
        //new StockHistoryDataCrawler(session, properties).execute();
        // 股票主力数据
        new StockMainForce(session, properties).execute();
        // 去燥
        //new Etl(session, properties).execute();
        // 股票策略
        //new StockStrategy(session, properties).execute();
    }
}
