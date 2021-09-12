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
# SH: 20141117
# SZ: 20161205
# 26天只有南向数据没有北向数据
#df = pro.query('moneyflow_hsgt', start_date='20140101', end_date='20150101')
#df = pro.query('moneyflow_hsgt', start_date='20150101', end_date='20160101')
#df = pro.query('moneyflow_hsgt', start_date='20160101', end_date='20170101')
#df = pro.query('moneyflow_hsgt', start_date='20170101', end_date='20180101')
#df = pro.query('moneyflow_hsgt', start_date='20180101', end_date='20190101')
#df = pro.query('moneyflow_hsgt', start_date='20190101', end_date='20200101')
#df = pro.query('moneyflow_hsgt', start_date='20200101', end_date='20210101')
df = pro.query('moneyflow_hsgt', start_date='20210101', end_date='20220101')

# -------------------common end-------------------
# load to mysql
res = df.to_sql('moneyflow_hsgt', engine, index=False, if_exists='append', chunksize=10000)
print(res)
