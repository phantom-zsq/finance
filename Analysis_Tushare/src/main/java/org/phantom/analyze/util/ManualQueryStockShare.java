package org.phantom.analyze.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.phantom.analyze.bean.CbNewestRatingBean;
import org.phantom.analyze.bean.CbPremiumBean;
import org.phantom.analyze.common.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 爬取股票股本
 */
public class ManualQueryStockShare {

    private static SparkSession session;
    private static Properties properties;

    public ManualQueryStockShare() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        new ManualQueryStockShare().execute();
    }

    public void execute() throws Exception {
        getData();
    }

    private void getData() {
        List<Row> list = session.read().jdbc(properties.getProperty("url"), "(select ts_code,stk_code,trade_date,price,price_hfq from manual_cb_premium) tt", properties).collectAsList();
        List<CbPremiumBean> result = new ArrayList<CbPremiumBean>();
        for (Row row : list) {
            String tsCode = row.getString(0);
            String stkCode = row.getString(1);
            String tradeDate = row.getString(2);
            Double price = row.getDouble(3);
            Double priceHfq = row.getDouble(4);
            String code = stkCode.replaceAll("(\\d+)\\.([a-zA-Z]+)","$2$1");
            try {
                Thread.sleep(100);
                String url = "http://emweb.securities.eastmoney.com/PC_HSF10/OperationsRequired/PageAjax?code="+code;
                String json = Jsoup.connect(url).ignoreContentType(true).get().text();
                JSONObject obj = JSON.parseObject(JSON.parseObject(json).getJSONArray("zxzb").get(0)+"");
                Double totalShare = Double.valueOf(obj.get("TOTAL_SHARE").toString()) * 10000;
                Double freeShare = Double.valueOf(obj.get("FREE_SHARE").toString()) * 10000;
                CbPremiumBean bean = new CbPremiumBean();
                bean.setTs_code(tsCode);
                bean.setStk_code(stkCode);
                bean.setTrade_date(tradeDate);
                bean.setPrice(price);
                bean.setPrice_hfq(priceHfq);
                bean.setTotal_share(totalShare);
                bean.setFree_share(freeShare);
                result.add(bean);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("error: " + tsCode);
            }
        }
        session.createDataFrame(result, CbPremiumBean.class)
                .write()
                .mode(SaveMode.Append)
                .jdbc(properties.getProperty("url"), "manual_cb_premium_tmp", properties);
    }
}
