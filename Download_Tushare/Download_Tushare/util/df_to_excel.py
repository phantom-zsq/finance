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
df = pd.read_sql_query('select * from hk_hold where ts_code=\'601318.SH\' order by trade_date', engine)
df.to_csv('/Users/xx/Downloads/Download_Tushare/601318.csv')
