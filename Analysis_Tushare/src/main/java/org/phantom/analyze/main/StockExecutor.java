package org.phantom.analyze.main;

import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.load.LoadBasicInformation;
import org.phantom.analyze.load.LoadExtendInformation;
import org.phantom.analyze.load.LoadOracleData;
import org.phantom.analyze.strategy.Strategy;
import org.phantom.analyze.verify.Verify;
import java.util.List;

public class StockExecutor {

    public static void main(String[] args) throws Exception {
        new LoadOracleData().load();
        List<StockBean> list = new LoadBasicInformation().load();
        new LoadExtendInformation().load(list);
        new Strategy().analyze(list);
        new Verify().verify(list);
    }
}
