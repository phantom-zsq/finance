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
basic_df = pd.read_sql_query('select ts_code from stock_basic where list_date<\'20210101\' and market is not null and ts_code>=\'000001.SZ\' order by ts_code', engine)
for index, row in basic_df.iterrows():
    print(index, row[0])
    time.sleep(2)
    df = pro.suspend_d(ts_code=row[0], end_date='20201231')
    res = df.to_sql('suspend_d', engine, index=False, if_exists='append', chunksize=10000)
