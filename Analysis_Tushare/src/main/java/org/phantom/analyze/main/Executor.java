package org.phantom.analyze.main;

import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.common.Config;
import org.phantom.analyze.load.LoadBasicInformation;
import org.phantom.analyze.load.LoadExtendInformation;
import org.phantom.analyze.load.LoadOracleData;
import org.phantom.analyze.strategy.Strategy;
import org.phantom.analyze.verify.Verify;

import java.util.List;
import java.util.Properties;

public class Executor {

    private static SparkSession session;
    private static Properties properties;

    public Executor(){
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        new LoadOracleData().load();
        List<StockBean> list = new LoadBasicInformation().load();
        new LoadExtendInformation().load(list);
        new Strategy().analyze(list);
        new Verify().verify(list);
    }
}
