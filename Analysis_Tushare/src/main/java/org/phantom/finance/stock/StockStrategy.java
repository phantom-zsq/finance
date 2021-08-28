package org.phantom.finance.stock;

import org.apache.spark.sql.SparkSession;
import org.phantom.finance.bean.StockHistoryDataBean;
import org.phantom.finance.util.StockUtil;
import java.util.*;

public class StockStrategy {

    private static SparkSession session;
    private static Properties properties;

    public StockStrategy(SparkSession session, Properties properties) {
        this.session = session;
        this.properties = properties;
    }

    public void execute() throws Exception {
        StockUtil stockUtil = new StockUtil(session, properties);
        // 历史数据
        List<StockHistoryDataBean> list = stockUtil.getStockList("zgpa_day", "\\'601318");
        // 训练数据
        List<StockHistoryDataBean> history = new ArrayList<StockHistoryDataBean>();
        // 当前价的高低点区间
        StockHistoryDataBean low = null;
        StockHistoryDataBean high = null;
        StockHistoryDataBean buy = null;
        // 资金
        double money = 10000;
        // 是否购买
        boolean isBuy = false;
        // 遍历
        for (int i=0; i<list.size(); i++){
            if(i>=100){
                // 判断当前趋势是上升下降还是震荡
                int qushi = stockUtil.getQushi(history, i);
                // 获取训练数据所有的最高点和最低点
                List<StockHistoryDataBean> result_10 = stockUtil.getHighLow(history, 10);
                // 高低点排序
                Collections.sort(result_10, new Comparator<StockHistoryDataBean>() {
                    public int compare(StockHistoryDataBean u1, StockHistoryDataBean u2) {
                        return Double.valueOf(u1.getClose()).compareTo(Double.valueOf(u2.getClose())); //升序
                    }
                });
                // 获取当前价的高低点区间
                StockHistoryDataBean tmpLow = null;
                StockHistoryDataBean tmpHigh = null;
                for(int j=0; j<result_10.size()-1; j++){
                    if(Double.valueOf(result_10.get(j).getClose()) <= Double.valueOf(list.get(i).getClose()) && Double.valueOf(result_10.get(j+1).getClose()) > Double.valueOf(list.get(i).getClose())){
                        tmpLow = result_10.get(j);
                        tmpHigh = result_10.get(j+1);
                        break;
                    }
                    if(j==0 && Double.valueOf(result_10.get(j).getClose()) > Double.valueOf(list.get(i).getClose())){
                        tmpLow = new StockHistoryDataBean();
                        tmpLow.setClose("0");
                        tmpHigh = result_10.get(j);
                        break;
                    }
                    if(j==result_10.size()-2 && Double.valueOf(result_10.get(j+1).getClose()) < Double.valueOf(list.get(i).getClose())){
                        tmpLow = result_10.get(j+1);
                        tmpHigh = new StockHistoryDataBean();
                        tmpHigh.setClose("200");
                        break;
                    }
                }
                // 打印相关信息
                System.out.println("--------------");
                for(StockHistoryDataBean bean : result_10){
                    System.out.println("low and high: " + bean.getClose());
                }
                System.out.println("低价: " + tmpLow.getClose());
                System.out.println("高价: " + tmpHigh.getClose());
                System.out.println("当前价: " + list.get(i).getClose());
                // 判断当前点是否是买点或卖点
                if(!isBuy && Double.valueOf(tmpLow.getClose())+1 >= Double.valueOf(list.get(i).getClose())){
                    buy = list.get(i);
                    isBuy = true;
                    low = tmpLow;
                    high = tmpHigh;
                    System.out.println("买: " + buy.getDate() + ": " + buy.getClose());
                }
                if(isBuy && Double.valueOf(high.getClose())-1 <= Double.valueOf(list.get(i).getClose())){
                    money = money * Double.valueOf(list.get(i).getClose()) / Double.valueOf(buy.getClose());
                    isBuy = false;
                    System.out.println("卖: " + list.get(i).getDate() + ": " + list.get(i).getClose());
                    System.out.println(money);
                }
                System.out.println("--------------");
            }
            history.add(list.get(i));
        }
        System.out.println(money);
    }

}
