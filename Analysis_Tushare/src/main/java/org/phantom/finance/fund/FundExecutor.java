package org.phantom.finance.fund;

import org.apache.spark.sql.SparkSession;

import java.util.Properties;

public class FundExecutor {

    private static SparkSession session;
    private static Properties properties;

    public FundExecutor(SparkSession session, Properties properties){
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {

    }
}
