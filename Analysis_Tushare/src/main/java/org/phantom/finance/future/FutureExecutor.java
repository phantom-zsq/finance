package org.phantom.finance.future;

import org.apache.spark.sql.SparkSession;

import java.util.Properties;

public class FutureExecutor {

    private static SparkSession session;
    private static Properties properties;

    public FutureExecutor(SparkSession session, Properties properties){
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {

    }
}
