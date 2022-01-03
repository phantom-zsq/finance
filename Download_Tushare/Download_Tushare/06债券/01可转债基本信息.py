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

#获取可转债基础信息列表20211231
df = pro.cb_basic(fields="ts_code,bond_full_name,bond_short_name,cb_code,stk_code,stk_short_name,maturity,par,issue_price,issue_size,remain_size,value_date,maturity_date,rate_type,coupon_rate,add_rate,pay_per_year,list_date,delist_date,exchange,conv_start_date,conv_end_date,first_conv_price,conv_price,rate_clause,put_clause,maturity_put_price,call_clause,reset_clause,conv_clause,guarantor,guarantee_type,issue_rating,newest_rating,rating_comp")

# -------------------common end-------------------
# load to mysql
res = df.to_sql('cb_basic', engine, index=False, if_exists='append', chunksize=10000)
print(res)
