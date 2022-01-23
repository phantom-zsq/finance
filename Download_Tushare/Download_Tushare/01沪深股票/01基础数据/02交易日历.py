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
df = pro.query('trade_cal', exchange='SSE', start_date='20210101', end_date='20211231', fields='exchange,cal_date,is_open,pretrade_date')
res = df.to_sql('trade_cal', engine, index=False, if_exists='append', chunksize=10000)
print(res)

df = pro.query('trade_cal', exchange='SZSE', start_date='20210101', end_date='20211231', fields='exchange,cal_date,is_open,pretrade_date')
res = df.to_sql('trade_cal', engine, index=False, if_exists='append', chunksize=10000)
print(res)

df = pro.query('trade_cal', exchange='CFFEX', start_date='20210101', end_date='20211231', fields='exchange,cal_date,is_open,pretrade_date')
res = df.to_sql('trade_cal', engine, index=False, if_exists='append', chunksize=10000)
print(res)

df = pro.query('trade_cal', exchange='SHFE', start_date='20210101', end_date='20211231', fields='exchange,cal_date,is_open,pretrade_date')
res = df.to_sql('trade_cal', engine, index=False, if_exists='append', chunksize=10000)
print(res)

df = pro.query('trade_cal', exchange='CZCE', start_date='20210101', end_date='20211231', fields='exchange,cal_date,is_open,pretrade_date')
res = df.to_sql('trade_cal', engine, index=False, if_exists='append', chunksize=10000)
print(res)

df = pro.query('trade_cal', exchange='DCE', start_date='20210101', end_date='20211231', fields='exchange,cal_date,is_open,pretrade_date')
res = df.to_sql('trade_cal', engine, index=False, if_exists='append', chunksize=10000)
print(res)

df = pro.query('trade_cal', exchange='INE', start_date='20210101', end_date='20211231', fields='exchange,cal_date,is_open,pretrade_date')
res = df.to_sql('trade_cal', engine, index=False, if_exists='append', chunksize=10000)
print(res)
