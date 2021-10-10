package org.phantom.analyze.main;

import org.phantom.analyze.bean.StockBean;
import org.phantom.analyze.load.LoadBasicInformation;
import org.phantom.analyze.load.LoadExtendInformation;
import org.phantom.analyze.load.LoadOracleData;
import org.phantom.analyze.load.LoadWhiteListData;
import org.phantom.analyze.strategy.Strategy;
import org.phantom.analyze.verify.Verify;
import java.util.List;
import java.util.Map;

public class StockExecutor {

    public static void main(String[] args) throws Exception {
        new LoadOracleData().load();
        new LoadWhiteListData().load();
        Map<String, List<StockBean>> map = new LoadBasicInformation().load();
        new LoadExtendInformation().load(null);
        new Strategy().analyze(null);
        new Verify().verify(null);
    }
}
