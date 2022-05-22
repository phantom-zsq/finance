package org.phantom.analyze.statistics;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.phantom.analyze.bean.BoneBean;
import org.phantom.analyze.bean.FirstNDaysOfBondBean;
import org.phantom.analyze.common.Config;
import org.phantom.analyze.load.LoadOracleData;
import java.util.*;

public class FirstNDaysOfBond {

    private static SparkSession session;
    private static Properties properties;

    public FirstNDaysOfBond() {
        this.session = Config.session;
        this.properties = Config.properties;
    }

    public static void main(String[] args) throws Exception {
        FirstNDaysOfBond bond = new FirstNDaysOfBond();
        bond.statistics();
    }

    public void statistics() throws Exception {
        new LoadOracleData().load();
        createRankTable();
        int[] array = {5, 9};
        for (int m = 0; m < array.length; m++) {
            int start = array[m];
            for (int n = m+1; n < array.length; n++) {
                int end = array[n];
                Dataset<Row> basicDetails = getBasicDetails(start, end);
                List<FirstNDaysOfBondBean> extendDetails = getExtendDetails(basicDetails);
                printBasicInformation(basicDetails, start, end);
                printSummary(extendDetails);
                printDetails(extendDetails);
            }
        }
    }

    public void createRankTable() throws Exception {
        Dataset<Row> rows = session.sql("select *,row_number() over(partition by ts_code order by trade_date) as rank from cb_daily where open>0");
        rows.createOrReplaceTempView("cb_daily_rank");
        rows.cache();
        rows.count();
    }

    public Dataset<Row> getBasicDetails(int start, int end) throws Exception {
        Dataset<Row> details = session.sql("" +
                "select " +
                "   ts_code, " +
                "   stk_code, " +
                "   trade_date, " +
                "   high_yield, " +
                "   yield, " +
                "   issue_size, " +
                "   premium, " +
                "   newest_rating, " +
                "   before_start_amount_rate, " +
                "   first_start_amount_rate, " +
                "   first_open, " +
                "   first_yield, " +
                "   first_amount_rate " +
                "from ( " +
                "   select " +
                "       cb_daily_rank_start.ts_code, " +
                "       cb_basic.stk_code, " +
                "       cb_daily_rank_start.trade_date, " +
                "       (cb_daily_rank_start_end.high-cb_daily_rank_start.open) * 100 / cb_daily_rank_start.open as high_yield, " +
                "       (cb_daily_rank_end.close-cb_daily_rank_start.open) * 100 / cb_daily_rank_start.open as yield, " +
                "       cb_issue.issue_size, " +
                "       cb_daily_rank_start.pre_close * cb_basic.first_conv_price * manual_cb_premium.price_hfq / 100 / manual_cb_premium.price as premium, " +
                "       manual_cb_newest_rating.newest_rating, " +
                "       cb_daily_rank_before_start.amount * 10000 * 100 / cb_issue.issue_size / 100000000 as before_start_amount_rate, " +
                "       cb_daily_rank_first_start.amount * 10000 * 100 / cb_issue.issue_size / 100000000 / " + (start-1) + " as first_start_amount_rate, " +
                "       cb_daily_rank_first.open as first_open, " +
                "       cb_daily_rank_first.pct_chg as first_yield, " +
                "       cb_daily_rank_first.amount * 10000 * 100 / cb_issue.issue_size / 100000000 as first_amount_rate " +
                "   from (select * from cb_daily_rank where rank=" + start + ") cb_daily_rank_start " +
                "   inner join (select * from cb_daily_rank where rank=" + end + ") cb_daily_rank_end on cb_daily_rank_start.ts_code=cb_daily_rank_end.ts_code " +
                "   inner join cb_basic on cb_daily_rank_start.ts_code=cb_basic.ts_code " +
                "   inner join cb_issue on cb_daily_rank_start.ts_code=cb_issue.ts_code " +
                "   inner join manual_cb_premium on cb_daily_rank_start.ts_code=manual_cb_premium.ts_code " +
                "   inner join manual_cb_newest_rating on cb_daily_rank_start.ts_code=manual_cb_newest_rating.ts_code " +
                "   inner join (select * from cb_daily_rank where rank=" + (start-1) + ") cb_daily_rank_before_start on cb_daily_rank_start.ts_code=cb_daily_rank_before_start.ts_code " +
                "   inner join (select ts_code,sum(amount) as amount from cb_daily_rank where rank<" + start + " group by ts_code) cb_daily_rank_first_start on cb_daily_rank_start.ts_code=cb_daily_rank_first_start.ts_code " +
                "   inner join (select * from cb_daily_rank where rank=1) cb_daily_rank_first on cb_daily_rank_start.ts_code=cb_daily_rank_first.ts_code " +
                "   inner join (select ts_code,max(high) as high from cb_daily_rank where rank>=" + start + " and rank<=" + end + " group by ts_code) cb_daily_rank_start_end on cb_daily_rank_start.ts_code=cb_daily_rank_start_end.ts_code " +
                ") a " +
                "order by yield"
        );
        return details;
    }

