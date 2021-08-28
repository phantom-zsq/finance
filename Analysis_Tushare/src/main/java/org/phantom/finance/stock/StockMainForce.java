package org.phantom.finance.stock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.phantom.finance.bean.StockInformationBasic;
import org.phantom.finance.bean.StockInformationSector;
import org.phantom.finance.constant.Constants;
import org.phantom.finance.constant.Urls;
import org.phantom.finance.util.HttpRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.spark.sql.functions.monotonically_increasing_id;

public class StockMainForce {

    private static SparkSession session;
    private static Properties properties;

    public StockMainForce(SparkSession session, Properties properties) {
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {

        String url = "http://datainterface3.eastmoney.com/EM_DataCenter_V3/api/GGZLSJTJ/GetGGZLSJTJ?js=jQuery112305407018142597002_1607403953456&tkn=eastmoney&reportDate=2020-06-30&code=601318.sh&cfg=ggzlsjtj&_=1607403953457";
        String json = Jsoup.connect(url).get().text();
        JSONArray array = JSON.parseObject(json).getJSONArray("Data");
        for (int j = 0; j < array.size(); j++) {
            if(j == array.size()-1){
                JSONObject obj = JSON.parseObject(array.get(j) + "");
                System.out.println(obj.toString());
            }

            //String code = obj.get("CODE").toString();
        }

        //http://datainterface3.eastmoney.com/EM_DataCenter_V3/api/ZLSJBGQ/GetBGQ?tkn=eastmoney&sortDirec=1&pageNum=1&pageSize=25&cfg=zlsjbgq&js=jQuery112305407018142597002_1607403953453&_=1607403953455
        //http://datainterface3.eastmoney.com/EM_DataCenter_V3/api/ZLSJBGQ/GetBGQ?tkn=eastmoney&sortDirec=1&pageNum=1&pageSize=25&cfg=zlsjbgq&js=jQuery11230895254434942086_1607404302880&_=1607404302882

        //http://datainterface3.eastmoney.com/EM_DataCenter_V3/api/GGZLSJTJ/GetGGZLSJTJ?js=jQuery112305407018142597002_1607403953456&tkn=eastmoney&reportDate=2020-06-30&code=601318.sh&cfg=ggzlsjtj&_=1607403953457
        //http://datainterface3.eastmoney.com/EM_DataCenter_V3/api/GGZLSJTJ/GetGGZLSJTJ?js=jQuery11230895254434942086_1607404302883&tkn=eastmoney&reportDate=2020-03-31&code=600547.sh&cfg=ggzlsjtj&_=1607404302884


    }
}

/**
2020-09-30
2020-06-30
2020-03-31
2019-12-31
2019-09-30
2019-06-30
2019-03-31
2018-12-31
2018-09-30
2018-06-30
2018-03-31
2017-12-31
2017-09-30
2017-06-30
2017-03-31
2016-12-31
2016-09-30
2016-06-30
2016-03-31
2015-12-31
2015-09-30
2015-06-30
2015-03-31
2014-12-31
2014-09-30
 **/