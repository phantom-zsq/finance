package org.phantom.finance.stock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.phantom.finance.bean.StockBasicSector;
import org.phantom.finance.bean.StockInformationSector;
import org.phantom.finance.constant.Urls;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StockInformationSectorCrawler {

    private static SparkSession session;
    private static Properties properties;

    public StockInformationSectorCrawler(SparkSession session, Properties properties){
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {
        List<StockInformationSector> sectorList = getSectors();
        writeToDb(sectorList);
        getSectorStocks(sectorList);
    }

    private List<StockInformationSector> getSectors() throws Exception {
        List<StockInformationSector> sectorList = new ArrayList<StockInformationSector>();
        Document document = Jsoup.connect(Urls.NETEASE_FINANCE_MARKET_CENTER).get();
        getHySectors(document, sectorList);
        getGnSectors(document, sectorList);
        getDySectors(document, sectorList);
        return sectorList;
    }

    private void getHySectors(Document document, List<StockInformationSector> sectorList) {
        Elements elements = document.select("[id=f0-f6] ul li");
        for (Element e : elements) {
            StockInformationSector sector = new StockInformationSector();
            String code = e.attr("qid");
            String name = e.select("a").first().attr("title");
            sector.setCode(code);
            sector.setName(name);
            sector.setType("1");
            if(!"".equals(code)){
                sectorList.add(sector);
            }
        }
    }

    private void getGnSectors(Document document, List<StockInformationSector> sectorList) {
        Elements elements = document.select("[id=f0-f4] ul li");
        for (Element e : elements) {
            StockInformationSector sector = new StockInformationSector();
            String code = e.attr("qid");
            String name = e.select("a").first().attr("title");
            sector.setCode(code);
            sector.setName(name);
            sector.setType("2");
            if(!"".equals(code)){
                sectorList.add(sector);
            }
        }
    }

    private void getDySectors(Document document, List<StockInformationSector> sectorList) {
        Elements elements = document.select("[id=f0-f5] ul li");
        for (Element e : elements) {
            StockInformationSector sector = new StockInformationSector();
            String code = e.attr("qid");
            String name = e.select("a").first().attr("title");
            sector.setCode(code);
            sector.setName(name);
            sector.setType("3");
            if(!"".equals(code)){
                sectorList.add(sector);
            }
        }
    }

    private void writeToDb(List<StockInformationSector> sectorList) {
        // write to stock_information_sector
        session.createDataFrame(sectorList, StockInformationSector.class)
                .write()
                .mode(SaveMode.Append)
                .jdbc(properties.getProperty("url"), "stock_information_sector", properties);
    }

    private void getSectorStocks(List<StockInformationSector> sectorList) throws Exception {
        for (StockInformationSector sector : sectorList) {
            Thread.sleep(1000);
            try {
                List<StockBasicSector> basicSectorList = new ArrayList<StockBasicSector>();
                String url = Urls.NETEASE_FINANCE_STOCK_MARKET
                        .replaceAll("\\$\\{query\\}", "PLATE_IDS:\\$\\{code\\}" )
                        .replaceAll("\\$\\{code\\}", sector.getCode())
                        .replaceAll("\\$\\{page\\}", 0+"")
                        .replaceAll("\\$\\{count\\}", "10000");
                String json = Jsoup.connect(url).get().text();
                JSONArray array = JSON.parseObject(json).getJSONArray("list");
                for (int j = 0; j < array.size(); j++) {
                    JSONObject obj = JSON.parseObject(array.get(j) + "");
                    String code = obj.get("CODE").toString().replaceAll("^\\d", "");
                    StockBasicSector stockBasicSector = new StockBasicSector();
                    stockBasicSector.setSector_code(sector.getCode());
                    stockBasicSector.setStock_code(code);
                    basicSectorList.add(stockBasicSector);
                }
                // write to stock_basic_sector
                session.createDataFrame(basicSectorList, StockBasicSector.class)
                        .write()
                        .mode(SaveMode.Append)
                        .jdbc(properties.getProperty("url"), "stock_basic_sector", properties);
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("error code: " + sector.getCode());
            }
        }
    }
}
