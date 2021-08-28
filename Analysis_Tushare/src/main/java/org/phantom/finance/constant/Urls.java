package org.phantom.finance.constant;

public class Urls {

    /******************** 东方财富 ********************/

    /**
     * 行情中心
     */
    public static String EAST_MONEY_MARKET_CENTER = "http://quote.eastmoney.com/stocklist.html";


    /******************** 网易财经 ********************/

    /**
     * 行情中心
     */
    public static String NETEASE_FINANCE_MARKET_CENTER = "http://quotes.money.163.com/old";

    /**
     * 沪深A股
     */
    public static String NETEASE_FINANCE_A = "http://quotes.money.163.com/old/#query=EQA&DataType=HS_RANK&sort=PERCENT&order=desc&count=10000&page=0";

    /**
     * 具体股票信息
     */
    public static String NETEASE_FINANCE_STOCK = "http://quotes.money.163.com/trade/lsjysj_" + "${code}" + ".html";

    /**
     * 历史交易数据
     */
    public static String NETEASE_FINANCE_HISTORY_DATA = "http://quotes.money.163.com/service/chddata.html?code=" + "${pre_code}" + "${code}" + "&start=" + "${start_date}" + "&end=" + "${end_date}" + "&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";

    /**
     * 板块对应的股票
     */
    public static String NETEASE_FINANCE_SECTOR_STOCK = "http://quotes.money.163.com/hs/service/diyrank.php?page=0&query=PLATE_IDS:" + "${code}" + "&fields=NO,SYMBOL,NAME,PRICE,PERCENT,UPDOWN,FIVE_MINUTE,OPEN,YESTCLOSE,HIGH,LOW,VOLUME,TURNOVER,HS,LB,WB,ZF,PE,MCAP,TCAP,MFSUM,MFRATIO.MFRATIO2,MFRATIO.MFRATIO10,SNAME,CODE,ANNOUNMT,UVSNEWS&sort=PERCENT&order=desc&count=10000&type=query";

    /**
     * 沪深行情api
     */
    public static String NETEASE_FINANCE_STOCK_MARKET = "http://quotes.money.163.com/hs/service/diyrank.php?page=" + "${page}" + "&query=" + "${query}" + "&fields=NO,SYMBOL,NAME,PRICE,PERCENT,UPDOWN,FIVE_MINUTE,OPEN,YESTCLOSE,HIGH,LOW,VOLUME,TURNOVER,HS,LB,WB,ZF,PE,MCAP,TCAP,MFSUM,MFRATIO.MFRATIO2,MFRATIO.MFRATIO10,SNAME,CODE,ANNOUNMT,UVSNEWS&sort=PERCENT&order=desc&count=" + "${count}" + "&type=query";

    /******************** 同花顺财经 ********************/

    /**
     * 行情中心
     */
    public static String a = "http://q.10jqka.com.cn/";

}
