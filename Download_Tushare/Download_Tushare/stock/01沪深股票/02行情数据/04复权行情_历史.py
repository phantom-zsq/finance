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
basic_df = pd.read_sql_query('select ts_code,list_date from stock_basic where list_date<\'20210101\' and market is not null order by ts_code', engine)
for index, row in basic_df.iterrows():
    print(index, row[0], row[1])
    time.sleep(1)
    # because pro_bar return length less than 5000
    if row[1] <= '20050101':
        df = ts.pro_bar(ts_code=row[0], adj='hfq', start_date=row[1], end_date='20050101')
        res = df.to_sql('pro_bar', engine, index=False, if_exists='append', chunksize=10000)
        df = ts.pro_bar(ts_code=row[0], adj='hfq', start_date='20050102', end_date='20201231')
        res = df.to_sql('pro_bar', engine, index=False, if_exists='append', chunksize=10000)
    elif row[1] > '20201231':
        print('null')
    else:
        df = ts.pro_bar(ts_code=row[0], adj='hfq', start_date=row[1], end_date='20201231')
        res = df.to_sql('pro_bar', engine, index=False, if_exists='append', chunksize=10000)
