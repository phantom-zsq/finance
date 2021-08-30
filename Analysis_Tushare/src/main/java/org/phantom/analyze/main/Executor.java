package org.phantom.analyze.main;

import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.common.Config;

import java.util.Properties;

public class Executor {

    private static SparkSession session;
    private static Properties properties;

    public Executor(){
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {

    }
}
