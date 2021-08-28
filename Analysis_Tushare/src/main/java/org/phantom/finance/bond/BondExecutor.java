package org.phantom.finance.bond;

import org.apache.spark.sql.SparkSession;

import java.util.Properties;

public class BondExecutor {

    private static SparkSession session;
    private static Properties properties;

    public BondExecutor(SparkSession session, Properties properties){
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {

    }
}
