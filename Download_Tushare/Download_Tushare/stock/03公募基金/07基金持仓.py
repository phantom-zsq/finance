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

df = pro.fund_portfolio(ts_code='001753.OF')

# -------------------common end-------------------
# load to mysql
res = df.to_sql('fund_portfolio', engine, index=False, if_exists='append', chunksize=10000)
print(res)
