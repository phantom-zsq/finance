package org.phantom.analyze.main;

import org.apache.spark.sql.SparkSession;

import java.util.Properties;

public class Executor {

    private static SparkSession session;
    private static Properties properties;

    public static void main(String[] args) throws Exception {
        init();
//        new StockExecutor(session, properties).execute();
        new AnalyzeExecutor(session, properties).execute();

    }

    private static void init() {
        initSpark();
        initProperties();
    }

    private static void initSpark() {
        session = SparkSession.builder().appName("tushare").master("local").getOrCreate();
    }

    private static void initProperties() {
        properties = new Properties();
        properties.put("driver", "com.mysql.jdbc.Driver");
        properties.put("url", "jdbc:mysql://localhost:3306/tushare");
        properties.put("user", "root");
        properties.put("password", "123456");
    }
}
