package org.phantom.analyze.main;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StockExecutor {

    private static SparkSession session;
    private static Properties properties;

    public StockExecutor(SparkSession session, Properties properties){
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {
        List<String> stockList = getStockList();
        int i = 1;
        for(String str: stockList){
            try {
                download(str);
                System.out.println("done: " + i++);
            }catch (Exception e){
                System.out.println("error: " + str);
                e.printStackTrace();
            }
        }
    }

    public void download(String str) throws Exception {
        Dataset<Row> proBar = session.read().jdbc(properties.getProperty("url"), "(select ts_code,date_format(trade_date,'%Y-%m-%d') as trade_date,close from pro_bar where ts_code=\'"+str+"\') tt", properties);
        Dataset<Row> hkHold = session.read().jdbc(properties.getProperty("url"), "(select ts_code,date_format(trade_date,'%Y-%m-%d') as trade_date,ratio from hk_hold where ts_code=\'"+str+"\') tt", properties);
        proBar.createOrReplaceTempView("pro_bar");
        hkHold.createOrReplaceTempView("hk_hold");
        session.sql("select trade_date,close,row_number() over(partition by ts_code order by trade_date) rank from pro_bar where ts_code=\'"+str+"\'").createOrReplaceTempView("a");
        session.sql("select trade_date,ratio,row_number() over(partition by ts_code order by trade_date) rank from hk_hold where ts_code=\'"+str+"\'").createOrReplaceTempView("b");
        session.sql("select to_date(a.trade_date) as trade_date,a.close,to_date(b.trade_date) as td,b.ratio from a left join b on a.rank = b.rank order by a.trade_date")
                .write()
                .format("com.crealytics.spark.excel")
                .option("useHeader", "false") //是否输出表头
                .mode("append") // Optional, default: overwrite. "append"模式下可以在一个Excel文件中追加多个sheet
                .save("/Users/xx/Downloads/tushare/download/"+str+".xlsx"); // 要输出的HDFS文件路径.
    }

    public List<String> getStockList() throws Exception {
        List<Row> list = session.read().jdbc(properties.getProperty("url"), "(select ts_code from hs_const where ts_code<'603997' order by ts_code desc) tt", properties).collectAsList();
        List<String> result = new ArrayList<String>();
        for (Row row : list) {
            result.add(row.getString(0));
        }
        return result;
    }

}
