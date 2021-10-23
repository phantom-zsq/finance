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
basic_df = pd.read_sql_query('select ts_code,list_date from stock_basic where market is not null and ts_code>=\'000001.SZ\' order by ts_code', engine)
for index, row in basic_df.iterrows():
    print(index, row[0], row[1])
    df = ts.pro_bar(ts_code=row[0], adj='hfq', start_date='20210101', end_date='20211231')
    res = df.to_sql('pro_bar', engine, index=False, if_exists='append', chunksize=10000)
