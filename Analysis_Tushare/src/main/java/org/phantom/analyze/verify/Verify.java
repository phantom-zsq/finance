package org.phantom.analyze.verify;

import org.phantom.analyze.bean.StockBean;

import java.util.ArrayList;
import java.util.List;

public class Verify {

    public static void main(String[] args) throws Exception{
        List<StockBean> list = new ArrayList<StockBean>();
        for(int i=0; i<10; i++){
            StockBean bean = new StockBean();
            bean.setClose(i);
            list.add(bean);
        }

        int buyNumber = 1;
        int sellNumber = 3;
        list.get(buyNumber).setStatus(1);
        list.get(sellNumber).setStatus(-1);
        list.get(sellNumber+1).setClose(1);

        buyNumber = 5;
        sellNumber = 8;
        list.get(buyNumber).setStatus(1);
        list.get(sellNumber).setStatus(-1);
        list.get(sellNumber+1).setClose(1);
        new Verify().verify(list);
    }

    public void verify(List<StockBean> list) throws Exception{
        long initMoney = 10000;
        double moneyAll = 0;
        int buyNumber = 0;
        boolean firstBuy = true;
        for (int i=0; i<list.size()-1; i++){
            int status = list.get(i).getStatus();
            if(status == 1){
                buyNumber = i;
            }else if(status == -1){
                if(firstBuy){
                    System.out.println("买入金额: " + initMoney);
                    System.out.println("买入价格: " + list.get(buyNumber+1).getClose());
                    System.out.println("卖出价格: " + list.get(i+1).getClose());
                    moneyAll = initMoney * list.get(i+1).getClose() / list.get(buyNumber+1).getClose();
                    firstBuy = false;
                }else{
                    System.out.println("买入金额: " + moneyAll);
                    System.out.println("买入价格: " + list.get(buyNumber+1).getClose());
                    System.out.println("卖出价格: " + list.get(i+1).getClose());
                    moneyAll += moneyAll * list.get(i+1).getClose() / list.get(buyNumber+1).getClose();
                }
            }
        }
        System.out.println("summary money: " + moneyAll);
        System.out.println("summary ratio: " + (moneyAll/10000-1)*100 + "%");
    }
}
