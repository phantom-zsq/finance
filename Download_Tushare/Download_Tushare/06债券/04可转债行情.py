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

basic_df = pd.read_sql_query('select ts_code from manual_cb_issue order by ts_code desc', engine)
for index, row in basic_df.iterrows():
    print(index, row[0])
    try:
        df = pro.cb_daily(ts_code=row[0], start_date='19900101', end_date='19970101')
        res = df.to_sql('cb_daily', engine, index=False, if_exists='append', chunksize=10000)
        print(res)
        time.sleep(15)
        df = pro.cb_daily(ts_code=row[0], start_date='19970101', end_date='20040101')
        res = df.to_sql('cb_daily', engine, index=False, if_exists='append', chunksize=10000)
        print(res)
        time.sleep(15)
        df = pro.cb_daily(ts_code=row[0], start_date='20040101', end_date='20110101')
        res = df.to_sql('cb_daily', engine, index=False, if_exists='append', chunksize=10000)
        print(res)
        time.sleep(15)
        df = pro.cb_daily(ts_code=row[0], start_date='20110101', end_date='20180101')
        res = df.to_sql('cb_daily', engine, index=False, if_exists='append', chunksize=10000)
        print(res)
        time.sleep(15)
        df = pro.cb_daily(ts_code=row[0], start_date='20180101', end_date='20220101')
    except Exception:
        print('exception: ' + row[0])
    res = df.to_sql('cb_daily', engine, index=False, if_exists='append', chunksize=10000)
    print(res)
    time.sleep(15)
