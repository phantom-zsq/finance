package org.phantom.analyze.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.phantom.analyze.bean.FswBean;
import org.phantom.analyze.bean.StkCodeCompanyBean;
import org.phantom.analyze.common.Config;

import java.util.*;

/**
 * 爬取公司名
 */
public class ManualQueryCompanyName {

    private static SparkSession session;
    private static Properties properties;

    public ManualQueryCompanyName() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        new ManualQueryCompanyName().execute();
    }

    public void execute() throws Exception {
        getData();
    }

    private void getData() {
        List<Row> list = session.read().jdbc(properties.getProperty("url"), "(select stk_code from manual_cb_basic where stk_code is not null order by list_date desc) tt", properties).collectAsList();
        List<StkCodeCompanyBean> result = new ArrayList<StkCodeCompanyBean>();
        for (Row row : list) {
            String stkCode = row.getString(0);
            try {
                Thread.sleep(100);
                String url = "https://data.eastmoney.com/dataapi/stockdata/f10api?type=PCF10/RptCompanyProfile&postData={\"SecurityCode\":\""+stkCode+"\"}&fields=CorpName,Representative,RegistryAddress,RegistryCapital,BusinessRange,CorpSummary";
                String json = Jsoup.connect(url).ignoreContentType(true).get().text();
                JSONArray array = JSON.parseObject(json).getJSONArray("data");
                for (int j = 0; j < array.size(); j++) {
                    StkCodeCompanyBean bean = new StkCodeCompanyBean();
                    JSONObject obj = JSON.parseObject(array.get(j) + "");
                    String company = obj.get("CorpName").toString();
                    bean.setStk_code(stkCode);
                    bean.setCompany(company);
                    result.add(bean);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("error: " + stkCode);
            }
        }
        session.createDataFrame(result, StkCodeCompanyBean.class)
                .write()
                .mode(SaveMode.Append)
                .jdbc(properties.getProperty("url"), "manual_stk_code_company", properties);
    }
}
