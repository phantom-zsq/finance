package org.phantom.analyze.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.phantom.analyze.bean.FswBean;
import org.phantom.analyze.common.Config;
import org.phantom.analyze.load.LoadOracleData;
import org.phantom.finance.bean.StockInformationBasic;
import org.phantom.finance.constant.Urls;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 下载证监会-发审委公告
 */
public class DownloadFswOfZjhUtils {

    private static SparkSession session;
    private static Properties properties;

    public DownloadFswOfZjhUtils() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        new DownloadFswOfZjhUtils().execute();
    }

    public void execute() throws Exception {
        // 从证监会下载数据
        //getData();
        // 解析公告内容
        parseContent();
    }

    private void getData() {
        Map<String, String> headersMap = new HashMap<String, String>();
        headersMap.put("Accept", "*/*");
        headersMap.put("Accept-Encoding", "gzip, deflate");
        headersMap.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        headersMap.put("Connection", "keep-alive");
        headersMap.put("Host", "www.csrc.gov.cn");
        headersMap.put("Referer", "http://www.csrc.gov.cn/csrc/c101954/zfxxgk_zdgk.shtml");
        headersMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:95.0) Gecko/20100101 Firefox/95.0");
        headersMap.put("X-Requested-With", "XMLHttpRequest");

        for (int i = 1; i <= 1; i++) {
            try {
                Thread.sleep(10000);
                System.out.println("start: " + i);
                List<FswBean> list = new ArrayList<FswBean>();
                String url = "http://www.csrc.gov.cn/searchList/f477b3febfb641b8841b1bd6ed3b4a77?_isAgg=true&_isJson=true&_pageSize=10&_template=index&_rangeTimeGte=&_channelName=&page=" + i;
                String json = Jsoup.connect(url).headers(headersMap).ignoreContentType(true).get().text();
                JSONArray array = JSON.parseObject(json).getJSONObject("data").getJSONArray("results");
                for (int j = 0; j < array.size(); j++) {
                    FswBean bean = new FswBean();
                    JSONObject obj = JSON.parseObject(array.get(j) + "");
                    String publishedTimeStr = obj.get("publishedTimeStr").toString();
                    String title = obj.get("title").toString();
                    String content = obj.get("content").toString();
                    bean.setPublishedTimeStr(publishedTimeStr);
                    bean.setDay(publishedTimeStr.substring(0, 10));
                    bean.setTitle(title);
                    bean.setContent(content);
                    bean.setYear(title.replaceAll(".*发审委(\\d+)年第(\\d+)次(工作会议|会议审核结果)公告.*", "$1"));
                    bean.setTimes(title.replaceAll(".*发审委(\\d+)年第(\\d+)次(工作会议|会议审核结果)公告.*", "$2"));
                    list.add(bean);
                }
                session.createDataFrame(list, FswBean.class)
                        .write()
                        .mode(SaveMode.Append)
                        .jdbc(properties.getProperty("url"), "manual_zjh_fsw", properties);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("error: " + i);
            }
        }
    }

    private void parseContent() throws Exception{
        Dataset<Row> manualZjhFsw = session.read().jdbc(properties.getProperty("url"), "(select publishedTimeStr,day,title,type,year,times,content from manual_zjh_fsw) tt", properties);
        List<Row> manualZjhFswList = manualZjhFsw.collectAsList();
        List<FswBean> list = new ArrayList<FswBean>();
        for(Row row : manualZjhFswList){
            int i=0;
            FswBean bean = new FswBean();
            list.add(bean);
            bean.setPublishedTimeStr(row.getString(i++));
            bean.setDay(row.getString(i++));
            bean.setTitle(row.getString(i++));
            bean.setType(row.getString(i++));
            bean.setYear(row.getString(i++));
            bean.setTimes(row.getString(i++));
            bean.setContent(row.getString(i++));
        }
        int i = 0;
        int j = 0;
        int k = 0;
        int q = 0;
        int w = 0;
        int d = 0;
        for(FswBean bean : list){
            String publishedTimeStr = bean.getPublishedTimeStr();
            String day = bean.getDay();
            String title = bean.getTitle();
            String type = bean.getType();
            String year = bean.getYear();
            String times = bean.getTimes();
            String content = bean.getContent();
            if(title.contains("工作会议")){
                i++;
                boolean cancel = content.contains("具体会议事项已经公告") || content.contains("因故取消"); // 决定取消/决定由/特此补充公告
                if(cancel){
                    k++;
                    continue;
                }
                String date = dealDate(content.replaceAll(".*审核委员会(定于)?(\\d+)年(\\d+)月(\\d+)日?召开.*","$2-$3-$4"));
                Pattern p = Pattern.compile("[^\\s.、：]+?（(可转债|首发|首发[,，]?会后事项|增发|配股|可分离债|配股会后事项|公开发行存托凭证|发行认股权证|认股权证)）");
                Matcher m = p.matcher(content);
                int c = 0;
                while(m.find()){
                    w++;
                    c++;
                    String companys = m.group(0);
                    if(companys.contains("可转债")){
                        System.out.println(day + ": " + date + ": " + companys);
                    }
                }
                if(c>0){
                    q++;
                }else{
                    d++;
                }
            }else{
                j++;
            }
        }
        System.out.println("工作会议: " + i);
        System.out.println("取消: " + k);
        System.out.println("未匹配到公司的工作会议: " + d);
        System.out.println("匹配到公司的工作会议: " + q);
        System.out.println("获取到公司总个数: " + w);
        System.out.println("审核结果: " + j);
    }

    private String dealDate(String date){
        String year = date.replaceAll("(\\d+)-(\\d+)-(\\d+)","$1");
        String month = date.replaceAll("(\\d+)-(\\d+)-(\\d+)","$2");
        String day = date.replaceAll("(\\d+)-(\\d+)-(\\d+)","$3");
        String result = year;
        if(month.length() == 1){
            result += "-0"+month;
        }else{
            result += "-"+month;
        }
        if(day.length() == 1){
            result += "-0"+day;
        }else{
            result += "-"+day;
        }
        return result;
    }
}
