package org.phantom.analyze.main;

import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.common.Config;
import org.phantom.analyze.load.LoadOracleData;

import java.util.*;

public class BondExecutor {

    private static SparkSession session;
    private static Properties properties;

    public BondExecutor() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        new LoadOracleData().load();
    }

}
