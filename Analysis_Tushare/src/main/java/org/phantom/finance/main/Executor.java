package org.phantom.finance.main;

import org.apache.spark.sql.SparkSession;
import org.phantom.finance.stock.StockExecutor;
import java.util.*;

public class Executor {

    private static SparkSession session;
    private static Properties properties;

    public static void main(String[] args) throws Exception {
        init();
        new StockExecutor(session, properties).execute();
    }

    private static void init() {
        initSpark();
        initProperties();
    }

    private static void initSpark() {
        session = SparkSession.builder().appName("finance").master("local").getOrCreate();
    }

    private static void initProperties() {
        properties = new Properties();
        properties.put("driver", "com.mysql.jdbc.Driver");
        properties.put("url", "jdbc:mysql://localhost:3306/finance");
        properties.put("user", "root");
        properties.put("password", "123456");
    }
}
