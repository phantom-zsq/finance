package org.phantom.analyze.common;

import org.apache.spark.sql.SparkSession;
import java.util.Properties;

public class Config {

    public static SparkSession session;
    public static Properties properties;

    static {
        if(session == null){
            init();
        }
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
