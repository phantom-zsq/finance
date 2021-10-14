package org.phantom.analyze.verify;

import org.phantom.analyze.bean.StockBean;
import java.util.List;
import java.util.Map;

public class Verify {

    public void verify(Map<String, List<StockBean>> map) throws Exception{
        long initMoney = 10000;
        double moneyAll = 0;
        int buyNumber = 0;
        boolean firstBuy = true;
        for(String tsCode : map.keySet()){
            List<StockBean> list = map.get(tsCode);
            for (int i=0; i<list.size()-1; i++){
                int status = list.get(i).getStatus();
                if(status == 1){
                    buyNumber = i;
                }else if(status == -1){
                    if(firstBuy){
                        System.out.println("买入金额: " + initMoney);
                        System.out.println("买入时间: " + list.get(buyNumber).getTrade_date());
                        System.out.println("买入价格: " + list.get(buyNumber).getClose());
                        System.out.println("卖出价格: " + list.get(i).getClose());
                        moneyAll = initMoney * list.get(i).getClose() / list.get(buyNumber).getClose();
                        firstBuy = false;
                    }else{
                        System.out.println("买入金额: " + moneyAll);
                        System.out.println("买入时间: " + list.get(buyNumber).getTrade_date());
                        System.out.println("买入价格: " + list.get(buyNumber).getClose());
                        System.out.println("卖出价格: " + list.get(i).getClose());
                        moneyAll = moneyAll * list.get(i).getClose() / list.get(buyNumber).getClose();
                    }
                }
            }
        }
        System.out.println("summary money: " + moneyAll);
        System.out.println("summary ratio: " + (moneyAll/10000-1)*100 + "%");
    }
}
