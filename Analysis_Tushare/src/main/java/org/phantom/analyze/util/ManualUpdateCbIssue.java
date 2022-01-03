package org.phantom.analyze.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.phantom.analyze.bean.CbIssueBean;
import org.phantom.analyze.bean.FswBean;
import org.phantom.analyze.common.Config;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManualUpdateCbIssue {

    private static SparkSession session;
    private static Properties properties;

    public ManualUpdateCbIssue() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        new ManualUpdateCbIssue().execute();
    }

    public void execute() throws Exception {

        List<CbIssueBean> list = new ArrayList<CbIssueBean>();
        list.add(setCbIssueBean("113608.SH","20201109"));
        list.add(setCbIssueBean("113597.SH","20200730"));
        list.add(setCbIssueBean("113037.SH","20200723"));
        list.add(setCbIssueBean("128114.SZ","20200617"));
        list.add(setCbIssueBean("113573.SH","20200417"));
        list.add(setCbIssueBean("113576.SH","20200410"));
        list.add(setCbIssueBean("113561.SH","20191231"));
        list.add(setCbIssueBean("128090.SZ","20191227"));
        list.add(setCbIssueBean("113030.SH","20191224"));
        list.add(setCbIssueBean("113558.SH","20191223"));
        list.add(setCbIssueBean("123037.SZ","20191219"));
        list.add(setCbIssueBean("128086.SZ","20191217"));
        list.add(setCbIssueBean("113552.SH","20191202"));
        list.add(setCbIssueBean("128080.SZ","20191118"));
        list.add(setCbIssueBean("123034.SZ","20191104"));
        list.add(setCbIssueBean("113548.SH","20191028"));
        list.add(setCbIssueBean("128077.SZ","20191016"));
        list.add(setCbIssueBean("123031.SZ","20190829"));
        list.add(setCbIssueBean("113540.SH","20190715"));
        list.add(setCbIssueBean("128068.SZ","20190604"));
        list.add(setCbIssueBean("128063.SZ","20190403"));
        list.add(setCbIssueBean("113532.SH","20190402"));
        list.add(setCbIssueBean("128061.SZ","20190327"));
        list.add(setCbIssueBean("110054.SH","20190318"));
        list.add(setCbIssueBean("113022.SH","20190312"));
        list.add(setCbIssueBean("123021.SZ","20190304"));
        list.add(setCbIssueBean("123020.SZ","20190301"));
        list.add(setCbIssueBean("123019.SZ","20190225"));
        list.add(setCbIssueBean("113526.SH","20190123"));
        list.add(setCbIssueBean("113523.SH","20181210"));
        list.add(setCbIssueBean("110048.SH","20181207"));
        list.add(setCbIssueBean("113518.SH","20180912"));
        list.add(setCbIssueBean("128042.SZ","20180730"));
        list.add(setCbIssueBean("123011.SZ","20180718"));
        list.add(setCbIssueBean("113511.SH","20180620"));
        list.add(setCbIssueBean("113510.SH","20180619"));
        list.add(setCbIssueBean("128038.SZ","20180322"));
        list.add(setCbIssueBean("128037.SZ","20180315"));
        list.add(setCbIssueBean("127005.SZ","20180312"));
        list.add(setCbIssueBean("113019.SH","20180301"));
        list.add(setCbIssueBean("123008.SZ","20180201"));
        list.add(setCbIssueBean("128033.SZ","20171227"));
        list.add(setCbIssueBean("128029.SZ","20171222"));
        list.add(setCbIssueBean("123005.SZ","20171219"));
        list.add(setCbIssueBean("128027.SZ","20171215"));
        list.add(setCbIssueBean("128022.SZ","20171201"));
        list.add(setCbIssueBean("128011.SZ","20160302"));
        list.add(setCbIssueBean("110035.SH","20160226"));
        list.add(setCbIssueBean("110031.SH","20150612"));
        list.add(setCbIssueBean("128009.SZ","20141212"));
        list.add(setCbIssueBean("128007.SZ","20140815"));
        list.add(setCbIssueBean("113005.SH","20131122"));
        list.add(setCbIssueBean("128002.SZ","20130726"));
        list.add(setCbIssueBean("110023.SH","20130315"));
        list.add(setCbIssueBean("128001.SZ","20130109"));
        list.add(setCbIssueBean("113003.SH","20120604"));
        list.add(setCbIssueBean("110015.SH","20110223"));
        list.add(setCbIssueBean("110012.SH","20110107"));
        list.add(setCbIssueBean("110011.SH","20101125"));
        list.add(setCbIssueBean("126729.SZ","20101015"));
        list.add(setCbIssueBean("128233.SZ","20100826"));
        list.add(setCbIssueBean("110006.SH","20090914"));
        list.add(setCbIssueBean("126019.SZ","20090731"));
        list.add(setCbIssueBean("126018.SZ","20080922"));
        list.add(setCbIssueBean("126017.SZ","20080626"));
        list.add(setCbIssueBean("126016.SZ","20080620"));
        list.add(setCbIssueBean("126015.SZ","20080508"));
        list.add(setCbIssueBean("126014.SZ","20080507"));
        list.add(setCbIssueBean("126013.SZ","20080402"));
        list.add(setCbIssueBean("126011.SZ","20080220"));
        list.add(setCbIssueBean("126012.SZ","20080220"));
        list.add(setCbIssueBean("115003.SH","20080130"));
        list.add(setCbIssueBean("126010.SZ","20080128"));
        list.add(setCbIssueBean("126009.SZ","20080128"));
        list.add(setCbIssueBean("110598.SH","20071219"));
        list.add(setCbIssueBean("126008.SZ","20071219"));
        list.add(setCbIssueBean("125709.SZ","20071214"));
        list.add(setCbIssueBean("126007.SZ","20071127"));
        list.add(setCbIssueBean("126006.SZ","20071009"));

        session.createDataFrame(list, CbIssueBean.class)
                .write()
                .mode(SaveMode.Append)
                .jdbc(properties.getProperty("url"), "manual_cb_issue_tmp", properties);
    }

    public CbIssueBean setCbIssueBean(String ts_code, String onl_date) {
        CbIssueBean bean = new CbIssueBean();
        bean.setTs_code(ts_code);
        bean.setOnl_date(onl_date);
        return bean;
    }
}
