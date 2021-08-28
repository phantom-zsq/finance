package org.phantom.finance.stock;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.phantom.finance.bean.StockInformationBasic;
import org.phantom.finance.constant.Constants;
import org.phantom.finance.constant.Urls;
import org.phantom.finance.util.HttpRequest;

import java.io.File;
import java.util.*;

import static org.apache.spark.sql.functions.monotonically_increasing_id;

public class StockHistoryDataCrawler {

    private static SparkSession session;
    private static Properties properties;

    public StockHistoryDataCrawler(SparkSession session, Properties properties) {
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {
        // 读取股票代码
        List<StockInformationBasic> stockList = getStockList();
        // 下载股票历史交易数据
        downLoadHistoryData(stockList);
        // 插入股票历史交易数据
        parseCsvToDatabase(stockList);
    }

    public List<StockInformationBasic> getStockList() throws Exception {
        List<Row> list = session.read().jdbc(properties.getProperty("url"), "(select code,start_time,end_time from stock_information_basic) tt", properties).collectAsList();
        List<StockInformationBasic> result = new ArrayList<StockInformationBasic>();
        for (Row row : list) {
            StockInformationBasic basic = new StockInformationBasic();
            basic.setCode(row.getString(0));
            basic.setStart_time(row.getString(1));
            basic.setEnd_time(row.getString(2));
            result.add(basic);
        }
        return result;
    }

    public void downLoadHistoryData(List<StockInformationBasic> stockList) throws Exception {
        List<StockInformationBasic> processSockList = stockList;
        List<StockInformationBasic> tmpStockList = new ArrayList<StockInformationBasic>();
        int i = 3;
        while(i-- > 0 && processSockList.size() > 0){
            for (StockInformationBasic stock : processSockList) {
                Thread.sleep(100);
                String code = stock.getCode();
                String startTime = stock.getStart_time();
                String endTime = stock.getEnd_time();
                if (endTime == null || "".equals(endTime)) {
                    endTime = "20200830";
                }
                File file = new File(Constants.STOCK_FILE_PATH + File.separator + code + ".csv");
                if(file.exists()){
                    long num = session.read()
                            .option("header", "false")
                            .option("encoding", "utf-8")
                            .csv(Constants.STOCK_FILE_PATH + File.separator + code + ".csv")
                            .count();
                    if(num > 1){
                        continue;
                    }else{
                        file.delete();
                    }
                }
                try {
                    String url0 = Urls.NETEASE_FINANCE_HISTORY_DATA
                            .replaceAll("\\$\\{pre_code\\}", "0")
                            .replaceAll("\\$\\{code\\}", code)
                            .replaceAll("\\$\\{start_date\\}", startTime)
                            .replaceAll("\\$\\{end_date\\}", endTime);
                    HttpRequest.downLoadFromUrl(url0, code + ".csv", Constants.STOCK_FILE_PATH);
                    long num0 = session.read()
                            .option("header", "false")
                            .option("encoding", "utf-8")
                            .csv(Constants.STOCK_FILE_PATH + File.separator + code + ".csv")
                            .count();
                    if(num0 <= 1){
                        File fileError = new File(Constants.STOCK_FILE_PATH + File.separator + code + ".csv");
                        fileError.delete();
                    }else{
                        continue;
                    }

                    String url1 = Urls.NETEASE_FINANCE_HISTORY_DATA
                            .replaceAll("\\$\\{pre_code\\}", "1")
                            .replaceAll("\\$\\{code\\}", code)
                            .replaceAll("\\$\\{start_date\\}", startTime)
                            .replaceAll("\\$\\{end_date\\}", endTime);
                    HttpRequest.downLoadFromUrl(url1, code + ".csv", Constants.STOCK_FILE_PATH);
                    long num1 = session.read()
                            .option("header", "false")
                            .option("encoding", "utf-8")
                            .csv(Constants.STOCK_FILE_PATH + File.separator + code + ".csv")
                            .count();
                    if(num1 <= 1){
                        tmpStockList.add(stock);
                        File fileError = new File(Constants.STOCK_FILE_PATH + File.separator + code + ".csv");
                        fileError.delete();
                    }

                } catch (Exception e) {
                    tmpStockList.add(stock);
                }
            }
            processSockList = tmpStockList;
            tmpStockList = new ArrayList<StockInformationBasic>();
        }
        for (StockInformationBasic stock : processSockList) {
            System.out.println("download: " + stock.getCode());
        }
    }

    public void parseCsvToDatabase(List<StockInformationBasic> stockList) throws Exception {
        List<String> errorList = new ArrayList<String>();
        for (StockInformationBasic stock : stockList) {
            String code = stock.getCode();
            try {
                session.read()
                        .option("header", "false")
                        .option("encoding", "utf-8")
                        .csv(Constants.STOCK_FILE_PATH + File.separator + code + ".csv")
                        .withColumnRenamed("_c0", "date")
                        .withColumnRenamed("_c1", "code")
                        .withColumnRenamed("_c2", "name")
                        .withColumnRenamed("_c3", "close")
                        .withColumnRenamed("_c4", "high")
                        .withColumnRenamed("_c5", "low")
                        .withColumnRenamed("_c6", "open")
                        .withColumnRenamed("_c7", "lopen")
                        .withColumnRenamed("_c8", "chg")
                        .withColumnRenamed("_c9", "pchg")
                        .withColumnRenamed("_c10", "turnover")
                        .withColumnRenamed("_c11", "voturnover")
                        .withColumnRenamed("_c12", "vaturnover")
                        .withColumnRenamed("_c13", "tcap")
                        .withColumnRenamed("_c14", "mcap")
                        .withColumn("row_seq", monotonically_increasing_id())
                        .filter("row_seq != 0")
                        .drop("row_seq")
                        .drop("name")
                        .write()
                        .mode(SaveMode.Append)
                        .jdbc(properties.getProperty("url"), "stock_history_data", properties);
            } catch (Exception e) {
                e.printStackTrace();
                errorList.add(code);
            }
        }
        for (String code : errorList) {
            System.out.println("parse: " + code);
        }
    }
}
