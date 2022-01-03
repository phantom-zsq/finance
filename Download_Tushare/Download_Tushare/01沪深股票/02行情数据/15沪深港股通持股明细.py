# coding=utf-8

import tushare as ts
import pandas as pd
import time
from sqlalchemy import create_engine

# create mysql engine
engine = create_engine('mysql+pymysql://root:123456@localhost:3306/tushare')
# set Download_Tushare token
ts.set_token('594b808743cb001d120038c81f3ec360ed7f6c81b45834d04a84a130')
# get Download_Tushare api
pro = ts.pro_api()

# -------------------common start-------------------
# 此接口最早时间是20160629, 所以缺384天数据
basic_df = pd.read_sql_query('select trade_date from moneyflow_hsgt where trade_date>=\'20200717\' and trade_date<\'20200718\' order by trade_date', engine)
for index, row in basic_df.iterrows():
    print(index, row[0])
    try:
        df = pro.query('hk_hold', trade_date=row[0])
    except Exception:
        print('exception: ' + row[0])
        df = pro.query('hk_hold', trade_date=row[0])
    res = df.to_sql('hk_hold', engine, index=False, if_exists='append', chunksize=10000)
    print(res)
    time.sleep(30)
