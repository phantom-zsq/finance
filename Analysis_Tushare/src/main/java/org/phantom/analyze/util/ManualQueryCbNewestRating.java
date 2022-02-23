package org.phantom.analyze.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.phantom.analyze.bean.CbNewestRatingBean;
import org.phantom.analyze.bean.StkCodeCompanyBean;
import org.phantom.analyze.common.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 爬取债券信用等级
 */
public class ManualQueryCbNewestRating {

    private static SparkSession session;
    private static Properties properties;

    public ManualQueryCbNewestRating() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        new ManualQueryCbNewestRating().execute();
    }

    public void execute() throws Exception {
        getData();
    }

    private void getData() {
        List<Row> list = session.read().jdbc(properties.getProperty("url"), "(select ts_code from manual_cb_basic where ts_code not in(select ts_code from manual_cb_newest_rating)) tt", properties).collectAsList();
        List<CbNewestRatingBean> result = new ArrayList<CbNewestRatingBean>();
        for (Row row : list) {
            String tsCode = row.getString(0);
            String code = tsCode.replaceAll("(\\d+)\\.[a-zA-Z]+","$1");
            try {
                Thread.sleep(100);
                String url = "https://datacenter-web.eastmoney.com/api/data/v1/get?callback=jQuery11230388490261033378_1645592977803&reportName=RPT_BOND_CB_LIST&columns=ALL&quoteColumns=&source=WEB&client=WEB&filter=(SECURITY_CODE%3D%22"+code+"%22)&_=1645592977804";
                String json = Jsoup.connect(url).ignoreContentType(true).get().text();
                json = json.replaceAll("^[^\\(]+\\((.*)\\);$","$1");
                JSONArray array = JSON.parseObject(json).getJSONObject("result").getJSONArray("data");
                for (int j = 0; j < array.size(); j++) {
                    CbNewestRatingBean bean = new CbNewestRatingBean();
                    JSONObject obj = JSON.parseObject(array.get(j) + "");
                    String newestRating = obj.get("RATING").toString();
                    String ratingComp="";
                    if(obj.containsKey("PARTY_NAME")){
                        ratingComp = obj.get("PARTY_NAME").toString();
                    }
                    bean.setTs_code(tsCode);
                    bean.setNewest_rating(newestRating);
                    bean.setRating_comp(ratingComp);
                    result.add(bean);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("error: " + tsCode);
            }
        }
        session.createDataFrame(result, CbNewestRatingBean.class)
                .write()
                .mode(SaveMode.Append)
                .jdbc(properties.getProperty("url"), "manual_cb_newest_rating", properties);
    }
}