    public List<FirstNDaysOfBondBean> getExtendDetails(Dataset<Row> details) throws Exception {
        List<FirstNDaysOfBondBean> list = new ArrayList<FirstNDaysOfBondBean>();
        for (Row row : details.collectAsList()) {
            int i = 0;
            // get column
            String ts_code = row.getString(i++);
            String stk_code = row.getString(i++);
            String trade_date = row.getString(i++);
            Double high_yield = row.getDouble(i++);
            Double yield = row.getDouble(i++);
            Double issue_size = row.getDouble(i++);
            Double premium = row.getDouble(i++);
            Double pre_close = session.sql("select pre_close from pro_bar where ts_code='" + stk_code + "' and trade_date='" + trade_date + "'").first().getDouble(0);
            Double premium_rate = (premium / pre_close - 1) * 100;
            String newest_rating = row.getString(i++);
            Double before_start_amount_rate = row.getDouble(i++);
            Double first_start_amount_rate = row.getDouble(i++);
            Double first_open = row.getDouble(i++);
            Double first_yield = row.getDouble(i++);
            Double first_amount_rate = row.getDouble(i++);
            // remove some case
            if(issue_size>=300 || premium_rate>=40 || newest_rating.startsWith("B") || "A".equals(newest_rating) || before_start_amount_rate>=300 || first_start_amount_rate>=300 || first_open>=140 || first_amount_rate>=300){
                continue;
            }else{

            }
            // set column
            FirstNDaysOfBondBean bean = new FirstNDaysOfBondBean();
            bean.setTs_code(ts_code);
            bean.setStk_code(stk_code);
            bean.setTrade_date(trade_date);
            bean.setHigh_yield(high_yield);
            bean.setYield(yield);
            bean.setIssue_size(issue_size);
            bean.setPremium_rate(premium_rate);
            bean.setNewest_rating(newest_rating);
            bean.setBefore_start_amount_rate(before_start_amount_rate);
            bean.setFirst_start_amount_rate(first_start_amount_rate);
            bean.setFirst_open(first_open);
            bean.setFirst_yield(first_yield);
            bean.setFirst_amount_rate(first_amount_rate);
            list.add(bean);
        }
        return list;
    }

    public void printBasicInformation(Dataset<Row> basicDetails, int start, int end) throws Exception {
        System.out.println("********************" + start + ": " + end + "********************");
        System.out.println("total: " + basicDetails.count());
    }

    public void printSummary(List<FirstNDaysOfBondBean> list) throws Exception {
        session.createDataFrame(list, FirstNDaysOfBondBean.class).createOrReplaceTempView("details");
        session.sql("select count(*),sum(case when high_yield >= 0 then 1 else 0 end) as high_profit_count,sum(high_yield) as high_yield,sum(case when yield >= 0 then 1 else 0 end) as profit_count,sum(yield) as yield,sum(case when yield >= 0 then yield else 0 end) as success_sum,sum(case when yield >= 0 then 0 else yield end) as fail_sum from details").show(false);
    }

    public void printDetails(List<FirstNDaysOfBondBean> list) throws Exception {
        for (FirstNDaysOfBondBean bean : list) {
            String ts_code = bean.getTs_code();
            String stk_code = bean.getStk_code();
            String trade_date = bean.getTrade_date();
            Double high_yield = bean.getHigh_yield();
            Double yield = bean.getYield();
            Double issue_size = bean.getIssue_size();
            Double premium_rate = bean.getPremium_rate();
            String newest_rating = bean.getNewest_rating();
            Double before_start_amount_rate = bean.getBefore_start_amount_rate();
            Double first_start_amount_rate = bean.getFirst_start_amount_rate();
            Double first_open = bean.getFirst_open();
            Double first_yield = bean.getFirst_yield();
            Double first_amount_rate = bean.getFirst_amount_rate();
            System.out.println(high_yield + "\t" + yield + "\t" + issue_size + "\t" + premium_rate + "\t" + newest_rating + "\t" + before_start_amount_rate + "\t" + first_start_amount_rate + "\t" + first_open + "\t" + first_yield + "\t" + first_amount_rate + "\t" + ts_code + "\t" + stk_code + "\t" + trade_date);
        }
    }
}
