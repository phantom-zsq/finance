package org.phantom.finance.stock;

import org.apache.spark.sql.SparkSession;
import org.phantom.finance.bean.StockInformationBasic;

import java.util.List;
import java.util.Properties;

public class Etl {

    private static SparkSession session;
    private static Properties properties;

    public Etl(SparkSession session, Properties properties) {
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {
        // stock_day
        // create table zgpa_day as select * from stock_history_data where code='\'601318' and close!=0;
        // stock_month
        // create table zgpa_month as select * from zgpa_day where substr(date,9)='01';
    }
}
